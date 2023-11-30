package com.example.e_library;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PdfActivity extends AppCompatActivity {
    PDFView pdfView;
    ProgressBar progressBar;
    String isbnno;

    DatabaseHelper databaseHelper;
    int defaultPage=1,totalPage=0;

    StoreData storeData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent intent=getIntent();
        String pdfUrl=intent.getStringExtra("url");
        isbnno=intent.getStringExtra("isbn");
        Log.d("uri", "onCreate: "+pdfUrl);

        pdfView=findViewById(R.id.pdfView);
        progressBar=findViewById(R.id.progress_bar);

        storeData= new StoreData(this);

        downloadAndOpenPdf(pdfUrl);

    }

    @Override
    protected void onStart() {
        databaseHelper= new DatabaseHelper(this);
        super.onStart();
    }

    private void downloadAndOpenPdf(String pdfUrl) {
        pdfView.setVisibility(PDFView.GONE);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        String fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1);


        File internalStorageDir = getFilesDir();
        File pdfFile = new File(internalStorageDir, fileName);

        if (pdfFile.exists()) {
            openPdfWithIntent(pdfFile);
        } else {
            Thread downloadThread = new Thread(() -> {
                try {
                    URL url = new URL(pdfUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    FileOutputStream outputStream = new FileOutputStream(pdfFile);
                    InputStream input = new BufferedInputStream(connection.getInputStream());

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    input.close();
                    outputStream.close();

                    openPdfWithIntent(pdfFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            downloadThread.start();
        }
    }

    private void openPdfWithIntent(File pdfFile) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        mainHandler.post(() -> {
            Uri uri = FileProvider.getUriForFile(this, "com.example.e_library.fileprovider", pdfFile);
            progressBar.setVisibility(ProgressBar.GONE);
            pdfView.setVisibility(PDFView.VISIBLE);
            storeData.addLastReadedBook(isbnno);
            pdfView.fromUri(uri)
                    .defaultPage(getDefaultPage())
                    .onPageChange((page, pageCount) -> defaultPage=page)
                    .enableDoubletap(true)
                    .onLoad(nbPages -> totalPage=pdfView.getPageCount())
                    .autoSpacing(true)
                    .enableSwipe(true)
                    .enableAntialiasing(true)
                    .load();


        });
    }

    private int getDefaultPage() {
        return databaseHelper.getDefaultPage(isbnno);
    }

    @Override
    protected void onPause() {
        storeData.addProgres(totalPage,defaultPage+1);
        super.onPause();
    }

    @Override
    protected void onStop() {
        databaseHelper.setDefaultPage(defaultPage,isbnno);
        databaseHelper.close();
        super.onStop();
    }
}