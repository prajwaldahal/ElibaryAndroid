package com.example.e_library;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
    List<RecentBook> recentBook;
    Context context;
    ItemClickListener itemClickListener;
    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public RecentAdapter(Context context, List<RecentBook> recentBook) {
        this.context = context;
        this.recentBook = recentBook;
    }

    public void setOnItemClickListener(RecentAdapter.ItemClickListener itemClickListener){
       this.itemClickListener=itemClickListener;
    }

    @NonNull
    @Override
    public RecentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=  LayoutInflater.from(context).inflate(R.layout.recent_layout,parent,false);
        return new RecentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentAdapter.ViewHolder holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.bind(recentBook.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return recentBook.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name,author;
        ImageView cover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            author=itemView.findViewById(R.id.author);
            cover=itemView.findViewById(R.id.cover_image);

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(position);
                    }
                }
            });
        }

        public void bind(RecentBook book) {
            name.setText(book.getName());
            author.setText(book.getAuthor());
            Glide.with(context)
                    .load(Config.getMyserverPicUrl()+book.getImg())
                    .into(cover);





        }
    }
}
