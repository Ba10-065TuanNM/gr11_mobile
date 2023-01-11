package com.technifysoft.bookapp.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.technifysoft.bookapp.Filter.FilterPdfAdmin;
import com.technifysoft.bookapp.Models.ModelPdf;
import com.technifysoft.bookapp.MyApplication;
import com.technifysoft.bookapp.PdfDetailActivity;
import com.technifysoft.bookapp.PdfEditActivity;
import com.technifysoft.bookapp.databinding.RowPdfAdminBinding;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

//    COntext
    private Context context;

//    ArrayList to hold list of data of type ModelPDf
    public ArrayList<ModelPdf> pdfArrayList, filterList;

//    View bining row_pdf_admin.xml
    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter ;

    private static final String TAG = "PDF_ADAPTER_TAG";

    //progress
    private ProgressDialog progressDialog;

//    Constructor


    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//         bind layout using view binding
        binding =  RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
//        Get data , set data , handle click,...

                    // get data
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();
//                  CONvert timestamp =>> dd/MM/yyyy format
        String formatedDate = MyApplication.formatTimestamp(timestamp);

//        Set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formatedDate);



//        Load further details like category, pdf from url, pdf size in seprate functions
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv

        );

        // handle click, show dialog option for admidn
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionDialog(model, holder);
            }

        });

        // Handle book/pdf click, open pdf detail page, pass pdf/book id to get detail of it
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });

    }

    private void moreOptionDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        // show option in dialog
        String[] option = {"Edit", "Delete"};

        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // handle dialog option clicl
                        if (which == 0 ){
                            //Edit Clicked, open PdfEditActivity to edit the book info
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);


                        }
                        else if (which == 1){
                            //Delete Clicked
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle);

                        }

                    }
                })
                .show();
    }



    @Override
    public int getItemCount() {
        return pdfArrayList.size(); // return number of records | list size
    }

    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter = new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }

    //    View Holder class for row_pdf_admin.xml
    class HolderPdfAdmin extends RecyclerView.ViewHolder{

//        UI Views of row_pdf_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv , dateTv;
        ImageButton moreBtn;

    public HolderPdfAdmin(@NonNull View itemView) {
        super(itemView);

//        init ui views
        pdfView = binding.pdfView;
        progressBar = binding.progressBar;
        titleTv = binding.titleTv;
        descriptionTv = binding.descriptionTv ;
        categoryTv = binding.categoryTv;
        sizeTv = binding.sizeTv;
        dateTv = binding.dateTv;
        moreBtn = binding.moreBtn;
    }
}
}
