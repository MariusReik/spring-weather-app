package com.example.weatherApp.service.api;

import com.example.weatherApp.model.WeatherData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpenWeatherMapClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OpenWeatherMapClient weatherClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(weatherClient, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(weatherClient, "apiUrl", "https://api.openweathermap.org/data/2.5");
    }

    @Test
    void getCurrentWeatherByCity_ShouldReturnWeatherData() {
        // Arrange
        String mockResponse = """
                {
                    "name": "Oslo",
                    "sys": {
                        "country": "NO"
                    },
                    "coord": {
                        "lat": 59.9139,
                        "lon": 10.7522
                    },
                    "main": {
                        "temp": 5.2,
                        "feels_like": 2.1,
                        "humidity": 80,
                        "pressure": 1010
                    },
                    "wind": {
                        "speed": 3.6
                    },
                    "weather": [
                        {
                            "description": "scattered clouds",
                            "icon": "03d"
                        }
                    ]
                }
                """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        WeatherData result = weatherClient.getCurrentWeatherByCity("Oslo");

        // Assert
        assertNotNull(result);
        assertEquals("Oslo", result.getCityName());
        assertEquals("NO", result.getCountryCode());
        assertEquals(59.9139, result.getCoordinates().getLatitude());
        assertEquals(10.7522, result.getCoordinates().getLongitude());
        assertEquals(5.2, result.getCurrentWeather().getTemperature());
        assertEquals(2.1, result.getCurrentWeather().getFeelsLike());
        assertEquals("scattered clouds", result.getCurrentWeather().getDescription());
    }

    @Test
    void getCurrentWeatherByCoordinates_ShouldReturnWeatherData() {
        // Arrange
        String mockResponse = """
                {
                    "name": "Oslo",
                    "sys": {
                        "country": "NO"
                    },
                    "coord": {
                        "lat": 59.9139,
                        "lon": 10.7522
                    },
                    "main": {
                        "temp": 5.2,
                        "feels_like": 2.1,
                        "humidity": 80,
                        "pressure": 1010
                    },
                    "wind": {
                        "speed": 3.6
                    },
                    "weather": [
                        {
                            "description": "scattered clouds",
                            "icon": "03d"
                        }
                    ]
                }
                """;

        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        WeatherData result = weatherClient.getCurrentWeatherByCoordinates(59.9139, 10.7522);

        // Assert
        assertNotNull(result);
        assertEquals("Oslo", result.getCityName());
        assertEquals(5.2, result.getCurrentWeather().getTemperature());
    }
}