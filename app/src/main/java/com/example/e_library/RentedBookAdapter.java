package com.example.e_library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RentedBookAdapter extends RecyclerView.Adapter<RentedBookAdapter.ViewHolder> {
    private final Context context;
    private final List<RentedBook> books;
    private ItemClickListener itemClickListener;

    public RentedBookAdapter(Context context, List<RentedBook> books) {
        this.context = context;
        this.books = books;
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }


    @NonNull
    @Override

    public RentedBookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rented_book_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RentedBookAdapter.ViewHolder holder, int position) {
        holder.bind(books.get(position));
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView bookAuth;
        final TextView bookName;
        final TextView expire;
        final Button readBook;
        final ImageView cover;


        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            bookName = itemView.findViewById(R.id.rented_book_name);
            bookAuth = itemView.findViewById(R.id.rented_book_author);
            expire = itemView.findViewById(R.id.expiry_date);
            readBook = itemView.findViewById(R.id.read_book);
            cover = itemView.findViewById(R.id.cover_image);

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(position);
                    }
                }
            });

        }

        @SuppressLint("SetTextI18n")
        public void bind(RentedBook rentedBook) {
            bookName.setText(rentedBook.getName());
            bookAuth.setText(rentedBook.getAuthor());
            expire.setText("Expires on " + rentedBook.getExpiryDate());
            Glide.with(context)
                    .load("https://conforming-entrance.000webhostapp.com/elib/coverpic/" + rentedBook.getImg())
                    .into(cover);

            readBook.setOnClickListener(v -> {
                String pdfUrl = "https://conforming-entrance.000webhostapp.com/elib/file/" + rentedBook.getFile();

                Intent intent = new Intent(context, PdfActivity.class);
                intent.putExtra("url",pdfUrl);
                intent.putExtra("isbn",rentedBook.getIsbnno());
                context.startActivity(intent);

            });
        }

    }
}