package com.example.backend.dto;

public class AutoSubstituteRequest {
    private Long profileId;
    private Double originalCalories;
    private Long originalMealAnalysisId;

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Double getOriginalCalories() { return originalCalories; }
    public void setOriginalCalories(Double originalCalories) { this.originalCalories = originalCalories; }
    public Long getOriginalMealAnalysisId() { return originalMealAnalysisId; }
    public void setOriginalMealAnalysisId(Long originalMealAnalysisId) { this.originalMealAnalysisId = originalMealAnalysisId; }
}
