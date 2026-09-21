package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Ingredient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Screen 1: Pantry List.
 * Shows every ingredient currently in the pantry (Read), lets the user add (Create),
 * tap to edit (Update) or press delete (Delete) - full CRUD lives across this screen
 * and AddEditIngredientActivity.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    public static final String EXTRA_INGREDIENT_ID = "extra_ingredient_id";

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerPantry);
        emptyView = findViewById(R.id.textEmptyPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PantryAdapter(dbHelper.getAllIngredients(), this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddIngredient);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddEditIngredientActivity.class)));

        findViewById(R.id.bottomNav).setSelected(true);
        ((com.google.android.material.bottomnavigation.BottomNavigationView) findViewById(R.id.bottomNav))
                .setSelectedItemId(R.id.nav_pantry);
        ((com.google.android.material.bottomnavigation.BottomNavigationView) findViewById(R.id.bottomNav))
                .setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_pantry) {
                        return true; // already here
                    } else if (id == R.id.nav_suggested) {
                        startActivity(new Intent(MainActivity.this, SuggestedRecipesActivity.class));
                        return true;
                    } else if (id == R.id.nav_settings) {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        return true;
                    }
                    return false;
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh every time we return here, so edits/deletes/adds made elsewhere show up
        // and so data is proven to persist across the Activity lifecycle (Section 5.1.2).
        refreshList();
    }

    private void refreshList() {
        List<Ingredient> ingredients = dbHelper.getAllIngredients();
        adapter.updateData(ingredients);
        boolean empty = ingredients.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEdit(Ingredient ingredient) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_INGREDIENT_ID, ingredient.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Ingredient ingredient) {
        dbHelper.deleteIngredient(ingredient.getId());
        refreshList();
    }
}
