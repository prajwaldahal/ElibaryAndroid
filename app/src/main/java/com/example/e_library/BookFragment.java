package com.example.e_library;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.khalti.checkout.helper.Config;
import com.khalti.checkout.helper.OnCheckOutListener;
import com.khalti.checkout.helper.PaymentPreference;
import com.khalti.widget.KhaltiButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookFragment extends Fragment {
    private final Context context;

    private Dialog dialog;

    private KhaltiButton khaltiButton;

    private DatabaseHelper databaseHelper;
    private ProgressBar progressBar;
    private List<Book> books;
    private BookAdapter bookAdapter;

    private TextView textView;

    private SortBy sortBy;

    private Spinner spinner;

    private StoreData storeData;

    private final APIServices apiServices;

    private RecyclerView recyclerView;

    public BookFragment(Context context) {

        this.context = context;
        this.books = new ArrayList<>();
        apiServices= RetrofitBook.getRetrofitInstance();

    }

    @Override
    public void onStart() {
        if(databaseHelper==null)
            databaseHelper=new DatabaseHelper(context);

        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_book, container, false);
        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView = view.findViewById(R.id.book_list);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);


        textView=view.findViewById(R.id.error_text);

        storeData=new StoreData(context);


        FloatingActionButton floatingActionButton = view.findViewById(R.id.refresh_book);

        floatingActionButton.setOnClickListener(v -> recreateFragment());

        spinner=view.findViewById(R.id.sort);

        getBooks();

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (books != null) {
                    String selectedValue = (String) parent.getSelectedItem();
                    Log.d("selected", "onItemSelected: "+selectedValue);
                    switch (selectedValue) {
                        case "by Name": {
                            sortBy = SortBy.NAME;
                            Log.i("bookfragment", "onItemSelected: name");
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
                    }

                    Sort<Book>sort = new Sort<>(sortBy, books);
                    books = sort.mergeSort();
                    bookAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        bookAdapter=new BookAdapter(context,books);
        recyclerView.setAdapter(bookAdapter);

        bookAdapter.setOnItemClickListener(this::showDialog);


        bookAdapter.setOnRentBookClickedListener((position) -> {
            dialog= new Dialog(context);
            dialog.setContentView(R.layout.buy_dialog_layout);


            DatePicker datePicker=dialog.findViewById(R.id.book_date_picker);
            TextView price=dialog.findViewById(R.id.price);
            khaltiButton=dialog.findViewById(R.id.khalti_button);
            khaltiButton.setVisibility(KhaltiButton.GONE);

            AtomicLong buy = new AtomicLong(0);



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                datePicker.setOnDateChangedListener((view1, year, monthOfYear, dayOfMonth) -> {
                    Calendar expirydate = Calendar.getInstance();
                    expirydate.set(year, monthOfYear, dayOfMonth);
                    Date expiryDate = expirydate.getTime();
                    Calendar todayCalender = Calendar.getInstance();
                    Date todayDate = todayCalender.getTime();

                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedExpiryDate = sdf.format(expirydate.getTime());

                    long differenceInMillis = expiryDate.getTime() - todayDate.getTime();

                    long daysDiff = TimeUnit.DAYS.convert(differenceInMillis, TimeUnit.MILLISECONDS);

                    if(daysDiff<2){
                        price.setText(R.string.invalid);
                        price.setTextColor(Color.RED);
                        price.setTextSize(20);
                        khaltiButton.setVisibility(KhaltiButton.GONE);
                    }
                    else{
                        implementKhaltiButton(price, buy, daysDiff, books.get(position), formattedExpiryDate);
                    }
                });
            }
            dialog.show();
            dialog.setCancelable(true);
        });


        return  view;

    }


    private void recreateFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,new BookFragment(context));
        fragmentTransaction.commit();
    }

    @SuppressLint("DefaultLocale")
    private void implementKhaltiButton(TextView price, AtomicLong buy, long daysDiff, Book book, String formattedExpiryDate) {
        khaltiButton.setVisibility(KhaltiButton.VISIBLE);
        buy.set(daysDiff * Long.parseLong(book.getRent())/30);
        price.setText(String.format("Price to pay:Rs%d", buy.get()));
        price.setTextColor(Color.GREEN);
        Config.Builder builder = new Config.Builder("test_public_key_863f0fdcc17a4ad78ede29f917e18802", book.getIsbnno(), book.getName(), buy.get()*100, new OnCheckOutListener() {
            @Override
            public void onError(@NonNull String action, @NonNull Map<String, String> errorMap) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(@NonNull Map<String, Object> data) {
                dialog.dismiss();
                SaveInRemoteServer(data,formattedExpiryDate);
            }
        })
                .paymentPreferences(new ArrayList<PaymentPreference>() {{
                    add(PaymentPreference.KHALTI);
                    add(PaymentPreference.EBANKING);
                    add(PaymentPreference.MOBILE_BANKING);
                    add(PaymentPreference.CONNECT_IPS);
                    add(PaymentPreference.SCT);
                }});

        Config config = builder.build();

        khaltiButton.setCheckOutConfig(config);
    }

    private  void SaveInRemoteServer(Map<String, Object> data, String formattedExpiryDate) {

        progressBar.setVisibility(ProgressBar.VISIBLE);

        RentBookDataSent rentBookDataSent=new RentBookDataSent(storeData.getUser(), (String) data.get("product_identity"),formattedExpiryDate);
        data.clear();
        Call<PostApiResponse> call=apiServices.insertRentedBook(rentBookDataSent);

        call.enqueue(new Callback<PostApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<PostApiResponse> call, @NonNull Response<PostApiResponse> response) {
                if(response.isSuccessful())
                {

                    PostApiResponse postApiResponse=response.body();
                    if (postApiResponse != null) {
                        if(postApiResponse.isInsertion()){
                            Toast.makeText(context,"Book is rented Successfully", Toast.LENGTH_SHORT).show();
                            saveLocally();
                        }
                        else {
                            progressBar.setVisibility(ProgressBar.GONE);
                            Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostApiResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(context, " something went wrong ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocally(){
        Call<List<RentedBook>> call=apiServices.getRentedBook(storeData.getUser());
        FragmentUtils.showLoading(progressBar,recyclerView,textView);
        call.enqueue(new Callback<List<RentedBook>>() {
            @Override
            public void onResponse(@NonNull Call<List<RentedBook>> call, @NonNull Response<List<RentedBook>> response) {
                if(response.isSuccessful()) {
                    List<RentedBook> rentedBooks=response.body();
                    if (rentedBooks != null) {
                        for (RentedBook rentedBook:rentedBooks) {
                            if (databaseHelper.getBookbyId(rentedBook.getIsbnno())) {
                                databaseHelper.saveBook(rentedBook);
                            }
                        }
                    }
                    ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
                    if(actionBar!=null)
                        actionBar.setTitle(R.string.rented_books);
                    FragmentUtils.replaceFragment(getParentFragmentManager(),new RentedFragment(context),R.id.fragment_container);
                }
                else {
                    FragmentUtils.showError(progressBar,recyclerView,textView, getString(R.string.ServerError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RentedBook>> call, @NonNull Throwable t) {
                Toast.makeText(context,"Unable to rent Text",Toast.LENGTH_SHORT);
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });
    }

    private void showDialog(int position) {
        TextView name,publisher,isbnno,rent,author;
        ImageView imageView;
        ProgressBar imageProgressBar ;
        Dialog dialog=new Dialog(context);
        dialog.setContentView(R.layout.book_dialog_layout);

        name=dialog.findViewById(R.id.book_dialog_name);
        publisher=dialog.findViewById(R.id.dialog_book_publisher);
        isbnno=dialog.findViewById(R.id.dialog_book_id);
        rent=dialog.findViewById(R.id.dialog_book_rent);
        author=dialog.findViewById(R.id.dialog_book_author);
        imageView=dialog.findViewById(R.id.dialog_cover_image);

//        imageProgressBar=dialog.findViewById(R.id.image_progress_bar);

        name.setText(books.get(position).getName());
        publisher.setText(books.get(position).getPublisher());
        name.setText(books.get(position).getName());
        isbnno.setText(books.get(position).getIsbnno());
        rent.setText(String.format("Rs.%s per month", books.get(position).getRent()));
        author.setText(books.get(position).getAuthor());
        Glide.with(context)
                .load("https://conforming-entrance.000webhostapp.com/elib/coverpic/"+books.get(position).getImg())
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        //imageProgressBar.setVisibility(ProgressBar.GONE);
                        return false;
                    }
                })
                .into(imageView);

        dialog.show();
    }

    private void getBooks() {

        progressBar.setVisibility(ProgressBar.VISIBLE);

        spinner.setEnabled(false);

        Call<List<Book>> call = apiServices.getAllBook(storeData.getUser());



        call.enqueue(new Callback<List<Book>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                progressBar.setVisibility(ProgressBar.GONE);
                try{
                    if (response.isSuccessful()) {
                        List<Book> fetchedBooks = response.body();
                        books.clear();
                        if (fetchedBooks != null) {
                            books.addAll(fetchedBooks);
                        }
                        if(books.size()==0)
                        {
                            textView.setVisibility(TextView.VISIBLE);
                        }
                        else{
                            spinner.setEnabled(true);
                        }
                        bookAdapter.notifyDataSetChanged();
                    } else {
                        FragmentUtils.showError(progressBar,recyclerView,textView, getString(R.string.ServerError));
                    }
                }catch (NullPointerException e){
                    Log.d("TAG", "onResponse: "+e.getMessage());
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                FragmentUtils.showError(progressBar,recyclerView,textView, getString(R.string.ServerError));
            }
        });
    }


    @Override
    public void onStop() {
        if(databaseHelper!=null)
            databaseHelper.close();
        super.onStop();
    }
}