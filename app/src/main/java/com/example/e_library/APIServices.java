package com.example.e_library;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIServices {
    @GET("{username}/all")
    Call<List<Book>> getAllBook(@Path("username") String username);

    @POST("rented")
    Call<PostApiResponse> insertRentedBook(@Body RentBookDataSent rentBookDataSent);
    @POST("user")
    Call<PostApiResponse> inserUserData(@Body User user);

    @GET("{username}")
    Call<List<RentedBook>> getRentedBook(@Path("username") String username);

    @GET("{username}/{isbnno}/deleted")
    Call<DeleteApiResponse> deleteRentedBook(
            @Path("username") String username,
            @Path("isbnno") String isbnno
    );


}
