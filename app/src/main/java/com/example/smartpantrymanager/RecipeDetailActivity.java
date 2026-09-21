package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Screen 4: Recipe Detail.
 * Receives a recipe id via Intent extra from SuggestedRecipesActivity and shows the
 * full ingredient list and method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(SuggestedRecipesActivity.EXTRA_RECIPE_ID, -1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Recipe recipe = dbHelper.getRecipe(recipeId);

        TextView nameView = findViewById(R.id.textDetailName);
        TextView ingredientsView = findViewById(R.id.textDetailIngredients);
        TextView stepsView = findViewById(R.id.textDetailSteps);

        if (recipe == null) {
            nameView.setText("Recipe not found");
            return;
        }

        nameView.setText(recipe.getName());

        StringBuilder ingredientsText = new StringBuilder();
        for (RecipeIngredient ri : recipe.getRequiredIngredients()) {
            ingredientsText.append("• ")
                    .append(formatQuantity(ri.getQuantity()))
                    .append(" ")
                    .append(ri.getUnit() == null ? "" : ri.getUnit())
                    .append(" ")
                    .append(ri.getName())
                    .append("\n");
        }
        ingredientsView.setText(ingredientsText.toString().trim());
        stepsView.setText(recipe.getSteps());
    }

    private String formatQuantity(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }
}
