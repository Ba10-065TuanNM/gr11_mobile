package com.technifysoft.bookapp;

import static com.technifysoft.bookapp.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

//applicaition class runs before your laucher activity
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

//    created a static method to convert timestamp to proper date format
    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
//        format timestamp to dd//MM/yyyy
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();

        return date;
    }
    public static void deleteBook(Context context,String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETA_BOOK_APP";


        Log.d(TAG, "deleteBook: deleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Deleting "+bookTitle+" ...");
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting from storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage");

                        Log.d(TAG, "onSuccess: Now Deleting info from db");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from db too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book Deleted Successfully!", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from db due to+"+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage()   , Toast.LENGTH_SHORT).show();
                    }
                });

    }

   public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView sizeTv) {

        String TAG = "PDF_SIZE-TAG";
//        using url we can get file and its metadata from firebase storage


        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //  Get size in bytes
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle+" "+bytes);

                        // convert bytes =>> KB , MB
                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if(mb >= 1 ){
                           sizeTv.setText(String.format("%2f",mb)+" MB");
                        }
                        else if (kb >= 1 ){
                            sizeTv.setText(String.format("%2f",kb)+" KB");
                        }
                        else {
                          sizeTv.setText(String.format("%2f",bytes)+" bytes");
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        failed to getting metadata
                        Log.d(TAG, "onFailure: "+e.getMessage());

                    }
                });
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {

        String TAG = "PDF_LOAD_SINGLE_TAG";


//        using url we can get file and its metadata from firebase storage


        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+pdfTitle   +" successfully got the file");

                        // set to pfd view
                        pdfView.fromBytes(bytes)
                                .pages(0) //show only first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //hide progress
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        //hide progress
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        //pdf loaded
                                        //hide progress
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");

                                        if(pagesTv != null){
                                            pagesTv.setText(""+nbPages);
                                        }

                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //hide progress
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: Fail getting file from url due to "+e.getMessage());

                    }
                });
    }

    public static void loadCategory(String categoryId, TextView categoryTv) {
//        get category using categoryId

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Catagories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get category
                        String category =""+snapshot.child("category").getValue();

                        // Set to category text view
                       categoryTv.setText(category);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

public static void incrementBookViewCount(String bookId){

        //1 get book viewscount
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
    ref.child(bookId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // get views count
                    String viewsCount =""+snapshot.child("viewsCount").getValue();
                    // in case of null replave with 0
                    if ( viewsCount.equals("") || viewsCount.equals("null") ) {
                        viewsCount = "0";
                    }
                    //increment viewscount
                    long newViewsCount = Long.parseLong(viewsCount)+1;
                    HashMap<String,Object> hashMap= new HashMap<>();
                    hashMap.put("viewsCount", newViewsCount);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                    reference.child(bookId)
                            .updateChildren(hashMap);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

}


public static void addToFavorite(Context context,String bookId){
        // we can add only if user is logged in
        // (1) check if user is logged in
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    if (firebaseAuth.getCurrentUser() == null){
        Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
    }
    else {
        long timestamp = System.currentTimeMillis();

        //setup data to add in firebase db of current user for favorite book
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+timestamp);

        //save to db

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
                .setValue(hashMap)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Added to favourite list...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to add to favourite list due to "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

}

    public static void removeFromFavorite(Context context, String bookId){
        // we can remove only if user is logged in
        // (1) check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
        }
        else {

        }

            //remove to db

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorite").child(bookId)
                    .removeValue()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Remove from favourite list...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to Remove from favourite list due to "+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }





}

