package com.example.backend;

import com.example.backend.service.BasicNutritionCalculator;
import org.junit.jupiter.api.Test;

class BackendApplicationTests {

    @Test
    void bmiCalculationShouldWork() {
        double bmi = BasicNutritionCalculator.calculateBmi(170, 60);
        // BMI ~ 20.8
        org.junit.jupiter.api.Assertions.assertTrue(bmi > 18 && bmi < 25);
    }

}
