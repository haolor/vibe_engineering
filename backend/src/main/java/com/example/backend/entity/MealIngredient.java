package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_ingredient")
public class MealIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_analysis_id", nullable = false)
    private Long mealAnalysisId;

    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @Column(name = "quantity_text")
    private String quantityText;

    @Column(name = "calories_estimated", nullable = false)
    private double caloriesEstimated;

    public Long getId() { return id; }
    public Long getMealAnalysisId() { return mealAnalysisId; }
    public void setMealAnalysisId(Long mealAnalysisId) { this.mealAnalysisId = mealAnalysisId; }
    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public String getQuantityText() { return quantityText; }
    public void setQuantityText(String quantityText) { this.quantityText = quantityText; }
    public double getCaloriesEstimated() { return caloriesEstimated; }
    public void setCaloriesEstimated(double caloriesEstimated) { this.caloriesEstimated = caloriesEstimated; }
}
