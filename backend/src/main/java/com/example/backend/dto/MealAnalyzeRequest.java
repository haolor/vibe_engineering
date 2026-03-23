package com.example.backend.dto;

import java.util.List;

public class MealAnalyzeRequest {
    private Long profileId;
    private String mealName;
    private String imageUrl;
    private List<MealIngredientInputDto> ingredients;

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<MealIngredientInputDto> getIngredients() { return ingredients; }
    public void setIngredients(List<MealIngredientInputDto> ingredients) { this.ingredients = ingredients; }
}
