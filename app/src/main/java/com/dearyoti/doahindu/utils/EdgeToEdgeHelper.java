package com.dearyoti.doahindu.utils;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public final class EdgeToEdgeHelper {

    private EdgeToEdgeHelper() {
    }

    public static void apply(Activity activity, View rootView) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        final int initialLeft = rootView.getPaddingLeft();
        final int initialTop = rootView.getPaddingTop();
        final int initialRight = rootView.getPaddingRight();
        final int initialBottom = rootView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom + systemBars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(rootView);
    }
}
