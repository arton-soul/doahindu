package com.dearyoti.doahindu.activity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.fragment.AboutUsFragment;
import com.dearyoti.doahindu.fragment.CategoryFragment;
import com.dearyoti.doahindu.fragment.FavoriteFragment;
import com.dearyoti.doahindu.fragment.HomeFragment;
import com.dearyoti.doahindu.fragment.PolicyFragment;
import com.dearyoti.doahindu.fragment.RecentFragment;
import com.dearyoti.doahindu.utils.Constant;
import com.dearyoti.doahindu.utils.EdgeToEdgeHelper;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private TextView txtToolbarTitle;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DatabaseHelper db;
    private MenuItem mMenuItem;
    public SearchView searchView;
    private FavoriteFragment favoriteFragment;
    private HomeFragment homeFragment;
    private CategoryFragment categoryFragment;
    private RecentFragment recentFragment;
    private NavigationView navigationView;

    public static String whichFragment = "";

    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof HomeFragment) {
                exitApp();
            } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdgeHelper.apply(this, findViewById(R.id.drawer));
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        init();
        requestNotificationPermission();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void init() {
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigation_drawer);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = new DatabaseHelper(this);

        txtToolbarTitle = findViewById(R.id.txt_toolbar_title);
        Typeface font = Typeface.createFromAsset(getAssets(), Constant.FONT_PATH_SEMIBOLD);

        txtToolbarTitle.setTypeface(font);

        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        insertData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        // set default fragment
        homeFragment = new HomeFragment();
        setDefaultFragment(homeFragment);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_item_home:
                FragmentManager fragmentManager = getSupportFragmentManager();
                homeFragment = new HomeFragment();
                loadFrag(homeFragment, "home", fragmentManager);
                whichFragment = "home";
                return true;
            case R.id.nav_item_category:
                FragmentManager fm = getSupportFragmentManager();
                categoryFragment = new CategoryFragment();
                loadFrag(categoryFragment, "category", fm);
                whichFragment = "category";
                return true;
            case R.id.nav_item_recent:
                FragmentManager fm1 = getSupportFragmentManager();
                recentFragment = new RecentFragment();
                loadFrag(recentFragment, "recent", fm1);
                whichFragment = "recent";
                return true;
            case R.id.nav_item_favorites:
                FragmentManager fm2 = getSupportFragmentManager();
                favoriteFragment = new FavoriteFragment();
                loadFrag(favoriteFragment, "favorite", fm2);
                whichFragment = "favorite";
                return true;
            case R.id.nav_item_rate:
                rateApp();
                return true;
            case R.id.nav_item_share:
                shareApp();
                return true;

            case R.id.nav_item_privacy:
                drawerLayout.closeDrawers();
                FragmentManager fm3 = getSupportFragmentManager();
                PolicyFragment policyFragment = new PolicyFragment();
                loadFrag(policyFragment, "privacy", fm3);
                return true;

            case R.id.nav_item_about:
                FragmentManager fm4 = getSupportFragmentManager();
                AboutUsFragment aboutUsFragment = new AboutUsFragment();
                loadFrag(aboutUsFragment, "about_us", fm4);
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            searchView.setQuery("", false);
            if (!searchView.isIconified()) {
                searchView.clearFocus();
                searchView.setIconified(true);
            }
        }
    }

    private void searchTopic(String query) {
        if (whichFragment.equals("home")) {
            homeFragment.filter(query);
        } else if (whichFragment.equals("favorite")) {
            favoriteFragment.filter(query);
        } else if (whichFragment.equals("category")) {
            categoryFragment.filter(query);
        } else if (whichFragment.equals("recent")) {
            recentFragment.filter(query);
        }
    }

        private void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name) + " \n\n" + "http://play.google.com/store/apps/details?id=" + getPackageName());
        startActivity(share);
    }

    private void rateApp() {
        final String appName = getPackageName();//your application package name i.e play store application url
        Log.e("package:", appName);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + appName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + appName)));
        }
    }


    private void insertData() {
        try {
            db.copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFrag(Fragment f1, String name, FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.fragment_container, f1, name).addToBackStack(null);
        ft.commit();
    }

    private void setDefaultFragment(Fragment fragment) {
        setToolbarTitle(getResources().getString(R.string.app_name));
        whichFragment = "home";
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null).
                replace(R.id.fragment_container, fragment).commit();
    }

    public void openCategoryFragment() {
        whichFragment = "category";
        categoryFragment = new CategoryFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, categoryFragment).addToBackStack(null).commit();
        setToolbarTitle(getString(R.string.category_text));
    }

    public void openRecentFragment() {
        whichFragment = "recent";
        recentFragment = new RecentFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, recentFragment).addToBackStack(null).commit();
        setToolbarTitle(getString(R.string.recent_text));
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
                searchTopic(query);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                HomeFragment.relativeBanner.setVisibility(View.VISIBLE);
                return false;
            }
        });
        return true;
    }

    public void highLightNavigation(int position, String name) {
        navigationView.getMenu().getItem(position).setChecked(true);
        setToolbarTitle(name);
    }

    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            txtToolbarTitle.setText(title);
        }
    }

    private void exitApp() {
        new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.exit_msg))
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
