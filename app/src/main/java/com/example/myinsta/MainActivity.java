package com.example.myinsta;

import com.example.myinsta.R;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.content.Context;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNav = findViewById(R.id.navmenu);


        loadFragment(new HomeFragment());
        bottomNav.setSelectedItemId(R.id.menuhomepage);

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int id=item.getItemId();

                if (id==R.id.menuhomepage) {
                    fragment=new HomeFragment();
                } else if (id==R.id.menuuploadmedia) {
                    fragment=new PostFragment();
                } else if (id==R.id.menuchat) {
                    fragment=new ChatFragment();
                } else if (id==R.id.menuprofile) {
                    fragment=new ProfileFragment();
                } else if (id==R.id.menusearch) {
                    fragment=new SearchFragment();
                }

                return loadFragment(fragment);
            }
        });


        String icon = getIntent().getStringExtra("openicon");
        String fragment = getIntent().getStringExtra("openfragment");
        if ("search".equals(icon)) {
            navigateToSearch();
        }
        if ("chats".equals(icon)) {
            navigatetochat();
        }
        if ("childfragment".equals(fragment)) {
            String childuid = getIntent().getStringExtra("childuid");
            getSupportFragmentManager().beginTransaction().replace(R.id.framecontainer, new childprofileFragment(childuid)).addToBackStack(null).commit();
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("chat_channel", "Chat Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).update("fcmToken", token);
            }});
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.framecontainer, fragment).commitAllowingStateLoss();
            return true;
        }
        return false;
    }

    public void navigateToSearch() {
        BottomNavigationView bottomNav=findViewById(R.id.navmenu);
        bottomNav.setSelectedItemId(R.id.menusearch);
    }

    public void navigatetoprofile() {
        BottomNavigationView bottomnav=findViewById(R.id.navmenu);
        bottomnav.setSelectedItemId(R.id.menuprofile);
    }

    public void navigatetochat() {
        BottomNavigationView bottomnav=findViewById(R.id.navmenu);
        bottomnav.setSelectedItemId(R.id.menuchat);
    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            String icon = getIntent().getStringExtra("openicon");
            if ("chats".equals(icon)) {
                navigatetochat();
            }
        } else {
            super.onBackPressed();
        }
    }




}
