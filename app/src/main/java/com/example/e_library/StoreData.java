package com.example.e_library;

import android.content.Context;
import android.content.SharedPreferences;

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

    public String getUser(){
        return sharedPreferences.getString("username","");
    }
}
