package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "calorie_log",
        indexes = {
                @Index(name = "idx_calorie_log_profile_date", columnList = "profile_id,log_date")
        }
)
public class CalorieLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "calories_in", nullable = false)
    private double caloriesIn;

    @Column(name = "meal_analysis_id")
    private Long mealAnalysisId;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
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
