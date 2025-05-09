package com.example.weatherApp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {
    private String cityName;
    private String countryCode;
    private Coordinates coordinates;
    private CurrentWeather currentWeather;
    private List<DailyForecast> dailyForecasts;
    private LocalDateTime timestamp;
}