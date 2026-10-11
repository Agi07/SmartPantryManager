package com.example.smartpantry.model;

import java.util.List;

/**
 * A recipe: a name, the ingredients it requires, and preparation steps.
 */
public class Recipe {

    private long id;
    private String name;
    private List<RecipeIngredient> ingredients;
    private String steps;

    public Recipe(long id, String name, List<RecipeIngredient> ingredients, String steps) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public String getSteps() { return steps; }
}