package com.example.backend.dto;

import java.util.List;

public class ManualSubstituteRequest {
    private Long profileId;
    private Double originalCalories;
    private Long originalMealAnalysisId;
    private String substituteMealName;
    private List<MealIngredientInputDto> ingredients;

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Double getOriginalCalories() { return originalCalories; }
    public void setOriginalCalories(Double originalCalories) { this.originalCalories = originalCalories; }
    public Long getOriginalMealAnalysisId() { return originalMealAnalysisId; }
    public void setOriginalMealAnalysisId(Long originalMealAnalysisId) { this.originalMealAnalysisId = originalMealAnalysisId; }
    public String getSubstituteMealName() { return substituteMealName; }
    public void setSubstituteMealName(String substituteMealName) { this.substituteMealName = substituteMealName; }
    public List<MealIngredientInputDto> getIngredients() { return ingredients; }
    public void setIngredients(List<MealIngredientInputDto> ingredients) { this.ingredients = ingredients; }
}
