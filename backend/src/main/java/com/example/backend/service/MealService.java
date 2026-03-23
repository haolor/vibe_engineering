package com.example.backend.service;

import com.example.backend.dto.AutoSubstituteRequest;
import com.example.backend.dto.AutoSubstituteResponse;
import com.example.backend.dto.ManualSubstituteRequest;
import com.example.backend.dto.ManualSubstituteResponse;
import com.example.backend.dto.MealAnalyzeRequest;
import com.example.backend.dto.MealAnalyzeResponse;
import com.example.backend.dto.MealIngredientInputDto;
import com.example.backend.dto.MealIngredientResultDto;
import com.example.backend.dto.SubstituteOptionDto;
import com.example.backend.entity.MealAnalysis;
import com.example.backend.entity.MealIngredient;
import com.example.backend.entity.MealSubstitution;
import com.example.backend.entity.NutritionProfile;
import com.example.backend.repository.MealAnalysisRepository;
import com.example.backend.repository.MealIngredientRepository;
import com.example.backend.repository.MealSubstitutionRepository;
import com.example.backend.repository.NutritionProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class MealService {
    private static final Map<String, Double> CALORIES_CATALOG = Map.ofEntries(
            Map.entry("com", 130.0),
            Map.entry("gao", 130.0),
            Map.entry("ga", 165.0),
            Map.entry("thit bo", 250.0),
            Map.entry("ca", 140.0),
            Map.entry("trung", 78.0),
            Map.entry("rau", 35.0),
            Map.entry("khoai tay", 77.0),
            Map.entry("banh mi", 265.0),
            Map.entry("pho", 430.0),
            Map.entry("bun bo", 520.0),
            Map.entry("hu tieu", 460.0),
            Map.entry("pizza", 285.0),
            Map.entry("hamburger", 295.0),
            Map.entry("my", 160.0)
    );

    private static final Map<String, Double> SUBSTITUTE_CATALOG = Map.ofEntries(
            Map.entry("Salad ga ap chao", 340.0),
            Map.entry("Bun ga nuong", 410.0),
            Map.entry("Com ga luoc", 420.0),
            Map.entry("Pho bo tai", 430.0),
            Map.entry("Banh mi trung op la", 360.0),
            Map.entry("Ca hoi nuong rau cu", 470.0),
            Map.entry("Yen mach sua chua trai cay", 320.0),
            Map.entry("Sandwich ga nguyen cam", 390.0)
    );

    private final NutritionProfileRepository profileRepository;
    private final MealAnalysisRepository mealAnalysisRepository;
    private final MealIngredientRepository mealIngredientRepository;
    private final MealSubstitutionRepository mealSubstitutionRepository;
    private final ObjectMapper objectMapper;

    public MealService(
            NutritionProfileRepository profileRepository,
            MealAnalysisRepository mealAnalysisRepository,
            MealIngredientRepository mealIngredientRepository,
            MealSubstitutionRepository mealSubstitutionRepository,
            ObjectMapper objectMapper
    ) {
        this.profileRepository = profileRepository;
        this.mealAnalysisRepository = mealAnalysisRepository;
        this.mealIngredientRepository = mealIngredientRepository;
        this.mealSubstitutionRepository = mealSubstitutionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MealAnalyzeResponse analyze(MealAnalyzeRequest req) {
        NutritionProfile profile = resolveProfile(req.getProfileId());
        String mealName = req.getMealName() == null || req.getMealName().isBlank() ? "Mon an" : req.getMealName().trim();

        List<MealIngredientInputDto> inputIngredients = req.getIngredients() == null ? List.of() : req.getIngredients();
        List<MealIngredientResultDto> results = new ArrayList<>();
        double total = 0;
        for (MealIngredientInputDto in : inputIngredients) {
            if (in == null || in.getName() == null || in.getName().isBlank()) {
                continue;
            }
            double amount = in.getAmount() == null || in.getAmount() <= 0 ? 1.0 : in.getAmount();
            double calories = estimateIngredientCalories(in.getName(), amount);
            total += calories;

            MealIngredientResultDto out = new MealIngredientResultDto();
            out.setName(in.getName().trim());
            out.setQuantityText(in.getQuantityText());
            out.setCaloriesEstimated(calories);
            results.add(out);
        }

        if (results.isEmpty()) {
            MealIngredientResultDto fallback = new MealIngredientResultDto();
            fallback.setName(mealName);
            fallback.setQuantityText(req.getImageUrl() != null && !req.getImageUrl().isBlank() ? "image-detected" : "1 phan");
            fallback.setCaloriesEstimated(estimateIngredientCalories(mealName, 1.0));
            results.add(fallback);
            total = fallback.getCaloriesEstimated();
        }

        MealAnalysis analysis = new MealAnalysis();
        analysis.setProfileId(profile.getId());
        analysis.setMealName(mealName);
        analysis.setSourceType(req.getImageUrl() != null && !req.getImageUrl().isBlank() ? "IMAGE" : "MANUAL");
        analysis.setImageUrl(req.getImageUrl());
        analysis.setTotalCalories(total);
        analysis.setAnalysisJson(objectMapper.valueToTree(results));
        MealAnalysis saved = mealAnalysisRepository.save(analysis);

        for (MealIngredientResultDto r : results) {
            MealIngredient ing = new MealIngredient();
            ing.setMealAnalysisId(saved.getId());
            ing.setIngredientName(r.getName());
            ing.setQuantityText(r.getQuantityText());
            ing.setCaloriesEstimated(r.getCaloriesEstimated());
            mealIngredientRepository.save(ing);
        }

        MealAnalyzeResponse response = new MealAnalyzeResponse();
        response.setMealAnalysisId(saved.getId());
        response.setMealName(saved.getMealName());
        response.setTotalCalories(saved.getTotalCalories());
        response.setIngredients(results);
        return response;
    }

    @Transactional(readOnly = true)
    public AutoSubstituteResponse autoSubstitute(AutoSubstituteRequest req) {
        double originalCalories = resolveOriginalCalories(req.getOriginalCalories(), req.getOriginalMealAnalysisId());
        List<SubstituteOptionDto> options = SUBSTITUTE_CATALOG.entrySet().stream()
                .map(e -> {
                    SubstituteOptionDto dto = new SubstituteOptionDto();
                    dto.setMealName(e.getKey());
                    dto.setCalories(e.getValue());
                    dto.setDeltaCalories(Math.abs(e.getValue() - originalCalories));
                    return dto;
                })
                .sorted(Comparator.comparingDouble(SubstituteOptionDto::getDeltaCalories))
                .limit(5)
                .toList();

        AutoSubstituteResponse response = new AutoSubstituteResponse();
        response.setOriginalCalories(originalCalories);
        response.setOptions(options);
        return response;
    }

    @Transactional
    public ManualSubstituteResponse manualSubstitute(ManualSubstituteRequest req) {
        NutritionProfile profile = resolveProfile(req.getProfileId());
        double originalCalories = resolveOriginalCalories(req.getOriginalCalories(), req.getOriginalMealAnalysisId());

        double substituteCalories = 0;
        if (req.getIngredients() != null) {
            for (MealIngredientInputDto i : req.getIngredients()) {
                if (i == null || i.getName() == null || i.getName().isBlank()) {
                    continue;
                }
                double amount = i.getAmount() == null || i.getAmount() <= 0 ? 1.0 : i.getAmount();
                substituteCalories += estimateIngredientCalories(i.getName(), amount);
            }
        }

        String name = req.getSubstituteMealName() == null || req.getSubstituteMealName().isBlank()
                ? "Mon thay the"
                : req.getSubstituteMealName().trim();
        if (substituteCalories <= 0) {
            substituteCalories = estimateIngredientCalories(name, 1.0);
        }

        double diff = Math.abs(substituteCalories - originalCalories);
        boolean acceptable = diff <= Math.max(80.0, originalCalories * 0.15);
        String note = acceptable ? "Co the thay the mon goi y" : "Chenh lech calo cao, can can nhac";

        MealSubstitution substitution = new MealSubstitution();
        substitution.setProfileId(profile.getId());
        substitution.setOriginalMealAnalysisId(req.getOriginalMealAnalysisId());
        substitution.setSubstituteMealName(name);
        substitution.setSubstituteCalories(substituteCalories);
        substitution.setMode("MANUAL");
        substitution.setAcceptable(acceptable);
        substitution.setNote(note);
        mealSubstitutionRepository.save(substitution);

        ManualSubstituteResponse response = new ManualSubstituteResponse();
        response.setSubstituteMealName(name);
        response.setOriginalCalories(originalCalories);
        response.setSubstituteCalories(substituteCalories);
        response.setAcceptable(acceptable);
        response.setNote(note);
        return response;
    }

    private double estimateIngredientCalories(String ingredientName, double amount) {
        String name = ingredientName.toLowerCase().trim();
        double base = CALORIES_CATALOG.entrySet().stream()
                .filter(e -> name.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(120.0);
        return base * amount;
    }

    private NutritionProfile resolveProfile(Long profileId) {
        if (profileId != null) {
            return profileRepository.findById(profileId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay profileId=" + profileId));
        }
        return profileRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException("Chua co profile. Hay nhap BMI truoc."));
    }

    private double resolveOriginalCalories(Double originalCalories, Long originalMealAnalysisId) {
        if (originalCalories != null && originalCalories > 0) {
            return originalCalories;
        }
        if (originalMealAnalysisId != null) {
            return mealAnalysisRepository.findById(originalMealAnalysisId)
                    .map(MealAnalysis::getTotalCalories)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay mon goc de doi"));
        }
        throw new IllegalArgumentException("Can originalCalories hoac originalMealAnalysisId");
    }
}
