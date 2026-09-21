package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Ingredient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;

/**
 * Screen 2: Add / Edit Ingredient.
 * Handles both Create and Update (Section 3.2's CRUD requirement) depending on whether
 * an ingredient id was passed in via Intent extra (Section 3.1's "correct use of Intents").
 * Includes input validation (Section 3.1).
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    private DatabaseHelper dbHelper;
    private EditText editName, editQuantity, editExpiry;
    private Spinner spinnerUnit;
    private Button buttonSave, buttonDelete;

    private long editingId = -1; // -1 means "adding a new ingredient"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        dbHelper = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        editExpiry.setOnClickListener(v -> showDatePicker());

        editingId = getIntent().getLongExtra(MainActivity.EXTRA_INGREDIENT_ID, -1);
        if (editingId != -1) {
            loadExistingIngredient();
            buttonDelete.setVisibility(android.view.View.VISIBLE);
        }

        buttonSave.setOnClickListener(v -> saveIngredient());
        buttonDelete.setOnClickListener(v -> {
            dbHelper.deleteIngredient(editingId);
            finish();
        });
    }

    private void loadExistingIngredient() {
        Ingredient ingredient = dbHelper.getIngredient(editingId);
        if (ingredient == null) return;

        editName.setText(ingredient.getName());
        editQuantity.setText(formatQuantity(ingredient.getQuantity()));
        editExpiry.setText(ingredient.getExpiryDate());

        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(ingredient.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
    }

    private String formatQuantity(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            editExpiry.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveIngredient() {
        String name = editName.getText().toString().trim();
        String quantityText = editQuantity.getText().toString().trim();
        String unit = (String) spinnerUnit.getSelectedItem();
        String expiry = editExpiry.getText().toString().trim();

        // --- Input validation (Section 3.1) ---
        if (name.isEmpty()) {
            editName.setError(getString(R.string.error_name_required));
            editName.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            editQuantity.setError(getString(R.string.error_quantity_required));
            editQuantity.requestFocus();
            return;
        }

        Ingredient ingredient = new Ingredient(editingId, name, quantity, unit, expiry.isEmpty() ? null : expiry);

        if (editingId == -1) {
            dbHelper.addIngredient(ingredient);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateIngredient(ingredient);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
