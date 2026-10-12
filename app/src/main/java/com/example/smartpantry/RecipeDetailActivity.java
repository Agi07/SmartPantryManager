package com.example.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;
import com.example.smartpantry.util.IngredientMatcher;

import java.util.List;
import java.util.Map;

/**
 * Screen 4: Recipe Detail. Receives a recipe id from the Intent, loads the
 * recipe from SQLite, and displays the full ingredient list (with a tick for
 * what the user already has) plus the cooking method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        DatabaseHelper db = new DatabaseHelper(this);
        Recipe recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            finish();
            return;
        }
        setTitle(recipe.getName());

        TextView nameText = findViewById(R.id.textRecipeName);
        TextView ingredientsText = findViewById(R.id.textIngredients);
        TextView stepsText = findViewById(R.id.textSteps);

        nameText.setText(recipe.getName());

        // Mark each ingredient with a tick if it is already in the pantry.
        List<PantryItem> pantry = db.getAllPantryItems();
        Map<String, Double> index = IngredientMatcher.pantryIndex(pantry);

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            Double have = index.get(IngredientMatcher.normalizeName(ing.getName()));
            double need = IngredientMatcher.toBase(ing.getQuantity(), ing.getUnit());
            boolean available = have != null && have >= need;
            sb.append(available ? "✓ " : "✗ ")
                    .append(ing.getName())
                    .append(" - ")
                    .append((int) ing.getQuantity() == ing.getQuantity()
                            ? String.valueOf((int) ing.getQuantity())
                            : String.valueOf(ing.getQuantity()))
                    .append(" ").append(ing.getUnit())
                    .append("\n");
        }
        ingredientsText.setText(sb.toString().trim());

        stepsText.setText(recipe.getSteps());
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}