package com.example.backend.dto;

import java.util.List;

public class MealPlanDayDto {
    private int dayIndex; // 1..N
    private String date; // ISO yyyy-MM-dd
    private List<MealDto> meals;

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<MealDto> getMeals() {
        return meals;
    }

    public void setMeals(List<MealDto> meals) {
        this.meals = meals;
    }
}

