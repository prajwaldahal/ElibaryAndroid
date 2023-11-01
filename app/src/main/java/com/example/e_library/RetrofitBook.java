package com.example.e_library;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBook {
    public static APIServices getRetrofitInstance() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://conforming-entrance.000webhostapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(APIServices.class);
    }
}
