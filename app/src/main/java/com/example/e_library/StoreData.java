package com.example.e_library;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class StoreData {
     Context context;
    SharedPreferences sharedPreferences;
    public StoreData(Context context){
        this.context=context;
        sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
    }

    public void addUser(String id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username",id);
        editor.apply();
    }

    public void addProgres(int totalPage,int currentPage){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        System.out.println("current "+currentPage+" total"+totalPage);
        int progress= (int) ( currentPage/(float)totalPage*100);
        Log.d("PROGRESS", "addProgres: "+progress);
        editor.putInt("progress",progress);
        editor.apply();
    }

    public int getProgress()
    {
        int Progress=sharedPreferences.getInt("progress",0);
        System.out.println(Progress);
        return Progress;
    }

    public void addLastReadedBook(String isbnno){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("isbnno",isbnno);
        editor.apply();
    }

    public String getLastRead(){
        return sharedPreferences.getString("isbnno","");
    }

    public String getUser(){
        return sharedPreferences.getString("username","");
    }
}
