package com.example.weatherApp.service;

import com.example.weatherApp.model.LocationData;
import com.example.weatherApp.model.WeatherData;
import com.example.weatherApp.model.WeatherSearchRequest;
import com.example.weatherApp.service.api.IpGeolocationClient;
import com.example.weatherApp.service.api.OpenWeatherMapClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final OpenWeatherMapClient weatherClient;
    private final IpGeolocationClient locationClient;

    public WeatherData getWeatherData(WeatherSearchRequest request, HttpServletRequest httpRequest) {
        WeatherData weatherData;
        
        // Use user's location based on IP address if requested
        if (request.getUseUserLocation() != null && request.getUseUserLocation()) {
            String ipAddress = getClientIpAddress(httpRequest);
            LocationData locationData = locationClient.getLocationByIp(ipAddress);
            
            int days = Optional.ofNullable(request.getDays()).orElse(5);
            weatherData = weatherClient.getForecastByCoordinates(
                    locationData.getLatitude(),
                    locationData.getLongitude(),
                    days
            );
            
            // Set the city name from geolocation if not present in weather data
            if (weatherData.getCityName() == null || weatherData.getCityName().isEmpty()) {
                weatherData.setCityName(locationData.getCity());
                weatherData.setCountryCode(locationData.getCountryCode());
            }
        }
        // Search by coordinates if provided
        else if (request.getLatitude() != null && request.getLongitude() != null) {
            double latitude = Double.parseDouble(request.getLatitude());
            double longitude = Double.parseDouble(request.getLongitude());
            
            int days = Optional.ofNullable(request.getDays()).orElse(5);
            weatherData = weatherClient.getForecastByCoordinates(latitude, longitude, days);
        }
        // Search by city if provided
        else if (request.getCity() != null && !request.getCity().isEmpty()) {
            int days = Optional.ofNullable(request.getDays()).orElse(5);
            weatherData = weatherClient.getForecastByCity(request.getCity(), days);
        }
        // Default to user's location
        else {
            String ipAddress = getClientIpAddress(httpRequest);
            LocationData locationData = locationClient.getLocationByIp(ipAddress);
            
            int days = Optional.ofNullable(request.getDays()).orElse(5);
            weatherData = weatherClient.getForecastByCoordinates(
                    locationData.getLatitude(),
                    locationData.getLongitude(),
                    days
            );
            
            // Set the city name from geolocation if not present in weather data
            if (weatherData.getCityName() == null || weatherData.getCityName().isEmpty()) {
                weatherData.setCityName(locationData.getCity());
                weatherData.setCountryCode(locationData.getCountryCode());
            }
        }
        
        return weatherData;
    }

    // Helper method to get client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        
        // If it's still empty, use a default IP for testing
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = "8.8.8.8"; // Google DNS as fallback
        }
        
        return clientIp;
    }
}