package com.example.backend.dto;

public class MealDto {
    private String mealType; // "SANG", "TRUA", "TOI"
    private String name;
    private String description;
    private double caloriesEstimated;

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCaloriesEstimated() {
        return caloriesEstimated;
    }

    public void setCaloriesEstimated(double caloriesEstimated) {
        this.caloriesEstimated = caloriesEstimated;
    }
}

