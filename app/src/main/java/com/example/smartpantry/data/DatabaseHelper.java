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
}