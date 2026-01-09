package com.classbuddy.app.data.remote;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content. Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core. app.NotificationCompat;

import com.classbuddy.app.ClassBuddyApplication;
import com. classbuddy.R;
import com. classbuddy.app.ui.splash.SplashActivity;
import com. classbuddy.app.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com. google.firebase.firestore.FirebaseFirestore;
import com.google.firebase. messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java. util.Map;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // Update token in Firestore
        updateTokenInFirestore(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received from:  " + remoteMessage.getFrom());

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage. getNotification().getTitle();
            String body = remoteMessage. getNotification().getBody();
            showNotification(title, body, null);
        }

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String body = data.get("body");
            String type = data.get("type");
            String referenceId = data.get("referenceId");

            handleDataMessage(title, body, type, referenceId);
        }
    }

    private void handleDataMessage(String title, String body, String type, String referenceId) {
        String channelId = ClassBuddyApplication. CHANNEL_ID;

        if (type != null) {
            switch (type) {
                case Constants. NOTIFICATION_TYPE_EXAM:
                    channelId = ClassBuddyApplication. CHANNEL_REMINDER;
                    break;
                case Constants.NOTIFICATION_TYPE_ROUTINE:
                    channelId = ClassBuddyApplication. CHANNEL_UPDATES;
                    break;
                case Constants.NOTIFICATION_TYPE_NOTICE:
                    channelId = ClassBuddyApplication. CHANNEL_ID;
                    break;
            }
        }

        showNotification(title, body, channelId);
    }

    private void showNotification(String title, String body, String channelId) {
        if (channelId == null) {
            channelId = ClassBuddyApplication. CHANNEL_ID;
        }

        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION. SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, flags
        );

        NotificationCompat.Builder builder = new NotificationCompat. Builder(this, channelId)
                .setSmallIcon(R. drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());
    }

    private void updateTokenInFirestore(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmToken", token);

            FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_USERS)
                    .document(user.getUid())
                    . update(updates)
                    .addOnSuccessListener(aVoid -> Log. d(TAG, "FCM Token updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM Token", e));
        }
    }

    public static void getToken(OnTokenReceivedListener listener) {
        com.google.firebase. messaging.FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(listener::onTokenReceived)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get FCM token", e);
                    listener.onTokenReceived(null);
                });
    }

    public interface OnTokenReceivedListener {
        void onTokenReceived(String token);
    }
}