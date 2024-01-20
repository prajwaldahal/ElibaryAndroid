package com.example.e_library;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "elibrary_db";
    private static final int DB_VERSION =4;
    private final Context context;
    private final APIServices apiServices;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context=context;
        apiServices=RetrofitBook.getRetrofitInstance();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_QUERY = "CREATE TABLE rented_book(isbnno VARCHAR(14) PRIMARY KEY, name TEXT, author TEXT, image TEXT,file TEXT,expiry_date TEXT,publisher TEXT,rented_date TEXT,page INTEGER,payment INTEGER)";
        db.execSQL(CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_TABLE_QUERY = "DROP TABLE IF EXISTS rented_book";
        db.execSQL(DROP_TABLE_QUERY);
        onCreate(db);
    }

    public void saveBook(@NonNull RentedBook book){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isbnno",book.getIsbnno());
        values.put("name",book.getName());
        values.put("author",book.getAuthor());
        values.put("image",book.getImg());
        values.put("file",book.getFile());
        values.put("expiry_date",book.getExpiryDate());
        values.put("publisher",book.getPublisher());
        values.put("rented_date",book.getRenteddate());
        values.put("page",0);
        values.put("payment",book.getPayment());
        Log.d("TAG", "saveBook: "+book);
        db.insert("rented_book", null, values);
        db.close();
    }


    public void deleteRecord(String isbnno){
        assert context != null;
        Call<DeleteApiResponse> call=apiServices.deleteRentedBook(getUsername(),isbnno);
        call.enqueue(new Callback<DeleteApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteApiResponse> call, @NonNull Response<DeleteApiResponse> response) {
                if(response.isSuccessful()){
                    DeleteApiResponse deleteApiResponse=response.body();
                    if (deleteApiResponse != null) {
                        Log.d("deleteresponse", "onResponse: "+deleteApiResponse.isDeletion());
                    }
                    assert deleteApiResponse != null;
                    if(deleteApiResponse.isDeletion()){
                        Toast.makeText(context, "your book has been expired", Toast.LENGTH_SHORT).show();
                        SQLiteDatabase db = DatabaseHelper.this.getWritableDatabase();
                        String whereClause = "isbnno=?";
                        String[] whereArgs = {String.valueOf(isbnno)};
                        db.delete("rented_book", whereClause, whereArgs);
                        db.close();
                    }
                    else{
                        Log.d("deleteresponse", "onResponse: "+response);
                    }
                }
                else{
                    Log.d("deleteresponse", "onResponse: "+response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteApiResponse> call, @NonNull Throwable t) {
                Log.d("failure", "onResponse: "+t.getMessage());
            }
        });
    }

    public void deleteallRecords(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("rented_book", null,null);
        db.close();
    }

    public ArrayList<RentedBook> getAllRecords(){
        ArrayList<RentedBook> rentedBookList = new ArrayList<>();

        SQLiteDatabase database = this.getReadableDatabase();
        Calendar todayCalender = Calendar.getInstance();
        Date todayDate = todayCalender.getTime();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedExpiryDate = sdf.format(todayDate.getTime());


        Cursor cursor = database.query("rented_book", null, null, null, null, null, null);



        if (cursor.moveToFirst()) {
            int isbnIndex = cursor.getColumnIndex("isbnno");
            int nameIndex = cursor.getColumnIndex("name");
            int authorIndex = cursor.getColumnIndex("author");
            int imageIndex=cursor.getColumnIndex("image");
            int fileIndex=cursor.getColumnIndex("file");
            int dateIndex=cursor.getColumnIndex("expiry_date");
            int publisherIndex=cursor.getColumnIndex("publisher");
            int rentedDateIndex= cursor.getColumnIndex("rented_date");
            int paymentIndex=cursor.getColumnIndex("payment");
            do {

                String isbnno= isbnIndex!=-1 ? cursor.getString(isbnIndex):null;
                String name =  nameIndex !=-1?cursor.getString(nameIndex):null ;
                String author = authorIndex != -1 ? cursor.getString(authorIndex) : null;
                String image = imageIndex != -1 ? cursor.getString(imageIndex) : null;
                String file = fileIndex != -1 ? cursor.getString(fileIndex) : null;
                String date=dateIndex != -1 ? cursor.getString(dateIndex):null;
                String publisher=publisherIndex!=-1?cursor.getString(publisherIndex):null;
                String rentedDate=rentedDateIndex!=-1?cursor.getString(rentedDateIndex):null;
                int payment=paymentIndex!=-1?cursor.getInt(paymentIndex):0;
                if (date != null && date.compareTo(formattedExpiryDate) < 0) {
                    deleteRecord(isbnno);
                    continue;
                }

                RentedBook rentedBook=new RentedBook(isbnno,name,author,date,image,file,publisher,rentedDate,payment);
                rentedBookList.add(rentedBook);

            }while(cursor.moveToNext());

            cursor.close();
        }

        database.close();
        return rentedBookList;
    }
    public boolean isBookAvailable(String isbnno){
        boolean presented;
        Log.d("isbnn", "getBookbyId: "+isbnno);
        SQLiteDatabase database = this.getReadableDatabase();
        String selection = "isbnno=?";
        String []selectionArgs={isbnno};
        @SuppressLint("Recycle") Cursor cursor = database.query("rented_book", null, selection, selectionArgs, null, null, null);
        presented= cursor.moveToFirst();
        database.close();
        return !presented;
    }

    public LastRead getLastReadBook(String isbnno) {
        String name ,author,image,file;
        SQLiteDatabase database = this.getReadableDatabase();
        String selection = "isbnno=?";
        String[] selectionArgs = {isbnno};
        @SuppressLint("Recycle") Cursor cursor = database.query("rented_book", null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {

            int nameIndex = cursor.getColumnIndex("name");
            int authorIndex = cursor.getColumnIndex("author");
            int imageIndex = cursor.getColumnIndex("image");
            int fileIndex= cursor.getColumnIndex("file");
            name = nameIndex != -1 ? cursor.getString(nameIndex) : null;
            author = authorIndex != -1 ? cursor.getString(authorIndex) : null;
            image = imageIndex != -1 ? cursor.getString(imageIndex) : null;
            file=fileIndex!=-1? cursor.getString(fileIndex) :null;
            return new LastRead(name, author, image,file);
        }
        return null;
    }

    public String getUsername(){
        StoreData storeData= new StoreData(context);
       return storeData.getUser();
    }

    public void setDefaultPage(int page,String isbnno){
        SQLiteDatabase database=getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("page",page);
        String whereClause = "isbnno=?";
        String[] whereArgs = {String.valueOf(isbnno)};
        database.update("rented_book",values,whereClause,whereArgs);
        database.close();
    }



    public int getDefaultPage(String isbnno) {
        SQLiteDatabase database = this.getReadableDatabase();
        String selection = "isbnno=?";
        String []selectionArgs={isbnno};
        Cursor cursor = database.query("rented_book", new String[]{"page"}, selection,selectionArgs, null, null, null, null);
        if(cursor.moveToFirst()){
            int pageIndex= cursor.getColumnIndex("page");
            return pageIndex!=-1 ? cursor.getInt(pageIndex):0;
        }
        cursor.close();
        return  0;
    }
}
