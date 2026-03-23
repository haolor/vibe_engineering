package com.example.backend.dto;

import jakarta.validation.constraints.Min;

public class ProfileRequest {

    private Long userId;

    // One of heightCm / heightFt should be provided.
    private Double heightCm;
    private Double heightFt;

    // One of weightKg / weightLbs should be provided.
    private Double weightKg;
    private Double weightLbs;

    @Min(1)
    private int age;

    // Expected: "Nam" or "Nữ" (Vietnamese). We also accept "male"/"female".
    private String gender;

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public Double getHeightFt() {
        return heightFt;
    }

    public void setHeightFt(Double heightFt) {
        this.heightFt = heightFt;
    }

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

