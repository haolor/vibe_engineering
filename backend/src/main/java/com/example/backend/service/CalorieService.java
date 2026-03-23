package com.example.backend.service;

import com.example.backend.dto.CalorieLogRequest;
import com.example.backend.dto.CalorieLogResponse;
import com.example.backend.entity.CalorieLog;
import com.example.backend.entity.NutritionProfile;
import com.example.backend.repository.CalorieLogRepository;
import com.example.backend.repository.NutritionProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CalorieService {

    private final CalorieLogRepository calorieLogRepository;
    private final NutritionProfileRepository profileRepository;

    public CalorieService(CalorieLogRepository calorieLogRepository, NutritionProfileRepository profileRepository) {
        this.calorieLogRepository = calorieLogRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public CalorieLogResponse addOrUpdate(CalorieLogRequest req) {
        NutritionProfile profile = resolveProfile(req.getProfileId());
        LocalDate logDate = req.getLogDate() != null ? req.getLogDate() : LocalDate.now();

        CalorieLog log = calorieLogRepository.findByProfileIdAndLogDate(profile.getId(), logDate)
                .orElseGet(CalorieLog::new);

        log.setProfileId(profile.getId());
        log.setLogDate(logDate);
        log.setCaloriesIn(req.getCaloriesIn());
        log.setMealAnalysisId(req.getMealAnalysisId());
        log.setNote(req.getNote());

        CalorieLog saved = calorieLogRepository.save(log);
        CalorieLogResponse resp = new CalorieLogResponse();
        resp.setLogId(saved.getId());
        resp.setLogDate(saved.getLogDate());
        resp.setCaloriesIn(saved.getCaloriesIn());
        return resp;
    }

    private NutritionProfile resolveProfile(Long profileId) {
        if (profileId != null) {
            return profileRepository.findById(profileId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay profileId=" + profileId));
        }
        return profileRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("Chua co profile. Hay nhap BMI truoc."));
    }
}
