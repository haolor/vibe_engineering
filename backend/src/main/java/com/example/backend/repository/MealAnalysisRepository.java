package com.example.backend.repository;

import com.example.backend.entity.MealAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealAnalysisRepository extends JpaRepository<MealAnalysis, Long> {
}
