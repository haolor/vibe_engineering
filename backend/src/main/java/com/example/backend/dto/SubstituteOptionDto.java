package com.example.backend.dto;

public class SubstituteOptionDto {
    private String mealName;
    private double calories;
    private double deltaCalories;

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }
    public double getDeltaCalories() { return deltaCalories; }
    public void setDeltaCalories(double deltaCalories) { this.deltaCalories = deltaCalories; }
}
