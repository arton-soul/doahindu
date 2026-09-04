package com.dearyoti.doahindu.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    private static final int MAX_DECODED_IMAGE_DIMENSION = 1024;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null || !remoteMessage.getData().isEmpty()) {
            showNotification(remoteMessage);
        }
    }

    private void showNotification(RemoteMessage message) {

        Bitmap bitmap = null;
        RemoteMessage.Notification notification = message.getNotification();
        String dataTitle = message.getData().get("title");
        String dataBody = message.getData().get("body");
        String title = notification != null && notification.getTitle() != null
                ? notification.getTitle() : dataTitle == null ? "Doa Hindu" : dataTitle;
        String body = notification != null && notification.getBody() != null
                ? notification.getBody() : dataBody == null ? "" : dataBody;
        Uri image = notification == null ? null : notification.getImageUrl();

        // Pass the intent to switch to the MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notificationTitle", "" + title);
        intent.putExtra("notificationBody", "" + body);
        String topicId = message.getData().get("topic_id");
        if (topicId != null) {
            try {
                intent.putExtra("notification_topic_id", Integer.parseInt(topicId));
            } catch (NumberFormatException ignored) {
            }
        }
        if (image != null) {
            intent.putExtra("notificationImage", image);
            bitmap = getBitmapfromUrl(image.toString());
        }

        String channel_id = getResources().getString(R.string.default_notification_channel_id);
        String channel_name = getResources().getString(R.string.default_notification_channel_name);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int notificationId = message.getMessageId() == null
                ? (int) System.currentTimeMillis() : message.getMessageId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder;
        if (bitmap != null) {
            builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, channel_name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(notificationId, builder.build());
    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(imageUrl);
            if (!"https".equalsIgnoreCase(url.getProtocol())) {
                return null;
            }

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(false);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            String contentType = connection.getContentType();
            int contentLength = connection.getContentLength();
            if (contentType == null || !contentType.startsWith("image/")
                    || contentLength > MAX_IMAGE_BYTES) {
                return null;
            }

            byte[] imageBytes;
            try (InputStream input = connection.getInputStream();
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int totalBytes = 0;
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    totalBytes += bytesRead;
                    if (totalBytes > MAX_IMAGE_BYTES) {
                        return null;
                    }
                    output.write(buffer, 0, bytesRead);
                }
                imageBytes = output.toByteArray();
            }

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(bounds);
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);

        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        int sampleSize = 1;
        while (options.outWidth / sampleSize > MAX_DECODED_IMAGE_DIMENSION
                || options.outHeight / sampleSize > MAX_DECODED_IMAGE_DIMENSION) {
            sampleSize *= 2;
        }
        return sampleSize;
    }
}
