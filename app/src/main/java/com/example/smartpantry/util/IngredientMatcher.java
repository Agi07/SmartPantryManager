package com.example.smartpantry.util;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the STRICT-MATCHING rule that defines this app:
 * a recipe only qualifies as "suggested" if EVERY ingredient it requires
 * is present in the pantry in at least the required quantity.
 *
 * The matcher is deliberately tolerant of real-world messiness:
 *  - ingredient names are normalized (case, punctuation, simple plurals)
 *  - units are converted to a common base before comparing quantities
 */
public class IngredientMatcher {

    /** No instances needed - all methods are static. */
    private IngredientMatcher() { }

    /**
     * Normalizes an ingredient name so "Tomatoes!", "tomatoes" and "Tomato"
     * all become the same key. Handles simple English plurals.
     */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String n = raw.toLowerCase().trim()
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll("\\s+", " ");

        // common irregular plurals
        n = n.replace("tomatoes", "tomato");
        n = n.replace("potatoes", "potato");

        // simple plural rules
        if (n.endsWith("ies") && n.length() > 4) {
            n = n.substring(0, n.length() - 3) + "y";
        } else if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 3) {
            n = n.substring(0, n.length() - 1);
        }
        return n;
    }

    /**
     * Converts a quantity to a base unit so different units can be compared:
     * kg -> g, l -> ml, everything else is compared as-is (g, ml, pcs).
     */
    public static double toBase(double quantity, String unit) {
        if (unit == null) return quantity;
        switch (unit) {
            case "kg": return quantity * 1000;   // to grams
            case "l":  return quantity * 1000;   // to millilitres
            default:   return quantity;          // g, ml, pcs stay unchanged
        }
    }

    /**
     * Builds a lookup map: normalized ingredient name -> total base quantity
     * available in the pantry. Two pantry rows like "Eggs 6 pcs" and
     * "egg 6 pcs" merge into one entry.
     */
    public static Map<String, Double> pantryIndex(List<PantryItem> items) {
        Map<String, Double> index = new HashMap<>();
        for (PantryItem item : items) {
            String key = normalizeName(item.getName());
            double baseQty = toBase(item.getQuantity(), item.getUnit());
            index.merge(key, baseQty, Double::sum);
        }
        return index;
    }

    /**
     * THE STRICT-MATCHING RULE.
     * Returns true only if every required ingredient is in the pantry
     * with quantity >= what the recipe needs. One missing ingredient
     * (even 1 g short) means the recipe does NOT qualify.
     */
    public static boolean canMake(Recipe recipe, Map<String, Double> pantryIndex) {
        for (RecipeIngredient ing : recipe.getIngredients()) {
            Double have = pantryIndex.get(normalizeName(ing.getName()));
            double need = toBase(ing.getQuantity(), ing.getUnit());
            if (have == null || have < need) {
                return false;
            }
        }
        return true;
    }

}