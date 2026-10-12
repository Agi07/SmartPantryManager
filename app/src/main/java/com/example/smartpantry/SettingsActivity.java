package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Screen 5: Settings. Stores preferences in SharedPreferences:
 *   - expiring-soon alerts toggle (read by MainActivity when rendering
 *     the pantry list)
 *   - units preference (metric vs household measures)
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String[] UNIT_OPTIONS = {"Metric (g, ml)", "Household (cups, tbsp)"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);

        SwitchMaterial alertsSwitch = findViewById(R.id.switchExpiryAlerts);
        Spinner unitSpinner = findViewById(R.id.spinnerUnitPref);

        // Load current values
        alertsSwitch.setChecked(prefs.getBoolean(MainActivity.KEY_EXPIRY_ALERTS, true));
        String currentUnit = prefs.getString(MainActivity.KEY_UNIT_PREF, UNIT_OPTIONS[0]);
        unitSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNIT_OPTIONS));
        for (int i = 0; i < UNIT_OPTIONS.length; i++) {
            if (UNIT_OPTIONS[i].equals(currentUnit)) unitSpinner.setSelection(i);
        }

        // Save changes. Flag prevents saving during initial spinner setup.
        alertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(MainActivity.KEY_EXPIRY_ALERTS, isChecked).apply();
            Toast.makeText(this, "Expiry alerts " + (isChecked ? "on" : "off"),
                    Toast.LENGTH_SHORT).show();
        });

        final boolean[] firstSelection = {true};
        unitSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view,
                                       int position, long id) {
                if (firstSelection[0]) {
                    firstSelection[0] = false;   // ignore the automatic initial call
                    return;
                }
                prefs.edit().putString(MainActivity.KEY_UNIT_PREF, UNIT_OPTIONS[position]).apply();
                Toast.makeText(SettingsActivity.this, "Unit preference saved",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
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