package com.dearyoti.doahindu.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.adapter.TopicsAdapter;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.model.TopicsModel;
import com.dearyoti.doahindu.utils.Constant;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class TopicActivity extends AppCompatActivity implements TopicsAdapter.itemInterface {

    private Toolbar toolbar;
    private TextView toolbarTopicTitle;
    private RecyclerView recyclerTopics;
    private TextView txtNoData;
    private SearchView searchView;
    private MenuItem mMenuItem;
    private ArrayList<TopicsModel> topicsList;
    private DatabaseHelper db;
    private Integer selectedCatId = 1;
    private String selectedCatName = "";
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        getIntentData();
        init();
        setAdapter();
        bannerLoad();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            selectedCatId = intent.getIntExtra("cat_id", 1);
            selectedCatName = intent.getStringExtra("cat_name");
        }
    }

    private void init() {
        db = new DatabaseHelper(TopicActivity.this);
        toolbar = findViewById(R.id.toolbar);
        toolbarTopicTitle = findViewById(R.id.toolbar_topic_title);
        toolbar.setTitle("");
        toolbarTopicTitle.setText("" + selectedCatName);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        recyclerTopics = findViewById(R.id.recycler_topics);
        txtNoData = findViewById(R.id.txt_no_data);

        Typeface font = Typeface.createFromAsset(getAssets(), Constant.FONT_PATH_SEMIBOLD);
        toolbarTopicTitle.setTypeface(font);


        topicsList = db.getAllTopicsByCategory(selectedCatId);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerTopics.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void setAdapter() {
        if (topicsList.size() > 0) {
            TopicsAdapter adapter = new TopicsAdapter(TopicActivity.this, topicsList, false, this::itemRemove);
            recyclerTopics.setAdapter(adapter);
            recyclerTopics.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);
        } else {
            recyclerTopics.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        }
    }

    private void searchTopic(Integer cat_id, String topic_name) {
        topicsList = db.getSearchTopics(cat_id, topic_name);
        setAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_topics, menu);
        mMenuItem = menu.findItem(R.id.nav_topics_search);
        searchView = (SearchView) mMenuItem.getActionView();

        int searchImgId = androidx.appcompat.R.id.search_button;
        ImageView v = searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mMenuItem.collapseActionView();
                searchTopic(selectedCatId, query);
                return false;
            }
        });
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.nav_topics_search) {
            mMenuItem.expandActionView();
            return true;
        }
        return false;
    }


    public void bannerLoad() {
        adView = new AdView(this);
        adView.setAdUnitId(getResources().getString(R.string.banner_ids));
        int adWidth = Math.round(getResources().getDisplayMetrics().widthPixels
                / getResources().getDisplayMetrics().density);
        adView.setAdSize(AdSize.getLargeAnchoredAdaptiveBannerAdSize(this, adWidth));
        LinearLayout layout = findViewById(R.id.banner_ads);
        layout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }

        if (topicsList != null) {
            topicsList = db.getAllTopicsByCategory(selectedCatId);
            setAdapter();
        }
        if (searchView != null) {
            searchView.setQuery("", false);
            if (!searchView.isIconified()) {
                searchView.clearFocus();
                searchView.setIconified(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void itemRemove() {
    }
}
