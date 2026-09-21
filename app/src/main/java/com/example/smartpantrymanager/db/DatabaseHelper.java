package com.example.smartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Single source of truth for persistence. Uses plain SQLiteOpenHelper (Section 3.2 option 1)
 * so the CRUD logic is transparent for the report/video rather than hidden behind Room.
 *
 * Three tables:
 *  - pantry_items      : the user's own data (full CRUD)
 *  - recipes           : seeded once on first run, read-only from the app's point of view
 *  - recipe_ingredients: the required-ingredient lines for each recipe (seeded)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // Pantry table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "_id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QTY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // Recipes table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "_id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    // Recipe ingredients table
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QTY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT, " +
                COL_PANTRY_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");

        seedRecipes(db);
        seedSamplePantry(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ---------------------------------------------------------------------
    // PANTRY CRUD
    // ---------------------------------------------------------------------

    public long addIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, ingredient.getName());
        cv.put(COL_PANTRY_QTY, ingredient.getQuantity());
        cv.put(COL_PANTRY_UNIT, ingredient.getUnit());
        cv.put(COL_PANTRY_EXPIRY, ingredient.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, cv);
    }

    public int updateIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, ingredient.getName());
        cv.put(COL_PANTRY_QTY, ingredient.getQuantity());
        cv.put(COL_PANTRY_UNIT, ingredient.getUnit());
        cv.put(COL_PANTRY_EXPIRY, ingredient.getExpiryDate());
        return db.update(TABLE_PANTRY, cv, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(ingredient.getId())});
    }

    public int deleteIngredient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Ingredient getIngredient(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Ingredient ingredient = null;
        if (c.moveToFirst()) {
            ingredient = ingredientFromCursor(c);
        }
        c.close();
        return ingredient;
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COL_PANTRY_NAME + " ASC");
        while (c.moveToNext()) {
            list.add(ingredientFromCursor(c));
        }
        c.close();
        return list;
    }

    private Ingredient ingredientFromCursor(Cursor c) {
        return new Ingredient(
                c.getLong(c.getColumnIndexOrThrow(COL_PANTRY_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_PANTRY_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_UNIT)),
                c.getString(c.getColumnIndexOrThrow(COL_PANTRY_EXPIRY))
        );
    }

    // ---------------------------------------------------------------------
    // RECIPES (read)
    // ---------------------------------------------------------------------

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, COL_RECIPE_NAME + " ASC");
        while (c.moveToNext()) {
            Recipe recipe = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS))
            );
            attachIngredients(db, recipe);
            recipes.add(recipe);
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_RECIPE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS))
            );
            attachIngredients(db, recipe);
        }
        c.close();
        return recipe;
    }

    private void attachIngredients(SQLiteDatabase db, Recipe recipe) {
        Cursor ric = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipe.getId())}, null, null, COL_RI_ID + " ASC");
        while (ric.moveToNext()) {
            recipe.addRequiredIngredient(new RecipeIngredient(
                    ric.getString(ric.getColumnIndexOrThrow(COL_RI_NAME)),
                    ric.getDouble(ric.getColumnIndexOrThrow(COL_RI_QTY)),
                    ric.getString(ric.getColumnIndexOrThrow(COL_RI_UNIT))
            ));
        }
        ric.close();
    }

    // ---------------------------------------------------------------------
    // SEED DATA
    // ---------------------------------------------------------------------

    private long insertRecipe(SQLiteDatabase db, String name, String steps) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME, name);
        cv.put(COL_RECIPE_STEPS, steps);
        return db.insert(TABLE_RECIPES, null, cv);
    }

    private void insertRecipeIngredient(SQLiteDatabase db, long recipeId, String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RI_RECIPE_ID, recipeId);
        cv.put(COL_RI_NAME, name);
        cv.put(COL_RI_QTY, qty);
        cv.put(COL_RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, cv);
    }

    /** Seeds 18 recipes with realistic ingredient lists, satisfying the 15-20 minimum. */
    private void seedRecipes(SQLiteDatabase db) {
        long id;

        id = insertRecipe(db, "Tomato Egg Stir-fry",
                "1. Beat eggs and season.\n2. Fry eggs, set aside.\n3. Stir-fry tomato until soft.\n4. Combine, season, serve.");
        insertRecipeIngredient(db, id, "egg", 3, "pcs");
        insertRecipeIngredient(db, id, "tomato", 2, "pcs");
        insertRecipeIngredient(db, id, "onion", 1, "pcs");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Simple Garlic Fried Rice",
                "1. Fry minced garlic in oil until golden.\n2. Add rice, stir-fry.\n3. Season with salt.\n4. Serve hot.");
        insertRecipeIngredient(db, id, "rice", 300, "g");
        insertRecipeIngredient(db, id, "garlic", 3, "pcs");
        insertRecipeIngredient(db, id, "oil", 2, "tbsp");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Cheesy Scrambled Eggs",
                "1. Whisk eggs with milk.\n2. Cook on low heat, stirring.\n3. Fold in cheese until melted.\n4. Serve.");
        insertRecipeIngredient(db, id, "egg", 3, "pcs");
        insertRecipeIngredient(db, id, "milk", 50, "ml");
        insertRecipeIngredient(db, id, "cheese", 50, "g");

        id = insertRecipe(db, "Basic Vegetable Soup",
                "1. Saute onion and carrot.\n2. Add stock and potato, simmer 20 min.\n3. Season and serve.");
        insertRecipeIngredient(db, id, "onion", 1, "pcs");
        insertRecipeIngredient(db, id, "carrot", 2, "pcs");
        insertRecipeIngredient(db, id, "potato", 2, "pcs");
        insertRecipeIngredient(db, id, "vegetable stock", 500, "ml");

        id = insertRecipe(db, "Chicken and Rice Bowl",
                "1. Season and pan-fry chicken until cooked.\n2. Slice.\n3. Serve over cooked rice with a drizzle of oil.");
        insertRecipeIngredient(db, id, "chicken breast", 200, "g");
        insertRecipeIngredient(db, id, "rice", 200, "g");
        insertRecipeIngredient(db, id, "oil", 1, "tbsp");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Classic Pancakes",
                "1. Mix flour, egg and milk into a batter.\n2. Pour onto a hot greased pan.\n3. Flip when bubbles form.\n4. Serve.");
        insertRecipeIngredient(db, id, "flour", 200, "g");
        insertRecipeIngredient(db, id, "egg", 2, "pcs");
        insertRecipeIngredient(db, id, "milk", 250, "ml");
        insertRecipeIngredient(db, id, "sugar", 2, "tbsp");

        id = insertRecipe(db, "Buttered Toast with Jam",
                "1. Toast bread.\n2. Spread butter while hot.\n3. Top with jam.\n4. Serve.");
        insertRecipeIngredient(db, id, "bread", 2, "pcs");
        insertRecipeIngredient(db, id, "butter", 20, "g");
        insertRecipeIngredient(db, id, "jam", 2, "tbsp");

        id = insertRecipe(db, "Creamy Mashed Potatoes",
                "1. Boil potato until soft.\n2. Mash with butter and milk.\n3. Season with salt.\n4. Serve.");
        insertRecipeIngredient(db, id, "potato", 4, "pcs");
        insertRecipeIngredient(db, id, "butter", 30, "g");
        insertRecipeIngredient(db, id, "milk", 100, "ml");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Pasta Aglio e Olio",
                "1. Boil pasta until al dente.\n2. Fry sliced garlic in oil until golden.\n3. Toss pasta through oil, season.\n4. Serve.");
        insertRecipeIngredient(db, id, "pasta", 250, "g");
        insertRecipeIngredient(db, id, "garlic", 4, "pcs");
        insertRecipeIngredient(db, id, "oil", 3, "tbsp");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Cucumber Onion Salad",
                "1. Slice cucumber and onion thinly.\n2. Toss with oil and salt.\n3. Chill and serve.");
        insertRecipeIngredient(db, id, "cucumber", 2, "pcs");
        insertRecipeIngredient(db, id, "onion", 1, "pcs");
        insertRecipeIngredient(db, id, "oil", 1, "tbsp");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Carrot and Potato Curry",
                "1. Saute onion and garlic.\n2. Add carrot and potato with curry powder, simmer until tender.\n3. Serve with rice.");
        insertRecipeIngredient(db, id, "onion", 1, "pcs");
        insertRecipeIngredient(db, id, "garlic", 2, "pcs");
        insertRecipeIngredient(db, id, "carrot", 2, "pcs");
        insertRecipeIngredient(db, id, "potato", 3, "pcs");
        insertRecipeIngredient(db, id, "curry powder", 2, "tbsp");

        id = insertRecipe(db, "Cheese Omelette",
                "1. Beat eggs, season.\n2. Pour into hot pan.\n3. Sprinkle cheese, fold.\n4. Serve.");
        insertRecipeIngredient(db, id, "egg", 3, "pcs");
        insertRecipeIngredient(db, id, "cheese", 40, "g");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Banana Milk Smoothie",
                "1. Blend banana, milk and sugar until smooth.\n2. Pour into a glass and serve chilled.");
        insertRecipeIngredient(db, id, "banana", 2, "pcs");
        insertRecipeIngredient(db, id, "milk", 300, "ml");
        insertRecipeIngredient(db, id, "sugar", 1, "tbsp");

        id = insertRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice.\n2. Add cheese between slices.\n3. Grill both sides until golden.\n4. Serve.");
        insertRecipeIngredient(db, id, "bread", 2, "pcs");
        insertRecipeIngredient(db, id, "cheese", 60, "g");
        insertRecipeIngredient(db, id, "butter", 10, "g");

        id = insertRecipe(db, "Chicken Vegetable Stir-fry",
                "1. Pan-fry chicken until cooked.\n2. Add carrot and onion, stir-fry.\n3. Season and serve.");
        insertRecipeIngredient(db, id, "chicken breast", 200, "g");
        insertRecipeIngredient(db, id, "carrot", 1, "pcs");
        insertRecipeIngredient(db, id, "onion", 1, "pcs");
        insertRecipeIngredient(db, id, "oil", 1, "tbsp");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Tomato Pasta",
                "1. Boil pasta.\n2. Simmer tomato and garlic into a sauce.\n3. Toss pasta through sauce.\n4. Serve.");
        insertRecipeIngredient(db, id, "pasta", 250, "g");
        insertRecipeIngredient(db, id, "tomato", 3, "pcs");
        insertRecipeIngredient(db, id, "garlic", 2, "pcs");
        insertRecipeIngredient(db, id, "oil", 1, "tbsp");

        id = insertRecipe(db, "Fluffy Rice Porridge",
                "1. Simmer rice in a large amount of water until soft.\n2. Season with salt.\n3. Serve warm, optionally topped with egg.");
        insertRecipeIngredient(db, id, "rice", 150, "g");
        insertRecipeIngredient(db, id, "salt", 1, "tsp");

        id = insertRecipe(db, "Carrot Banana Muffins",
                "1. Mix flour, sugar, mashed banana and grated carrot.\n2. Add egg, combine.\n3. Bake until golden.\n4. Cool and serve.");
        insertRecipeIngredient(db, id, "flour", 200, "g");
        insertRecipeIngredient(db, id, "sugar", 100, "g");
        insertRecipeIngredient(db, id, "banana", 2, "pcs");
        insertRecipeIngredient(db, id, "carrot", 1, "pcs");
        insertRecipeIngredient(db, id, "egg", 1, "pcs");
    }

    /** A few starter pantry items so the app isn't empty on first launch (easy to edit/delete). */
    private void seedSamplePantry(SQLiteDatabase db) {
        insertPantrySeed(db, "Eggs", 6, "pcs", null);
        insertPantrySeed(db, "Tomatoes", 3, "pcs", null);
        insertPantrySeed(db, "Rice", 500, "g", null);
        insertPantrySeed(db, "Onion", 2, "pcs", null);
        insertPantrySeed(db, "Garlic", 4, "pcs", null);
        insertPantrySeed(db, "Salt", 200, "g", null);
        insertPantrySeed(db, "Oil", 250, "ml", null);
    }

    private void insertPantrySeed(SQLiteDatabase db, String name, double qty, String unit, String expiry) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, name);
        cv.put(COL_PANTRY_QTY, qty);
        cv.put(COL_PANTRY_UNIT, unit);
        cv.put(COL_PANTRY_EXPIRY, expiry);
        db.insert(TABLE_PANTRY, null, cv);
    }
}
