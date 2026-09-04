package com.dearyoti.doahindu.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.dearyoti.doahindu.MyApplication;
import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.utils.Constant;
import com.dearyoti.doahindu.utils.MySharedPref;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Timestamp;

public class StoriesActivity extends AppCompatActivity {

    private TextView txtStoryTitle, txtStory;
    private ImageView navFavorite;
    private MySharedPref mySharedPref;
    private DatabaseHelper db;
    private Integer selectedTopicId = 1, selectedCatId = 1;
    private String selectedCatName = "", selectedTopicName = "", selectedTopicStory = "", flag = "";
    private AdView adView;
    private FloatingActionButton fabShare;
    ImageView imgTopic;
    byte[] imageBytes;

    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (MyApplication.interstitialAd != null) {
                MyApplication.interstitialAd.show(StoriesActivity.this);
            }
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        getIntentData();
        init();
        bannerLoad();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            selectedTopicId = intent.getIntExtra("topic_id", 1);
            selectedCatId = intent.getIntExtra("cat_id", 1);
            selectedTopicName = intent.getStringExtra("topic_name");
            selectedTopicStory = intent.getStringExtra("topic_story");
            flag = intent.getStringExtra("flag");
            imageBytes = getIntent().getByteArrayExtra("topic_image");
        }
    }

    private void init() {
        db = new DatabaseHelper(StoriesActivity.this);
        mySharedPref = new MySharedPref();
        selectedCatName = "" + db.getCategoryName(selectedCatId);

        Toolbar toolbar = findViewById(R.id.toolbar_stories);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarStoriesTitle = findViewById(R.id.toolbar_stories_title);
        toolbar.setTitle("");

        if (flag.equals("from_latest")) {
            toolbarStoriesTitle.setText("Cerita Hindu");
        } else {
            toolbarStoriesTitle.setText("" + selectedCatName);
        }

        txtStoryTitle = findViewById(R.id.txt_story_title);
        txtStory = findViewById(R.id.txt_story);
        fabShare = findViewById(R.id.fab_share);
        imgTopic = findViewById(R.id.img_topic);

        txtStory.setText("" + selectedTopicStory);
        txtStoryTitle.setText("" + selectedTopicName);

        Typeface font_bold = Typeface.createFromAsset(getAssets(), Constant.FONT_PATH_SEMIBOLD);
        toolbarStoriesTitle.setTypeface(font_bold);
        txtStoryTitle.setTypeface(font_bold);

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imgTopic.setImageBitmap(bitmap);
        }

        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, txtStoryTitle.getText().toString() + "\n\n" + Html.fromHtml(txtStory.getText().toString()));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
        if (selectedTopicId != null && selectedTopicId != 0) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            db.updateLastViewed(selectedTopicId, timestamp.toString());
            mySharedPref.setRecentViewedTopicId(this, db);
        }
    }

    private void isFavorite() {
        if (db.isFavorite(selectedTopicId)) {
            navFavorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.favorite_select));
        } else {
            navFavorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.favorite_unselect));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stories, menu);
        MenuItem item = menu.findItem(R.id.nav_stories_fav);

        if (flag.equals("from_latest")) {
            item.setVisible(false);
        }
        navFavorite = (ImageView) menu.findItem(R.id.nav_stories_fav).getActionView();
        isFavorite();
        navFavorite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (db.isFavorite(selectedTopicId)) {
                    if (db.updateFavorite(selectedTopicId, 0)) {
                        Snackbar.make(view, "Story removed from favorite.", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    if (db.updateFavorite(selectedTopicId, 1)) {
                        Snackbar.make(view, "Story added to favorite.", Snackbar.LENGTH_LONG).show();
                    }
                }
                mySharedPref.setFavoriteTopicId(StoriesActivity.this, db);
                isFavorite();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.nav_stories_fav) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

}
