package com.dearyoti.doahindu.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.utils.Constant;

public class SplashActivity extends AppCompatActivity {
    private static final int DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView name = (TextView) findViewById(R.id.name);
        Typeface font = Typeface.createFromAsset(getAssets(), Constant.FONT_PATH_SEMIBOLD);
        name.setTypeface(font);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, DURATION);
    }
}
