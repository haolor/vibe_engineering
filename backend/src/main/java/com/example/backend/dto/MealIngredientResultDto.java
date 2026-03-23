package com.example.backend.dto;

public class MealIngredientResultDto {
    private String name;
    private String quantityText;
    private double caloriesEstimated;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getQuantityText() { return quantityText; }
    public void setQuantityText(String quantityText) { this.quantityText = quantityText; }
    public double getCaloriesEstimated() { return caloriesEstimated; }
    public void setCaloriesEstimated(double caloriesEstimated) { this.caloriesEstimated = caloriesEstimated; }
}
