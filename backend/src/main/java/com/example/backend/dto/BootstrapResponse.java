package com.example.backend.dto;

public class BootstrapResponse {
    private ProfileResponse latestProfile;
    private PlanResponse latestPlan;

    public ProfileResponse getLatestProfile() {
        return latestProfile;
    }

    public void setLatestProfile(ProfileResponse latestProfile) {
        this.latestProfile = latestProfile;
    }

    public PlanResponse getLatestPlan() {
        return latestPlan;
    }

    public void setLatestPlan(PlanResponse latestPlan) {
        this.latestPlan = latestPlan;
    }
}
