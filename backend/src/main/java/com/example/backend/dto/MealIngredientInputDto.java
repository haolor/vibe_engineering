package com.example.backend.dto;

public class MealIngredientInputDto {
    private String name;
    private String quantityText;
    private Double amount;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getQuantityText() { return quantityText; }
    public void setQuantityText(String quantityText) { this.quantityText = quantityText; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
