package com.example.backend.dto;

import java.time.LocalDate;

public class WeightLogRequest {

    private Double weightKg;
    private Double weightLbs;

    private LocalDate logDate; // optional: default now

    // optional (single-user MVP có thể bỏ)
    private Long profileId;

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getWeightLbs() {
        return weightLbs;
    }

    public void setWeightLbs(Double weightLbs) {
        this.weightLbs = weightLbs;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
}

