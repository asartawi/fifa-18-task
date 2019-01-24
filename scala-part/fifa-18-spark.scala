import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import java.sql.Timestamp;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.DataFrame;
import spark.implicits._
import scala.util.parsing.json._
import scala.collection.mutable.MutableList

val sqlContext = new org.apache.spark.sql.SQLContext(sc)

var url = ""
val baseUrl = "https://fifa-18.herokuapp.com/countries?page="
var page = 0
var rows = 0
//var countries  = new MutableList[Tuple2[String, String]]( )
val countryToContinent =  scala.collection.mutable.Map[String, String]()
do{
	url = baseUrl + page
	val jsonString = scala.io.Source.fromURL(url).mkString
	//println(jsonString)
	val result = JSON.parseFull(jsonString).map {
	 case json: Map[String, List[Map[String, Map[String, String]]]] =>
	    json("content").map(l => ( l("name") , l("continentCode")("name") ))
	}.get

	result.foreach(tuple=>{
		
		val a = (tuple +"").split(",")(0).replace("(","")
		val b = (tuple +"").split(",")(1).replace(")","")
		//println(a, b, a)
 		//countries+= new Tuple2(a, b)
		countryToContinent += (a -> b)
	})
	rows = result.length
	page = page + 1
}while(rows >= 10 )


//Using parallelism is a requirement, We are expecting one file per continent as an output.  
sc.parallelize(countries) //parralized automatically

/*val rdd = sc.parallelize(countries)
val df = rdd.toDF("CNTY", "CONTI")
df.createOrReplaceTempView("CNTY_CONTI")
val sqlDF = spark.sql("SELECT * FROM CNTY_CONTI")
sqlDF.show()
*/

val inputDF = spark.read.format("csv").option("header",true).option("inferSchema", true).load("FIFA-18-Video-Game-Player-Stats.csv" )

//sc.parallelize(inputDF) 


// Assign the “table name” readings to the inputDS Dataset
inputDF.createOrReplaceTempView("Fifa");

// Define a User Defined Function called full(Integer free_slots)
// that returns 1 if the value of free_slots is equal to 0,
// 1 if free_slots is greater than 0.
val ValueClean = {}
spark.udf.register("getValue",  (input: String) => { (input. replaceAll("€", "")). replaceAll("M", "") })
spark.udf.register("getSalary", (input: String) => { (input. replaceAll("€", "")). replaceAll("K", "") })
spark.udf.register("getContinent", (input: String) => countryToContinent.get(input))

val sqlDF = spark.sql("SELECT F.name as player, getContinent(Nationality) as continent FROM Fifa F  ORDER BY Nationality")		 
sqlDF.show()

// Assign the “table name”  result1  to the sqlDF Dataset
sqlDF.createOrReplaceTempView("player_continent")
// Save the result in the output folder
//1.Use the provided data sample to store a new file that contains the player name and the continent he is coming from. You are expected to use the API to do that.
sqlDF.write.format("csv").mode("overwrite").option("header", true) .save("player_continent")

//2.Using parallelism is a requirement, We are expecting one file per continent as an output.
sqlDF.write.partitionBy("continent").format("csv").mode("overwrite").option("header", true) .save("player_continent_parts")

//------------------------------------------------------------------------------------------------
//sqlDF.write.mode("overwrite").saveAsTable("HiveResult55")
//After a while, we will have an updated document that has the latest salaries of the players, you are expected to build your spark application to support taking these updates and merge the new values of these players salaries into our warehouse. The new file will only have the players with the updated salaries only. 
// Palyer Salary
val sqlSalaryDF = spark.sql("SELECT name, Salary FROM Fifa F  ")		 
sqlSalaryDF.show()
// Assign the “table name”  result1  to the sqlDF Dataset
sqlSalaryDF.createOrReplaceTempView("player_salary")
// Save the result in the output folder
sqlSalaryDF.write.format("csv").mode("overwrite").option("header", true) .save("player_salary")

