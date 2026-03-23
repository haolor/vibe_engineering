package com.example.backend.repository;

import com.example.backend.entity.CalorieLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalorieLogRepository extends JpaRepository<CalorieLog, Long> {

    Optional<CalorieLog> findByProfileIdAndLogDate(Long profileId, LocalDate logDate);

    List<CalorieLog> findByProfileIdAndLogDateBetweenOrderByLogDateAsc(Long profileId, LocalDate from, LocalDate to);
}
