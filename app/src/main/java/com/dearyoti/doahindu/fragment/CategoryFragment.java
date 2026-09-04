package com.dearyoti.doahindu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.activity.MainActivity;
import com.dearyoti.doahindu.adapter.CategoryAdapter;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.model.CategoryModel;

import java.util.ArrayList;

public class CategoryFragment extends Fragment {

    private View view;
    private RecyclerView recyclerCategoryView;
    private ArrayList<CategoryModel> categoryList;
    private DatabaseHelper db;
    private CategoryAdapter categoryAdapter;
    private TextView txtNoData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_category, container, false);
        init();
        setAdapter();
        return view;
    }

    public void init() {
        categoryList = new ArrayList<>();
        recyclerCategoryView = view.findViewById(R.id.category_recycler);
        txtNoData = view.findViewById(R.id.txt_no_data);
    }

    public void setAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        recyclerCategoryView.setLayoutManager(gridLayoutManager);

        categoryList = db.getAllCategories();
        if (categoryList.size() > 0) {
            categoryAdapter = new CategoryAdapter(getContext(), categoryList);
            recyclerCategoryView.setAdapter(categoryAdapter);
            recyclerCategoryView.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);
        } else {
            recyclerCategoryView.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }
    }

    public void filter(String text) {
        ArrayList<CategoryModel> temp = new ArrayList();
        for (CategoryModel d : categoryList) {
            if (d.getCat_name().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
        if (categoryAdapter != null) {
            categoryAdapter.updateList(temp);
        }
        if (temp.size() > 0) {
            recyclerCategoryView.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);

        } else {
            recyclerCategoryView.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((MainActivity) requireActivity()).highLightNavigation(1, "Kategori");
            if (((MainActivity) getActivity()).searchView != null) {
                ((MainActivity) getActivity()).searchView.setVisibility(View.VISIBLE);
            }
            MainActivity.whichFragment = "category";
        }
    }
}
