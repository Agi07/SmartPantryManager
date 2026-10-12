package com.example.smartpantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.RecipeIngredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.util.IngredientMatcher;

import java.util.List;
import java.util.Map;

/**
 * RecyclerView adapter for recipe lists. Two modes:
 *   suggested mode - subtitle shows "all ingredients available"
 *   almost mode    - subtitle shows which single ingredient is missing
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final boolean almostMode;
    private final Map<String, Double> pantryIndex;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, boolean almostMode,
                         Map<String, Double> pantryIndex, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.almostMode = almostMode;
        this.pantryIndex = pantryIndex;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.nameText.setText(recipe.getName());

        if (almostMode) {
            List<RecipeIngredient> missing = IngredientMatcher.missingIngredients(recipe, pantryIndex);
            StringBuilder sb = new StringBuilder("Missing: ");
            for (int i = 0; i < missing.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(missing.get(i).getName());
            }
            holder.subtitleText.setText(sb.toString());
        } else {
            holder.subtitleText.setText(recipe.getIngredients().size()
                    + " ingredients - all in your pantry");
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, subtitleText;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textRecipeName);
            subtitleText = itemView.findViewById(R.id.textRecipeSubtitle);
        }
    }
}