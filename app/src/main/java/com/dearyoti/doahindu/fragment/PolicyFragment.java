package com.dearyoti.doahindu.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.activity.MainActivity;


public class PolicyFragment extends Fragment {

    private static final String ASSET_URL_PREFIX = "file:///android_asset/";

    private View view;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_policy, container, false);
        init();
        return view;
    }

    public void init() {
        progressBar = view.findViewById(R.id.progressBar);

        WebView webview = view.findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(false);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("file:///android_asset/privacy_policy.html");

        WebSettings settings = webview.getSettings();
        settings.setDefaultFontSize(16);
    }

    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleNavigation(request.getUrl());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleNavigation(Uri.parse(url));
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }

    private boolean handleNavigation(Uri uri) {
        if (uri.toString().startsWith(ASSET_URL_PREFIX)) {
            return false;
        }

        String host = uri.getHost();
        boolean allowedHttps = "https".equals(uri.getScheme()) && ("www.google.com".equals(host)
                || "policies.google.com".equals(host) || "support.google.com".equals(host)
                || "firebase.google.com".equals(host));
        if (allowedHttps || "mailto".equals(uri.getScheme())) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (ActivityNotFoundException ignored) {
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setToolbarTitle(
                    getString(R.string.menu_privacy));

            ((MainActivity) requireActivity()).highLightNavigation(9,
                    getString(R.string.menu_privacy));

            if (((MainActivity) getActivity()).searchView != null) {
                ((MainActivity) getActivity()).searchView.setVisibility(View.GONE);
            }
        }
    }

}
