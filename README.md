# Smart Pantry Manager

A Java Android application that helps reduce food waste by tracking the ingredients a
user actually has at home and suggesting recipes that can be made **strictly** from those
leftover ingredients — no shopping trip required.

## Database choice: SQLite (SQLiteOpenHelper)

SQLite was chosen because the app's data (pantry items and a seeded recipe collection)
is naturally local, relational, and doesn't need multi-device sync or a backend server.
Using `SQLiteOpenHelper` directly (rather than Room) keeps the CRUD SQL explicit and easy
to explain line-by-line in the video demonstration, which suited the module's coverage of
persistent data storage.

## Core feature: the strict-matching rule

A recipe is only suggested if **every** required ingredient is present in the pantry in at
least the required quantity — see `util/RecipeMatcher.java`. Ingredient names are normalised
(lower-cased, simple singular/plural folding, e.g. "tomatoes" → "tomato") and quantities are
converted to a common base unit per category (grams, millilitres, pieces) before comparison,
via `util/IngredientNormalizer.java`, so the match is robust to small real-world messiness
without needing a full NLP solution.

## Screens

1. **Pantry List** (`MainActivity`) — view, add, edit, delete pantry items (RecyclerView + SQLite CRUD)
2. **Add / Edit Ingredient** (`AddEditIngredientActivity`) — form with validation, shared for Create and Update
3. **Suggested Recipes** (`SuggestedRecipesActivity`) — runs the strict-matching rule live against the pantry
4. **Recipe Detail** (`RecipeDetailActivity`) — full ingredient list and method for a selected recipe
5. **Settings** (`SettingsActivity`) — expiry alert toggle and units preference (SharedPreferences)

## Setup / run instructions

1. Open Android Studio (Giraffe or newer recommended).
2. `File > Open` and select the `SmartPantryManager` project folder.
3. Let Gradle sync (this downloads dependencies automatically — internet required the first time).
4. Create/select an emulator (API 26+) or connect a physical device with USB debugging enabled.
5. Click **Run ▶** to build and install the app.
6. The database is seeded automatically on first launch with 18 recipes and a handful of
   starter pantry items, so the Suggested Recipes screen has data immediately.

## Out of scope (per assignment brief)

No Google Maps, mapping SDK, or device location/GPS features are used anywhere in this app.
