package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.RecipeAdapter;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Screen 3: Suggested Recipes. Applies the strict-matching rule to every
 * seeded recipe against the current pantry:
 *   - recipes the user can make right now -> "You can make right now"
 *   - recipes missing exactly 1 ingredient -> "Almost there" (bonus section,
 *     kept clearly separate from the strict suggestions)
 * If neither list has entries, a helpful empty message is shown instead of
 * a blank screen.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private TextView headerSuggested, headerAlmost, emptySuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle("Suggested Recipes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        headerSuggested = findViewById(R.id.headerSuggested);
        headerAlmost = findViewById(R.id.headerAlmost);
        emptySuggestions = findViewById(R.id.emptySuggestions);
        RecyclerView recyclerSuggested = findViewById(R.id.recyclerSuggested);
        RecyclerView recyclerAlmost = findViewById(R.id.recyclerAlmost);
        recyclerSuggested.setLayoutManager(new LinearLayoutManager(this));
        recyclerAlmost.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);

        // 1. Load data and build the pantry lookup (normalised name -> qty)
        List<Recipe> allRecipes = db.getAllRecipes();
        List<PantryItem> pantry = db.getAllPantryItems();
        Map<String, Double> index = IngredientMatcher.pantryIndex(pantry);

        // 2. Apply the strict-matching rule to every recipe
        List<Recipe> suggested = new ArrayList<>();
        List<Recipe> almost = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (IngredientMatcher.canMake(recipe, index)) {
                suggested.add(recipe);                    // strict list
            } else if (IngredientMatcher.missingIngredients(recipe, index).size() == 1) {
                almost.add(recipe);                       // bonus "Almost there"
            }
        }

        // 3. Render the two sections (or the empty message)
        recyclerSuggested.setAdapter(new RecipeAdapter(suggested, false, index, this));
        recyclerAlmost.setAdapter(new RecipeAdapter(almost, true, index, this));

        boolean strictEmpty = suggested.isEmpty();
        boolean almostEmpty = almost.isEmpty();

        emptySuggestions.setVisibility(strictEmpty && almostEmpty ? View.VISIBLE : View.GONE);
        headerSuggested.setVisibility(strictEmpty ? View.GONE : View.VISIBLE);
        headerAlmost.setVisibility(almostEmpty ? View.GONE : View.VISIBLE);
    }

    /** Opens the recipe detail screen, passing the recipe id via the Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
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