package com.example.backend.controller;

import com.example.backend.dto.ProfileRequest;
import com.example.backend.dto.ProfileResponse;
import com.example.backend.service.NutritionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final NutritionService nutritionService;

    public ProfileController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @PostMapping("/profile")
    public ProfileResponse upsertProfile(@Valid @RequestBody ProfileRequest req) {
        return nutritionService.upsertProfile(req);
    }
}

