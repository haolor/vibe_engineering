package com.example.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diet_plan")
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "goal_type", nullable = false, length = 10)
    private String goalType;

    @Column(name = "target_weight_kg", nullable = false)
    private double targetWeightKg;

    @Column(name = "timeframe_type", nullable = false, length = 10)
    private String timeframeType;

    @Column(name = "timeframe_value", nullable = false)
    private int timeframeValue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "calories_per_day", nullable = false)
    private double caloriesPerDay;

    // Store meal plan as JSON (JSONB in Postgres).
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plan_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode planJson;

    @Column(name = "llm_raw_text")
    private String llmRawText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
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

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public double getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(double targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public String getTimeframeType() {
        return timeframeType;
    }

    public void setTimeframeType(String timeframeType) {
        this.timeframeType = timeframeType;
    }

    public int getTimeframeValue() {
        return timeframeValue;
    }

    public void setTimeframeValue(int timeframeValue) {
        this.timeframeValue = timeframeValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getCaloriesPerDay() {
        return caloriesPerDay;
    }

    public void setCaloriesPerDay(double caloriesPerDay) {
        this.caloriesPerDay = caloriesPerDay;
    }

    public JsonNode getPlanJson() {
        return planJson;
    }

    public void setPlanJson(JsonNode planJson) {
        this.planJson = planJson;
    }

    public String getLlmRawText() {
        return llmRawText;
    }

    public void setLlmRawText(String llmRawText) {
        this.llmRawText = llmRawText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

