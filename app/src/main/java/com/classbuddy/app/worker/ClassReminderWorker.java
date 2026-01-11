package com.classbuddy.app.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.classbuddy.app.util.NotificationUtils;

public class ClassReminderWorker extends Worker {

    public ClassReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String routineId = getInputData().getString("routineId");
        String subject = getInputData().getString("subject");
        String room = getInputData().getString("room");
        String time = getInputData().getString("time");

        if (subject == null) {
            return Result.failure();
        }

        String title = "Class Reminder: " + subject;
        String message = "Your class starts in 15 minutes";
        if (room != null && ! room.isEmpty()) {
            message += " in Room " + room;
        }

        NotificationUtils. showClassReminder(getApplicationContext(), title, message);

        return Result.success();
    }
}
