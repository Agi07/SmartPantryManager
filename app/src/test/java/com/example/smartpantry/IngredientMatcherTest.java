package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;
import com.example.smartpantry.util.IngredientMatcher;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for the strict-matching rule (runs on the computer, no emulator needed).
 */
public class IngredientMatcherTest {

    private Recipe omelette() {
        return new Recipe(1, "Omelette", Arrays.asList(
                new RecipeIngredient(1, 1, "Eggs", 2, "pcs"),
                new RecipeIngredient(2, 1, "Milk", 20, "ml"),
                new RecipeIngredient(3, 1, "Butter", 10, "g")), "steps");
    }

    private Map<String, Double> index(PantryItem... items) {
        return IngredientMatcher.pantryIndex(Arrays.asList(items));
    }

    @Test
    public void allIngredientsPresent_isSuggested() {
        Map<String, Double> pantry = index(
                new PantryItem(1, "Eggs", 6, "pcs", ""),
                new PantryItem(2, "Milk", 1, "l", ""),
                new PantryItem(3, "Butter", 100, "g", ""));
        assertTrue(IngredientMatcher.canMake(omelette(), pantry));
    }

    @Test
    public void oneIngredientMissing_isNotSuggested() {
        Map<String, Double> pantry = index(
                new PantryItem(1, "Eggs", 6, "pcs", ""),
                new PantryItem(2, "Milk", 1, "l", ""));
        assertFalse(IngredientMatcher.canMake(omelette(), pantry));
    }

    @Test
    public void quantityTooLow_isNotSuggested() {
        Map<String, Double> pantry = index(
                new PantryItem(1, "Eggs", 1, "pcs", ""),
                new PantryItem(2, "Milk", 1, "l", ""),
                new PantryItem(3, "Butter", 100, "g", ""));
        assertFalse(IngredientMatcher.canMake(omelette(), pantry));
    }

    @Test
    public void singularVsPlural_stillMatches() {
        Recipe soup = new Recipe(2, "Soup", Arrays.asList(
                new RecipeIngredient(4, 2, "Tomatoes", 2, "pcs")), "steps");
        assertTrue(IngredientMatcher.canMake(soup,
                index(new PantryItem(1, "tomato", 3, "pcs", ""))));
    }

    @Test
    public void kilogramsVsGrams_stillMatches() {
        Recipe bread = new Recipe(3, "Bread", Arrays.asList(
                new RecipeIngredient(5, 3, "Flour", 500, "g")), "steps");
        assertTrue(IngredientMatcher.canMake(bread,
                index(new PantryItem(1, "Flour", 1, "kg", ""))));
    }

    @Test
    public void normalizeName_handlesCaseAndPlurals() {
        assertEquals("tomato", IngredientMatcher.normalizeName("Tomatoes!"));
        assertEquals("egg", IngredientMatcher.normalizeName("  EGGS "));
        assertEquals("berry", IngredientMatcher.normalizeName("Berries"));
    }
}