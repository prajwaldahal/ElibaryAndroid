package com.example.e_library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.File;

public class ClearDataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_DATA_CLEARED.equals(intent.getAction())) {
            deleteFiles(context.getFilesDir());
        }
    }

    private void deleteFiles(File directory) {
        if (directory != null && directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.delete()) {
                        Log.d("ClearDataReceiver", "File deleted: " + file.getAbsolutePath());
                    } else {
                        Log.e("ClearDataReceiver", "Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }
}

