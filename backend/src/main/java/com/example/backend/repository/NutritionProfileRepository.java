package com.example.backend.repository;

import com.example.backend.entity.NutritionProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutritionProfileRepository extends JpaRepository<NutritionProfile, Long> {

    Optional<NutritionProfile> findTopByOrderByIdDesc();

    Optional<NutritionProfile> findTopByUserIdOrderByIdDesc(Long userId);
}

