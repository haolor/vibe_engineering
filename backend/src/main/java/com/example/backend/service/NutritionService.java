package com.example.backend.service;

import com.example.backend.dto.GoalPlanRequest;
import com.example.backend.dto.BootstrapResponse;
import com.example.backend.dto.PlanResponse;
import com.example.backend.dto.ProfileRequest;
import com.example.backend.dto.ProfileResponse;
import com.example.backend.dto.MealPlanDto;
import com.example.backend.entity.DietPlan;
import com.example.backend.entity.NutritionProfile;
import com.example.backend.repository.DietPlanRepository;
import com.example.backend.repository.NutritionProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class NutritionService {

    private final NutritionProfileRepository profileRepository;
    private final DietPlanRepository dietPlanRepository;
    private final ObjectMapper objectMapper;
    private final MealPlanHeuristicService heuristicService = new MealPlanHeuristicService();
    private final GeminiMealPlanService geminiMealPlanService;

    public NutritionService(
            NutritionProfileRepository profileRepository,
            DietPlanRepository dietPlanRepository,
            ObjectMapper objectMapper,
            GeminiMealPlanService geminiMealPlanService
    ) {
        this.profileRepository = profileRepository;
        this.dietPlanRepository = dietPlanRepository;
        this.objectMapper = objectMapper;
        this.geminiMealPlanService = geminiMealPlanService;
    }

    @Transactional
    public ProfileResponse upsertProfile(ProfileRequest req) {
        double heightCm = BasicNutritionCalculator.heightCmFromEither(req.getHeightCm(), req.getHeightFt());
        double weightKg = BasicNutritionCalculator.weightKgFromEither(req.getWeightKg(), req.getWeightLbs());
        String gender = BasicNutritionCalculator.normalizeGender(req.getGender());
        int age = req.getAge();
        if (age <= 0) {
            throw new IllegalArgumentException("Tuổi phải > 0");
        }

        double bmi = BasicNutritionCalculator.calculateBmi(heightCm, weightKg);

        NutritionProfile profile = new NutritionProfile();
        profile.setUserId(req.getUserId());

        profile.setHeightCm(heightCm);
        profile.setWeightKg(weightKg);
        profile.setAge(age);
        profile.setGender(gender);
        profile.setBmi(bmi);

        NutritionProfile saved = profileRepository.save(profile);

        ProfileResponse resp = new ProfileResponse();
        resp.setProfileId(saved.getId());
        resp.setHeightCm(saved.getHeightCm());
        resp.setWeightKg(saved.getWeightKg());
        resp.setAge(saved.getAge());
        resp.setGender(saved.getGender());
        resp.setBmi(saved.getBmi());
        return resp;
    }

    @Transactional
    public PlanResponse createPlan(GoalPlanRequest req) {
        NutritionProfile profile = resolveProfile(req.getProfileId(), req.getUserId());
        double heightCm = profile.getHeightCm();
        double currentWeightKg = profile.getWeightKg();
        int age = profile.getAge();
        String gender = profile.getGender();

        String goalTypeRaw = req.getGoalType();
        if (req.getTargetWeightKg() == null || req.getTargetWeightKg() <= 0) {
            throw new IllegalArgumentException("Cần có targetWeightKg > 0");
        }

        long numberOfDays = BasicNutritionCalculator.timeframeDays(req.getTimeframeType(), req.getTimeframeValue());
        LocalDate start = BasicNutritionCalculator.defaultStartDate();
        LocalDate end = start.plusDays(numberOfDays - 1);

        double caloriesPerDay = BasicNutritionCalculator.calculateCaloriesPerDay(
                gender,
                age,
                heightCm,
                currentWeightKg,
                goalTypeRaw
        );

        MealPlanDto mealPlan = null;
        String llmRawText = null;

        if (geminiMealPlanService.isConfigured()) {
            try {
                var result = geminiMealPlanService.generateMealPlan(
                        gender,
                        age,
                        heightCm,
                        currentWeightKg,
                        req.getTargetWeightKg(),
                        goalTypeRaw,
                        numberOfDays,
                        start,
                        caloriesPerDay
                );
                mealPlan = result.plan();
                llmRawText = result.rawText();
            } catch (Exception e) {
                // Nếu Gemini chưa sẵn sàng hoặc response không parse được, fallback heuristic để app vẫn chạy.
                mealPlan = null;
                llmRawText = null;
            }
        }

        if (mealPlan == null) {
            mealPlan = heuristicService.generatePlan(
                    gender,
                    age,
                    heightCm,
                    currentWeightKg,
                    req.getTargetWeightKg(),
                    goalTypeRaw,
                    numberOfDays,
                    start,
                    caloriesPerDay
            );
        }

        try {
            DietPlan dp = new DietPlan();
            dp.setProfileId(profile.getId());
            dp.setGoalType(goalTypeRaw);
            dp.setTargetWeightKg(req.getTargetWeightKg());
            dp.setTimeframeType(req.getTimeframeType());
            dp.setTimeframeValue(req.getTimeframeValue());
            dp.setStartDate(start);
            dp.setEndDate(end);
            dp.setCaloriesPerDay(caloriesPerDay);
            JsonNode planJson = objectMapper.valueToTree(mealPlan);
            dp.setPlanJson(planJson);
            dp.setLlmRawText(llmRawText);
            dp.setCreatedAt(null); // sẽ được @PrePersist set

            DietPlan saved = dietPlanRepository.save(dp);

            PlanResponse resp = new PlanResponse();
            resp.setPlanId(saved.getId());
            resp.setProfileId(profile.getId());
            resp.setGoalType(saved.getGoalType());
            resp.setTargetWeightKg(saved.getTargetWeightKg());
            resp.setStartDate(start);
            resp.setEndDate(end);
            resp.setCaloriesPerDay(caloriesPerDay);
            resp.setPlanJson(mealPlan);
            return resp;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi serialize plan JSON", e);
        }
    }

    public NutritionProfile resolveProfile(Long profileId) {
        return resolveProfile(profileId, null);
    }

    public NutritionProfile resolveProfile(Long profileId, Long userId) {
        if (profileId != null) {
            return profileRepository.findById(profileId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy profileId=" + profileId));
        }
        if (userId != null) {
            return profileRepository.findTopByUserIdOrderByIdDesc(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Chua co chi so cho userId=" + userId));
        }
        return profileRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("Chưa có profile. Hãy nhập BMI trước."));
    }

    @Transactional(readOnly = true)
    public BootstrapResponse getBootstrap(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId la bat buoc");
        }
        NutritionProfile profile = profileRepository.findTopByUserIdOrderByIdDesc(userId).orElse(null);
        DietPlan plan = null;
        if (profile != null) {
            plan = dietPlanRepository.findTopByProfileIdOrderByIdDesc(profile.getId()).orElse(null);
        }

        BootstrapResponse resp = new BootstrapResponse();
        resp.setLatestProfile(profile == null ? null : toProfileResponse(profile));
        resp.setLatestPlan(plan == null ? null : toPlanResponse(plan));
        return resp;
    }

    private ProfileResponse toProfileResponse(NutritionProfile saved) {
        ProfileResponse resp = new ProfileResponse();
        resp.setProfileId(saved.getId());
        resp.setHeightCm(saved.getHeightCm());
        resp.setWeightKg(saved.getWeightKg());
        resp.setAge(saved.getAge());
        resp.setGender(saved.getGender());
        resp.setBmi(saved.getBmi());
        return resp;
    }

    private PlanResponse toPlanResponse(DietPlan saved) {
        PlanResponse resp = new PlanResponse();
        resp.setPlanId(saved.getId());
        resp.setProfileId(saved.getProfileId());
        resp.setGoalType(saved.getGoalType());
        resp.setTargetWeightKg(saved.getTargetWeightKg());
        resp.setStartDate(saved.getStartDate());
        resp.setEndDate(saved.getEndDate());
        resp.setCaloriesPerDay(saved.getCaloriesPerDay());
        resp.setPlanJson(objectMapper.convertValue(saved.getPlanJson(), MealPlanDto.class));
        return resp;
    }
}

