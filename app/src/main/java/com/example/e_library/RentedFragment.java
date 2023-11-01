package com.example.e_library;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RentedFragment extends Fragment {

    ProgressBar progressBar;
    RecyclerView recyclerView;
    Context context;
    ArrayList<RentedBook> rentedBooks;
    RentedBookAdapter rentedBookAdapter;

    DatabaseHelper databaseHelper;

    FloatingActionButton floatingActionButton;

    TextView textView;

    StoreData storeData;

    Spinner spinner;

    SortBy sortBy;

    APIServices apiServices;

    public RentedFragment(Context context) {
        this.context = context;
        apiServices=RetrofitBook.getRetrofitInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        storeData = new StoreData(context);

        View view=inflater.inflate(R.layout.fragment_rented, container, false);
        progressBar = view.findViewById(R.id.progress_bar);
        textView=view.findViewById(R.id.error_text);
        floatingActionButton=view.findViewById(R.id.refresh);
        floatingActionButton.setOnClickListener(v -> saveLocally());
        databaseHelper = new DatabaseHelper(context);
        recyclerView=view.findViewById(R.id.rented_book_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        rentedBooks=new ArrayList<>();
        getRentedData();

        spinner=view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (rentedBooks != null) {
                    String selectedValue = (String) parent.getSelectedItem();
                    Log.d("selected", "onItemSelected: "+selectedValue);
                    switch (selectedValue) {
                        case "by Name": {
                            sortBy = SortBy.NAME;
                            break;
                        }
                        case "by Price": {
                            sortBy = SortBy.PRICE;
                            break;
                        }
                        case "by ISBN no": {
                            sortBy = SortBy.ISBNNO;
                            break;
                        }

                        case "by Expiry Date": {
                            sortBy=SortBy.EXPIRY;
                            Log.d("SORT_VALUE", "onItemSelected:expiry");
                            break;
                        }

                        case "by Rented Date":{
                            sortBy=SortBy.RENTED;
                            break;
                        }
                    }

                    Sort <RentedBook> sort = new Sort<>(sortBy, rentedBooks);
                    rentedBooks = (ArrayList<RentedBook>) sort.mergeSort();
                    rentedBookAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rentedBookAdapter=new RentedBookAdapter(context,rentedBooks);
        rentedBookAdapter.setOnItemClickListener(this::showDialog);

        recyclerView.setAdapter(rentedBookAdapter);


        return  view;
    }

    @Override
    public void onStart() {

        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }
        super.onStart();

    }

    private void showDialog(int position) {

        TextView name,publisher,isbnno,rentedDate,author,expiryDate;
        ImageView imageView;
        Dialog dialog=new Dialog(context);
        dialog.setContentView(R.layout.rented_book_dialog);

        name=dialog.findViewById(R.id.book_dialog_name);
        publisher=dialog.findViewById(R.id.dialog_book_publisher);
        isbnno=dialog.findViewById(R.id.dialog_book_id);
        rentedDate=dialog.findViewById(R.id.dialog_book_rented_date);
        author=dialog.findViewById(R.id.dialog_book_author);
        imageView=dialog.findViewById(R.id.dialog_cover_image);
        expiryDate=dialog.findViewById(R.id.dialog_book_expiry_date);

        name.setText(rentedBooks.get(position).getName());
        publisher.setText(rentedBooks.get(position).getPublisher());
        name.setText(rentedBooks.get(position).getName());
        isbnno.setText(rentedBooks.get(position).getIsbnno());
        rentedDate.setText(rentedBooks.get(position).getRenteddate());
        author.setText(rentedBooks.get(position).getAuthor());
        expiryDate.setText(rentedBooks.get(position).getExpiryDate());
        Glide.with(context)
                .load("https://conforming-entrance.000webhostapp.com/elib/coverpic/"+rentedBooks.get(position).getImg())
                .into(imageView);

        dialog.show();
    }


    private void saveLocally(){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,new RentedFragment(context));

        FragmentUtils.showLoading(progressBar,recyclerView,textView);

        databaseHelper.deleteallRecords();

        Call<List<RentedBook>> call=apiServices.getRentedBook(storeData.getUser());

        call.enqueue(new Callback<List<RentedBook>>() {
            @Override
            public void onResponse(@NonNull Call<List<RentedBook>> call, @NonNull Response<List<RentedBook>> response) {
                if(response.isSuccessful()) {
                    FragmentUtils.showData(progressBar,recyclerView,textView);
                    List<RentedBook> rentedBooks=response.body();
                    assert rentedBooks != null;
                    for (RentedBook rentedBook:rentedBooks){
                        if(databaseHelper.getBookbyId(rentedBook.getIsbnno())){
                            databaseHelper.saveBook(rentedBook);
                        }
                    }

                    fragmentTransaction.commit();

                }
                else{
                    FragmentUtils.showError(progressBar,recyclerView,textView, getString(R.string.ServerError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RentedBook>> call, @NonNull Throwable t) {
                FragmentUtils.showError(progressBar,recyclerView,textView, getString(R.string.ServerError));
            }
        });
    }

    private void getRentedData() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        rentedBooks=databaseHelper.getAllRecords();
        Log.d("books", "getRentedData: "+rentedBooks.size());
        if(rentedBooks.size()==0){
            textView.setVisibility(TextView.VISIBLE);
        }
        progressBar.setVisibility(ProgressBar.GONE);
    }

    @Override
    public void onStop() {
        if(databaseHelper!=null)
            databaseHelper.close();
        super.onStop();
    }
}