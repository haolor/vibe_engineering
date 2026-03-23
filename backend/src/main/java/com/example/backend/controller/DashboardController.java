package com.example.backend.controller;

import com.example.backend.dto.DashboardResponse;
import com.example.backend.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(
            @RequestParam(required = false) Long profileId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate now = LocalDate.now();
        LocalDate fromEffective = from != null ? from : now.minusDays(30);
        LocalDate toEffective = to != null ? to : now;
        return dashboardService.getDashboard(profileId, fromEffective, toEffective);
    }
}

