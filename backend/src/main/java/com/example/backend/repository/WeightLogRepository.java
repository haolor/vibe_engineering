package com.example.backend.repository;

import com.example.backend.entity.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {

    List<WeightLog> findByProfileIdAndLogDateBetweenOrderByLogDateAsc(
            Long profileId,
            LocalDate from,
            LocalDate to
    );
}

