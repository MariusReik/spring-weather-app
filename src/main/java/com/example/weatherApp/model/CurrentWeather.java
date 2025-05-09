package com.example.weatherApp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeather {
    private Double temperature;
    private Double feelsLike;
    private Double humidity;
    private Double pressure;
    private Double windSpeed;
    private String description;
    private String icon;
}