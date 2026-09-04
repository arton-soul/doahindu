package com.dearyoti.doahindu.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dearyoti.doahindu.BuildConfig;
import com.dearyoti.doahindu.utils.Constant;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

public final class ContentUpdateManager {

    public enum Result {
        UPDATED,
        ALREADY_CURRENT,
        CONFIGURATION_REQUIRED,
        INCOMPATIBLE_APP,
        INVALID_PACKAGE,
        NETWORK_ERROR
    }

    public interface Callback {
        void onResult(Result result);
    }

    private static final String PREFS_NAME = "CONTENT_UPDATE_PREFS";
    private static final String KEY_CONTENT_VERSION = "content_version";
    private static final String KEY_LAST_AUTOMATIC_CHECK = "last_automatic_check";
    private static final long AUTOMATIC_CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L;
    private static final long MAX_DATABASE_BYTES = 25L * 1024L * 1024L;
    private static final int MAX_MANIFEST_BYTES = 64 * 1024;
    private static final int CONNECT_TIMEOUT_MS = 8_000;
    private static final int READ_TIMEOUT_MS = 15_000;
    private static final int EXPECTED_SCHEMA_VERSION = 1;
    private static final ExecutorService NETWORK_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private final Context context;
    private final SharedPreferences preferences;
    private final String manifestUrl;

    public ContentUpdateManager(Context context) {
        this(context, BuildConfig.CONTENT_MANIFEST_URL);
    }

