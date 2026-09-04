package com.dearyoti.doahindu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.model.CategoryModel;
import com.dearyoti.doahindu.model.TopicsModel;
import com.dearyoti.doahindu.utils.Constant;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void upgradesUserDatabaseV1IntoDefaultCollection() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DatabaseHelper.closeAllInstances();
        context.deleteDatabase("doahindu_user.sqlite");
        java.io.File file = context.getDatabasePath("doahindu_user.sqlite");
        file.getParentFile().mkdirs();
        try (SQLiteDatabase versionOne = SQLiteDatabase.openOrCreateDatabase(file, null)) {
            versionOne.execSQL("CREATE TABLE user_favorite "
                    + "(topic_id INTEGER PRIMARY KEY NOT NULL, created_at INTEGER NOT NULL)");
            versionOne.execSQL("CREATE TABLE user_recent "
                    + "(topic_id INTEGER PRIMARY KEY NOT NULL, last_viewed TEXT NOT NULL)");
            versionOne.execSQL("INSERT INTO user_favorite VALUES (42, 1000)");
            versionOne.setVersion(1);
        }
        DatabaseHelper upgraded = new DatabaseHelper(context);
        assertEquals("Favorit", upgraded.getFavoriteCollections().get(1L));
        assertTrue(upgraded.isFavorite(42));
        assertTrue(upgraded.getCollectionIdsForTopic(42).contains(1L));
        upgraded.close();
    }

    @Test
    public void migratesLegacyStateAndKeepsItWhenContentIsReplaced() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        appContext.deleteDatabase(Constant.DB_NAME);
        appContext.deleteDatabase("doahindu_user.sqlite");
        appContext.getSharedPreferences("DATABASE_MIGRATIONS", Context.MODE_PRIVATE)
                .edit().clear().commit();

        DatabaseHelper initialDatabase = new DatabaseHelper(appContext);
        initialDatabase.copyDataBase();
        ArrayList<CategoryModel> categories = initialDatabase.getAllCategories();
        assertFalse(categories.isEmpty());
        ArrayList<TopicsModel> topics = initialDatabase
                .getAllTopicsByCategory(categories.get(0).getCat_id());
        assertFalse(topics.isEmpty());
        int topicId = topics.get(0).getTopic_id();
        initialDatabase.close();

        ContentValues legacyState = new ContentValues();
        legacyState.put(Constant.TBL_TOPIC_COLUMN_ISFAVORITE, 1);
        legacyState.put(Constant.TBL_TOPIC_COLUMN_LASTVIEWED, "2026-09-04 12:00:00");
        try (SQLiteDatabase legacyDatabase = SQLiteDatabase.openDatabase(
                appContext.getDatabasePath(Constant.DB_NAME).getPath(), null,
                SQLiteDatabase.OPEN_READWRITE)) {
            assertEquals(1, legacyDatabase.update(Constant.TBL_TOPICS, legacyState,
                    Constant.TBL_TOPIC_COLUMN_ID + " = ?",
                    new String[]{String.valueOf(topicId)}));
        }

        appContext.deleteDatabase("doahindu_user.sqlite");
        appContext.getSharedPreferences("DATABASE_MIGRATIONS", Context.MODE_PRIVATE)
                .edit().clear().commit();
        DatabaseHelper migratedDatabase = new DatabaseHelper(appContext);
        migratedDatabase.copyDataBase();

        try (Cursor foreignKeys = migratedDatabase.getReadableDatabase()
                .rawQuery("PRAGMA foreign_keys", null)) {
            assertTrue(foreignKeys.moveToFirst());
            assertEquals(1, foreignKeys.getInt(0));
        }
        assertTrue(migratedDatabase.isFavorite(topicId));
        assertEquals("Favorit", migratedDatabase.getFavoriteCollections().get(1L));
        long ceremonyCollection = migratedDatabase.createFavoriteCollection("Upacara");
        assertTrue(ceremonyCollection > 1L);
        assertTrue(migratedDatabase.addTopicToCollection(ceremonyCollection, topicId));
        assertTrue(migratedDatabase.getCollectionIdsForTopic(topicId).contains(1L));
        assertTrue(migratedDatabase.getCollectionIdsForTopic(topicId).contains(ceremonyCollection));
        assertTrue(migratedDatabase.saveTopicNote(topicId, "Catatan pribadi pengujian"));
        assertEquals("Catatan pribadi pengujian", migratedDatabase.getTopicNote(topicId));
        assertEquals(topicId, migratedDatabase.getRecentViewed().get(0).getTopic_id().intValue());
        assertTrue(migratedDatabase.getSearchTopics(categories.get(0).getCat_id(),
                "%' OR 1=1 --").isEmpty());
        migratedDatabase.close();

        assertTrue(appContext.deleteDatabase(Constant.DB_NAME));
        DatabaseHelper replacedContentDatabase = new DatabaseHelper(appContext);
        replacedContentDatabase.copyDataBase();
        assertTrue(replacedContentDatabase.isFavorite(topicId));
        assertEquals("Upacara", replacedContentDatabase.getFavoriteCollections()
                .get(ceremonyCollection));
        assertTrue(replacedContentDatabase.getFavoriteTopics(ceremonyCollection).stream()
                .anyMatch(topic -> topic.getTopic_id() == topicId));
        assertEquals("Catatan pribadi pengujian", replacedContentDatabase.getTopicNote(topicId));
        assertTrue(replacedContentDatabase.deleteFavoriteCollection(ceremonyCollection));
        assertTrue(replacedContentDatabase.isFavorite(topicId));
        assertTrue(replacedContentDatabase.saveTopicNote(topicId, ""));
        assertEquals("", replacedContentDatabase.getTopicNote(topicId));
        assertEquals(topicId,
                replacedContentDatabase.getRecentViewed().get(0).getTopic_id().intValue());
        replacedContentDatabase.close();
    }
}
