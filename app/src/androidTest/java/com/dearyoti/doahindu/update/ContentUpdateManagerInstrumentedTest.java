package com.dearyoti.doahindu.update;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.model.CategoryModel;
import com.dearyoti.doahindu.model.TopicsModel;
import com.dearyoti.doahindu.utils.Constant;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ContentUpdateManagerInstrumentedTest {

    @Test
    public void reportsMissingConfigurationAndRejectsNonHttpsSource() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AtomicReference<ContentUpdateManager.Result> result = new AtomicReference<>();
        new ContentUpdateManager(context, "").checkForUpdate(true, result::set);
        assertEquals(ContentUpdateManager.Result.CONFIGURATION_REQUIRED, result.get());

        CountDownLatch callback = new CountDownLatch(1);
        new ContentUpdateManager(context, "http://example.com/manifest.json")
                .checkForUpdate(true, updateResult -> {
                    result.set(updateResult);
                    callback.countDown();
                });
        assertTrue(callback.await(5, TimeUnit.SECONDS));
        assertEquals(ContentUpdateManager.Result.NETWORK_ERROR, result.get());
    }

    @Test
    public void validatesChecksumAndRollsBackFailedReplacement() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(Constant.DB_NAME);
        context.deleteDatabase("doahindu_user.sqlite");
        context.getSharedPreferences("DATABASE_MIGRATIONS", Context.MODE_PRIVATE)
                .edit().clear().commit();

        DatabaseHelper database = new DatabaseHelper(context);
        database.copyDataBase();
        ArrayList<CategoryModel> categories = database.getAllCategories();
        ArrayList<TopicsModel> topics = database
                .getAllTopicsByCategory(categories.get(0).getCat_id());
        int favoriteId = topics.get(0).getTopic_id();
        assertTrue(database.updateFavorite(favoriteId, 1));
        database.close();

        File active = context.getDatabasePath(Constant.DB_NAME);
        File candidate = new File(context.getCacheDir(), "valid-content.sqlite");
        copy(active, candidate);

        ContentUpdateManager updater = new ContentUpdateManager(context);
        assertTrue(updater.validateDatabase(candidate, 1));
        assertFalse(updater.validateDatabase(candidate, 2));
        assertTrue(updater.hasExpectedChecksum(candidate, sha256(candidate)));
        assertFalse(updater.hasExpectedChecksum(candidate,
                "0000000000000000000000000000000000000000000000000000000000000000"));

        assertTrue(updater.replaceAtomically(candidate));
        assertTrue(context.getDatabasePath(Constant.DB_NAME + ".backup").exists());
        DatabaseHelper replacedDatabase = new DatabaseHelper(context);
        assertTrue(replacedDatabase.isFavorite(favoriteId));
        replacedDatabase.close();

        File missingCandidate = new File(context.getCacheDir(), "missing-content.sqlite");
        assertFalse(updater.replaceAtomically(missingCandidate));
        assertTrue(active.exists());
        DatabaseHelper rolledBackDatabase = new DatabaseHelper(context);
        assertTrue(rolledBackDatabase.isFavorite(favoriteId));
        rolledBackDatabase.close();
    }

    private static void copy(File source, File destination) throws Exception {
        try (InputStream input = new FileInputStream(source);
             FileOutputStream output = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            output.getFD().sync();
        }
    }

    private static String sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                digest.update(buffer, 0, count);
            }
        }
        StringBuilder result = new StringBuilder();
        for (byte value : digest.digest()) {
            result.append(String.format(Locale.US, "%02x", value));
        }
        return result.toString();
    }
}
