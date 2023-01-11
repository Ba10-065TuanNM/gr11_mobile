package com.technifysoft.bookapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technifysoft.bookapp.Models.ModelPdf;
import com.technifysoft.bookapp.adapters.AdapterPdfUser;
import com.technifysoft.bookapp.databinding.FragmentBookUserBinding;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BooksUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BooksUserFragment extends Fragment {

    private  String categoryId;
    private String category;
    private String uid;

    private ArrayList<ModelPdf>pdfArrayList;
    private AdapterPdfUser adapterPdfUser;

    //view binding
    private FragmentBookUserBinding binding;

    private static final String TAG = "BOOKS_USER_TAG";


    public BooksUserFragment() {
        // Required empty public constructor
    }

    public static BooksUserFragment newInstance(String categoryid, String category, String uid) {
        BooksUserFragment fragment = new BooksUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryid);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =FragmentBookUserBinding.inflate(LayoutInflater.from(getContext()), container,false);
        Log.d(TAG, "onCreateView:Category: "+category);

        if(category.equals("All")){
            //load all books
            loadAllBooks();
        }
        else if(category.equals("Most Viewed")){
//            load most Views book
            loadMostViewdBook("viewsCount");
        }
        else {
            //load selected category book
            loadCategorizedBooks();
        }

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                try {
                        adapterPdfUser.getFilter().filter(s);
                }
                catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }
                adapterPdfUser.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return binding.getRoot();
    }

    private void loadCategorizedBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            // add to list
                            pdfArrayList.add(model);
                        }
                        //setup adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                        // set adapter to recyclerview
                        binding.booksRv.setAdapter(adapterPdfUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMostViewdBook(String orderBy) {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    // add to list
                    pdfArrayList.add(model);
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                // set adapter to recyclerview
                binding.booksRv.setAdapter(adapterPdfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    // add to list
                    pdfArrayList.add(model);
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(),pdfArrayList);
                // set adapter to recyclerview
                binding.booksRv.setAdapter(adapterPdfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}