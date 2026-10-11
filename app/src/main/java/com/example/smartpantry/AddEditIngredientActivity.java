package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.data.DatabaseHelper;

import java.util.Calendar;
import java.util.Locale;

/**
 * Screen 2: Add / Edit Ingredient. One form handles both create and update;
 * which mode is used is decided by whether an item id was passed in the Intent.
 * Demonstrates input validation and passing data between screens via Intents.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_QUANTITY = "quantity";
    public static final String EXTRA_UNIT = "unit";
    public static final String EXTRA_EXPIRY = "expiry";

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs"};

    private EditText nameEdit, quantityEdit, expiryEdit;
    private Spinner unitSpinner;
    private DatabaseHelper db;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = new DatabaseHelper(this);

        nameEdit = findViewById(R.id.editName);
        quantityEdit = findViewById(R.id.editQuantity);
        expiryEdit = findViewById(R.id.editExpiry);
        unitSpinner = findViewById(R.id.spinnerUnit);
        Button saveButton = findViewById(R.id.buttonSave);

        unitSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS));

        // If an id arrived in the Intent we are editing; otherwise adding.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            setTitle("Edit Ingredient");
            nameEdit.setText(getIntent().getStringExtra(EXTRA_NAME));
            quantityEdit.setText(String.valueOf(getIntent().getDoubleExtra(EXTRA_QUANTITY, 0)));
            String unit = getIntent().getStringExtra(EXTRA_UNIT);
            for (int i = 0; i < UNITS.length; i++) {
                if (UNITS[i].equals(unit)) unitSpinner.setSelection(i);
            }
            expiryEdit.setText(getIntent().getStringExtra(EXTRA_EXPIRY));
        } else {
            setTitle("Add Ingredient");
        }

        expiryEdit.setOnClickListener(v -> showDatePicker());

        saveButton.setOnClickListener(v -> saveItem());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
                expiryEdit.setText(String.format(Locale.getDefault(),
                        "%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void saveItem() {
        // Validation and saving are added in the next commit.
    }
}