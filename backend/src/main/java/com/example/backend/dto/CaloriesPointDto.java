package com.example.backend.dto;

public class CaloriesPointDto {
    private String date; // yyyy-MM-dd
    private double caloriesPerDay;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getCaloriesPerDay() {
        return caloriesPerDay;
    }

    public void setCaloriesPerDay(double caloriesPerDay) {
        this.caloriesPerDay = caloriesPerDay;
    }
}