//------------------------------------------------------------------------------------------------
// Which top 3 countries that achieve the highest income through their players?
val sqlTopCountries = spark.sql("SELECT Nationality, sum(getSalary(Salary)) as total FROM Fifa GROUP BY  Nationality ORDER BY total DESC limit 3")		 
sqlTopCountries.show()
// Assign the “table name”  result1  to the sqlDF Dataset
sqlTopCountries.createOrReplaceTempView("top_3_countries_income")
// Save the result in the output folder
sqlTopCountries.write.format("csv").mode("overwrite").option("header", true) .save("top_3_countries_income")

//------------------------------------------------------------------------------------------------
//List The club that includes the most valuable players
val sqlTopClub = spark.sql("SELECT Club, sum(getValue(Value)) as total FROM Fifa GROUP BY Club ORDER BY total DESC limit 1")		 
sqlTopClub.show()
// Assign the “table name”  result1  to the sqlDF Dataset
sqlTopClub.createOrReplaceTempView("max_player_valuable_club")
// Save the result in the output folder
sqlTopClub.write.format("csv").mode("overwrite").option("header", true) .save("max_player_valuable_club")

//------------------------------------------------------------------------------------------------
//The top 5 clubs that spends highest salaries 
val sqlTop5Club = spark.sql("SELECT Club, sum(getSalary(Salary)) as Salaries FROM Fifa GROUP BY Club ORDER BY Salaries DESC limit 5")		 
sqlTop5Club.show()
// Assign the “table name”  result1  to the sqlDF Dataset
sqlTop5Club.createOrReplaceTempView("top_5_player_valuable_clubs")
// Save the result in the output folder
sqlTop5Club.write.format("csv").mode("overwrite").option("header", true) .save("top_5_player_valuable_clubs")


//------------------------------------------------------------------------------------------------
//Which of Europe or America - on continent level - has the best FIFA players?
//here we assume America is 2 continents: North America, South America
val sqlDF = spark.sql("""
SELECT getContinent(Nationality) as continent, avg(F.`Fifa Score`) as Score FROM Fifa F  WHERE getContinent(Nationality)  IN ( 'Europe' , 'North America', 'South America')
GROUP BY continent
ORDER BY Score DESC LIMIT 1""")		 
sqlDF.show()
// Assign the “table name”  result1  to the sqlDF Dataset
sqlDF.createOrReplaceTempView("continent_best_score")
// Save the result in the output folder
sqlDF.write.format("csv").mode("overwrite").option("header", true) .save("continent_best_score")

//------------------------------------------------------------------------------------------------
//sqlDF.write.mode("overwrite").saveAsTable("HiveResult55")
//After a while, we will have an updated document that has the latest salaries of the players, you are expected to build your spark application to support taking these updates and merge the new values of these players salaries into our warehouse. The new file will only have the players with the updated salaries only. 
// Save the result in the output folder
spark.sql("DROP TABLE IF EXISTS player_salary")       //spark 2.0
sqlDF.write.format("csv").mode("overwrite").option("header", true) .saveAsTable("player_salary")

//------------------------------------------------------------------------------------------------
// Which top 3 countries that achieve the highest income through their players?
spark.sql("DROP TABLE IF EXISTS top_3_countries_income")       //spark 2.0
sqlDF.write.format("csv").mode("overwrite").option("header", true) .saveAsTable("top_3_countries_income")
continent_best_score

//------------------------------------------------------------------------------------------------
//List The club that includes the most valuable players
spark.sql("DROP TABLE IF EXISTS max_player_valuable_club")       //spark 2.0
sqlDF.write.format("csv").mode("overwrite").option("header", true) .saveAsTable("max_player_valuable_club")

//------------------------------------------------------------------------------------------------
//The top 5 clubs that spends highest salaries 
spark.sql("DROP TABLE IF EXISTS top_5_player_valuable_clubs")       //spark 2.0
sqlDF.write.format("csv").mode("overwrite").option("header", true) .saveAsTable("top_5_player_valuable_clubs")

//------------------------------------------------------------------------------------------------
//Which of Europe or America - on continent level - has the best FIFA players?
//here we assume America is 2 continents: North America, South America
spark.sql("DROP TABLE IF EXISTS continent_best_score")       //spark 2.0
sqlDF.write.format("csv").mode("overwrite").option("header", true) .saveAsTable("continent_best_score")



//example of build hive table.
//sqlDF.write.mode("overwrite").option("header", true).saveAsTable("HiveResult55")







