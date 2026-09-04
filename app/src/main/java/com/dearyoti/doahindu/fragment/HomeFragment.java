package com.dearyoti.doahindu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.MyApplication;
import com.dearyoti.doahindu.activity.MainActivity;
import com.dearyoti.doahindu.adapter.HomeCategoryAdapter;
import com.dearyoti.doahindu.adapter.TopicsAdapter;
import com.dearyoti.doahindu.adapter.ViewPagerAdapter;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.database.DatabaseExecutor;
import com.dearyoti.doahindu.model.CategoryModel;
import com.dearyoti.doahindu.model.LatestStoryModel;
import com.dearyoti.doahindu.model.TopicsModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;

public class HomeFragment extends
        Fragment implements TopicsAdapter.itemInterface {

    private View view;
    private RecyclerView recyclerCategoryView;
    private RecyclerView recyclerRecentView;
    private ArrayList<CategoryModel> categoryList;
    private ArrayList<TopicsModel> recentList;
    private ArrayList<TopicsModel> limitRecent;
    private ArrayList<LatestStoryModel> latestStoryList;
    private RelativeLayout layoutCategory;
    private RelativeLayout layoutRecent;
    private DatabaseHelper db;
    private HomeCategoryAdapter categoryAdapter;
    private TopicsAdapter topicsAdapter;
    private TextView txtNoData;
    private AdView adView;
    public static CircleIndicator circlePageIndicator;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager mPager;
    private int currentCount = 0;
    public static RelativeLayout relativeBanner;
    private AppCompatButton btnMoreCategory, btnMoreRecent;
    private ArrayList<CategoryModel> homeCatList;
    private int L = 6;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        init();
        setViewPager();
        setAdapter();
        setRecentAdapter();
        bannerLoad();
        return view;
    }

    public void init() {
        categoryList = new ArrayList<>();
        recentList = new ArrayList<>();
        homeCatList = new ArrayList<>();
        limitRecent = new ArrayList<>();
        mPager = view.findViewById(R.id.view_pager);
        circlePageIndicator = view.findViewById(R.id.indicator);
        recyclerCategoryView = view.findViewById(R.id.category_recycler);
        recyclerRecentView = view.findViewById(R.id.recent_recycler);
        txtNoData = view.findViewById(R.id.txt_no_data);
        btnMoreRecent = view.findViewById(R.id.btn_more_recent);
        relativeBanner = view.findViewById(R.id.relative_banner);
        btnMoreCategory = view.findViewById(R.id.btn_more_category);
        layoutCategory = view.findViewById(R.id.layout_category);
        layoutRecent = view.findViewById(R.id.layout_recent);


        btnMoreRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) requireActivity()).openRecentFragment();
                ((MainActivity) requireActivity()).highLightNavigation(2, getString(R.string.recent_text));
            }
        });
        btnMoreCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) requireActivity()).openCategoryFragment();
                ((MainActivity) requireActivity()).highLightNavigation(1, getString(R.string.category_text));
            }
        });
    }

    public void setViewPager() {
        DatabaseExecutor.execute(db::getLatestStory, result -> {
            if (!isAdded()) {
                return;
            }
            latestStoryList = result;
            viewPagerAdapter = new ViewPagerAdapter(requireContext(), latestStoryList);
            mPager.setAdapter(viewPagerAdapter);
            circlePageIndicator.setViewPager(mPager);
            playViewPager(mPager);
        }, error -> {
            if (isAdded()) {
                relativeBanner.setVisibility(View.GONE);
            }
        });
    }

    public void setRecentAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerRecentView.setLayoutManager(linearLayoutManager);
        DatabaseExecutor.execute(db::getRecentViewed, result -> {
            if (!isAdded()) {
                return;
            }
            recentList = result;
            limitRecent = new ArrayList<>();
            if (!recentList.isEmpty() && recentList.get(0).getLast_viewed() != null
                    && !recentList.get(0).getLast_viewed().isEmpty()) {
                for (int i = 0; i < Math.min(3, recentList.size()); i++) {
                    if (recentList.get(i).getLast_viewed() != null
                            && !recentList.get(i).getLast_viewed().isEmpty()) {
                        limitRecent.add(recentList.get(i));
                    }
                }
                topicsAdapter = new TopicsAdapter(requireContext(), limitRecent, false, this::itemRemove);
                recyclerRecentView.setAdapter(topicsAdapter);
                recyclerRecentView.setVisibility(View.VISIBLE);
                layoutRecent.setVisibility(View.VISIBLE);
            } else {
                recyclerRecentView.setVisibility(View.GONE);
                layoutRecent.setVisibility(View.GONE);
            }
        }, error -> {
            if (isAdded()) {
                recyclerRecentView.setVisibility(View.GONE);
                layoutRecent.setVisibility(View.GONE);
            }
        });
    }

    public void setAdapter() {
        int columnCount = getResources().getInteger(R.integer.category_grid_columns);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), columnCount) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerCategoryView.setLayoutManager(gridLayoutManager);
        DatabaseExecutor.execute(db::getAllCategories, result -> {
            if (!isAdded()) {
                return;
            }
            categoryList = result;
            homeCatList = new ArrayList<>(categoryList.subList(0, Math.min(L, categoryList.size())));
            if (!homeCatList.isEmpty()) {
                categoryAdapter = new HomeCategoryAdapter(requireContext(), homeCatList);
                recyclerCategoryView.setAdapter(categoryAdapter);
            }
        }, error -> {
            if (isAdded()) {
                layoutCategory.setVisibility(View.GONE);
            }
        });
    }

    public void filter(String text) {
        ArrayList<CategoryModel> temp = new ArrayList();
        for (CategoryModel d : homeCatList) {
            if (d.getCat_name().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
        if (categoryAdapter != null) {
            categoryAdapter.updateList(temp);
        }

        ArrayList<TopicsModel> temp1 = new ArrayList();
        for (TopicsModel d1 : limitRecent) {
            if (d1.getTopic_name().toLowerCase().contains(text.toLowerCase())) {
                temp1.add(d1);
            }
        }
        if (topicsAdapter != null) {
            topicsAdapter.updateList(temp1);
        }

        if (temp.size() > 0) {
            recyclerCategoryView.setVisibility(View.VISIBLE);
            layoutCategory.setVisibility(View.VISIBLE);
        } else {
            recyclerCategoryView.setVisibility(View.GONE);
            layoutCategory.setVisibility(View.GONE);
        }

        if (temp1.size() > 0) {
            recyclerRecentView.setVisibility(View.VISIBLE);
            layoutRecent.setVisibility(View.VISIBLE);
        } else {
            recyclerRecentView.setVisibility(View.GONE);
            layoutRecent.setVisibility(View.GONE);
        }

        if (temp.size() == 0 && temp1.size() == 0) {
            txtNoData.setVisibility(View.VISIBLE);
        } else {
            txtNoData.setVisibility(View.GONE);
        }
        if (text.length() > 0) {
            relativeBanner.setVisibility(View.GONE);
            circlePageIndicator.setVisibility(View.GONE);
        } else {
            relativeBanner.setVisibility(View.VISIBLE);
            circlePageIndicator.setVisibility(View.VISIBLE);
        }

    }

    private void playViewPager(final ViewPager viewPager) {
        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (viewPagerAdapter != null && viewPager.getAdapter().getCount() > 0) {
                        int position = currentCount % viewPagerAdapter.getCount();
                        currentCount++;
                        viewPager.setCurrentItem(position);
                        playViewPager(viewPager);
                    }
                } catch (Exception e) {
                }
            }
        }, 5000);
    }

    public void bannerLoad() {
        if (!MyApplication.areAdsInitialized()) return;
        adView = new AdView(getActivity());
        adView.setAdUnitId(getResources().getString(R.string.banner_ids));
        int adWidth = Math.round(getResources().getDisplayMetrics().widthPixels
                / getResources().getDisplayMetrics().density);
        adView.setAdSize(AdSize.getLargeAnchoredAdaptiveBannerAdSize(requireContext(), adWidth));
        LinearLayout layout = view.findViewById(R.id.banner_ads);
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
        circlePageIndicator = view.findViewById(R.id.indicator);
        if (adView != null) {
            adView.resume();
        }
        if (limitRecent != null) {
            setRecentAdapter();
        }
        if (getActivity() != null) {
            ((MainActivity) requireActivity()).highLightNavigation(0, getResources().getString(R.string.app_name));

            if (((MainActivity) getActivity()).searchView != null) {
                ((MainActivity) getActivity()).searchView.setVisibility(View.VISIBLE);
            }
            MainActivity.whichFragment = "home";
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
