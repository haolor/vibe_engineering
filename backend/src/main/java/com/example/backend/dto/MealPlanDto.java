package com.example.backend.dto;

import java.util.List;

public class MealPlanDto {
    private String planTitle;
    private double caloriesPerDay;
    private List<MealPlanDayDto> days;

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public double getCaloriesPerDay() {
        return caloriesPerDay;
    }

    public void setCaloriesPerDay(double caloriesPerDay) {
        this.caloriesPerDay = caloriesPerDay;
    }

    public List<MealPlanDayDto> getDays() {
        return days;
    }

    public void setDays(List<MealPlanDayDto> days) {
        this.days = days;
    }
}

