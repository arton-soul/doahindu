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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.dearyoti.doahindu.MyApplication;
import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.database.DatabaseExecutor;
import com.dearyoti.doahindu.model.LatestStoryModel;
import com.dearyoti.doahindu.model.TopicsModel;
import com.dearyoti.doahindu.utils.Constant;
import com.dearyoti.doahindu.utils.EdgeToEdgeHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class StoriesActivity extends AppCompatActivity {

    private TextView txtStoryTitle, txtStory;
    private ImageView navFavorite;
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
        EdgeToEdgeHelper.apply(this, findViewById(R.id.stories_root));
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        getIntentData();
        init();
        bannerLoad();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            selectedTopicId = intent.getIntExtra("topic_id", 1);
            flag = intent.getStringExtra("flag");
        }
    }

    private void init() {
        db = new DatabaseHelper(StoriesActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar_stories);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarStoriesTitle = findViewById(R.id.toolbar_stories_title);
        toolbar.setTitle("");

        txtStoryTitle = findViewById(R.id.txt_story_title);
        txtStory = findViewById(R.id.txt_story);
        fabShare = findViewById(R.id.fab_share);
        imgTopic = findViewById(R.id.img_topic);

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
        loadStory(toolbarStoriesTitle);
    }

    private void loadStory(TextView toolbarStoriesTitle) {
        DatabaseExecutor.execute(() -> {
            if ("from_latest".equals(flag)) {
                LatestStoryModel latestStory = db.getLatestStoryById(selectedTopicId);
                if (latestStory != null) {
                    selectedTopicName = latestStory.getTopic_name();
                    selectedTopicStory = latestStory.getTopic_story();
                    imageBytes = latestStory.getTopic_image();
                }
            } else {
                TopicsModel topic = db.getTopicById(selectedTopicId);
                if (topic != null) {
                    selectedCatId = topic.getCat_id();
                    selectedTopicName = topic.getTopic_name();
                    selectedTopicStory = topic.getTopic_story();
                    imageBytes = topic.getTopic_image();
                }
                selectedCatName = db.getCategoryName(selectedCatId);
            }
            if (selectedTopicId != null && selectedTopicId != 0) {
                db.updateLastViewed(selectedTopicId,
                        new Timestamp(System.currentTimeMillis()).toString());
            }
            return selectedTopicName != null;
        }, found -> {
            if (!found || isFinishing() || isDestroyed()) {
                return;
            }
            toolbarStoriesTitle.setText("from_latest".equals(flag)
                    ? "Cerita Hindu" : selectedCatName);
            txtStory.setText(selectedTopicStory);
            txtStoryTitle.setText(selectedTopicName);
            if (imageBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imgTopic.setImageBitmap(bitmap);
            }
        }, error -> finish());
    }

    private void showFavorite(boolean favorite) {
        if (navFavorite == null) {
            return;
        }
        if (favorite) {
            navFavorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.favorite_select));
            navFavorite.setContentDescription(getString(R.string.action_remove_favorite));
        } else {
            navFavorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.favorite_unselect));
            navFavorite.setContentDescription(getString(R.string.action_add_favorite));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stories, menu);
        MenuItem item = menu.findItem(R.id.nav_stories_fav);

        if ("from_latest".equals(flag)) {
            item.setVisible(false);
        }
        navFavorite = (ImageView) menu.findItem(R.id.nav_stories_fav).getActionView();
        DatabaseExecutor.execute(() -> db.isFavorite(selectedTopicId), this::showFavorite,
                error -> showFavorite(false));
        navFavorite.setOnClickListener(view -> showCollectionPicker());
        return true;
    }

    private void showCollectionPicker() {
        DatabaseExecutor.execute(() -> {
            Map<Long, String> collections = db.getFavoriteCollections();
            ArrayList<Long> selected = db.getCollectionIdsForTopic(selectedTopicId);
            return new Object[]{collections, selected};
        }, data -> {
            @SuppressWarnings("unchecked") Map<Long, String> collections =
                    (LinkedHashMap<Long, String>) data[0];
            @SuppressWarnings("unchecked") ArrayList<Long> selected = (ArrayList<Long>) data[1];
            Long[] ids = collections.keySet().toArray(new Long[0]);
            String[] names = collections.values().toArray(new String[0]);
            boolean[] checked = new boolean[ids.length];
            for (int i = 0; i < ids.length; i++) checked[i] = selected.contains(ids[i]);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.favorite_collections)
                    .setMultiChoiceItems(names, checked, (d, which, value) -> checked[which] = value)
                    .setPositiveButton(R.string.save, (d, which) -> DatabaseExecutor.execute(() -> {
                        for (int i = 0; i < ids.length; i++) {
                            if (checked[i]) db.addTopicToCollection(ids[i], selectedTopicId);
                            else db.removeTopicFromCollection(ids[i], selectedTopicId);
                        }
                        return db.isFavorite(selectedTopicId);
                    }, favorite -> { showFavorite(favorite); Snackbar.make(navFavorite,
                            R.string.saved, Snackbar.LENGTH_SHORT).show(); }, error -> {}))
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.new_collection, null).create();
            dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                    .setOnClickListener(v -> showCreateCollectionDialog(this::showCollectionPicker)));
            dialog.show();
        }, error -> Snackbar.make(navFavorite, R.string.update_invalid, Snackbar.LENGTH_LONG).show());
    }

    private void showCreateCollectionDialog(Runnable afterCreated) {
        EditText input = new EditText(this); input.setHint(R.string.collection_name);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.new_collection)
                .setView(input).setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) { input.setError(getString(R.string.collection_name)); return; }
                    DatabaseExecutor.execute(() -> db.createFavoriteCollection(name), id -> {
                        if (id > 0) { dialog.dismiss(); afterCreated.run(); }
                        else input.setError(getString(R.string.collection_name));
                    }, error -> input.setError(getString(R.string.collection_name)));
                }));
        dialog.show();
    }

    private void showNoteDialog() {
        DatabaseExecutor.execute(() -> db.getTopicNote(selectedTopicId), note -> {
            EditText input = new EditText(this); input.setHint(R.string.personal_note_hint);
            input.setMinLines(4); input.setText(note); input.setSelection(input.length());
            new AlertDialog.Builder(this).setTitle(R.string.personal_note).setView(input)
                    .setPositiveButton(R.string.save, (d, w) -> DatabaseExecutor.execute(
                            () -> db.saveTopicNote(selectedTopicId, input.getText().toString()),
                            saved -> Snackbar.make(txtStory, R.string.saved, Snackbar.LENGTH_SHORT).show(),
                            error -> {})).setNegativeButton(R.string.cancel, null).show();
        }, error -> {});
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
        if (id == R.id.nav_stories_note) {
            showNoteDialog();
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
