package com.example.backend.repository;

import com.example.backend.entity.MealIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealIngredientRepository extends JpaRepository<MealIngredient, Long> {
    List<MealIngredient> findByMealAnalysisIdOrderByIdAsc(Long mealAnalysisId);
}
