package com.example.e_library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class InternetConnection {

    public interface ConnectivityCallback {
        void onConnectivityChecked(boolean isConnected);
    }

    public static void checkConnectivityInBackground(Context context, ConnectivityCallback callback) {

        Thread thread = new Thread(() -> {
            boolean isConnected = isConnected(context);
            callback.onConnectivityChecked(isConnected);
        });

        thread.start();
    }

    private static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            try {
                URL url = new URL("https://www.google.com/");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000);
                urlc.connect();
                return urlc.getResponseCode() == 200;
            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }


}
