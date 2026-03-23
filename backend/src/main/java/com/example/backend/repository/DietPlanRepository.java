package com.example.backend.repository;

import com.example.backend.entity.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {

    Optional<DietPlan> findTopByProfileIdOrderByIdDesc(Long profileId);

    Optional<DietPlan> findTopByProfileIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByIdDesc(
            Long profileId,
            LocalDate start,
            LocalDate end
    );
}

