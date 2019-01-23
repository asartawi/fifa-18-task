package com.javahelps.restservice.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.javahelps.restservice.controller.CountryController;

import java.util.Date;

@ControllerAdvice(assignableTypes = CountryController.class)
@RequestMapping(produces = "application/vnd.error+json")
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(CountryNotFoundException.class)
	public ResponseEntity<Object> handleCountryNotFoundException(CountryNotFoundException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		System.out.println("handleCountryNotFoundException");
		ErrorMessage errorMessage = new ErrorMessage(new Date(), status, ex.getMessage());
		return new ResponseEntity<>(errorMessage, headers, status);

	}

	@ExceptionHandler(NumberFormatException.class)
	protected ResponseEntity<Object> handleNumberFormatException(NumberFormatException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		System.out.println("handleNumberFormatException");
		ErrorMessage errorMessage = new ErrorMessage(new Date(), status, ex.getMessage());
		return new ResponseEntity<>(errorMessage, headers, status);

	}

	
	/**
	 * @Override: handleMissingServletRequestParameter because we
	 *            use @ControllerAdvice to return error message when don't pass
	 *            required params
	 */
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String name = ex.getParameterName();
		ErrorMessage errorMessage = new ErrorMessage(new Date(), status, ex.getMessage());
		logger.error(name + " parameter is missing");
		return new ResponseEntity<>(errorMessage, headers, status);

	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleAnyException(final Exception exception, final HttpStatus httpStatus,
			WebRequest request) {
		ErrorMessage errorMessage = new ErrorMessage(new Date(), httpStatus, exception.getMessage());
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), httpStatus);
	}

}