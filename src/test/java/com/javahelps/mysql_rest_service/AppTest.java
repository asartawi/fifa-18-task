package com.javahelps.mysql_rest_service;

import com.javahelps.restservice.controller.CountryController;
import com.javahelps.restservice.entity.Continents;
import com.javahelps.restservice.entity.Countries;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest("CountryController")
@AutoConfigureMockMvc
public class AppTest {
	
	@Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CountryController stockAPI;
    
    @Test
    public void contextLoads() {
    }
    
    @Test
    public void findAll() {
//    	Continents continent = new Continents("EU", "Europe");
//        Countries country = new Countries("F", "France", "French Republic", "FRA", "250", continent);
//        
//        List<Countries> data = new ArrayList<Countries>();
//        data.add(country);
//        //given(stockAPI.listAllCountries(null, "0")).willReturn(ResponseEntity.ok(data));
//
//        this.mockMvc.perform(get("/countries?page=0"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.*", hasSize(data.size())));
    }

    @Test
    public void findById() {
//    	Continents continent = new Continents("EU", "Europe");
//        Countries country = new Countries("F", "France", "French Republic", "FRA", "250", continent);
//        //given(stockAPI.search(null, country.getName())).willReturn((ResponseEntity<?>) ResponseEntity.ok());
//
//        this.mockMvc.perform(get("/countries/" + "France"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value(country.getName()));
    }
}
