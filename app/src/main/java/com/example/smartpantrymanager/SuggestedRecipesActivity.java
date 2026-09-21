package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.util.RecipeMatcher;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * Screen 3: Suggested Recipes.
 * Runs the strict-matching rule (Section 2.3) against the live pantry every time the
 * screen is shown, so adding/removing an ingredient elsewhere immediately changes
 * what's suggested here - this is exactly what the video demo (Section 5.1.2) must prove.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        dbHelper = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerSuggested);
        emptyView = findViewById(R.id.textEmptyRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecipeAdapter(java.util.Collections.emptyList(), this, false);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    private void refreshSuggestions() {
        List<Ingredient> pantry = dbHelper.getAllIngredients();
        List<Recipe> allRecipes = dbHelper.getAllRecipes();

        // Core business logic call - see RecipeMatcher for the strict-matching implementation.
        List<Recipe> matches = RecipeMatcher.findStrictMatches(allRecipes, pantry);

        adapter.updateData(matches);
        boolean empty = matches.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
