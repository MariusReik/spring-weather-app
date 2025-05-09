package com.example.weatherApp.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        log.error("Validation error: {}", errors);
        return new ResponseEntity<>(new ErrorResponse("Validation Error", errors), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException ex) {
        log.error("HTTP client error: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse("External API Error", ex.getStatusCode().value() + " - " + ex.getStatusText()),
                ex.getStatusCode());
    }
    
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException ex) {
        log.error("Rest client error: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse("External API Error", "Could not connect to external service"),
                HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return new ResponseEntity<>(
                new ErrorResponse("Server Error", "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private Object details;
    }
}