package com.example.smartpantrymanager.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the "simple real-world messiness" requirement from the brief:
 *  - lower-casing / trimming ingredient names
 *  - naive but effective singular/plural folding ("tomatoes" -> "tomato")
 *  - unit synonym folding + conversion to a common base unit so that
 *    "1 kg" can be correctly compared against a recipe that needs "500 g".
 *
 * This is deliberately NOT a full NLP solution (the brief says that isn't required),
 * but it goes well beyond a naive exact-string match.
 */
public class IngredientNormalizer {

    // unit -> [category, multiplier to base unit of that category]
    private static final Map<String, String> UNIT_CATEGORY = new HashMap<>();
    private static final Map<String, Double> UNIT_TO_BASE = new HashMap<>();

    static {
        // Weight -> base unit: grams
        put("g", "weight", 1.0);
        put("gram", "weight", 1.0);
        put("grams", "weight", 1.0);
        put("kg", "weight", 1000.0);
        put("kilogram", "weight", 1000.0);
        put("kilograms", "weight", 1000.0);

        // Volume -> base unit: millilitres
        put("ml", "volume", 1.0);
        put("milliliter", "volume", 1.0);
        put("millilitre", "volume", 1.0);
        put("milliliters", "volume", 1.0);
        put("millilitres", "volume", 1.0);
        put("l", "volume", 1000.0);
        put("liter", "volume", 1000.0);
        put("litre", "volume", 1000.0);
        put("liters", "volume", 1000.0);
        put("litres", "volume", 1000.0);
        put("tsp", "volume", 4.93);
        put("teaspoon", "volume", 4.93);
        put("teaspoons", "volume", 4.93);
        put("tbsp", "volume", 14.79);
        put("tablespoon", "volume", 14.79);
        put("tablespoons", "volume", 14.79);
        put("cup", "volume", 240.0);
        put("cups", "volume", 240.0);

        // Count -> base unit: pieces
        put("pc", "count", 1.0);
        put("pcs", "count", 1.0);
        put("piece", "count", 1.0);
        put("pieces", "count", 1.0);
        put("unit", "count", 1.0);
        put("units", "count", 1.0);
    }

    private static void put(String unit, String category, double multiplier) {
        UNIT_CATEGORY.put(unit, category);
        UNIT_TO_BASE.put(unit, multiplier);
    }

    /** Lower-cases, trims, and folds simple plurals so "Tomatoes" and "tomato" match. */
    public static String normalizeName(String rawName) {
        if (rawName == null) return "";
        String s = rawName.trim().toLowerCase();
        s = s.replaceAll("\\s+", " ");

        if (s.endsWith("ies") && s.length() > 4) {
            // e.g. "berries" -> "berry"
            s = s.substring(0, s.length() - 3) + "y";
        } else if (s.endsWith("oes") && s.length() > 4) {
            // e.g. "tomatoes" -> "tomato", "potatoes" -> "potato"
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("ches") || s.endsWith("shes") || s.endsWith("xes")) {
            // e.g. "peaches" -> "peach"
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("s") && !s.endsWith("ss") && s.length() > 3) {
            // e.g. "eggs" -> "egg", "onions" -> "onion"
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** Returns the unit's category ("weight", "volume", "count") or "unknown". */
    public static String categoryOf(String unit) {
        if (unit == null) return "unknown";
        String key = unit.trim().toLowerCase();
        String cat = UNIT_CATEGORY.get(key);
        return cat == null ? "unknown" : cat;
    }

    /** Converts a quantity in the given unit to the category's base unit (g / ml / pcs). */
    public static double toBaseUnits(double quantity, String unit) {
        if (unit == null) return quantity;
        String key = unit.trim().toLowerCase();
        Double multiplier = UNIT_TO_BASE.get(key);
        return multiplier == null ? quantity : quantity * multiplier;
    }
}
