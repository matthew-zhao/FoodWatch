package com.foodwatch;

public class FoodItem {
    private String name;
    private double calories;
    private double protein;
    private double totalFat;
    private double carbs;
    private double fiber;
    private double sugars;
    private double calcium;
    private double iron;
    private double potassium;
    private double sodium;
    private double vitC;

    public FoodItem(String name, double calories, double protein, double totalFat, double carbs,
                    double fiber, double sugars, double calcium, double iron, double potassium,
                    double sodium, double vitC) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.totalFat = totalFat;
        this.carbs = carbs;
        this.fiber = fiber;
        this.sugars = sugars;
        this.calcium = calcium;
        this.iron = iron;
        this.potassium = potassium;
        this.sodium = sodium;
        this.vitC = vitC;
    }
}
