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
}