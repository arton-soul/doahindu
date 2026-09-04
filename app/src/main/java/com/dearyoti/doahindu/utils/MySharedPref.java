package com.dearyoti.doahindu.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.model.TopicsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MySharedPref {

    public static final String PREFS_NAME = "STORIES_PREFS";
    public static final String PREFS_KEY_GETSTORIES = "STORIES_DATA";
    public static final String PREFS_KEY_SAVEFAVORITE = "STORIES_FAVORITE";
    public static final String PREFS_KEY_SAVERECENT = "STORIES_RECENT";
    private DatabaseHelper db;

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


    public void saveFavorites(Context context, List<Integer> favorites) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(PREFS_KEY_SAVEFAVORITE, jsonFavorites);

        editor.apply();
    }

    public void saveRecentViewed(Context context, ArrayList<TopicsModel> recents) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonRecents = gson.toJson(recents);

        editor.putString(PREFS_KEY_SAVERECENT, jsonRecents);
        editor.apply();
    }

    public void setFavoriteTopicId(Context context, DatabaseHelper db) {
        ArrayList<TopicsModel> favorites = db.getFavoriteTopics();
        if (favorites == null) {
            favorites = new ArrayList<TopicsModel>();
        }
        ArrayList<Integer> favoriteId = new ArrayList<>();
        for (int i = 0; i < favorites.size(); i++) {
            favoriteId.add(favorites.get(i).getTopic_id());
        }
        saveFavorites(context, favoriteId);
    }

    public void setRecentViewedTopicId(Context context, DatabaseHelper db) {
        ArrayList<TopicsModel> recents = db.getRecentViewed();
        if (recents == null) {
            recents = new ArrayList<TopicsModel>();
        }
        saveRecentViewed(context, recents);
    }

    public ArrayList<Integer> getFavorites(Context context) {
        SharedPreferences settings;
        List<Integer> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(PREFS_KEY_SAVEFAVORITE)) {
            String jsonFavorites = settings.getString(PREFS_KEY_SAVEFAVORITE, null);
            Gson gson = new Gson();
            Integer[] favoriteItems = gson.fromJson(jsonFavorites,
                    Integer[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<Integer>(favorites);
        } else
            return null;

        return (ArrayList<Integer>) favorites;
    }

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
