package com.dearyoti.doahindu.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.SharedPreferences;

import com.dearyoti.doahindu.model.CategoryModel;
import com.dearyoti.doahindu.model.LatestStoryModel;
import com.dearyoti.doahindu.model.TopicsModel;
import com.dearyoti.doahindu.utils.Constant;
import com.dearyoti.doahindu.utils.MySharedPref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static com.dearyoti.doahindu.utils.Constant.DB_NAME;

public class DatabaseHelper extends SQLiteOpenHelper {

    // The Android's default system path
    // of your application database.
    private static final String MIGRATION_PREFS = "DATABASE_MIGRATIONS";
    private static final String MIGRATION_USER_STATE_V1 = "user_state_v1";
    private static final Set<DatabaseHelper> OPEN_HELPERS = Collections.newSetFromMap(
            new WeakHashMap<>());
    private final Context myContext;
    private final UserDatabaseHelper userDatabase;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context.getApplicationContext();
        this.userDatabase = new UserDatabaseHelper(this.myContext);
        synchronized (OPEN_HELPERS) {
            OPEN_HELPERS.add(this);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_topics_category ON "
                    + Constant.TBL_TOPICS + " (" + Constant.TBL_TOPIC_CAT_COLUMN_ID + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_topics_name ON "
                    + Constant.TBL_TOPICS + " (" + Constant.TBL_TOPIC_COLUMN_NAME + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_latest_topic ON "
                    + Constant.TBL_LATEST + " (" + Constant.TBL_TOPIC_COLUMN_ID + ")");
        }
    }

    //copy database from assets folder (.sqlite) file to an empty database
    public void copyDataBase() throws IOException {
        File databaseFile = myContext.getDatabasePath(DB_NAME);
        if (!databaseFile.exists()) {
            File databaseDirectory = databaseFile.getParentFile();
            if (databaseDirectory != null && !databaseDirectory.exists()
                    && !databaseDirectory.mkdirs()) {
                throw new IOException("Unable to create database directory");
            }

            try (InputStream myInput = myContext.getAssets().open(DB_NAME);
                 OutputStream myOutput = new FileOutputStream(databaseFile)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
            }
        }
        migrateLegacyUserState();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    @Override
    public synchronized void close() {
        try {
            super.close();
            userDatabase.close();
        } finally {
            synchronized (OPEN_HELPERS) {
                OPEN_HELPERS.remove(this);
            }
        }
    }

    /** Closes every helper that may hold the replaceable content database open. */
    public static void closeAllInstances() {
        DatabaseHelper[] helpers;
        synchronized (OPEN_HELPERS) {
            helpers = OPEN_HELPERS.toArray(new DatabaseHelper[0]);
        }
        for (DatabaseHelper helper : helpers) {
            helper.close();
        }
    }

    public String getCategoryName(Integer cat_id) {
        String cat_name = "";
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(Constant.TBL_CATEGORY,
                     new String[]{Constant.TBL_CATEGORY_COLUMN_NAME},
                     Constant.TBL_CATEGORY_COLUMN_ID + " = ?",
                     new String[]{String.valueOf(cat_id)}, null, null, null, "1")) {
            if (cursor.moveToFirst()) {
                cat_name = cursor.getString(0);
            }
        }
        return cat_name;
    }


    public ArrayList<TopicsModel> getSearchTopics(Integer selected_cat_id, String searched_topic_name) {
        ArrayList<TopicsModel> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Constant.TBL_TOPICS
                + " WHERE " + Constant.TBL_CATEGORY_COLUMN_ID + " = ? AND "
                + Constant.TBL_TOPIC_COLUMN_NAME + " LIKE ? AND "
                + Constant.TBL_TOPIC_COLUMN_STORIES + " <> ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(selected_cat_id),
                "%" + searched_topic_name + "%", Constant.PLACEHOLDER_CONTENT});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Integer topic_id = cursor.getInt(0);
                Integer topic_cat_id = cursor.getInt(1);
                String topic_name = cursor.getString(2);
                byte[] topic_image = cursor.getBlob(3);
                String topic_story = cursor.getString(4);
                Boolean is_topic_fav = userDatabase.isFavorite(topic_id);
                String last_viewed = null;
                TopicsModel topicsModel = new TopicsModel(topic_id, topic_cat_id, topic_name, topic_image, topic_story, is_topic_fav, last_viewed);
                list.add(topicsModel);//adding 2nd column data
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        return list;
    }

    public boolean updateFavorite(Integer topic_id, Integer is_fav) {
        return userDatabase.setFavorite(topic_id, is_fav != null && is_fav > 0);
    }

    public boolean updateLastViewed(Integer topic_id, String timestamp) {
        return userDatabase.setLastViewed(topic_id, timestamp);
    }

    public ArrayList<TopicsModel> getRecentViewed() {
        ArrayList<TopicsModel> list = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : userDatabase.getRecentItems(10).entrySet()) {
            TopicsModel topic = getTopicById(entry.getKey());
            if (topic != null && !Constant.PLACEHOLDER_CONTENT.equals(topic.getTopic_story())) {
                topic.setLast_viewed(entry.getValue());
                list.add(topic);
            }
        }
        return list;
    }

    public ArrayList<TopicsModel> getFavoriteTopics() {
        return getFavoriteTopics(UserDatabaseHelper.DEFAULT_COLLECTION_ID);
    }

    public ArrayList<TopicsModel> getFavoriteTopics(long collectionId) {
        ArrayList<TopicsModel> list = new ArrayList<>();
        for (Integer topicId : userDatabase.getFavoriteIds(collectionId)) {
            TopicsModel topic = getTopicById(topicId);
            if (topic != null && !Constant.PLACEHOLDER_CONTENT.equals(topic.getTopic_story())) {
                list.add(topic);
            }
        }
        return list;
    }

    public Boolean isFavorite(Integer topic_id) {
        return userDatabase.isFavorite(topic_id);
    }

    public Map<Long, String> getFavoriteCollections() { return userDatabase.getCollections(); }
    public long createFavoriteCollection(String name) { return userDatabase.createCollection(name); }
    public boolean renameFavoriteCollection(long id, String name) { return userDatabase.renameCollection(id, name); }
    public boolean deleteFavoriteCollection(long id) { return userDatabase.deleteCollection(id); }
    public boolean addTopicToCollection(long id, int topicId) { return userDatabase.addToCollection(id, topicId); }
    public boolean removeTopicFromCollection(long id, int topicId) { return userDatabase.removeFromCollection(id, topicId); }
    public ArrayList<Long> getCollectionIdsForTopic(int topicId) { return userDatabase.getCollectionIdsForTopic(topicId); }
    public String getTopicNote(int topicId) { return userDatabase.getNote(topicId); }
    public boolean saveTopicNote(int topicId, String note) { return userDatabase.saveNote(topicId, note); }

    public ArrayList<CategoryModel> getAllCategories() {
        ArrayList<CategoryModel> list = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Constant.TBL_CATEGORY;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Integer cat_id = cursor.getInt(0);
                String cat_name = cursor.getString(1);
                byte[] cat_image = cursor.getBlob(2);
                CategoryModel categoryModel = new CategoryModel(cat_id, cat_name, cat_image);
                list.add(categoryModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public ArrayList<TopicsModel> getAllTopicsByCategory(Integer cat_id) {
        ArrayList<TopicsModel> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Constant.TBL_TOPICS + " WHERE "
                + Constant.TBL_CATEGORY_COLUMN_ID + " = ? AND "
                + Constant.TBL_TOPIC_COLUMN_STORIES + " <> ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(cat_id),
                Constant.PLACEHOLDER_CONTENT});

        if (cursor.moveToFirst()) {
            do {
                Integer topic_id = cursor.getInt(0);
                Integer topic_cat_id = cursor.getInt(1);
                String topic_name = cursor.getString(2);
                byte[] topic_image = cursor.getBlob(3);
                String topic_story = cursor.getString(4);
                Boolean is_topic_fav = userDatabase.isFavorite(topic_id);
                String last_viewed = null;
                TopicsModel topicsModel = new TopicsModel(topic_id, topic_cat_id, topic_name, topic_image, topic_story, is_topic_fav, last_viewed);
                list.add(topicsModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public ArrayList<LatestStoryModel> getLatestStory() {

        ArrayList<LatestStoryModel> list = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + Constant.TBL_LATEST;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Integer topic_id = cursor.getInt(0);
                String topic_name = cursor.getString(1);
                byte[] topic_image = cursor.getBlob(2);
                String topic_story = cursor.getString(3);
                LatestStoryModel latestModel = new LatestStoryModel(topic_id, topic_name, topic_image, topic_story);
                list.add(latestModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public TopicsModel getTopicById(Integer topicId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constant.TBL_TOPICS, null,
                     Constant.TBL_TOPIC_COLUMN_ID + " = ?",
                     new String[]{String.valueOf(topicId)}, null, null, null, "1")) {
            if (cursor.moveToFirst()) {
                return new TopicsModel(cursor.getInt(0), cursor.getInt(1), cursor.getString(2),
                        cursor.getBlob(3), cursor.getString(4), userDatabase.isFavorite(topicId), null);
            }
        }
        return null;
    }

    public LatestStoryModel getLatestStoryById(Integer topicId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constant.TBL_LATEST, null,
                     Constant.TBL_TOPIC_COLUMN_ID + " = ?",
                     new String[]{String.valueOf(topicId)}, null, null, null, "1")) {
            if (cursor.moveToFirst()) {
                return new LatestStoryModel(cursor.getInt(0), cursor.getString(1),
                        cursor.getBlob(2), cursor.getString(3));
            }
        }
        return null;
    }

    private void migrateLegacyUserState() {
        SharedPreferences migrations = myContext.getSharedPreferences(MIGRATION_PREFS,
                Context.MODE_PRIVATE);
        if (migrations.getBoolean(MIGRATION_USER_STATE_V1, false)) {
            return;
        }

        SQLiteDatabase contentDatabase = getReadableDatabase();
        try (Cursor cursor = contentDatabase.query(Constant.TBL_TOPICS,
                new String[]{Constant.TBL_TOPIC_COLUMN_ID,
                        Constant.TBL_TOPIC_COLUMN_ISFAVORITE,
                        Constant.TBL_TOPIC_COLUMN_LASTVIEWED},
                null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                int topicId = cursor.getInt(0);
                if (cursor.getInt(1) > 0) {
                    userDatabase.setFavorite(topicId, true);
                }
                String lastViewed = cursor.getString(2);
                if (lastViewed != null && !lastViewed.isEmpty()) {
                    userDatabase.setLastViewed(topicId, lastViewed);
                }
            }
        }

        MySharedPref legacyPreferences = new MySharedPref();
        List<Integer> favoriteIds = legacyPreferences.getFavorites(myContext);
        if (favoriteIds != null) {
            for (Integer topicId : favoriteIds) {
                if (topicId != null) {
                    userDatabase.setFavorite(topicId, true);
                }
            }
        }
        List<TopicsModel> recentTopics = legacyPreferences.getRecentViewed(myContext);
        if (recentTopics != null) {
            for (TopicsModel topic : recentTopics) {
                if (topic != null && topic.getTopic_id() != null
                        && topic.getLast_viewed() != null && !topic.getLast_viewed().isEmpty()) {
                    userDatabase.setLastViewed(topic.getTopic_id(), topic.getLast_viewed());
                }
            }
        }

        migrations.edit().putBoolean(MIGRATION_USER_STATE_V1, true).apply();
    }
}
