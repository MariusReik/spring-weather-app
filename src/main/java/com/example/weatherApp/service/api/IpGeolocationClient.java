package com.example.weatherApp.service.api;

import com.example.weatherApp.model.LocationData;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class IpGeolocationClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ipgeolocation.api.key}")
    private String apiKey;

    @Value("${ipgeolocation.api.url}")
    private String apiUrl;

    @Cacheable(value = "ipLocation", key = "#ipAddress")
    public LocationData getLocationByIp(String ipAddress) {
        String uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("apiKey", apiKey)
                .queryParam("ip", ipAddress)
                .toUriString();

        log.debug("Calling IP Geolocation API for IP: {}", ipAddress);
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        return parseLocationResponse(response.getBody(), ipAddress);
    }

    private LocationData parseLocationResponse(String responseBody, String ipAddress) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            return LocationData.builder()
                    .city(root.path("city").asText())
                    .country(root.path("country_name").asText())
                    .countryCode(root.path("country_code2").asText())
                    .latitude(Double.parseDouble(root.path("latitude").asText()))
                    .longitude(Double.parseDouble(root.path("longitude").asText()))
                    .ipAddress(ipAddress)
                    .build();
            
        } catch (Exception e) {
            log.error("Error parsing location response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse location data", e);
        }
    }
}