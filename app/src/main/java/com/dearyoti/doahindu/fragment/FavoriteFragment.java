package com.dearyoti.doahindu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.activity.MainActivity;
import com.dearyoti.doahindu.adapter.TopicsAdapter;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.database.DatabaseExecutor;
import com.dearyoti.doahindu.model.TopicsModel;

import java.util.ArrayList;
import java.util.Map;

public class FavoriteFragment extends Fragment implements TopicsAdapter.itemInterface {
    private View view;
    private RecyclerView recyclerFavorite;
    private TextView txtNoData;
    private DatabaseHelper db;
    private ArrayList<TopicsModel> topicsList;
    private TopicsAdapter adapter;
    private Spinner collectionSpinner;
    private ArrayList<Long> collectionIds = new ArrayList<>();
    private long selectedCollectionId = 1L;

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
        collectionSpinner = view.findViewById(R.id.spinner_collection);
        view.findViewById(R.id.button_add_collection).setOnClickListener(v -> editCollection(0, ""));
        view.findViewById(R.id.button_manage_collection).setOnClickListener(v -> showManageDialog());
        collectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View selected, int position, long id) {
                if (position < collectionIds.size()) { selectedCollectionId = collectionIds.get(position); setAdapter(); }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
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
        DatabaseExecutor.execute(() -> db.getFavoriteTopics(selectedCollectionId), result -> {
            if (!isAdded()) {
                return;
            }
            topicsList = result;
            if (!topicsList.isEmpty()) {
                adapter = new TopicsAdapter(requireContext(), topicsList, selectedCollectionId, this::itemRemove);
                recyclerFavorite.setAdapter(adapter);
                recyclerFavorite.setVisibility(View.VISIBLE);
                txtNoData.setVisibility(View.GONE);
            } else {
                recyclerFavorite.setVisibility(View.GONE);
                txtNoData.setVisibility(View.VISIBLE);
            }
        }, error -> showNoData());
    }

    private void loadCollections() {
        DatabaseExecutor.execute(db::getFavoriteCollections, collections -> {
            if (!isAdded()) return;
            collectionIds = new ArrayList<>(collections.keySet());
            ArrayAdapter<String> names = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, new ArrayList<>(collections.values()));
            names.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            collectionSpinner.setAdapter(names);
            int position = collectionIds.indexOf(selectedCollectionId);
            collectionSpinner.setSelection(position < 0 ? 0 : position);
        }, error -> showNoData());
    }

    private void editCollection(long id, String currentName) {
        EditText input = new EditText(requireContext()); input.setHint(R.string.collection_name);
        input.setText(currentName); input.setSelection(input.length());
        new AlertDialog.Builder(requireContext()).setTitle(id == 0 ? R.string.new_collection : R.string.rename_collection)
                .setView(input).setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    DatabaseExecutor.execute(() -> id == 0 ? db.createFavoriteCollection(name)
                            : (db.renameFavoriteCollection(id, name) ? id : -1), result -> {
                        if (result > 0) { selectedCollectionId = result; loadCollections(); }
                    }, error -> { });
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private void showManageDialog() {
        if (selectedCollectionId == 1L) return;
        String name = collectionSpinner.getSelectedItem().toString();
        new AlertDialog.Builder(requireContext()).setTitle(name)
                .setItems(new String[]{getString(R.string.rename_collection), getString(R.string.delete_collection)},
                        (dialog, which) -> { if (which == 0) editCollection(selectedCollectionId, name);
                        else confirmDeleteCollection(); }).show();
    }

    private void confirmDeleteCollection() {
        new AlertDialog.Builder(requireContext()).setTitle(R.string.delete_collection)
                .setMessage(R.string.delete_collection_confirmation)
                .setPositiveButton(R.string.delete_collection, (dialog, which) -> DatabaseExecutor.execute(
                        () -> db.deleteFavoriteCollection(selectedCollectionId), deleted -> {
                            selectedCollectionId = 1L; loadCollections();
                        }, error -> { })).setNegativeButton(R.string.cancel, null).show();
    }

    private void showNoData() {
        if (isAdded()) {
            recyclerFavorite.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCollections();
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
        setAdapter();
    }
}
