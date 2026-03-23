package com.example.backend.dto;

import java.util.List;

public class MealAnalyzeResponse {
    private Long mealAnalysisId;
    private String mealName;
    private double totalCalories;
    private List<MealIngredientResultDto> ingredients;

    public Long getMealAnalysisId() { return mealAnalysisId; }
    public void setMealAnalysisId(Long mealAnalysisId) { this.mealAnalysisId = mealAnalysisId; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }
    public List<MealIngredientResultDto> getIngredients() { return ingredients; }
    public void setIngredients(List<MealIngredientResultDto> ingredients) { this.ingredients = ingredients; }
}
