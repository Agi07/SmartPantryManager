# Smart Pantry Manager

An Android app (Java) that helps reduce food waste. The user keeps track of the ingredients they already have at home, and the app suggests only the recipes they can cook right now, using strictly those ingredients. No shopping trip required.

Built for **Mobile App Development 700** (Richfield, Faculty of Information Technology).

## Features

- **Pantry management**: add, edit and delete ingredients (name, quantity, unit, optional expiry date), with input validation
- **Pantry list**: a RecyclerView with a custom adapter, bound to the database; items expiring within 3 days are highlighted in red
- **17 preloaded recipes**: seeded into the database on first run
- **Suggested Recipes**: runs the strict-matching rule against the current pantry
- **Almost There** (bonus): a separate list of recipes missing exactly one ingredient, kept apart from the strict suggestions
- **Recipe detail**: full ingredient list (with a tick for what you have) and the cooking method
- **Settings**: expiring-soon alerts toggle and a units preference, saved with SharedPreferences
- A friendly message when no recipes match the pantry yet

## The strict-matching rule

A recipe is only suggested if **every** ingredient it needs is in the pantry, in at least the required quantity. A recipe that needs 5 ingredients when the pantry has 4 is never shown in the main list.

The matching tolerates simple real-world differences: capital letters, punctuation, singular vs plural ("tomato" vs "tomatoes"), and units (1 kg counts as 1000 g, 1 l as 1000 ml). The logic lives in `util/IngredientMatcher.java` and is covered by unit tests.

## Database choice: SQLite

I chose **SQLite** (using `SQLiteOpenHelper`) because the app's data is small, private to one user, and needs to work fully offline. It needs no server or account setup, and it handles the relational structure well: a `recipes` table linked to a `recipe_ingredients` table by a foreign key, plus a separate `pantry_items` table that holds the user's own data. Data is stored on the device, so it is still there after the app is closed and reopened.

## Project structure

- `model/`: `PantryItem`, `Recipe`, `RecipeIngredient`
- `data/`: `DatabaseHelper` (tables, CRUD, recipe seed data)
- `adapter/`: `PantryAdapter`, `RecipeAdapter` (RecyclerView adapters)
- `util/`: `IngredientMatcher` (strict-matching logic)
- Activities: `MainActivity` (pantry list), `AddEditIngredientActivity`, `SuggestedRecipesActivity`, `RecipeDetailActivity`, `SettingsActivity`

## Setup and run instructions

1. Install **Android Studio** (it includes the JDK and the Android SDK).
2. Clone this repository: `git clone https://github.com/Agi07/SmartPantryManager.git`
3. In Android Studio choose **Open** and select the cloned `SmartPantryManager` folder.
4. Wait for the Gradle sync to finish (the first sync can take a few minutes).
5. Create an emulator in **Device Manager** (or plug in an Android phone with USB debugging on). The minimum supported version is Android 7.0 (API 24).
6. Select the **app** run configuration and press **Run**.

No API keys or extra configuration are needed. The database is created and filled with the 17 recipes automatically the first time the app starts. To reset it, clear the app's storage or uninstall the app.

## Running the unit tests

Right-click `IngredientMatcherTest` (in `app/src/test`) and choose **Run**. The tests run on the computer and don't need an emulator.

## Scope

As required by the brief, the app does not use Google Maps, any mapping SDK, or location services.

## Author

Moagi Makhudu