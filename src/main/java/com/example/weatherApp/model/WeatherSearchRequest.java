package com.example.weatherApp.model;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherSearchRequest {
    private String city;
    
    @Pattern(regexp = "^-?([1-8]?[0-9]|90)(\\.\\d+)?$", message = "Latitude must be between -90 and 90 degrees")
    private String latitude;
    
    @Pattern(regexp = "^-?((1?[0-7]?|[0-9]?)[0-9]|180)(\\.\\d+)?$", message = "Longitude must be between -180 and 180 degrees")
    private String longitude;
    
    private Integer days;
    private Boolean useUserLocation;
}