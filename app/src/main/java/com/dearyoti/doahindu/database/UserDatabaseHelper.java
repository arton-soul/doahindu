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
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_FAVORITE = "user_favorite";
    private static final String TABLE_RECENT = "user_recent";
    private static final String TABLE_COLLECTION = "favorite_collection";
    private static final String TABLE_COLLECTION_ITEM = "favorite_collection_item";
    private static final String TABLE_NOTE = "user_note";
    static final long DEFAULT_COLLECTION_ID = 1L;

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
        createCollectionAndNoteTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createCollectionAndNoteTables(db);
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_COLLECTION_ITEM
                    + " (collection_id, topic_id, created_at) SELECT "
                    + DEFAULT_COLLECTION_ID + ", topic_id, created_at FROM " + TABLE_FAVORITE);
        }
    }

    private void createCollectionAndNoteTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COLLECTION
                + " (collection_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE,"
                + " created_at INTEGER NOT NULL)");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_COLLECTION
                + " (collection_id, name, created_at) VALUES (" + DEFAULT_COLLECTION_ID
                + ", 'Favorit', 0)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COLLECTION_ITEM
                + " (collection_id INTEGER NOT NULL, topic_id INTEGER NOT NULL, created_at INTEGER NOT NULL,"
                + " PRIMARY KEY (collection_id, topic_id), FOREIGN KEY (collection_id) REFERENCES "
                + TABLE_COLLECTION + "(collection_id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX IF NOT EXISTS index_collection_item_topic ON "
                + TABLE_COLLECTION_ITEM + " (topic_id)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTE
                + " (topic_id INTEGER PRIMARY KEY NOT NULL, note TEXT NOT NULL, updated_at INTEGER NOT NULL)");
    }

    boolean setFavorite(int topicId, boolean favorite) {
        SQLiteDatabase db = getWritableDatabase();
        if (!favorite) {
            db.delete(TABLE_FAVORITE, "topic_id = ?", new String[]{String.valueOf(topicId)});
            return removeFromCollection(DEFAULT_COLLECTION_ID, topicId);
        }
        ContentValues values = new ContentValues();
        values.put("topic_id", topicId);
        values.put("created_at", System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_FAVORITE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return addToCollection(DEFAULT_COLLECTION_ID, topicId);
    }

    boolean isFavorite(int topicId) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_COLLECTION_ITEM,
                new String[]{"topic_id"}, "topic_id = ?", new String[]{String.valueOf(topicId)},
                null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    ArrayList<Integer> getFavoriteIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        return getFavoriteIds(DEFAULT_COLLECTION_ID);
    }

    ArrayList<Integer> getFavoriteIds(long collectionId) {
        ArrayList<Integer> ids = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_COLLECTION_ITEM,
                new String[]{"topic_id"}, "collection_id = ?",
                new String[]{String.valueOf(collectionId)}, null, null, "created_at DESC")) {
            while (cursor.moveToNext()) {
                ids.add(cursor.getInt(0));
            }
        }
        return ids;
    }

    Map<Long, String> getCollections() {
        Map<Long, String> collections = new LinkedHashMap<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_COLLECTION,
                new String[]{"collection_id", "name"}, null, null, null, null,
                "CASE WHEN collection_id = 1 THEN 0 ELSE 1 END, name COLLATE NOCASE")) {
            while (cursor.moveToNext()) collections.put(cursor.getLong(0), cursor.getString(1));
        }
        return collections;
    }

    long createCollection(String name) {
        ContentValues values = new ContentValues();
        values.put("name", name.trim()); values.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insert(TABLE_COLLECTION, null, values);
    }

    boolean renameCollection(long id, String name) {
        if (id == DEFAULT_COLLECTION_ID) return false;
        ContentValues values = new ContentValues(); values.put("name", name.trim());
        return getWritableDatabase().update(TABLE_COLLECTION, values, "collection_id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    boolean deleteCollection(long id) {
        return id != DEFAULT_COLLECTION_ID && getWritableDatabase().delete(TABLE_COLLECTION,
                "collection_id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    boolean addToCollection(long collectionId, int topicId) {
        ContentValues values = new ContentValues(); values.put("collection_id", collectionId);
        values.put("topic_id", topicId); values.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insertWithOnConflict(TABLE_COLLECTION_ITEM, null, values,
                SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    boolean removeFromCollection(long collectionId, int topicId) {
        return getWritableDatabase().delete(TABLE_COLLECTION_ITEM,
                "collection_id = ? AND topic_id = ?",
                new String[]{String.valueOf(collectionId), String.valueOf(topicId)}) > 0;
    }

    ArrayList<Long> getCollectionIdsForTopic(int topicId) {
        ArrayList<Long> ids = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_COLLECTION_ITEM,
                new String[]{"collection_id"}, "topic_id = ?", new String[]{String.valueOf(topicId)},
                null, null, "collection_id")) { while (cursor.moveToNext()) ids.add(cursor.getLong(0)); }
        return ids;
    }

    String getNote(int topicId) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_NOTE, new String[]{"note"},
                "topic_id = ?", new String[]{String.valueOf(topicId)}, null, null, null, "1")) {
            return cursor.moveToFirst() ? cursor.getString(0) : "";
        }
    }

    boolean saveNote(int topicId, String note) {
        if (note.trim().isEmpty()) return getWritableDatabase().delete(TABLE_NOTE, "topic_id = ?",
                new String[]{String.valueOf(topicId)}) >= 0;
        ContentValues values = new ContentValues(); values.put("topic_id", topicId);
        values.put("note", note.trim()); values.put("updated_at", System.currentTimeMillis());
        return getWritableDatabase().insertWithOnConflict(TABLE_NOTE, null, values,
                SQLiteDatabase.CONFLICT_REPLACE) != -1;
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
