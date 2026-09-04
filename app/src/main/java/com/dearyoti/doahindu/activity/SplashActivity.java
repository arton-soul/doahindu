package com.dearyoti.doahindu.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.ads.AdsConsentManager;
import com.dearyoti.doahindu.utils.Constant;
import com.dearyoti.doahindu.utils.EdgeToEdgeHelper;

public class SplashActivity extends AppCompatActivity {
    private static final int DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EdgeToEdgeHelper.apply(this, findViewById(R.id.splash_root));

        TextView name = (TextView) findViewById(R.id.name);
        Typeface font = Typeface.createFromAsset(getAssets(), Constant.FONT_PATH_SEMIBOLD);
        name.setTypeface(font);

        long startedAt = System.currentTimeMillis();
        new AdsConsentManager(this).gatherConsent(() -> {
            long remaining = Math.max(0, DURATION - (System.currentTimeMillis() - startedAt));
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                int notificationTopicId = getIntent().getIntExtra("notification_topic_id", -1);
                String topicId = getIntent().getStringExtra("topic_id");
                if (notificationTopicId > 0) {
                    mainIntent.putExtra("notification_topic_id", notificationTopicId);
                } else if (topicId != null) {
                    try {
                        mainIntent.putExtra("notification_topic_id", Integer.parseInt(topicId));
                    } catch (NumberFormatException ignored) {
                    }
                }
                startActivity(mainIntent);
                finish();
            }, remaining);
        });
    }
}
