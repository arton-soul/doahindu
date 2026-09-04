package com.dearyoti.doahindu.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dearyoti.doahindu.model.CategoryModel;
import com.dearyoti.doahindu.model.LatestStoryModel;
import com.dearyoti.doahindu.model.TopicsModel;
import com.dearyoti.doahindu.utils.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.dearyoti.doahindu.utils.Constant.DB_NAME;

public class DatabaseHelper extends SQLiteOpenHelper {

    // The Android's default system path
    // of your application database.
    private static String DB_PATH = "";
    private final Context myContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
        DB_PATH = myContext.getDatabasePath(DB_NAME)
                .toString();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    //copy database from assets folder (.sqlite) file to an empty database
    public void copyDataBase() throws IOException {
        File databaseFile = myContext.getDatabasePath(DB_NAME);
        if (databaseFile.exists()) {
            return;
        }

        File databaseDirectory = databaseFile.getParentFile();
        if (databaseDirectory != null && !databaseDirectory.exists()
                && !databaseDirectory.mkdirs()) {
            throw new IOException("Unable to create database directory");
        }

        try (InputStream myInput = myContext.getAssets().open(DB_NAME);
             OutputStream myOutput = new FileOutputStream(databaseFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public String getCategoryName(Integer cat_id) {
        String selectQuery = "SELECT " + Constant.TBL_CATEGORY_COLUMN_NAME + " FROM " + Constant.TBL_CATEGORY
                + " WHERE " + Constant.TBL_CATEGORY_COLUMN_ID + "=" + cat_id;

        String cat_name = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            Log.d("isFavorite : ", "" + cursor.getInt(0));
            cat_name = cursor.getString(0);
        }
        cursor.close();
        db.close();

        return cat_name;
    }


    public ArrayList<TopicsModel> getSearchTopics(Integer selected_cat_id, String searched_topic_name) {
        ArrayList<TopicsModel> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + Constant.TBL_TOPICS +
                " WHERE " + Constant.TBL_CATEGORY_COLUMN_ID + "=" + selected_cat_id + " AND " +
                Constant.TBL_TOPIC_COLUMN_NAME + " LIKE '%" + searched_topic_name + "%'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Integer topic_id = cursor.getInt(0);
                Integer topic_cat_id = cursor.getInt(1);
                String topic_name = cursor.getString(2);
                byte[] topic_image = cursor.getBlob(3);
                String topic_story = cursor.getString(4);
                Boolean is_topic_fav = cursor.getInt(5) > 0;
                String last_viewed = cursor.getString(6);
                TopicsModel topicsModel = new TopicsModel(topic_id, topic_cat_id, topic_name, topic_image, topic_story, is_topic_fav, last_viewed);
                list.add(topicsModel);//adding 2nd column data
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        return list;
    }

    public boolean updateFavorite(Integer topic_id, Integer is_fav) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(Constant.TBL_TOPIC_COLUMN_ISFAVORITE, is_fav);
        return db.update(Constant.TBL_TOPICS, args,
                Constant.TBL_TOPIC_COLUMN_ID + "=" + topic_id, null) > 0;
    }

    public boolean updateLastViewed(Integer topic_id, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(Constant.TBL_TOPIC_COLUMN_LASTVIEWED, timestamp);
        return db.update(Constant.TBL_TOPICS, args,
                Constant.TBL_TOPIC_COLUMN_ID + "=" + topic_id, null) > 0;
    }

    public ArrayList<TopicsModel> getRecentViewed() {
        ArrayList<TopicsModel> list = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + Constant.TBL_TOPICS +
                " ORDER BY " + Constant.TBL_TOPIC_COLUMN_LASTVIEWED + " DESC LIMIT 10";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);//selectQuery,selectedArguments

        if (cursor.moveToFirst()) {
            do {
                Integer topic_id = cursor.getInt(0);
                Integer topic_cat_id = cursor.getInt(1);
                String topic_name = cursor.getString(2);
                byte[] topic_image = cursor.getBlob(3);
                String topic_story = cursor.getString(4);
                Boolean is_topic_fav = cursor.getInt(5) > 0;
                String last_viewed = cursor.getString(6);
                TopicsModel topicsModel = new TopicsModel(topic_id, topic_cat_id, topic_name, topic_image, topic_story, is_topic_fav, last_viewed);
                list.add(topicsModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<TopicsModel> getFavoriteTopics() {
        ArrayList<TopicsModel> list = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + Constant.TBL_TOPICS +
                " WHERE " + Constant.TBL_TOPIC_COLUMN_ISFAVORITE + " = 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);//selectQuery,selectedArguments

        if (cursor.moveToFirst()) {
            do {
                Integer topic_id = cursor.getInt(0);
                Integer topic_cat_id = cursor.getInt(1);
                String topic_name = cursor.getString(2);
                byte[] topic_image = cursor.getBlob(3);
                String topic_story = cursor.getString(4);
                Boolean is_topic_fav = cursor.getInt(5) > 0;
                String last_viewed = cursor.getString(6);
                TopicsModel topicsModel = new TopicsModel(topic_id, topic_cat_id, topic_name, topic_image, topic_story, is_topic_fav, last_viewed);
                list.add(topicsModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Boolean isFavorite(Integer topic_id) {
        String selectQuery = "SELECT " + Constant.TBL_TOPIC_COLUMN_ISFAVORITE + " FROM " + Constant.TBL_TOPICS
                + " WHERE " + Constant.TBL_TOPIC_COLUMN_ID + "=" + topic_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Boolean isFav = false;
        if (cursor.moveToFirst()) {
            Log.d("isFavorite : ", "" + cursor.getInt(0));
            if (cursor.getInt(0) > 0) {
                isFav = true;
            }
        }
        cursor.close();
        db.close();

        return isFav;
    }

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
        db.close();
        return list;
    }

    public ArrayList<TopicsModel> getAllTopicsByCategory(Integer cat_id) {
        ArrayList<TopicsModel> list = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + Constant.TBL_TOPICS + " WHERE " + Constant.TBL_CATEGORY_COLUMN_ID + " = " + cat_id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Integer topic_id = cursor.getInt(0);
                Integer topic_cat_id = cursor.getInt(1);
                String topic_name = cursor.getString(2);
                byte[] topic_image = cursor.getBlob(3);
                String topic_story = cursor.getString(4);
                Boolean is_topic_fav = cursor.getInt(5) > 0;
                String last_viewed = cursor.getString(6);
                TopicsModel topicsModel = new TopicsModel(topic_id, topic_cat_id, topic_name, topic_image, topic_story, is_topic_fav, last_viewed);
                list.add(topicsModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
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
        db.close();
        return list;
    }
}
