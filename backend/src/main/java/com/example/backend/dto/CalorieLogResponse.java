package com.example.backend.dto;

import java.time.LocalDate;

public class CalorieLogResponse {

    private Long logId;
    private LocalDate logDate;
    private double caloriesIn;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public double getCaloriesIn() {
        return caloriesIn;
    }

    public void setCaloriesIn(double caloriesIn) {
        this.caloriesIn = caloriesIn;
    }
}
