package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Ingredient;

import java.util.List;

/**
 * Binds the user's pantry list (from SQLite) into a RecyclerView.
 * Each row supports tap-to-edit and a delete button (full CRUD from the UI).
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface Listener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
    }

    private List<Ingredient> ingredients;
    private final Listener listener;

    public PantryAdapter(List<Ingredient> ingredients, Listener listener) {
        this.ingredients = ingredients;
        this.listener = listener;
    }

    public void updateData(List<Ingredient> newIngredients) {
        this.ingredients = newIngredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);

        holder.name.setText(ingredient.getName());

        String qtyText = formatQuantity(ingredient.getQuantity()) + " " +
                (ingredient.getUnit() == null ? "" : ingredient.getUnit());
        holder.quantity.setText(qtyText.trim());

        if (ingredient.getExpiryDate() != null && !ingredient.getExpiryDate().isEmpty()) {
            holder.expiry.setVisibility(View.VISIBLE);
            holder.expiry.setText("Expires: " + ingredient.getExpiryDate());
        } else {
            holder.expiry.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onEdit(ingredient));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(ingredient));
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, expiry;
        View deleteButton;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textIngredientName);
            quantity = itemView.findViewById(R.id.textIngredientQuantity);
            expiry = itemView.findViewById(R.id.textIngredientExpiry);
            deleteButton = itemView.findViewById(R.id.buttonDeleteIngredient);
        }
    }
}
