package com.dearyoti.doahindu.ads;

import android.app.Activity;

import com.dearyoti.doahindu.MyApplication;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public class AdsConsentManager {
    private final Activity activity;
    private final ConsentInformation consentInformation;

    public AdsConsentManager(Activity activity) {
        this.activity = activity;
        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
    }

    public void gatherConsent(Runnable completion) {
        ConsentRequestParameters parameters = new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(activity, parameters,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity,
                        formError -> finish(completion)),
                requestConsentError -> finish(completion));
    }

    private void finish(Runnable completion) {
        if (consentInformation.canRequestAds()) {
            ((MyApplication) activity.getApplication()).initializeAds();
        }
        completion.run();
    }

    public boolean isPrivacyOptionsRequired() {
        return consentInformation.getPrivacyOptionsRequirementStatus()
                == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    public void showPrivacyOptions(Runnable completion) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> {
            if (consentInformation.canRequestAds()) {
                ((MyApplication) activity.getApplication()).initializeAds();
            }
            completion.run();
        });
    }
}
