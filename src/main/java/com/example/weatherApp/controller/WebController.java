package com.example.weatherApp.controller;

import com.example.weatherApp.model.WeatherData;
import com.example.weatherApp.model.WeatherSearchRequest;
import com.example.weatherApp.service.WeatherService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final WeatherService weatherService;
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("searchRequest", new WeatherSearchRequest());
        return "index";
    }
    
    @PostMapping("/weather")
    public String getWeather(@ModelAttribute WeatherSearchRequest searchRequest, 
                             Model model, 
                             HttpServletRequest request) {
        
        WeatherData weatherData = weatherService.getWeatherData(searchRequest, request);
        
        model.addAttribute("weatherData", weatherData);
        model.addAttribute("searchRequest", searchRequest);
        
        return "weather";
    }
}