package com.dearyoti.doahindu;

import android.app.Application;

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

    @Override
    public void onCreate() {
        super.onCreate();
        ReadingPreferences.applyTheme(this);
        MobileAds.initialize(this, initializationStatus -> { });
        loadInterstiallAds();
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
