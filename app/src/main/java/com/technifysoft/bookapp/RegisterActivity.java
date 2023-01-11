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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.technifysoft.bookapp.databinding.ActivityRegisterBinding;
import com.technifysoft.bookapp.ui.main.RegisterFragment;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
//    View binding
    private ActivityRegisterBinding binding ;
// Firebase Auth
    private FirebaseAuth firebaseAuth;
//    progress dialog
    private ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

//        set up progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Plesase Wait !");
        progressDialog.setCanceledOnTouchOutside(false);


//        handle Click , go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });
//        handle Click, begin register
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateData();
            }
        });

    }
    private String name = "", email = "" ,password = "" ;
    private void validateData() {

//        get data
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
         String cPassword = binding.cPasswordEt.getText().toString().trim();
//       Validate Data
        if(TextUtils.isEmpty(name)) {

            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email Pattern...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Enter your password...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cPassword)) {

            Toast.makeText(this, "Confirm Password !!", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(cPassword)){
            Toast.makeText(this, "Password does not match !!", Toast.LENGTH_SHORT).show();
        }
        else {
            createUserAccount();
        }
    }

    private void createUserAccount() {
                        // show progress dialog
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
                        //Create user in firebase auth

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //  account creation success, => add in firebase realtime databse
                        updateUserInfo();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
//                        creating account failed
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving User Info...");

//        timestamp
        long timestamp = System.currentTimeMillis();
//        Get user uid
        String uid = firebaseAuth.getUid();

//  setup data to add in db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("profileImage","");
        hashMap.put("userType","user");  //admin or user
        hashMap.put("timestamp",timestamp);

//        set data to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
//                        data added to db
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

}