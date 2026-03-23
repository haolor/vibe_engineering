package com.example.backend.controller;

import com.example.backend.dto.AutoSubstituteRequest;
import com.example.backend.dto.AutoSubstituteResponse;
import com.example.backend.dto.ManualSubstituteRequest;
import com.example.backend.dto.ManualSubstituteResponse;
import com.example.backend.dto.MealAnalyzeRequest;
import com.example.backend.dto.MealAnalyzeResponse;
import com.example.backend.service.MealService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meals")
public class MealsController {
    private final MealService mealService;

    public MealsController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping("/analyze")
    public MealAnalyzeResponse analyze(@RequestBody MealAnalyzeRequest req) {
        return mealService.analyze(req);
    }

    @PostMapping("/substitute/auto")
    public AutoSubstituteResponse autoSubstitute(@RequestBody AutoSubstituteRequest req) {
        return mealService.autoSubstitute(req);
    }

    @PostMapping("/substitute/manual")
    public ManualSubstituteResponse manualSubstitute(@RequestBody ManualSubstituteRequest req) {
        return mealService.manualSubstitute(req);
    }
}
