package com.example.weatherApp.service.api;

import com.example.weatherApp.model.Coordinates;
import com.example.weatherApp.model.CurrentWeather;
import com.example.weatherApp.model.DailyForecast;
import com.example.weatherApp.model.WeatherData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenWeatherMapClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.api.url}")
    private String apiUrl;

    @Cacheable(value = "currentWeather", key = "#city")
    public WeatherData getCurrentWeatherByCity(String city) {
        String uri = UriComponentsBuilder.fromHttpUrl(apiUrl + "/weather")
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .toUriString();

        log.debug("Calling OpenWeatherMap API for city: {}", city);
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        return parseCurrentWeatherResponse(response.getBody());
    }

    @Cacheable(value = "currentWeather", key = "#latitude + '-' + #longitude")
    public WeatherData getCurrentWeatherByCoordinates(Double latitude, Double longitude) {
        String uri = UriComponentsBuilder.fromHttpUrl(apiUrl + "/weather")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .toUriString();

        log.debug("Calling OpenWeatherMap API for coordinates: {}, {}", latitude, longitude);
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        return parseCurrentWeatherResponse(response.getBody());
    }

    @Cacheable(value = "forecastWeather", key = "#city + '-' + #days")
    public WeatherData getForecastByCity(String city, int days) {
        // First get current weather to get coordinates
        WeatherData currentWeather = getCurrentWeatherByCity(city);
        return getForecastByCoordinates(
                currentWeather.getCoordinates().getLatitude(),
                currentWeather.getCoordinates().getLongitude(),
                days
        );
    }

    @Cacheable(value = "forecastWeather", key = "#latitude + '-' + #longitude + '-' + #days")
    public WeatherData getForecastByCoordinates(Double latitude, Double longitude, int days) {
        // First get the current weather
        WeatherData currentWeather = getCurrentWeatherByCoordinates(latitude, longitude);
        
        // Now get the forecast data using the OneCall API
        String uri = UriComponentsBuilder.fromHttpUrl(apiUrl + "/onecall")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("exclude", "minutely,hourly,alerts")
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .toUriString();

        log.debug("Calling OpenWeatherMap OneCall API for forecast: {}, {}", latitude, longitude);
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        
        // Parse the forecast data and add it to the current weather data
        List<DailyForecast> forecasts = parseForecastResponse(response.getBody(), days);
        currentWeather.setDailyForecasts(forecasts);
        
        return currentWeather;
    }

    private WeatherData parseCurrentWeatherResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            String cityName = root.path("name").asText();
            String countryCode = root.path("sys").path("country").asText();
            
            // Parse coordinates
            double latitude = root.path("coord").path("lat").asDouble();
            double longitude = root.path("coord").path("lon").asDouble();
            Coordinates coordinates = new Coordinates(latitude, longitude);
            
            // Parse current weather
            JsonNode mainNode = root.path("main");
            JsonNode weatherNode = root.path("weather").get(0);
            
            CurrentWeather currentWeather = CurrentWeather.builder()
                    .temperature(mainNode.path("temp").asDouble())
                    .feelsLike(mainNode.path("feels_like").asDouble())
                    .humidity(mainNode.path("humidity").asDouble())
                    .pressure(mainNode.path("pressure").asDouble())
                    .windSpeed(root.path("wind").path("speed").asDouble())
                    .description(weatherNode.path("description").asText())
                    .icon(weatherNode.path("icon").asText())
                    .build();
            
            return WeatherData.builder()
                    .cityName(cityName)
                    .countryCode(countryCode)
                    .coordinates(coordinates)
                    .currentWeather(currentWeather)
                    .dailyForecasts(new ArrayList<>())
                    .timestamp(LocalDateTime.now())
                    .build();
            
        } catch (Exception e) {
            log.error("Error parsing weather response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse weather data", e);
        }
    }

    private List<DailyForecast> parseForecastResponse(String responseBody, int days) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dailyArray = root.path("daily");
            
            List<DailyForecast> dailyForecasts = new ArrayList<>();
            
            // Limit the number of days to what was requested or available
            int limit = Math.min(days, dailyArray.size());
            
            for (int i = 0; i < limit; i++) {
                JsonNode day = dailyArray.get(i);
                
                // Get the date
                long dt = day.path("dt").asLong();
                LocalDate forecastDate = Instant.ofEpochSecond(dt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                // Get the temperatures
                JsonNode temp = day.path("temp");
                double minTemp = temp.path("min").asDouble();
                double maxTemp = temp.path("max").asDouble();
                
                // Get humidity
                double humidity = day.path("humidity").asDouble();
                
                // Get weather description and icon
                JsonNode weather = day.path("weather").get(0);
                String description = weather.path("description").asText();
                String icon = weather.path("icon").asText();
                
                // Create the forecast object
                DailyForecast forecast = DailyForecast.builder()
                        .date(forecastDate)
                        .minTemperature(minTemp)
                        .maxTemperature(maxTemp)
                        .humidity(humidity)
                        .description(description)
                        .icon(icon)
                        .build();
                
                dailyForecasts.add(forecast);
            }
            
            return dailyForecasts;
            
        } catch (Exception e) {
            log.error("Error parsing forecast response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse forecast data", e);
        }
    }
}