package com.example.backend.service;

import com.example.backend.dto.MealDto;
import com.example.backend.dto.MealPlanDayDto;
import com.example.backend.dto.MealPlanDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealPlanHeuristicService {

    private static final List<String> BREAKFAST_NAMES = List.of(
            "Yogurt Hy Lạp kèm trái cây",
            "Trứng luộc + bánh mì nguyên cám",
            "Bột yến mạch + chuối",
            "Salad rau củ + ức gà xé",
            "Sữa chua không đường + hạt chia"
    );

    private static final List<String> LUNCH_NAMES = List.of(
            "Cơm gạo lứt + ức gà áp chảo",
            "Bún gạo lứt + thịt nạc + rau",
            "Cá hấp + rau luộc + khoai lang",
            "Cơm + tôm + rau xào ít dầu",
            "Mì soba + đậu hũ + rau"
    );

    private static final List<String> DINNER_NAMES = List.of(
            "Canh rau + thịt nạc xào",
            "Gỏi salad + cá hồi (hoặc cá ngừ)",
            "Cháo yến mạch + rau + tôm",
            "Đậu hũ sốt cà chua + rau",
            "Cơm ít + rau củ + trứng"
    );

    public MealPlanDto generatePlan(
            String normalizedGender,
            int age,
            double heightCm,
            double currentWeightKg,
            double targetWeightKg,
            String goalType,
            long numberOfDays,
            LocalDate startDate,
            double caloriesPerDay
    ) {
        String g = goalType == null ? "" : goalType.trim().toLowerCase();
        String title = (g.contains("giam") || g.contains("lose")) ? "Lộ trình giảm cân" : "Lộ trình tăng cân";

        List<MealPlanDayDto> days = new ArrayList<>();

        for (int i = 0; i < numberOfDays; i++) {
            int dayIndex = i + 1;
            LocalDate date = startDate.plusDays(i);

            double breakfastCalories = Math.round(caloriesPerDay * 0.25);
            double lunchCalories = Math.round(caloriesPerDay * 0.35);
            double dinnerCalories = Math.round(caloriesPerDay - breakfastCalories - lunchCalories);

            MealDto breakfast = new MealDto();
            breakfast.setMealType("SANG");
            breakfast.setName(BREAKFAST_NAMES.get(i % BREAKFAST_NAMES.size()));
            breakfast.setDescription("Bữa sáng cân bằng năng lượng, giàu đạm và chất xơ.");
            breakfast.setCaloriesEstimated(breakfastCalories);

            MealDto lunch = new MealDto();
            lunch.setMealType("TRUA");
            lunch.setName(LUNCH_NAMES.get(i % LUNCH_NAMES.size()));
            lunch.setDescription("Bữa trưa ưu tiên tinh bột phức + đạm nạc, giảm dầu mỡ.");
            lunch.setCaloriesEstimated(lunchCalories);

            MealDto dinner = new MealDto();
            dinner.setMealType("TOI");
            dinner.setName(DINNER_NAMES.get(i % DINNER_NAMES.size()));
            dinner.setDescription("Bữa tối nhẹ nhàng, tập trung rau và đạm, hạn chế tinh bột nhiều.");
            dinner.setCaloriesEstimated(dinnerCalories);

            List<MealDto> meals = List.of(breakfast, lunch, dinner);

            MealPlanDayDto dayDto = new MealPlanDayDto();
            dayDto.setDayIndex(dayIndex);
            dayDto.setDate(date.toString());
            dayDto.setMeals(meals);
            days.add(dayDto);
        }

        MealPlanDto plan = new MealPlanDto();
        plan.setPlanTitle(title);
        plan.setCaloriesPerDay(caloriesPerDay);
        plan.setDays(days);
        return plan;
    }
}

