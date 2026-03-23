package com.example.backend.dto;

public class ProfileResponse {
    private Long profileId;
    private double heightCm;
    private double weightKg;
    private int age;
    private String gender;
    private double bmi;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
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

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }
}

