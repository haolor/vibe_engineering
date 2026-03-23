package com.example.backend.dto;

public class ManualSubstituteResponse {
    private String substituteMealName;
    private double originalCalories;
    private double substituteCalories;
    private boolean acceptable;
    private String note;

    public String getSubstituteMealName() { return substituteMealName; }
    public void setSubstituteMealName(String substituteMealName) { this.substituteMealName = substituteMealName; }
    public double getOriginalCalories() { return originalCalories; }
    public void setOriginalCalories(double originalCalories) { this.originalCalories = originalCalories; }
    public double getSubstituteCalories() { return substituteCalories; }
    public void setSubstituteCalories(double substituteCalories) { this.substituteCalories = substituteCalories; }
    public boolean isAcceptable() { return acceptable; }
    public void setAcceptable(boolean acceptable) { this.acceptable = acceptable; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
