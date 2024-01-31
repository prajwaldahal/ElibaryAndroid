package com.example.e_library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ActionBarDrawerToggle toggle;

    int tapCount;
    long lastBackPressedTime;
    List<DrawerItem> drawerItems;
    DrawerLayout drawerLayout;
    RecyclerView navRecyclerView;
    TextView email, name;
    ImageView profile;

    View rootView;

    ActionBar actionBar;

    FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rootView = findViewById(R.id.fragment_container);
        drawerLayout = findViewById(R.id.drawer_layout);

        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        profile = findViewById(R.id.profile);

        drawerItems = new ArrayList<>();
        setDrawerItems();

        fragmentManager = getSupportFragmentManager();

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        actionBar = getSupportActionBar();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        InternetConnection.checkConnectivityInBackground(this, isConnected -> {
            if (!isConnected) {
                System.out.println("no internet conection");
                Snackbar.make(rootView, "no internet Connectivity", Snackbar.LENGTH_LONG).show();
            }
        });

        showAccountDetail();

        HomeFragment homeFragment = new HomeFragment(this);
        FragmentTransaction transaction = fragmentManager.beginTransaction().add(R.id.fragment_container, homeFragment);
        transaction.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        navRecyclerView = findViewById(R.id.nav_drawer);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        navRecyclerView.setLayoutManager(layoutManager);

        NavDrawerAdapter adapter = new NavDrawerAdapter(drawerItems, this);
        navRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            handleFragmentTransition(position);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

    }

    private void handleFragmentTransition(int position) {

        switch (position) {
            case 0:
                actionBar.setTitle("E-Library");
                replaceFragment(new HomeFragment(this), "home");
                break;

            case 1:
                actionBar.setTitle("Available Books");
                replaceFragment(new BookFragment(this), "books");
                break;

            case 2:
                actionBar.setTitle("Rented Books");
                replaceFragment(new RentedFragment(this), "rented");
                break;

            case 3:
                logOut();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
    }


    private void logOut() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        AlertDialog.Builder alertDialogBUilder = new AlertDialog.Builder(this);
        alertDialogBUilder.setMessage("are you sure?");

        alertDialogBUilder.setNegativeButton("no", (dialog, which) -> {

        });

        alertDialogBUilder.setPositiveButton("Logout", (dialog, which) -> googleSignInClient.signOut().addOnCompleteListener(task -> {
            try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
                databaseHelper.deleteallRecords();
            } catch (Exception e) {
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }));

        alertDialogBUilder.show();
    }

    private void setDrawerItems() {
        String[] item = {"Home", "Book List", "Rented Book", "Log Out"};
        int[] image = {R.drawable.baseline_home_24, R.drawable.baseline_menu_book_24, R.drawable.baseline_library_books_24, R.drawable.baseline_logout_24};

        for (int i = 0; i < item.length; i++) {
            DrawerItem drawerItem = new DrawerItem(image[i], item[i]);
            drawerItems.add(drawerItem);
        }
    }

    private void showAccountDetail() {
        Intent intent = getIntent();
        GoogleSignInAccount account = intent.getParcelableExtra("account");
        name.setText(account.getDisplayName());
        email.setText(account.getEmail());
        if (account.getPhotoUrl() == null) {
            Glide.with(this)
                    .load(R.drawable.user)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile);
        } else {
            Glide.with(this)
                    .load(account.getPhotoUrl().toString())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastBackPressedTime;
            Log.d("BACKPRESSED CHECK", "onBackPressed: Tapcount=" + tapCount + "\nTime diff=" + timeDiff);
            if (tapCount == 0 || timeDiff > 2000) {
                tapCount = 1;
                lastBackPressedTime = currentTime;
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            } else if (tapCount == 1 && timeDiff <= 2000) {
                super.onBackPressed();
            }
        }
    @Override
    protected void onRestart() {
        tapCount = 0;
        lastBackPressedTime=0;
        super.onRestart();
    }
}