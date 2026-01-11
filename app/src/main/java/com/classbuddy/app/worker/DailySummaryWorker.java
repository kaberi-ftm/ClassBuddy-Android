package com.classbuddy.app.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.classbuddy.app.ClassBuddyApplication;
import com.classbuddy.app.util.NotificationUtils;

public class DailySummaryWorker extends Worker {

    public DailySummaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int classCount = getInputData().getInt("classCount", 0);
        int examCount = getInputData().getInt("examCount", 0);

        StringBuilder message = new StringBuilder("Today's Summary:  ");

        if (classCount > 0) {
            message.append(classCount).append(" class").append(classCount > 1 ? "es" : "");
        }

        if (examCount > 0) {
            if (classCount > 0) message.append(", ");
            message.append(examCount).append(" exam").append(examCount > 1 ?  "s" : "");
        }

        if (classCount == 0 && examCount == 0) {
            message. append("No classes or exams scheduled.");
        }

        NotificationUtils.showNotification(
                getApplicationContext(),
                "Daily Academic Summary",
                message.toString(),
                ClassBuddyApplication.CHANNEL_ID,
                1001
        );

        return Result.success();
    }
}
