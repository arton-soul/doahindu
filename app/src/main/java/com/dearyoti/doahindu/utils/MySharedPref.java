package com.dearyoti.doahindu.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dearyoti.doahindu.model.TopicsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MySharedPref {

    public static final String PREFS_NAME = "STORIES_PREFS";
    public static final String PREFS_KEY_GETSTORIES = "STORIES_DATA";
    public static final String PREFS_KEY_SAVEFAVORITE = "STORIES_FAVORITE";
    public static final String PREFS_KEY_SAVERECENT = "STORIES_RECENT";

    public MySharedPref() {
        super();
    }

    public void setFirst(Context context, Boolean isFirst) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putBoolean(PREFS_KEY_GETSTORIES, isFirst);

        editor.apply();
    }


    /** Reads the old preference format once during database migration. */
    public ArrayList<Integer> getFavorites(Context context) {
        SharedPreferences settings;
        List<Integer> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(PREFS_KEY_SAVEFAVORITE)) {
            String jsonFavorites = settings.getString(PREFS_KEY_SAVEFAVORITE, null);
            Gson gson = new Gson();
            Type type = new TypeToken<List<Integer>>() { }.getType();
            favorites = gson.fromJson(jsonFavorites, type);
            if (favorites == null) {
                return null;
            }
            favorites = new ArrayList<>(favorites);
        } else
            return null;

        return new ArrayList<>(favorites);
    }

    /** Reads the old full-object preference format once during database migration. */
    public ArrayList<TopicsModel> getRecentViewed(Context context) {
        SharedPreferences settings;
        ArrayList<TopicsModel> recents = new ArrayList<>();

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(PREFS_KEY_SAVERECENT)) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<TopicsModel>>() {
            }.getType();
            recents = gson.fromJson(settings.getString(PREFS_KEY_SAVERECENT, null), type);
        } else {
            return null;
        }

        return recents;
    }
}
