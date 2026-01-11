package com.classbuddy.app.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.classbuddy.app.ClassBuddyApplication;
import com.classbuddy.app.R;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.ui.splash.SplashActivity;
import com.classbuddy.app.worker.ExamReminderWorker;

import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    private static final int NOTIFICATION_ID_BASE = 1000;

    public static void showNotification(Context context, String title, String message,
                                        String channelId, int notificationId) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION. SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent. FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent. getActivity(
                context, notificationId, intent, flags
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setPriority(NotificationCompat. PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }

    public static void showExamReminder(Context context, String title, String message) {
        showNotification(context, title, message,
                ClassBuddyApplication.CHANNEL_REMINDER,
                (int) System.currentTimeMillis());
    }

    public static void showClassReminder(Context context, String title, String message) {
        showNotification(context, title, message,
                ClassBuddyApplication.CHANNEL_REMINDER,
                (int) System.currentTimeMillis());
    }

    public static void scheduleExamReminder(Context context, Exam exam, long reminderType) {
        if (exam == null || exam.getExamDate() == null) return;

        long examTime = DateTimeUtils.getMillisFromTime(
                exam.getStartTime(),
                exam.getExamDate().toDate()
        );

        long currentTime = System.currentTimeMillis();
        long delay;

        if (reminderType == Constants.REMINDER_24_HOURS) {
            delay = examTime - currentTime - TimeUnit.HOURS.toMillis(24);
        } else if (reminderType == Constants.REMINDER_1_HOUR) {
            delay = examTime - currentTime - TimeUnit.HOURS.toMillis(1);
        } else {
            return;
        }

        // Don't schedule if time has passed
        if (delay <= 0) return;

        Data inputData = new Data.Builder()
                .putString("examId", exam.getId())
                .putString("examTitle", exam.getExamTypeDisplay() + ": " + exam.getCourseName())
                .putString("examTime", DateTimeUtils.formatTime(exam.getStartTime()))
                .putLong("reminderType", reminderType)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest. Builder(ExamReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("exam_reminder_" + exam.getId() + "_" + reminderType)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }

    public static void cancelExamReminder(Context context, String examId) {
        WorkManager.getInstance(context).cancelAllWorkByTag("exam_reminder_" + examId + "_" + Constants.REMINDER_24_HOURS);
        WorkManager. getInstance(context).cancelAllWorkByTag("exam_reminder_" + examId + "_" + Constants. REMINDER_1_HOUR);
    }

    public static void cancelAllReminders(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
    }
}
