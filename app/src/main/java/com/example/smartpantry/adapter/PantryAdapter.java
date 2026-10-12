package com.example.smartpantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.PantryItem;

import java.util.List;

/**
 * RecyclerView adapter that binds the pantry_items table to the list screen.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Callbacks so the Activity handles edit/delete (keeps adapter reusable). */
    public interface OnPantryActionListener {
        void onEditClick(PantryItem item);
        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> items;
    private final boolean showExpiryAlerts;
    private final OnPantryActionListener listener;

    public PantryAdapter(List<PantryItem> items, boolean showExpiryAlerts,
                         OnPantryActionListener listener) {
        this.items = items;
        this.showExpiryAlerts = showExpiryAlerts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.nameText.setText(item.getName());
        holder.qtyText.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());

        // Expiry line: red "expiring soon" warning if alerts are on, else a
        // plain "Expires:" line when a date was captured.
        if (showExpiryAlerts && item.isExpiringSoon(3)) {
            holder.expiryText.setVisibility(View.VISIBLE);
            holder.expiryText.setText("Expiring soon: " + item.getExpiryDate());
            holder.expiryText.setTextColor(holder.itemView.getContext().getColor(R.color.expiry_red));
        } else if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            holder.expiryText.setVisibility(View.VISIBLE);
            holder.expiryText.setText("Expires: " + item.getExpiryDate());
            holder.expiryText.setTextColor(holder.itemView.getContext().getColor(R.color.ok_green));
        } else {
            holder.expiryText.setVisibility(View.GONE);
        }

        holder.editButton.setOnClickListener(v -> listener.onEditClick(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Avoids printing "2.0 pcs" when the quantity is a whole number. */
    private String formatQuantity(double qty) {
        return qty == Math.floor(qty) && !Double.isInfinite(qty)
                ? String.valueOf((int) qty)
                : String.valueOf(qty);
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, qtyText, expiryText;
        ImageButton editButton, deleteButton;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textName);
            qtyText = itemView.findViewById(R.id.textQty);
            expiryText = itemView.findViewById(R.id.textExpiry);
            editButton = itemView.findViewById(R.id.buttonEdit);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }
}