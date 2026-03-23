package com.example.backend.dto;

public class GoalPlanRequest {

    // Expected: "TANG"/"GIAM" or "GAIN"/"LOSE"
    private String goalType;

    private Double targetWeightKg;

    // timeframeType: "DAYS"/"WEEKS"/"MONTHS"/"YEARS"
    private String timeframeType;
    private Integer timeframeValue;

    // optional (single-user MVP có thể bỏ)
    private Long profileId;

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public Double getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(Double targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public String getTimeframeType() {
        return timeframeType;
    }

    public void setTimeframeType(String timeframeType) {
        this.timeframeType = timeframeType;
    }

    public Integer getTimeframeValue() {
        return timeframeValue;
    }

    public void setTimeframeValue(Integer timeframeValue) {
        this.timeframeValue = timeframeValue;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
}

