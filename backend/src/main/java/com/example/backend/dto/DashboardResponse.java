package com.example.backend.dto;

import java.util.List;

public class DashboardResponse {

    private Long profileId;
    private List<WeightPointDto> weightHistory;
    private List<CaloriesPointDto> plannedCaloriesHistory;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public List<WeightPointDto> getWeightHistory() {
        return weightHistory;
    }

    public void setWeightHistory(List<WeightPointDto> weightHistory) {
        this.weightHistory = weightHistory;
    }

    public List<CaloriesPointDto> getPlannedCaloriesHistory() {
        return plannedCaloriesHistory;
    }

    public void setPlannedCaloriesHistory(List<CaloriesPointDto> plannedCaloriesHistory) {
        this.plannedCaloriesHistory = plannedCaloriesHistory;
    }
}

