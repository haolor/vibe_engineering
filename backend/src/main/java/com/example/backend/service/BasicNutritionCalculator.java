package com.example.backend.service;

import java.time.LocalDate;

public class BasicNutritionCalculator {

    private static final double INCH_TO_CM = 2.54;
    private static final double FT_TO_CM = 30.48;
    private static final double LB_TO_KG = 0.45359237;

    public static double heightCmFromEither(Double heightCm, Double heightFt) {
        if (heightCm != null && heightCm > 0) {
            return heightCm;
        }
        if (heightFt != null && heightFt > 0) {
            return heightFt * FT_TO_CM;
        }
        throw new IllegalArgumentException("Thiếu chiều cao: cung cấp heightCm hoặc heightFt.");
    }

    public static double weightKgFromEither(Double weightKg, Double weightLbs) {
        if (weightKg != null && weightKg > 0) {
            return weightKg;
        }
        if (weightLbs != null && weightLbs > 0) {
            return weightLbs * LB_TO_KG;
        }
        throw new IllegalArgumentException("Thiếu cân nặng: cung cấp weightKg hoặc weightLbs.");
    }

    public static double calculateBmi(double heightCm, double weightKg) {
        double h = heightCm / 100.0;
        if (h <= 0) {
            throw new IllegalArgumentException("Chiều cao không hợp lệ.");
        }
        return weightKg / (h * h);
    }

    public static String normalizeGender(String gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Thiếu giới tính.");
        }
        String g = gender.trim().toLowerCase();
        if (g.equals("nam") || g.equals("male")) return "Nam";
        if (g.equals("nữ") || g.equals("nu") || g.equals("female")) return "Nữ";
        // Fallback: keep as-is (để dev dễ bắt lỗi dữ liệu UI)
        return gender.trim();
    }

    public static double calculateCaloriesPerDay(String normalizedGender, int age, double heightCm, double weightKg, String goalType) {
        // Mifflin-St Jeor equation. Activity factor fixed to 1.2 for MVP.
        double bmr;
        boolean isMale = "Nam".equalsIgnoreCase(normalizedGender);
        if (isMale) {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
        } else {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
        }
        double maintenance = bmr * 1.2;

        // MVP heuristic: fixed deficit/surplus.
        String g = goalType == null ? "" : goalType.trim().toLowerCase();
        double target;
        if (g.equals("tang") || g.equals("gain") || g.equals("tăng")) {
            target = maintenance + 300;
        } else if (g.equals("giam") || g.equals("lose") || g.equals("giảm")) {
            target = maintenance - 500;
        } else {
            // default: lose
            target = maintenance - 500;
        }

        // Practical floor so model doesn't suggest unsafe low calories.
        return Math.max(1200, target);
    }

    public static long timeframeDays(String timeframeType, int timeframeValue) {
        if (timeframeType == null || timeframeValue <= 0) {
            throw new IllegalArgumentException("Thiếu timeframe.");
        }
        String t = timeframeType.trim().toUpperCase();
        return switch (t) {
            case "DAYS" -> timeframeValue;
            case "WEEKS" -> (long) timeframeValue * 7;
            case "MONTHS" -> (long) timeframeValue * 30;
            case "YEARS" -> (long) timeframeValue * 365;
            default -> throw new IllegalArgumentException("timeframeType không hợp lệ: " + timeframeType);
        };
    }

    public static LocalDate defaultStartDate() {
        return LocalDate.now();
    }
}

