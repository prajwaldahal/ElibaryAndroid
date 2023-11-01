package com.example.e_library;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.khalti.utils.Store;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private GoogleSignInClient googleSignInClient;
    private ProgressBar progressBar;
    private TextView textView;

    ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        Intent data = result.getData();

        if (resultCode == RESULT_OK && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else if(data==null){
            Toast.makeText(this, "something went wrong! please try again", Toast.LENGTH_SHORT).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.loadingProgressBar);
        textView=findViewById(R.id.signing_in);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient= GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null)
        {
            loggedInSucces(account);
        }

        SignInButton signInButton = findViewById(R.id.google_button);

        signInButton.setOnClickListener(v -> signInButtonActionPerformed());
    }

    private void updateUI(GoogleSignInAccount account) {
        if(account!=null){
            progressBar.setVisibility(ProgressBar.VISIBLE);
            textView.setVisibility(TextView.VISIBLE);
            APIServices apiServices=RetrofitBook.getRetrofitInstance();
            Call<PostApiResponse> call=apiServices.inserUserData(new User(account.getId(),account.getDisplayName()));
            call.enqueue(new Callback<PostApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<PostApiResponse> call, @NonNull Response<PostApiResponse> response) {
                    if(response.isSuccessful()){
                        PostApiResponse postApiResponse=response.body();
                        if (postApiResponse != null) {
                            if(postApiResponse.
                                    isInsertion()){
                                progressBar.setVisibility(ProgressBar.GONE);
                                textView.setVisibility(TextView.GONE);
                                loggedInSucces(account);
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "Login Unsuccesful", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(ProgressBar.GONE);
                                textView.setVisibility(TextView.GONE);
                            }
                        }
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Login Unsuccesful", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PostApiResponse> call, @NonNull Throwable t) {
                    Log.e("failed", "onResponse: " + t.getMessage());
                    Toast.makeText(LoginActivity.this, "Login Unsuccesful", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void loggedInSucces(GoogleSignInAccount account) {
        StoreData storeData= new StoreData(this);
        storeData.addUser(account.getId());
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("id", "updateUI: "+account.getId());
        editor.putString("username",account.getId());
        editor.apply();

        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra("account",account);
        startActivity(intent);
        finish();

    }

    private void signInButtonActionPerformed() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Toast.makeText(this, R.string.try_again, Toast.LENGTH_SHORT).show();
        }
    }
}