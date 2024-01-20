package com.example.e_library;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    private boolean focus;
    private Dialog dialog;

    private KhaltiButton khaltiButton;

    private DatabaseHelper databaseHelper;
    private ProgressBar progressBar;
    private ArrayList<Book> books;
    private final ArrayList<Book> tempBooks;
    private BookAdapter bookAdapter;

    private TextView textView;

    private SortBy sortBy;

    private Spinner spinner;

    private StoreData storeData;

    private final APIServices apiServices;

    private RecyclerView recyclerView;

    private FloatingActionButton floatingActionButton;

    public BookFragment(Context context) {
        this.focus=false;
        this.context = context;
        this.books = new ArrayList<>();
        this.tempBooks=new ArrayList<>();
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


        floatingActionButton = view.findViewById(R.id.refresh_book);

        floatingActionButton.setOnClickListener(v -> getBooks());

        spinner=view.findViewById(R.id.sort);

        getBooks();


        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean canSort;
                if (books != null) {
                    String selectedValue = (String) parent.getSelectedItem();
                    Log.d("sorted value", "onItemSelected: "+selectedValue);
                    switch (selectedValue) {
                        case "by Name": {
                            sortBy = SortBy.NAME;
                            canSort=true;
                            Log.i("bookfragment", "onItemSelected: name");
                            break;
                        }
                        case "by Price": {
                            canSort=true;
                            sortBy = SortBy.PRICE;
                            break;
                        }
                        case "by ISBN no": {
                            canSort=true;
                            sortBy = SortBy.ISBNNO;
                            break;
                        }
                        default:{
                            canSort=false;
                        }
                    }
                    if(canSort){
                        Sort<Book>sort = new Sort<>(sortBy, books);
                        books = (ArrayList<Book>) sort.mergeSort();
                        bookAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        EditText searchBar = view.findViewById(R.id.search_book);


        bookAdapter=new BookAdapter(context,books);
        recyclerView.setAdapter(bookAdapter);

        searchBar.setOnFocusChangeListener((v, hasFocus) -> focus=hasFocus);

        searchBar.setOnClickListener(v -> {
            if(focus) {
                searchBar.clearFocus();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               searchBook(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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

    @SuppressLint("NotifyDataSetChanged")
    private void searchBook(String searchText) {
         if(!searchText.isEmpty()){
             books.clear();
             books.addAll(tempBooks);
             books= (ArrayList<Book>) Search.search(books,searchText);
         }
         else{
             if(tempBooks.size()!=books.size()) {
                 books.clear();
                 books.addAll(tempBooks);
             }
         }
        bookAdapter.notifyDataSetChanged();
    }

    @SuppressLint("DefaultLocale")
    private void implementKhaltiButton(TextView price, AtomicLong buy, long daysDiff, Book book, String formattedExpiryDate) {
        khaltiButton.setVisibility(KhaltiButton.VISIBLE);
        buy.set(daysDiff * Long.parseLong(book.getRent())/30);
        price.setText(String.format("Price to pay:Rs%d", buy.get()));
        price.setTextColor(Color.GREEN);
        Config.Builder builder = new Config.Builder(com.example.e_library.Config.getKhaltiTestId(), book.getIsbnno(), book.getName(), buy.get()*100, new OnCheckOutListener() {
            @Override
            public void onError(@NonNull String action, @NonNull Map<String, String> errorMap) {
                Log.d("ERROR KHALTI", "onError: "+ errorMap);
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

        RentBookDataSent rentBookDataSent=new RentBookDataSent(storeData.getUser(), (String) data.get("product_identity"),formattedExpiryDate, (Integer) data.get("amount")/100);
        data.clear();
        Log.e("ERROR", "SaveInRemoteServer: "+rentBookDataSent);
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
                            Log.e("error", "onResponse: ERROR OCCURED" );
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
                            if (databaseHelper.isBookAvailable(rentedBook.getIsbnno())) {
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
                Toast.makeText(context,"Unable to rent Text",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });
    }

    private void showDialog(int position) {
        TextView name,publisher,isbnno,rent,author;
        ImageView imageView;
        Dialog dialog=new Dialog(context);
        dialog.setContentView(R.layout.book_dialog_layout);

        name=dialog.findViewById(R.id.book_dialog_name);
        publisher=dialog.findViewById(R.id.dialog_book_publisher);
        isbnno=dialog.findViewById(R.id.dialog_book_id);
        rent=dialog.findViewById(R.id.dialog_book_rent);
        author=dialog.findViewById(R.id.dialog_book_author);
        imageView=dialog.findViewById(R.id.dialog_cover_image);

        name.setText(books.get(position).getName());
        publisher.setText(books.get(position).getPublisher());
        name.setText(books.get(position).getName());
        isbnno.setText(books.get(position).getIsbnno());
        rent.setText(String.format("Rs.%s per month", books.get(position).getRent()));
        author.setText(books.get(position).getAuthor());
        Glide.with(context)
                .load(com.example.e_library.Config.getMyserverPicUrl() +books.get(position).getImg())
                .into(imageView);

        dialog.show();
    }

    private void getBooks() {
        textView.setVisibility(TextView.GONE);
        floatingActionButton.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        if(recyclerView!=null)
            recyclerView.setVisibility(RecyclerView.GONE);
        spinner.setEnabled(false);


        Call<List<Book>> call = apiServices.getAllBook(storeData.getUser());
        call.enqueue(new Callback<List<Book>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                progressBar.setVisibility(ProgressBar.GONE);
                recyclerView.setVisibility(RecyclerView.VISIBLE);
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
                        tempBooks.addAll(books);
                        bookAdapter.notifyDataSetChanged();
                    } else {
                        FragmentUtils.showError(progressBar,recyclerView,textView, getString(R.string.ServerError));
                    }
                    floatingActionButton.setEnabled(true);
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