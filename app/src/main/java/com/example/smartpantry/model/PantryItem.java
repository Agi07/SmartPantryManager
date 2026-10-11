package com.example.smartpantry.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A single ingredient stored in the user's pantry.
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;       // g, kg, ml, l or pcs
    private String expiryDate; // yyyy-MM-dd, or empty/null if none

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }

    /**
     * Returns true if the item expires within the given number of days.
     */
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null || expiryDate.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);
            long diffMillis = expiry.getTime() - System.currentTimeMillis();
            return diffMillis <= days * 24L * 60 * 60 * 1000;
        } catch (ParseException e) {
            return false;
        }
    }
}