package com.example.backend.controller;

import com.example.backend.dto.WeightLogRequest;
import com.example.backend.dto.WeightLogResponse;
import com.example.backend.service.WeightService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WeightsController {

    private final WeightService weightService;

    public WeightsController(WeightService weightService) {
        this.weightService = weightService;
    }

    @PostMapping("/weights")
    public WeightLogResponse addWeight(@Valid @RequestBody WeightLogRequest req) {
        return weightService.addWeightLog(req);
    }
}

