package com.example.weatherApp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationData {
    private String city;
    private String country;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private String ipAddress;
}