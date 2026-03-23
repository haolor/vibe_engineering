package com.example.backend.dto;

import java.util.List;

public class AutoSubstituteResponse {
    private double originalCalories;
    private List<SubstituteOptionDto> options;

    public double getOriginalCalories() { return originalCalories; }
    public void setOriginalCalories(double originalCalories) { this.originalCalories = originalCalories; }
    public List<SubstituteOptionDto> getOptions() { return options; }
    public void setOptions(List<SubstituteOptionDto> options) { this.options = options; }
}
