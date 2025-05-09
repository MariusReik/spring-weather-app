package com.example.weatherApp.controller;

import com.example.weatherApp.model.WeatherData;
import com.example.weatherApp.model.WeatherSearchRequest;
import com.example.weatherApp.service.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Get weather data based on search parameters
     * 
     * @param request Search parameters (city, coordinates, etc.)
     * @param httpRequest HTTP request (to extract client IP if needed)
     * @return Weather data
     */
    @PostMapping("/search")
    public ResponseEntity<WeatherData> searchWeather(
            @Valid @RequestBody WeatherSearchRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Received weather search request: {}", request);
        WeatherData weatherData = weatherService.getWeatherData(request, httpRequest);
        return ResponseEntity.ok(weatherData);
    }
    
    /**
     * Get weather data by city name
     * 
     * @param city City name
     * @param days Number of forecast days (optional, defaults to 5)
     * @return Weather data
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<WeatherData> getWeatherByCity(
            @PathVariable String city,
            @RequestParam(required = false, defaultValue = "5") int days) {
        
        log.info("Getting weather for city: {}, days: {}", city, days);
        WeatherSearchRequest request = WeatherSearchRequest.builder()
                .city(city)
                .days(days)
                .build();
        
        WeatherData weatherData = weatherService.getWeatherData(request, null);
        return ResponseEntity.ok(weatherData);
    }
    
    /**
     * Get weather data by coordinates
     * 
     * @param latitude Latitude
     * @param longitude Longitude
     * @param days Number of forecast days (optional, defaults to 5)
     * @return Weather data
     */
    @GetMapping("/coordinates")
    public ResponseEntity<WeatherData> getWeatherByCoordinates(
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam(required = false, defaultValue = "5") int days) {
        
        log.info("Getting weather for coordinates: {}, {}, days: {}", latitude, longitude, days);
        WeatherSearchRequest request = WeatherSearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .days(days)
                .build();
        
        WeatherData weatherData = weatherService.getWeatherData(request, null);
        return ResponseEntity.ok(weatherData);
    }
    
    /**
     * Get weather data based on user's location (IP address)
     * 
     * @param days Number of forecast days (optional, defaults to 5)
     * @param httpRequest HTTP request (to extract client IP)
     * @return Weather data
     */
    @GetMapping("/current-location")
    public ResponseEntity<WeatherData> getWeatherForCurrentLocation(
            @RequestParam(required = false, defaultValue = "5") int days,
            HttpServletRequest httpRequest) {
        
        log.info("Getting weather for current location, days: {}", days);
        WeatherSearchRequest request = WeatherSearchRequest.builder()
                .useUserLocation(true)
                .days(days)
                .build();
        
        WeatherData weatherData = weatherService.getWeatherData(request, httpRequest);
        return ResponseEntity.ok(weatherData);
    }
}