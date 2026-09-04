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

public class FavoriteFragment extends Fragment implements TopicsAdapter.itemInterface {
    private View view;
    private RecyclerView recyclerFavorite;
    private TextView txtNoData;
    private DatabaseHelper db;
    private ArrayList<TopicsModel> topicsList;
    private TopicsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getContext());
        topicsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorite, container, false);
        init();
        setAdapter();
        return view;
    }

    public void init() {
        recyclerFavorite = view.findViewById(R.id.recycler_favorite);
        txtNoData = view.findViewById(R.id.txt_no_data);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerFavorite.setLayoutManager(linearLayoutManager);
    }

    public void filter(String text) {
        ArrayList<TopicsModel> temp = new ArrayList();
        for (TopicsModel d : topicsList) {
            if (d.getTopic_name().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
        if (adapter != null) {
            adapter.updateList(temp);
        }
        if (temp.size() > 0) {
            recyclerFavorite.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);
        } else {
            recyclerFavorite.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }
    }

    private void setAdapter() {
        topicsList = db.getFavoriteTopics();
        if (topicsList != null) {
            if (topicsList.size() > 0) {
                adapter = new TopicsAdapter(getContext(), topicsList, true, this::itemRemove);
                recyclerFavorite.setAdapter(adapter);
                recyclerFavorite.setVisibility(View.VISIBLE);
                txtNoData.setVisibility(View.GONE);
            } else {
                recyclerFavorite.setVisibility(View.GONE);
                txtNoData.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerFavorite.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (topicsList != null) {
            setAdapter();
        }
        if (getActivity() != null) {
            ((MainActivity) requireActivity()).highLightNavigation(3, "Favorit");

            if (((MainActivity) getActivity()).searchView != null) {
                ((MainActivity) getActivity()).searchView.setVisibility(View.VISIBLE);
            }
            MainActivity.whichFragment = "favorite";
        }
    }

    @Override
    public void itemRemove() {
        topicsList = db.getFavoriteTopics();
        if (topicsList != null) {
            recyclerFavorite.setAdapter(new TopicsAdapter(getContext(), topicsList, true, this::itemRemove));
            if (topicsList.isEmpty()) {
                txtNoData.setVisibility(View.VISIBLE);
            } else {
                txtNoData.setVisibility(View.GONE);
            }
        } else {
            txtNoData.setVisibility(View.GONE);
        }
    }
}