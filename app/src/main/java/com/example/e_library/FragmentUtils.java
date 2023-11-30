package com.example.e_library;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentUtils {

    private static Dialog dialog;
    public static void replaceFragment(FragmentManager fragmentManager, Fragment fragment, int containerId) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerId, fragment);
        fragmentTransaction.commit();
    }

    public static void showLoading(ProgressBar progressBar, RecyclerView recyclerView, TextView textView) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
    }

    public static void showData(ProgressBar progressBar, RecyclerView recyclerView, TextView textView) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
    }

    public static void showError(ProgressBar progressBar, RecyclerView recyclerView, TextView textView, String errorMessage) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        textView.setText(errorMessage);
    }

    public static void showLoading(Context context) {
        dialog= new Dialog(context);
        dialog.setContentView(R.layout.progress_dialog_show);
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void closeLoading(){
        dialog.dismiss();
    }
}
