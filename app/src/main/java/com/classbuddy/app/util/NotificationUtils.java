package com.classbuddy.app.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content. Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app. NotificationCompat;
import androidx.work.Data;
import androidx. work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work. WorkManager;

import com.classbuddy.app.ClassBuddyApplication;
import com.classbuddy.R;
import com.classbuddy.app.data.model.Exam;
import com. classbuddy.app.data.model. Routine;
import com. classbuddy.app.ui.splash.SplashActivity;
import com. classbuddy.app.worker.ClassReminderWorker;
import com.classbuddy.app.worker.ExamReminderWorker;

import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    private static final int NOTIFICATION_ID_BASE = 1000;

    public static void showNotification(Context context, String title, String message,
                                        String channelId, int notificationId) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION. SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        NotificationCompat. Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R. drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(notificationId, builder.build());
    }

    public static void showExamReminder(Context context, String examTitle, String message) {
        showNotification(context, examTitle, message,
                ClassBuddyApplication.CHANNEL_REMINDER,
                generateNotificationId("exam"));
    }

    public static void showClassReminder(Context context, String className, String message) {
        showNotification(context, className, message,
                ClassBuddyApplication. CHANNEL_REMINDER,
                generateNotificationId("class"));
    }

    public static void showNoticeAlert(Context context, String title, String message) {
        showNotification(context, title, message,
                ClassBuddyApplication.CHANNEL_ID,
                generateNotificationId("notice"));
    }

    public static void scheduleExamReminder(Context context, Exam exam, long reminderTime) {
        long delay = exam.getExamDate().toDate().getTime() - reminderTime - System.currentTimeMillis();

        if (delay <= 0) return;

        Data inputData = new Data.Builder()
                .putString("examId", exam.getId())
                .putString("examTitle", exam.getCourseName() + " - " + exam.getExamTypeDisplay())
                .putString("examTime", DateTimeUtils.formatDateTime(exam.getExamDate()))
                .putLong("reminderType", reminderTime)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ExamReminderWorker.class)
                .setInitialDelay(delay, TimeUnit. MILLISECONDS)
                .setInputData(inputData)
                .addTag("exam_reminder_" + exam.getId())
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "exam_reminder_" + exam.getId() + "_" + reminderTime,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                );
    }

    public static void scheduleClassReminder(Context context, Routine routine, long triggerTime) {
        long delay = triggerTime - System. currentTimeMillis();

        if (delay <= 0) return;

        Data inputData = new Data.Builder()
                .putString("routineId", routine.getId())
                .putString("subject", routine.getSubject())
                .putString("room", routine.getRoom())
                .putString("time", routine.getStartTime())
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ClassReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("class_reminder_" + routine. getId())
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "class_reminder_" + routine.getId(),
                        ExistingWorkPolicy. REPLACE,
                        workRequest
                );
    }

    public static void cancelExamReminder(Context context, String examId) {
        WorkManager.getInstance(context).cancelAllWorkByTag("exam_reminder_" + examId);
    }

    public static void cancelClassReminder(Context context, String routineId) {
        WorkManager.getInstance(context).cancelAllWorkByTag("class_reminder_" + routineId);
    }

    public static void cancelAllReminders(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
    }

    private static int generateNotificationId(String prefix) {
        return NOTIFICATION_ID_BASE + (prefix. hashCode() & 0xfffffff) % 10000
                + (int) (System.currentTimeMillis() % 1000);
    }
}