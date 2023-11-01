package com.example.e_library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.Objects;

public class AppUninstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();

        if (context.getPackageName().equals(packageName)) {
            File filesDir = context.getFilesDir();
            deleteRecursive(filesDir);
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                Log.d("deleted file", "deleteRecursive: " + child.getName());
                deleteRecursive(child);
                fileOrDirectory.delete();
            }
        }
    }
}

