package com.example.e_library;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.ViewHolder> {

    private final List<DrawerItem> drawerItems;
    private OnItemClickListener itemClickListener;
    private final Context context;

    public NavDrawerAdapter(List<DrawerItem> drawerItems, Context context) {
        this.drawerItems = drawerItems;
        this.context=context;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawer_item_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(drawerItems.get(position));
    }

    @Override
    public int getItemCount() {
        return drawerItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTextView = itemView.findViewById(R.id.item_text_view);


            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(position);
                    }
                }
            });

            itemTextView.setOnHoverListener((v, event) -> {
                itemTextView.setBackgroundColor(Color.GRAY);
                return true;
            });
        }

        public void bind(DrawerItem drawerItem) {
            itemTextView.setText(drawerItem.getItem());
            Drawable leftDrawable = ContextCompat.getDrawable(context, drawerItem.getImage());
            itemTextView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);
        }
    }
}
