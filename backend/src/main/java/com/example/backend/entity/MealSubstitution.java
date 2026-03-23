package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_substitution")
public class MealSubstitution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "original_meal_analysis_id")
    private Long originalMealAnalysisId;

    @Column(name = "substitute_meal_name", nullable = false)
    private String substituteMealName;

    @Column(name = "substitute_calories", nullable = false)
    private double substituteCalories;

    @Column(name = "mode", nullable = false)
    private String mode;

    @Column(name = "is_acceptable", nullable = false)
    private boolean acceptable;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getOriginalMealAnalysisId() { return originalMealAnalysisId; }
    public void setOriginalMealAnalysisId(Long originalMealAnalysisId) { this.originalMealAnalysisId = originalMealAnalysisId; }
    public String getSubstituteMealName() { return substituteMealName; }
    public void setSubstituteMealName(String substituteMealName) { this.substituteMealName = substituteMealName; }
    public double getSubstituteCalories() { return substituteCalories; }
    public void setSubstituteCalories(double substituteCalories) { this.substituteCalories = substituteCalories; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public boolean isAcceptable() { return acceptable; }
    public void setAcceptable(boolean acceptable) { this.acceptable = acceptable; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
