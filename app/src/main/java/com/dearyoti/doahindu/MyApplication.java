package com.dearyoti.doahindu;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import com.dearyoti.doahindu.utils.ReadingPreferences;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


public class MyApplication extends Application {

    public static InterstitialAd interstitialAd;
    private static boolean adsInitialized;
    private static final String AD_PREFS = "AD_PREFERENCES";
    private static final String READING_EXIT_COUNT = "reading_exit_count";

    @Override
    public void onCreate() {
        super.onCreate();
        ReadingPreferences.applyTheme(this);
    }

    public synchronized void initializeAds() {
        if (adsInitialized) return;
        adsInitialized = true;
        MobileAds.initialize(this, initializationStatus -> { });
        loadInterstiallAds();
    }

    public static boolean areAdsInitialized() {
        return adsInitialized;
    }

    public static boolean shouldShowInterstitial(Context context) {
        int count = context.getSharedPreferences(AD_PREFS, MODE_PRIVATE)
                .getInt(READING_EXIT_COUNT, 0) + 1;
        context.getSharedPreferences(AD_PREFS, MODE_PRIVATE).edit()
                .putInt(READING_EXIT_COUNT, count).apply();
        return count % 5 == 0;
    }

    private void loadInterstiallAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getResources().getString(R.string.interstiall_ids), adRequest, new InterstitialAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAds) {
                // The mInterstitialAd reference will be null until
                interstitialAd = interstitialAds;
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {

                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        interstitialAd = null;
                        loadInterstiallAds();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                    }
                });

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                interstitialAd = null;
            }
        });

    }
}
