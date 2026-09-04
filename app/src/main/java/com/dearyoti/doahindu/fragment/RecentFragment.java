package com.dearyoti.doahindu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.activity.MainActivity;
import com.dearyoti.doahindu.adapter.TopicsAdapter;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.model.TopicsModel;

import java.util.ArrayList;

public class RecentFragment extends Fragment implements TopicsAdapter.itemInterface {

    private RecyclerView recyclerRecent;
    private TextView txtNoData;
    private ArrayList<TopicsModel> recentList;
    private DatabaseHelper db;
    private View view;
    private TopicsAdapter topicsAdapter;
    private ArrayList<TopicsModel> limitRecent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recent, container, false);
        init();
        setAdapter();
        //bannerLoad();
        return view;
    }

    private void init() {
        db = new DatabaseHelper(getActivity());
        recyclerRecent = view.findViewById(R.id.recycler_recent);
        txtNoData = view.findViewById(R.id.txt_no_data);

        recentList = new ArrayList<>();
        recentList = db.getRecentViewed();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerRecent.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView

    }

    private void setAdapter() {
        if (recentList.size() > 0) {
            limitRecent = new ArrayList<>();
            for (int i = 0; i < Math.min(10, recentList.size()); i++) {
                if (recentList.get(i).getLast_viewed() != null && !recentList.get(i).getLast_viewed().isEmpty()) {
                    limitRecent.add(recentList.get(i));
                }
            }
            topicsAdapter = new TopicsAdapter(getActivity(), limitRecent, false, this::itemRemove);
            recyclerRecent.setAdapter(topicsAdapter);

            if (limitRecent.size() > 0) {
                recyclerRecent.setVisibility(View.VISIBLE);
                txtNoData.setVisibility(View.GONE);
            } else {
                recyclerRecent.setVisibility(View.GONE);
                txtNoData.setVisibility(View.VISIBLE);
            }
        }
    }

    public void filter(String text) {
        ArrayList<TopicsModel> temp = new ArrayList();
        for (TopicsModel d : limitRecent) {
            if (d.getTopic_name().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
        if (topicsAdapter != null) {
            topicsAdapter.updateList(temp);
        }
        if (temp.size() > 0) {
            recyclerRecent.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);

        } else {
            recyclerRecent.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((MainActivity) requireActivity()).highLightNavigation(2, "Telah Dilihat");

            if (((MainActivity) getActivity()).searchView != null) {
                ((MainActivity) getActivity()).searchView.setVisibility(View.VISIBLE);
            }
            MainActivity.whichFragment = "recent";
        }
    }

    @Override
    public void itemRemove() {

    }
}