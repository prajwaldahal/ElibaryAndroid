package com.example.e_library;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.khalti.checkout.helper.OnCheckOutListener;
import com.khalti.checkout.helper.PaymentPreference;
import com.khalti.widget.KhaltiButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private ProgressBar readingProgressBar,imageProgressBar,progressBar;
     private final Context context;
    private DatabaseHelper databaseHelper;

    private TextView name,author,nobooks,progressText;

    private ImageView cover;


   private final StoreData storeData;

   private RecyclerView recyclerView;

   private final APIServices apiServices;

   private final ArrayList<RecentBook> recentBookList;


   private View clickAbleArea ;

   private Dialog dialog;

   private KhaltiButton khaltiButton;

   private RecentAdapter recentAdapter;
    public HomeFragment(Context context) {
        this.context = context;
        storeData= new StoreData(context);
        apiServices=RetrofitBook.getRetrofitInstance();
        recentBookList=new ArrayList<>();
    }

    @Override
    public void onStart() {
        Log.d("STARTED", "onStart: start");
        if(databaseHelper==null)
            databaseHelper=new DatabaseHelper(context);
        updateLastRead();
        super.onStart();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_home, container, false);
        databaseHelper=new DatabaseHelper(context);

        readingProgressBar = view.findViewById(R.id.readingProgressBar);

        name=view.findViewById(R.id.book_name);
        author=view.findViewById(R.id.book_author);
        cover=view.findViewById(R.id.last_read_cover_image);
        nobooks=view.findViewById(R.id.nobook);

        progressText=view.findViewById(R.id.progresstext);

        progressBar=view.findViewById(R.id.progressBar);

        imageProgressBar=view.findViewById(R.id.progress_bar);

        clickAbleArea  =view.findViewById(R.id.clickableArea);

        recyclerView =view.findViewById(R.id.recent_added);
        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);

        getRecent();


        recentAdapter= new RecentAdapter(context,recentBookList);
        recentAdapter.setOnItemClickListener(position -> {
            if(!databaseHelper.isBookAvailable(recentBookList.get(position).getIsbnno())){
                Toast.makeText(context, "you have already rented book", Toast.LENGTH_SHORT).show();
            }
            else{
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
                            implementKhaltiButton(price, buy, daysDiff, recentBookList.get(position), formattedExpiryDate);
                        }
                    });
                }
                dialog.show();
                dialog.setCancelable(true);
            }
        });
            recyclerView.setAdapter(recentAdapter);


            return view;

    }

    @SuppressLint("DefaultLocale")
    private void implementKhaltiButton(TextView price, AtomicLong buy, long daysDiff, RecentBook recentBook, String formattedExpiryDate) {
        khaltiButton.setVisibility(KhaltiButton.VISIBLE);
        buy.set(daysDiff * Long.parseLong(recentBook.getRent())/30);
        price.setText(String.format("Price to pay:Rs%d", buy.get()));
        price.setTextColor(Color.GREEN);
        com.khalti.checkout.helper.Config.Builder builder = new com.khalti.checkout.helper.Config.Builder(com.example.e_library.Config.getKhaltiTestId(), recentBook.getIsbnno(), recentBook.getName(), buy.get()*100, new OnCheckOutListener() {
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

        com.khalti.checkout.helper.Config config = builder.build();

        khaltiButton.setCheckOutConfig(config);
    }

    private  void SaveInRemoteServer(Map<String, Object> data, String formattedExpiryDate) {

        FragmentUtils.showLoading(context);

        RentBookDataSent rentBookDataSent=new RentBookDataSent(storeData.getUser(), (String) data.get("product_identity"),formattedExpiryDate, (Integer) data.get("amount")/100);
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
                           FragmentUtils.closeLoading();
                            Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostApiResponse> call, @NonNull Throwable t) {
                FragmentUtils.closeLoading();
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(context, " something went wrong ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLocally(){
        Call<List<RentedBook>> call=apiServices.getRentedBook(storeData.getUser());
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
                    FragmentUtils.closeLoading();
                    FragmentUtils.replaceFragment(getParentFragmentManager(),new RentedFragment(context),R.id.fragment_container);
                }
                else {
                    FragmentUtils.closeLoading();
                    Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RentedBook>> call, @NonNull Throwable t) {
                FragmentUtils.closeLoading();
                Toast.makeText(context,"ERROR OCCURED",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });
    }


    private void getRecent() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        Call<List<RecentBook>> call = apiServices.getRecent();
        call.enqueue(new Callback<List<RecentBook>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<RecentBook>> call, @NonNull Response<List<RecentBook>> response) {
                recyclerView.setVisibility(RecyclerView.VISIBLE);
                try{
                    if (response.isSuccessful()) {
                        progressBar.setVisibility(ProgressBar.GONE);
                        List<RecentBook> fetchedBooks = response.body();
                        recentBookList.clear();
                        if (fetchedBooks != null) {
                            recentBookList.addAll(fetchedBooks);
                        }
                        if(recentAdapter!=null)
                            recentAdapter.notifyDataSetChanged();
                    }
                }catch (NullPointerException e){
                    Log.d("TAG", "onResponse: "+e.getMessage());
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<RecentBook>> call, @NonNull Throwable t) {
            }
        });
    }

    private void updateLastRead() {

       String isbnno=storeData.getLastRead();
        Log.d("UPDATE", "updateLastRead: "+isbnno);
       if(isbnno !=null) {


           LastRead lastRead=databaseHelper.getLastReadBook(isbnno);
           clickAbleArea .setOnClickListener(v -> {
               Intent intent= new Intent(context,PdfActivity.class);
               intent.putExtra("isbn",isbnno);
               intent.putExtra("url", Config.getMyserverFileUrl() +lastRead.getFile());
               startActivity(intent);
           });
           if(lastRead!=null){
               name.setText(lastRead.getName());
               author.setText(lastRead.getAuthor());
               Glide.with(context)
                       .load( Config.getMyserverPicUrl()+lastRead.getImage().trim())
                       .addListener(new RequestListener<Drawable>() {
                           @Override
                           public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                               return false;
                           }

                           @Override
                           public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                               imageProgressBar.setVisibility(ProgressBar.GONE);
                               return false;
                           }
                       })

                       .into(cover);
               updateReadingProgress();
           }
           else{
               clickAbleArea.setVisibility(View.GONE);
               nobooks.setVisibility(TextView.VISIBLE);
               readingProgressBar.setVisibility(ProgressBar.GONE);
               imageProgressBar.setVisibility(ProgressBar.GONE);
           }
       }

    }
    private void updateReadingProgress() {
       int progress=storeData.getProgress();
       readingProgressBar.setProgress(progress);
       progressText.setText(String.format(Locale.getDefault(),"%d%%", progress));
    }

    public void onStop() {
        if(databaseHelper!=null)
            databaseHelper.close();
        super.onStop();
    }
}