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

}