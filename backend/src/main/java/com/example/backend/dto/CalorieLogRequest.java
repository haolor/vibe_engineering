package com.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CalorieLogRequest {

    private Long profileId;

    @NotNull
    @Min(1)
    private Double caloriesIn;

    private LocalDate logDate;

    private Long mealAnalysisId;

    private String note;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Double getCaloriesIn() {
        return caloriesIn;
    }

    public void setCaloriesIn(Double caloriesIn) {
        this.caloriesIn = caloriesIn;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public Long getMealAnalysisId() {
        return mealAnalysisId;
    }

    public void setMealAnalysisId(Long mealAnalysisId) {
        this.mealAnalysisId = mealAnalysisId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
