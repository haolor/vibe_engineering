package com.example.backend.controller;

import com.example.backend.dto.CalorieLogRequest;
import com.example.backend.dto.CalorieLogResponse;
import com.example.backend.service.CalorieService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CaloriesController {

    private final CalorieService calorieService;

    public CaloriesController(CalorieService calorieService) {
        this.calorieService = calorieService;
    }

    @PostMapping("/calories")
    public CalorieLogResponse addCalories(@Valid @RequestBody CalorieLogRequest req) {
        return calorieService.addOrUpdate(req);
    }
}
