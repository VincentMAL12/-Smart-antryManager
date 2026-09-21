package com.example.smartpantrymanager.model;

/**
 * One required ingredient line inside a Recipe (not a pantry item).
 */
public class RecipeIngredient {

    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
