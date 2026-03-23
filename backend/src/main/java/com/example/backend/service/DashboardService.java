package com.example.backend.service;

import com.example.backend.dto.CaloriesPointDto;
import com.example.backend.dto.DashboardResponse;
import com.example.backend.dto.WeightPointDto;
import com.example.backend.entity.DietPlan;
import com.example.backend.entity.NutritionProfile;
import com.example.backend.entity.WeightLog;
import com.example.backend.repository.DietPlanRepository;
import com.example.backend.repository.NutritionProfileRepository;
import com.example.backend.repository.WeightLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final NutritionProfileRepository profileRepository;
    private final WeightLogRepository weightLogRepository;
    private final DietPlanRepository dietPlanRepository;

    public DashboardService(
            NutritionProfileRepository profileRepository,
            WeightLogRepository weightLogRepository,
            DietPlanRepository dietPlanRepository
    ) {
        this.profileRepository = profileRepository;
        this.weightLogRepository = weightLogRepository;
        this.dietPlanRepository = dietPlanRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long profileId, LocalDate from, LocalDate to) {
        NutritionProfile profile = resolveProfile(profileId);

        List<WeightLog> weights = weightLogRepository.findByProfileIdAndLogDateBetweenOrderByLogDateAsc(profile.getId(), from, to);

        List<WeightPointDto> weightHistory = new ArrayList<>();
        for (WeightLog w : weights) {
            WeightPointDto p = new WeightPointDto();
            p.setDate(w.getLogDate().toString());
            p.setWeightKg(w.getWeightKg());
            weightHistory.add(p);
        }

        List<CaloriesPointDto> calories = new ArrayList<>();
        DietPlan plan = dietPlanRepository.findTopByProfileIdOrderByIdDesc(profile.getId())
                .orElse(null);

        if (plan != null) {
            LocalDate planStart = plan.getStartDate();
            LocalDate planEnd = plan.getEndDate();

            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                if (!d.isBefore(planStart) && !d.isAfter(planEnd)) {
                    CaloriesPointDto c = new CaloriesPointDto();
                    c.setDate(d.toString());
                    c.setCaloriesPerDay(plan.getCaloriesPerDay());
                    calories.add(c);
                }
            }
        }

        DashboardResponse resp = new DashboardResponse();
        resp.setProfileId(profile.getId());
        resp.setWeightHistory(weightHistory);
        resp.setPlannedCaloriesHistory(calories);
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

