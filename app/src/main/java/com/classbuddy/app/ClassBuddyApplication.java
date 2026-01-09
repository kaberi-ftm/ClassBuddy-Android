package com.classbuddy.app;

import android.app.Application;
import android. app.NotificationChannel;
import android. app.NotificationManager;
import android. os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google. firebase.firestore.FirebaseFirestoreSettings;

public class ClassBuddyApplication extends Application {

    public static final String CHANNEL_ID = "classbuddy_channel";
    public static final String CHANNEL_REMINDER = "classbuddy_reminders";
    public static final String CHANNEL_UPDATES = "classbuddy_updates";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Configure Firestore settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        // Create notification channels
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION. SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // General notifications channel
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "General Notifications",
                    NotificationManager. IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("General app notifications");
            generalChannel.enableVibration(true);
            manager.createNotificationChannel(generalChannel);

            // Reminders channel
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDER,
                    "Reminders",
                    NotificationManager. IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Class and exam reminders");
            reminderChannel.enableVibration(true);
            manager. createNotificationChannel(reminderChannel);

            // Updates channel
            NotificationChannel updateChannel = new NotificationChannel(
                    CHANNEL_UPDATES,
                    "Updates",
                    NotificationManager. IMPORTANCE_DEFAULT
            );
            updateChannel.setDescription("Routine and notice updates");
            manager.createNotificationChannel(updateChannel);
        }
    }
}