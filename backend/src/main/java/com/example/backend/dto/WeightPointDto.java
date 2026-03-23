package com.example.backend.dto;

public class WeightPointDto {
    private String date; // yyyy-MM-dd
    private double weightKg;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }
}

