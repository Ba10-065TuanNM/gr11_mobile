 package com.technifysoft.bookapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technifysoft.bookapp.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class SplashActivity extends AppCompatActivity {
    //    firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        init firebase
        firebaseAuth = FirebaseAuth.getInstance();

//    start main screen after 2s
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        }, 2000);

    }

    private void checkUser() {
//        get current user, if logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
//            user not logged in
//            start main screen
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
//                  check in db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange( DataSnapshot snapshot) {

//                            get user type
                            String userType = "" +snapshot.child("userType").getValue();
//                        check user type
                            if (userType.equals("user")) {
//                            open userdashboard
                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                finish();

                            }
                            else if (userType.equals("admin")){
//                            open admin dasboard
                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                finish();
                            };


                        }

                        @Override
                        public void onCancelled( DatabaseError error) {

                        }
                    });
        }
    }
}