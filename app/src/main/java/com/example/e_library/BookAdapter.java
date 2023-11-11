package com.example.e_library;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private final Context context;
    private final List<Book> books;


    private ItemClickListener itemClickListener;

    private  RentBookClickListener rentBookClickListener;

    public BookAdapter(Context context, List<Book> books) {
        this.context = context;
        this.books = books;
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public interface RentBookClickListener{
        void onRentBookClicked(int position);
    }



    public void setOnItemClickListener(BookAdapter.ItemClickListener listener) {
        this.itemClickListener = listener;
    }
    public void setOnRentBookClickedListener(RentBookClickListener rentBookClickListener){
        this.rentBookClickListener = rentBookClickListener;
    }

    @NonNull
    @Override

    public BookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=  LayoutInflater.from(context).inflate(R.layout.book_layout,parent,false);
        return new BookAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.bind(books.get(position));
        }
    }


    @Override
    public int getItemCount() {
        return books.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        final TextView bookAuth;
        final TextView bookName;
        final TextView bookRent;
        final Button rentBook;
        final ImageView cover;
        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            bookName=itemView.findViewById(R.id.book_name);
            bookAuth=itemView.findViewById(R.id.book_author);
            rentBook=itemView.findViewById(R.id.btn_rent_book);
            cover=itemView.findViewById(R.id.book_cover_image);
            bookRent=itemView.findViewById(R.id.book_rent);


            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(position);
                    }
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void bind(Book book) {
            bookName.setText(book.getName());
            bookAuth.setText(book.getAuthor());
            bookRent.setText(String.format("%s per month", book.getRent()));
            Glide.with(context)
                    .load(Config.getMyserverPicUrl()+book.getImg())
                    .into(cover);



            rentBook.setOnClickListener(v -> rentBookClickListener.onRentBookClicked(getBindingAdapterPosition()));


        }


    }
}
