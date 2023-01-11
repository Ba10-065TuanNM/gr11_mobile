package com.technifysoft.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technifysoft.bookapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
//    View binding
    private ActivityLoginBinding binding ;

//    firebase auth
    private FirebaseAuth firebaseAuth ;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Plesase Wait !");
        progressDialog.setCanceledOnTouchOutside(false);


//        handle Click , go to register screen
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

//        handle click => begin login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
    private String email = "" , password = "";
    private void validateData() {
//        data validation

//                   get data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
//        validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email Pattern...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Enter your password...", Toast.LENGTH_SHORT).show();
        }
        else {
            loginUser();
        }

    }

    private void loginUser() {
//        show progress
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

//        login user
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
//                          login sucess => check user or admin
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking...");
//        check from realtime database
//                get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

//                  check in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        progressDialog.dismiss();
//                            get user type
                        String userType = "" +snapshot.child("userType").getValue();
//                        check user type
                        if (userType.equals("user")) {
//                            open userdashboard
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();

                        }
                        else if (userType.equals("admin")){
//                            open admin dasboard
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        };


                        }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });

    }
}