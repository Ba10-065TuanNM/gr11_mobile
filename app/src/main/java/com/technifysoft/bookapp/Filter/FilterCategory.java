package com.technifysoft.bookapp.Filter;
import android.widget.Filter;

import com.technifysoft.bookapp.Models.ModelCategory;
import com.technifysoft.bookapp.adapters.AdapterCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {
//    array in which we want to search
    ArrayList<ModelCategory> filterList;
//    adapter in which filter need to be implemented
    AdapterCategory adapterCategory;
//    constructor

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
//        value should not be null and empty
        if(constraint != null && constraint.length() > 0 ) {
//            change to upper case , or lower case to avoid
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
//                  validate
                if(filterList.get(i).getCategory().toUpperCase().contains((constraint))){
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
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)filterResults.values;

//        notify changes
        adapterCategory.notifyDataSetChanged();

    }
}
