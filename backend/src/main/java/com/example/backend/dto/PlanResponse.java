package com.example.backend.dto;

import java.time.LocalDate;

public class PlanResponse {

    private Long planId;
    private Long profileId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double caloriesPerDay;

    private MealPlanDto planJson;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getCaloriesPerDay() {
        return caloriesPerDay;
    }

    public void setCaloriesPerDay(double caloriesPerDay) {
        this.caloriesPerDay = caloriesPerDay;
    }

    public MealPlanDto getPlanJson() {
        return planJson;
    }

    public void setPlanJson(MealPlanDto planJson) {
        this.planJson = planJson;
    }
}

