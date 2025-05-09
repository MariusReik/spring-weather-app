package com.example.weatherApp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyForecast {
    private LocalDate date;
    private Double minTemperature;
    private Double maxTemperature;
    private Double humidity;
    private String description;
    private String icon;
}