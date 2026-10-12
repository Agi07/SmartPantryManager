package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.PantryAdapter;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Screen 1: Pantry List. Shows every ingredient in the SQLite database in a
 * RecyclerView, with add (FAB), edit and delete actions.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.OnPantryActionListener {

    // SharedPreferences keys (also used by SettingsActivity)
    public static final String PREFS = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final String KEY_UNIT_PREF = "unit_pref";

    private DatabaseHelper db;
    private RecyclerView recyclerView;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.pantryRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Opens the Add screen (no extras in the Intent = "add" mode)
        FloatingActionButton fab = findViewById(R.id.fabAddIngredient);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });
    }

    /** Activity lifecycle: reloading in onResume means the list always
     * reflects the database after returning from another screen. */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> pantryItems = db.getAllPantryItems();

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean alertsOn = prefs.getBoolean(KEY_EXPIRY_ALERTS, true);

        PantryAdapter adapter = new PantryAdapter(pantryItems, alertsOn, this);
        recyclerView.setAdapter(adapter);

        if (pantryItems.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // ---------------- Adapter callbacks ----------------

    @Override
    public void onEditClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        intent.putExtra(AddEditIngredientActivity.EXTRA_NAME, item.getName());
        intent.putExtra(AddEditIngredientActivity.EXTRA_QUANTITY, item.getQuantity());
        intent.putExtra(AddEditIngredientActivity.EXTRA_UNIT, item.getUnit());
        intent.putExtra(AddEditIngredientActivity.EXTRA_EXPIRY, item.getExpiryDate());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove \"" + item.getName() + "\" from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------- Toolbar menu ----------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_suggested) {
            startActivity(new Intent(this, SuggestedRecipesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}