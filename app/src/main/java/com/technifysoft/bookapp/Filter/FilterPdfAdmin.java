package com.technifysoft.bookapp.Filter;
import android.widget.Filter;

import com.technifysoft.bookapp.Models.ModelCategory;
import com.technifysoft.bookapp.Models.ModelPdf;
import com.technifysoft.bookapp.adapters.AdapterCategory;
import com.technifysoft.bookapp.adapters.AdapterPdfAdmin;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
//    array in which we want to search
    ArrayList<ModelPdf> filterList;
//    adapter in which filter need to be implemented
    AdapterPdfAdmin adapterPdfAdmin;
//    constructor

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
//        value should not be null and empty
        if(constraint != null && constraint.length() > 0 ) {
//            change to upper case , or lower case to avoid
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
//                  validate
                if(filterList.get(i).getTitle().toUpperCase().contains((constraint))){
//                add to filered list
                filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;

        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//            apply filter changes
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)filterResults.values;

//        notify changes
        adapterPdfAdmin.notifyDataSetChanged();

    }
}
