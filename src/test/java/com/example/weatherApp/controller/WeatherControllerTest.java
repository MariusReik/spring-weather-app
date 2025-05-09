package com.example.weatherApp.controller;

import com.example.weatherApp.model.Coordinates;
import com.example.weatherApp.model.CurrentWeather;
import com.example.weatherApp.model.WeatherData;
import com.example.weatherApp.model.WeatherSearchRequest;
import com.example.weatherApp.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    public void getWeatherByCity_ShouldReturnWeatherData() throws Exception {
        // Arrange
        String city = "Oslo";
        WeatherData mockData = createMockWeatherData(city, "NO");
        
        when(weatherService.getWeatherData(any(WeatherSearchRequest.class), eq(null)))
                .thenReturn(mockData);

        // Act & Assert
        mockMvc.perform(get("/api/weather/city/{city}", city)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityName").value(city))
                .andExpect(jsonPath("$.countryCode").value("NO"))
                .andExpect(jsonPath("$.currentWeather.temperature").exists())
                .andExpect(jsonPath("$.coordinates.latitude").exists());
    }

    @Test
    public void getWeatherByCoordinates_ShouldReturnWeatherData() throws Exception {
        // Arrange
        String latitude = "59.9139";
        String longitude = "10.7522";
        WeatherData mockData = createMockWeatherData("Oslo", "NO");
        
        when(weatherService.getWeatherData(any(WeatherSearchRequest.class), eq(null)))
                .thenReturn(mockData);

        // Act & Assert
        mockMvc.perform(get("/api/weather/coordinates")
                .param("latitude", latitude)
                .param("longitude", longitude)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityName").exists())
                .andExpect(jsonPath("$.currentWeather").exists());
    }

    // Helper method to create mock weather data
    private WeatherData createMockWeatherData(String cityName, String countryCode) {
        CurrentWeather currentWeather = CurrentWeather.builder()
                .temperature(10.5)
                .feelsLike(9.0)
                .humidity(70.0)
                .pressure(1013.0)
                .windSpeed(5.0)
                .description("few clouds")
                .icon("02d")
                .build();

        Coordinates coordinates = new Coordinates(59.9139, 10.7522);

        return WeatherData.builder()
                .cityName(cityName)
                .countryCode(countryCode)
                .coordinates(coordinates)
                .currentWeather(currentWeather)
                .dailyForecasts(new ArrayList<>())
                .timestamp(LocalDateTime.now())
                .build();
    }
}