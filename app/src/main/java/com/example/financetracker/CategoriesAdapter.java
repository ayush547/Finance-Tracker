package com.example.financetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {
    ArrayList<CategoryItem> categoryItems;

    public CategoriesAdapter(ArrayList<CategoryItem> categoryItems) {
        this.categoryItems = categoryItems;
    }

    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        CategoriesViewHolder viewHolder = new CategoriesViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder holder, int position) {
        CategoryItem current = categoryItems.get(position);
        holder.titleTv.setText(current.getTitle());
        holder.amountTv.setText("Rs. " + current.getAmount());
        holder.amountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExpensesTracker.editNumberPopup(holder.getBindingAdapterPosition(), holder.amountTv);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryItems.size();
    }

    public static class CategoriesViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTv, amountTv;
        public RelativeLayout categoryRL;

        public CategoriesViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.tv_category);
            amountTv = itemView.findViewById(R.id.tv_amount);
            categoryRL = itemView.findViewById(R.id.rl_category);
        }
    }
}
