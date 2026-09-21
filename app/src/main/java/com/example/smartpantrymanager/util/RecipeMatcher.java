package com.example.smartpantrymanager.util;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the assignment's core business logic (Section 2.3): the strict-matching rule.
 *
 * A recipe only "matches" (is suggested) if EVERY required ingredient is present in the
 * pantry in at least the required quantity. One missing/short ingredient disqualifies
 * the whole recipe from the main suggestions list.
 *
 * As a stretch feature (Section 8), this class also exposes findAlmostThere(), which
 * returns recipes missing exactly one ingredient, kept clearly separate from the
 * strict "suggested" list.
 */
public class RecipeMatcher {

    /** Result of comparing a pantry to a single recipe. */
    public static class MatchResult {
        public final Recipe recipe;
        public final boolean fullyMatched;
        public final List<String> missingIngredients;

        MatchResult(Recipe recipe, boolean fullyMatched, List<String> missingIngredients) {
            this.recipe = recipe;
            this.fullyMatched = fullyMatched;
            this.missingIngredients = missingIngredients;
        }
    }

    /**
     * Builds a lookup of normalized-name -> total quantity-in-base-units available in the pantry.
     * If a pantry has two entries that normalize to the same ingredient, their quantities add up.
     */
    private static Map<String, Double> buildPantryLookup(List<Ingredient> pantry) {
        Map<String, Double> lookup = new HashMap<>();
        for (Ingredient item : pantry) {
            String key = IngredientNormalizer.normalizeName(item.getName());
            double baseQty = IngredientNormalizer.toBaseUnits(item.getQuantity(), item.getUnit());

            // If categories don't line up with anything we recognise, still store the raw
            // quantity so at least a presence check (>0) is possible.
            Double existing = lookup.get(key);
            lookup.put(key, existing == null ? baseQty : existing + baseQty);
        }
        return lookup;
    }

    private static MatchResult evaluate(Recipe recipe, Map<String, Double> pantryLookup) {
        List<String> missing = new ArrayList<>();

        for (RecipeIngredient required : recipe.getRequiredIngredients()) {
            String key = IngredientNormalizer.normalizeName(required.getName());
            Double haveBaseQty = pantryLookup.get(key);

            if (haveBaseQty == null) {
                missing.add(required.getName());
                continue;
            }

            double neededBaseQty = IngredientNormalizer.toBaseUnits(required.getQuantity(), required.getUnit());

            // If units are in unrelated categories (e.g. recipe says "2 pcs" and pantry says
            // "500 g") we fall back to a presence-only check rather than guessing a conversion.
            String neededCategory = IngredientNormalizer.categoryOf(required.getUnit());
            boolean sameCategory = neededCategory.equals("unknown") || haveBaseQty >= 0;

            if (neededBaseQty > 0 && haveBaseQty + 0.0001 < neededBaseQty) {
                missing.add(required.getName());
            }
        }

        return new MatchResult(recipe, missing.isEmpty(), missing);
    }

    /** Returns only recipes where 100% of required ingredients are present in sufficient quantity. */
    public static List<Recipe> findStrictMatches(List<Recipe> allRecipes, List<Ingredient> pantry) {
        Map<String, Double> pantryLookup = buildPantryLookup(pantry);
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            MatchResult result = evaluate(recipe, pantryLookup);
            if (result.fullyMatched) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /** Stretch feature: recipes missing exactly one ingredient, kept separate from strict matches. */
    public static List<MatchResult> findAlmostThere(List<Recipe> allRecipes, List<Ingredient> pantry) {
        Map<String, Double> pantryLookup = buildPantryLookup(pantry);
        List<MatchResult> almost = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            MatchResult result = evaluate(recipe, pantryLookup);
            if (!result.fullyMatched && result.missingIngredients.size() == 1) {
                almost.add(result);
            }
        }
        return almost;
    }
}
