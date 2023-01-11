package com.technifysoft.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technifysoft.bookapp.databinding.ActivityPdfDetailBinding;

public class PdfDetailActivity extends AppCompatActivity {

    //view binding
    private ActivityPdfDetailBinding binding;

    //pdf id, get from intent
    String bookId, bookTitle,bookUrl;

    boolean isInMyFavorite = false;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent e.g bookId
        Intent intent= getIntent();
        bookId = intent.getStringExtra("bookId");

        firebaseAuth = FirebaseAuth.getInstance();
        if ( firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }
        loadBookDetail();

        // Increment Book Views count , whenever this page start
            MyApplication.incrementBookViewCount(bookId);

        // handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //hanlde click, open to view pdf
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);// create activity for reading book
                intent1.putExtra("bookId",bookId);
                startActivity(intent1);
            }
        });

        //Hanlde click, add/remove favorite
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You are not logged in", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(isInMyFavorite){
                        //in favorite, remove from favrorite
                        MyApplication.removeFromFavorite(PdfDetailActivity.this,bookId);
                    }
                    else{
                        // not in favorite, add to favorite
                        MyApplication.addToFavorite(PdfDetailActivity.this,bookId);

                    }

                }
            }
        });

    }

    private void loadBookDetail() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                         bookTitle =""+snapshot.child("title").getValue();
                        String description =""+snapshot.child("description").getValue();
                        String categoryId =""+snapshot.child("categoryId").getValue();
                        String viewsCount =""+snapshot.child("viewsCount").getValue();
                        String downloadsCount =""+snapshot.child("downloadsCount").getValue();
                        bookUrl =""+snapshot.child("url").getValue();
                        String timestamp =""+snapshot.child("timestamp").getValue();


                        // format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pageTv
                        );
                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeTv
                        );


                        //set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null","N/A"));
                        binding.downloadsLabelTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }) ;
    }

    private void checkIsFavorite(){
        //logged in => check its in favorite list or not
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();//true : if exist, false if not exist
                        if (isInMyFavorite){
                            //exist in favorite
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.ic_favorite_white, 0 , 0 );
                            binding.favoriteBtn.setText("Remove Favorite");
                        }
                        else {
                            // not exist in favorite
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,R.drawable.ic_favorite_border_white, 0 , 0 );
                            binding.favoriteBtn.setText("Add Favorite");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}