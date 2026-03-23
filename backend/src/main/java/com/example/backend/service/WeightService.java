package com.example.backend.service;

import com.example.backend.dto.WeightLogRequest;
import com.example.backend.dto.WeightLogResponse;
import com.example.backend.entity.NutritionProfile;
import com.example.backend.entity.WeightLog;
import com.example.backend.repository.NutritionProfileRepository;
import com.example.backend.repository.WeightLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class WeightService {

    private final WeightLogRepository weightLogRepository;
    private final NutritionProfileRepository profileRepository;

    public WeightService(WeightLogRepository weightLogRepository, NutritionProfileRepository profileRepository) {
        this.weightLogRepository = weightLogRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public WeightLogResponse addWeightLog(WeightLogRequest req) {
        NutritionProfile profile = resolveProfile(req.getProfileId());
        double weightKg = BasicNutritionCalculator.weightKgFromEither(req.getWeightKg(), req.getWeightLbs());
        LocalDate logDate = req.getLogDate() != null ? req.getLogDate() : LocalDate.now();

        WeightLog wl = new WeightLog();
        wl.setProfileId(profile.getId());
        wl.setLogDate(logDate);
        wl.setWeightKg(weightKg);

        WeightLog saved = weightLogRepository.save(wl);
        return toResponse(saved);
    }

    private WeightLogResponse toResponse(WeightLog wl) {
        WeightLogResponse resp = new WeightLogResponse();
        resp.setLogId(wl.getId());
        resp.setLogDate(wl.getLogDate());
        resp.setWeightKg(wl.getWeightKg());
        return resp;
    }

    private NutritionProfile resolveProfile(Long profileId) {
        if (profileId != null) {
            return profileRepository.findById(profileId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy profileId=" + profileId));
        }
        return profileRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("Chưa có profile. Hãy nhập BMI trước."));
    }
}

