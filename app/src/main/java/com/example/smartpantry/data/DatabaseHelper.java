package com.example.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database for the Smart Pantry Manager.
 * Three tables:
 *   pantry_items          - the user's own ingredients (full CRUD)
 *   recipes               - seeded recipe collection (17 recipes)
 *   recipe_ingredients    - the ingredients each recipe requires
 *
 * SQLite was chosen because the data is small, private, must work fully
 * offline, and needs no server setup - see the report for full justification.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry_items (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "expiry TEXT)");

        db.execSQL("CREATE TABLE recipes (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE NOT NULL, " +
                "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE recipe_ingredients (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL REFERENCES recipes(_id) ON DELETE CASCADE, " +
                "ingredient_name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry_items");
        onCreate(db);
    }

    // ---------------- Pantry CRUD ----------------

    /** CREATE */
    public long addPantryItem(String name, double quantity, String unit, String expiry) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("quantity", quantity);
        cv.put("unit", unit);
        if (expiry != null && !expiry.isEmpty()) cv.put("expiry", expiry);
        return getWritableDatabase().insert("pantry_items", null, cv);
    }

    /** READ */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT _id, name, quantity, unit, expiry FROM pantry_items ORDER BY name", null);
        while (c.moveToNext()) {
            String expiry = c.isNull(4) ? "" : c.getString(4);
            items.add(new PantryItem(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3), expiry));
        }
        c.close();
        return items;
    }

    /** UPDATE */
    public int updatePantryItem(long id, String name, double quantity, String unit, String expiry) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("quantity", quantity);
        cv.put("unit", unit);
        if (expiry != null && !expiry.isEmpty()) cv.put("expiry", expiry);
        return getWritableDatabase().update("pantry_items", cv, "_id=?",
                new String[]{String.valueOf(id)});
    }

    /** DELETE */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete("pantry_items", "_id=?",
                new String[]{String.valueOf(id)});
    }

    // ---------------- Recipes ----------------

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT _id, name, steps FROM recipes ORDER BY name", null);
        while (c.moveToNext()) {
            long id = c.getLong(0);
            recipes.add(new Recipe(id, c.getString(1), getIngredientsFor(id), c.getString(2)));
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long id) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name, steps FROM recipes WHERE _id=?",
                new String[]{String.valueOf(id)});
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(id, c.getString(0), getIngredientsFor(id), c.getString(1));
        }
        c.close();
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsFor(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT _id, ingredient_name, quantity, unit FROM recipe_ingredients WHERE recipe_id=?",
                new String[]{String.valueOf(recipeId)});
        while (c.moveToNext()) {
            ingredients.add(new RecipeIngredient(c.getLong(0), recipeId, c.getString(1),
                    c.getDouble(2), c.getString(3)));
        }
        c.close();
        return ingredients;
    }

    // ---------------- Seed data ----------------

    /** Small helper so seed data reads like a list of ingredients. */
    private Object[] ing(String name, double qty, String unit) {
        return new Object[]{name, qty, unit};
    }

    /** Inserts one recipe and all of its ingredients. */
    private void addRecipe(SQLiteDatabase db, String name, String steps, Object[]... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert("recipes", null, recipeValues);

        for (Object[] ing : ingredients) {
            ContentValues cv = new ContentValues();
            cv.put("recipe_id", recipeId);
            cv.put("ingredient_name", (String) ing[0]);
            cv.put("quantity", (Double) ing[1]);
            cv.put("unit", (String) ing[2]);
            db.insert("recipe_ingredients", null, cv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "1. Crack the eggs into a bowl, add a pinch of salt and whisk.\n" +
                        "2. Melt the butter in a non-stick pan over medium heat.\n" +
                        "3. Pour in the eggs and stir gently until just set.",
                ing("Eggs", 2.0, "pcs"), ing("Butter", 10.0, "g"), ing("Salt", 1.0, "pcs"));

        addRecipe(db, "Tomato and Egg Stir Fry",
                "1. Chop the tomatoes.\n" +
                        "2. Scramble the eggs in hot oil and set aside.\n" +
                        "3. Fry the tomatoes until soft, return the eggs, season and serve.",
                ing("Eggs", 2.0, "pcs"), ing("Tomatoes", 2.0, "pcs"), ing("Oil", 15.0, "ml"));

        addRecipe(db, "Garlic Butter Pasta",
                "1. Boil the pasta in salted water until al dente.\n" +
                        "2. Melt the butter and gently fry the sliced garlic.\n" +
                        "3. Toss the pasta through the garlic butter and serve.",
                ing("Pasta", 100.0, "g"), ing("Butter", 20.0, "g"), ing("Garlic", 2.0, "pcs"));

        addRecipe(db, "Pancakes",
                "1. Whisk the flour, sugar, egg and milk into a smooth batter.\n" +
                        "2. Ladle batter into a hot, lightly greased pan.\n" +
                        "3. Flip when bubbles form and cook the other side until golden.",
                ing("Flour", 100.0, "g"), ing("Eggs", 1.0, "pcs"), ing("Milk", 150.0, "ml"), ing("Sugar", 10.0, "g"));

        addRecipe(db, "Cheese Toastie",
                "1. Butter the outsides of the bread slices.\n" +
                        "2. Sandwich the grated cheese between them.\n" +
                        "3. Toast in a pan until both sides are golden and the cheese melts.",
                ing("Bread", 2.0, "pcs"), ing("Cheese", 50.0, "g"), ing("Butter", 10.0, "g"));

        addRecipe(db, "Tomato Soup",
                "1. Roughly chop the tomatoes, onion and garlic.\n" +
                        "2. Soften them in hot oil for 5 minutes.\n" +
                        "3. Add 250 ml water, simmer 15 minutes, then blend until smooth.",
                ing("Tomatoes", 3.0, "pcs"), ing("Onion", 1.0, "pcs"), ing("Garlic", 1.0, "pcs"), ing("Oil", 10.0, "ml"));

        addRecipe(db, "Fried Rice",
                "1. Cook the rice and let it cool.\n" +
                        "2. Scramble the egg in hot oil and set aside.\n" +
                        "3. Fry the rice, add soy sauce, return the egg and stir through.",
                ing("Rice", 150.0, "g"), ing("Eggs", 1.0, "pcs"), ing("Soy sauce", 15.0, "ml"), ing("Oil", 10.0, "ml"));

        addRecipe(db, "Omelette",
                "1. Whisk the eggs with the milk and a pinch of salt.\n" +
                        "2. Melt the butter in a pan over medium heat.\n" +
                        "3. Pour in the eggs, cook until set, fold and slide onto a plate.",
                ing("Eggs", 2.0, "pcs"), ing("Milk", 20.0, "ml"), ing("Butter", 10.0, "g"));

        addRecipe(db, "Pasta Aglio e Olio",
                "1. Boil the pasta in salted water.\n" +
                        "2. Gently fry the sliced garlic and chilli in the olive oil.\n" +
                        "3. Toss the drained pasta through the oil and serve.",
                ing("Pasta", 100.0, "g"), ing("Garlic", 3.0, "pcs"), ing("Olive oil", 20.0, "ml"), ing("Chilli flakes", 1.0, "pcs"));

        addRecipe(db, "Baked Potato",
                "1. Prick the potatoes and microwave 8-10 minutes until soft.\n" +
                        "2. Split them open and season.\n" +
                        "3. Fill with butter and grated cheese and melt under a grill.",
                ing("Potatoes", 2.0, "pcs"), ing("Butter", 15.0, "g"), ing("Cheese", 30.0, "g"));

        addRecipe(db, "Banana Smoothie",
                "1. Peel the banana and break it into chunks.\n" +
                        "2. Blend with the milk and honey until smooth.\n" +
                        "3. Pour into a glass and serve immediately.",
                ing("Banana", 1.0, "pcs"), ing("Milk", 200.0, "ml"), ing("Honey", 10.0, "ml"));

        addRecipe(db, "French Toast",
                "1. Whisk the egg, milk and cinnamon in a shallow bowl.\n" +
                        "2. Soak the bread slices on both sides.\n" +
                        "3. Fry in a buttered pan until golden on both sides.",
                ing("Bread", 2.0, "pcs"), ing("Eggs", 1.0, "pcs"), ing("Milk", 50.0, "ml"), ing("Cinnamon", 1.0, "pcs"));

        addRecipe(db, "Vegetable Stir Fry",
                "1. Cut the broccoli into small florets.\n" +
                        "2. Fry the garlic in hot oil for 30 seconds.\n" +
                        "3. Add the broccoli, stir fry 4 minutes, then add soy sauce and serve.",
                ing("Oil", 15.0, "ml"), ing("Garlic", 2.0, "pcs"), ing("Broccoli", 100.0, "g"), ing("Soy sauce", 15.0, "ml"));

        addRecipe(db, "Rice and Beans",
                "1. Cook the rice according to the packet instructions.\n" +
                        "2. Fry the chopped onion in oil until soft.\n" +
                        "3. Add the beans, heat through, and serve over the rice.",
                ing("Rice", 150.0, "g"), ing("Beans", 100.0, "g"), ing("Onion", 1.0, "pcs"), ing("Oil", 10.0, "ml"));

        addRecipe(db, "Garlic Bread",
                "1. Mix the softened butter with crushed garlic.\n" +
                        "2. Spread generously over the bread.\n" +
                        "3. Bake at 180 C for 8-10 minutes until golden.",
                ing("Bread", 2.0, "pcs"), ing("Butter", 20.0, "g"), ing("Garlic", 2.0, "pcs"));

        addRecipe(db, "Egg Fried Noodles",
                "1. Cook the noodles, drain and set aside.\n" +
                        "2. Scramble the egg in hot oil.\n" +
                        "3. Add the noodles and soy sauce, toss well and serve.",
                ing("Noodles", 100.0, "g"), ing("Eggs", 1.0, "pcs"), ing("Soy sauce", 15.0, "ml"), ing("Oil", 10.0, "ml"));

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water.\n" +
                        "2. Fry the garlic, then add chopped tomatoes and simmer 10 minutes.\n" +
                        "3. Toss the sauce through the drained pasta and serve.",
                ing("Pasta", 100.0, "g"), ing("Tomatoes", 2.0, "pcs"), ing("Garlic", 2.0, "pcs"), ing("Olive oil", 15.0, "ml"));
    }

}