    ContentUpdateManager(Context context, String manifestUrl) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.manifestUrl = manifestUrl;
    }

    public void checkForUpdate(boolean manual, Callback callback) {
        if (manifestUrl.isEmpty()) {
            callback.onResult(Result.CONFIGURATION_REQUIRED);
            return;
        }
        long now = System.currentTimeMillis();
        if (!manual && now - preferences.getLong(KEY_LAST_AUTOMATIC_CHECK, 0L)
                < AUTOMATIC_CHECK_INTERVAL_MS) {
            callback.onResult(Result.ALREADY_CURRENT);
            return;
        }
        NETWORK_EXECUTOR.execute(() -> {
            Result result = performUpdate();
            if (!manual) {
                preferences.edit().putLong(KEY_LAST_AUTOMATIC_CHECK,
                        System.currentTimeMillis()).apply();
            }
            MAIN_HANDLER.post(() -> callback.onResult(result));
        });
    }

    public long getInstalledContentVersion() {
        return preferences.getLong(KEY_CONTENT_VERSION, 1L);
    }

    private Result performUpdate() {
        File temporaryDatabase = new File(context.getCacheDir(), "content-update.sqlite.tmp");
        try {
            URL remoteManifest = requireHttps(manifestUrl);
            String manifestJson = downloadText(remoteManifest, MAX_MANIFEST_BYTES);
            ContentManifest manifest = new Gson().fromJson(manifestJson, ContentManifest.class);
            if (!isManifestValid(manifest)) {
                return Result.INVALID_PACKAGE;
            }
            if (manifest.minimumAppVersion > BuildConfig.VERSION_CODE) {
                return Result.INCOMPATIBLE_APP;
            }
            if (manifest.contentVersion <= getInstalledContentVersion()) {
                return Result.ALREADY_CURRENT;
            }
            downloadFile(requireHttps(manifest.databaseUrl), temporaryDatabase,
                    manifest.sizeBytes);
            if (!hasExpectedChecksum(temporaryDatabase, manifest.sha256)
                    || !validateDatabase(temporaryDatabase, manifest.schemaVersion)) {
                return Result.INVALID_PACKAGE;
            }
            if (!replaceAtomically(temporaryDatabase)) {
                return Result.INVALID_PACKAGE;
            }
            preferences.edit().putLong(KEY_CONTENT_VERSION, manifest.contentVersion).commit();
            return Result.UPDATED;
        } catch (IOException exception) {
            return Result.NETWORK_ERROR;
        } catch (RuntimeException | NoSuchAlgorithmException exception) {
            return Result.INVALID_PACKAGE;
        } finally {
            if (temporaryDatabase.exists()) {
                temporaryDatabase.delete();
            }
        }
    }

    private boolean isManifestValid(ContentManifest manifest) {
        return manifest != null
                && manifest.schemaVersion == EXPECTED_SCHEMA_VERSION
                && manifest.contentVersion > 0
                && manifest.minimumAppVersion > 0
                && manifest.publishedAt != null && !manifest.publishedAt.isEmpty()
                && manifest.databaseUrl != null && !manifest.databaseUrl.isEmpty()
                && manifest.sha256 != null && manifest.sha256.matches("(?i)[0-9a-f]{64}")
                && manifest.sizeBytes > 0 && manifest.sizeBytes <= MAX_DATABASE_BYTES;
    }

    private String downloadText(URL url, int maximumBytes) throws IOException {
        try (InputStream input = openHttpsStream(url);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input,
                     StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            char[] buffer = new char[2048];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                if (result.length() + count > maximumBytes) {
                    throw new IOException("Manifest is too large");
                }
                result.append(buffer, 0, count);
            }
            return result.toString();
        }
    }

    private void downloadFile(URL url, File destination, long expectedBytes) throws IOException {
        if (destination.exists() && !destination.delete()) {
            throw new IOException("Unable to clear temporary update");
        }
        long total = 0;
        try (InputStream input = new BufferedInputStream(openHttpsStream(url));
             FileOutputStream output = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (total > MAX_DATABASE_BYTES || total > expectedBytes) {
                    throw new IOException("Content package is too large");
                }
                output.write(buffer, 0, count);
            }
            output.getFD().sync();
        }
        if (total != expectedBytes) {
            throw new IOException("Content package size does not match manifest");
        }
    }

    private InputStream openHttpsStream(URL initialUrl) throws IOException {
        URL currentUrl = initialUrl;
        for (int redirect = 0; redirect <= 5; redirect++) {
            HttpURLConnection connection = (HttpURLConnection) currentUrl.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/octet-stream, application/json");
            connection.setRequestProperty("User-Agent", "DoaHindu/" + BuildConfig.VERSION_NAME);
            int responseCode = connection.getResponseCode();
            if (responseCode >= 300 && responseCode < 400) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location == null) {
                    throw new IOException("Redirect has no location");
                }
                currentUrl = requireHttps(new URL(currentUrl, location).toString());
                continue;
            }
            if (responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                throw new IOException("Unexpected HTTP response " + responseCode);
            }
            return connection.getInputStream();
        }
        throw new IOException("Too many redirects");
    }

    private URL requireHttps(String value) throws IOException {
        URL url = new URL(value);
        if (!"https".equalsIgnoreCase(url.getProtocol()) || url.getHost().isEmpty()) {
            throw new IOException("Only HTTPS content URLs are allowed");
        }
        return url;
    }

    boolean hasExpectedChecksum(File file, String expectedChecksum)
            throws IOException, NoSuchAlgorithmException {
        return expectedChecksum != null && sha256(file).equalsIgnoreCase(expectedChecksum);
    }

    private String sha256(File file) throws IOException, NoSuchAlgorithmException {
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

    boolean validateDatabase(File file, int schemaVersion) {
        SQLiteDatabase database = null;
        try {
            database = SQLiteDatabase.openDatabase(file.getPath(), null,
                    SQLiteDatabase.OPEN_READONLY);
            if (queryInt(database, "PRAGMA user_version") != schemaVersion
                    || !"ok".equalsIgnoreCase(queryString(database, "PRAGMA integrity_check"))) {
                return false;
            }
            if (!hasColumns(database, Constant.TBL_CATEGORY,
                    "cat_id", "cat_name", "cat_image")
                    || !hasColumns(database, Constant.TBL_TOPICS, "topic_id", "cat_id",
                    "topic_name", "topic_image", "topic_stories")
                    || !hasColumns(database, Constant.TBL_LATEST, "topic_id", "topic_name",
                    "topic_image", "topic_stories")) {
                return false;
            }
            return queryInt(database, "SELECT COUNT(*) FROM (SELECT topic_id FROM tbl_topics "
                    + "GROUP BY topic_id HAVING COUNT(*) > 1)") == 0
                    && queryInt(database, "SELECT COUNT(*) FROM tbl_topics t LEFT JOIN "
                    + "tbl_category c ON c.cat_id=t.cat_id WHERE c.cat_id IS NULL") == 0;
        } catch (RuntimeException exception) {
            return false;
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    private boolean hasColumns(SQLiteDatabase database, String table, String... requiredColumns) {
        Set<String> columns = new HashSet<>();
        try (Cursor cursor = database.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            }
        }
        for (String required : requiredColumns) {
            if (!columns.contains(required)) {
                return false;
            }
        }
        return true;
    }

    private int queryInt(SQLiteDatabase database, String sql) {
        try (Cursor cursor = database.rawQuery(sql, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : -1;
        }
    }

    private String queryString(SQLiteDatabase database, String sql) {
        try (Cursor cursor = database.rawQuery(sql, null)) {
            return cursor.moveToFirst() ? cursor.getString(0) : "";
        }
    }

    synchronized boolean replaceAtomically(File downloadedDatabase) {
        File activeDatabase = context.getDatabasePath(Constant.DB_NAME);
        File backupDatabase = context.getDatabasePath(Constant.DB_NAME + ".backup");
        if (backupDatabase.exists() && !backupDatabase.delete()) {
            return false;
        }
        if (activeDatabase.exists() && !activeDatabase.renameTo(backupDatabase)) {
            return false;
        }
        if (downloadedDatabase.renameTo(activeDatabase)) {
            return true;
        }
        if (activeDatabase.exists()) {
            activeDatabase.delete();
        }
        backupDatabase.renameTo(activeDatabase);
        return false;
    }
}
