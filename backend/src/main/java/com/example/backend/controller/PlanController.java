package com.example.backend.controller;

import com.example.backend.dto.GoalPlanRequest;
import com.example.backend.dto.PlanResponse;
import com.example.backend.service.NutritionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlanController {

    private final NutritionService nutritionService;

    public PlanController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @PostMapping("/plan")
    public PlanResponse createPlan(@Valid @RequestBody GoalPlanRequest req) {
        return nutritionService.createPlan(req);
    }
}

