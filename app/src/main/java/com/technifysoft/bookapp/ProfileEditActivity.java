package com.technifysoft.bookapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.technifysoft.bookapp.databinding.ActivityPdfEditBinding;
import com.technifysoft.bookapp.databinding.ActivityProfileEditBinding;

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {
    // view binding
    private ActivityProfileEditBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private static final String TAG = "PROFILE_EDIT_TAG";

    private Uri imageUri = null ;

    private String name = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

         //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        //handle click , goback
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click, pick image
        binding.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageAttachMenu();
            }
        });

        //handle clicl, update profile
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private void validateData() {
        //get data
        name = binding.nameEt.getText().toString().trim();

        //validate data
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please Enter Name...", Toast.LENGTH_SHORT).show();
        }
        else{
            if(imageUri == null ){
                updateProfile("");
            }
            else {
                uploadImage();

            }
        }

    }

    private void uploadImage() {
        Log.d(TAG, "uploadImage: Uploading profile image...");
        progressDialog.setMessage("Updating profile image");
        progressDialog.show();

        String filePathAndName = "ProfileImage/"+firebaseAuth.getUid();

        //storage reference
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Profile image uploaded");
                        Log.d(TAG, "onSuccess: Getting url of uploaded image");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl =""+uriTask.getResult();

                        Log.d(TAG, "onSuccess: Uploaded Imgae Url"+uploadedImageUrl);

                        updateProfile(uploadedImageUrl);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to upload image due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed to upload image due to\"+e.getMessage()", Toast.LENGTH_SHORT).show();
                        
                    }
                });
    }

    private void updateProfile(String imageUrl){
        Log.d(TAG, "updateProfile: Updating user profile");
        progressDialog.setMessage("Updating user profile...");
        progressDialog.show();

        //setup data to update in db
        HashMap<String,Object> hashMap =  new HashMap<>();
        hashMap.put("name", ""+name);
        if(imageUri != null){
            hashMap.put("profileImage", ""+imageUrl);
        }

        //update data to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Profile updated...");
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "profile updated...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed tp update db due to"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed to update db due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                });


    }


    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user info of user "+firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info of user here from snapshot
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp ").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();


                        //set data to ui
                        binding.nameEt.setText(name);


                        //set image using glide

                        Glide.with(ProfileEditActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_grey)
                                .into(binding.profileIv);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showImageAttachMenu() {
        //init/setup popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE,0,0 ,"Camera");
        popupMenu.getMenu().add(Menu.NONE,1,1 ,"Gallery");

        popupMenu.show();

        //handle menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //get id of item which
                int which = menuItem.getItemId();
                if(which == 0){
                    //camera clicked
                    pickImageCamera();

                }
                else {
                    // gallery clicked
                    pickImageGallery();

                }
                return false;
            }
        });


    }

    private void pickImageCamera() {
        //intent to pick image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Pick "); //image title
        values.put(MediaStore.Images.Media.DESCRIPTION,"Sample Image Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);
    }
    private void pickImageGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to hanlde result of camera intent
                    // get uri of image
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Pick From Camera "+imageUri);
                        Intent data = result.getData();
                        binding.profileIv.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(ProfileEditActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    // get uri of image
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: "+imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult:Picked From Gallery ");
                        binding.profileIv.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(ProfileEditActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );
}