package com.dearyoti.doahindu.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ReadingPreferences {
    private static final String PREFS = "READING_PREFERENCES";
    private static final String TEXT_SIZE = "text_size_sp";
    private static final String LINE_SPACING = "line_spacing";
    private static final String THEME = "theme_mode";
    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String SCROLL_PREFIX = "scroll_";

    private final SharedPreferences preferences;

    public ReadingPreferences(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getTextSizeSp() { return preferences.getInt(TEXT_SIZE, 16); }
    public float getLineSpacing() { return preferences.getFloat(LINE_SPACING, 1.0f); }
    public boolean keepScreenOn() { return preferences.getBoolean(KEEP_SCREEN_ON, false); }
    public int getThemeMode() {
        return preferences.getInt(THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void save(int textSizeSp, float lineSpacing, int themeMode,
                     boolean keepScreenOn) {
        preferences.edit().putInt(TEXT_SIZE, textSizeSp)
                .putFloat(LINE_SPACING, lineSpacing).putInt(THEME, themeMode)
                .putBoolean(KEEP_SCREEN_ON, keepScreenOn).apply();
    }

    public int getScrollPosition(String contentKey) {
        return preferences.getInt(SCROLL_PREFIX + contentKey, 0);
    }

    public void saveScrollPosition(String contentKey, int position) {
        preferences.edit().putInt(SCROLL_PREFIX + contentKey, Math.max(position, 0)).apply();
    }

    public static void applyTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(new ReadingPreferences(context).getThemeMode());
    }
}
