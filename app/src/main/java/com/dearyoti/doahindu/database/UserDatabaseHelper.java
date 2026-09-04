package com.dearyoti.doahindu.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Stores data owned by the user separately from replaceable prayer content. */
final class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "doahindu_user.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_FAVORITE = "user_favorite";
    private static final String TABLE_RECENT = "user_recent";

    UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FAVORITE
                + " (topic_id INTEGER PRIMARY KEY NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_RECENT
                + " (topic_id INTEGER PRIMARY KEY NOT NULL, last_viewed TEXT NOT NULL)");
        db.execSQL("CREATE INDEX index_user_recent_last_viewed ON " + TABLE_RECENT
                + " (last_viewed DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Future versions must use explicit, non-destructive migrations.
    }

    boolean setFavorite(int topicId, boolean favorite) {
        SQLiteDatabase db = getWritableDatabase();
        if (!favorite) {
            return db.delete(TABLE_FAVORITE, "topic_id = ?",
                    new String[]{String.valueOf(topicId)}) > 0;
        }
        ContentValues values = new ContentValues();
        values.put("topic_id", topicId);
        values.put("created_at", System.currentTimeMillis());
        return db.insertWithOnConflict(TABLE_FAVORITE, null, values,
                SQLiteDatabase.CONFLICT_REPLACE) != -1;
    }

    boolean isFavorite(int topicId) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_FAVORITE,
                new String[]{"topic_id"}, "topic_id = ?",
                new String[]{String.valueOf(topicId)}, null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    ArrayList<Integer> getFavoriteIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_FAVORITE,
                new String[]{"topic_id"}, null, null, null, null, "created_at DESC")) {
            while (cursor.moveToNext()) {
                ids.add(cursor.getInt(0));
            }
        }
        return ids;
    }

    boolean setLastViewed(int topicId, String timestamp) {
        ContentValues values = new ContentValues();
        values.put("topic_id", topicId);
        values.put("last_viewed", timestamp);
        return getWritableDatabase().insertWithOnConflict(TABLE_RECENT, null, values,
                SQLiteDatabase.CONFLICT_REPLACE) != -1;
    }

    Map<Integer, String> getRecentItems(int limit) {
        Map<Integer, String> items = new LinkedHashMap<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_RECENT,
                new String[]{"topic_id", "last_viewed"}, null, null, null, null,
                "last_viewed DESC", String.valueOf(limit))) {
            while (cursor.moveToNext()) {
                items.put(cursor.getInt(0), cursor.getString(1));
            }
        }
        return items;
    }
}
