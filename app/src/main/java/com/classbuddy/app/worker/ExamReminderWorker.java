package com.classbuddy.app.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.NotificationUtils;

public class ExamReminderWorker extends Worker {

    public ExamReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String examId = getInputData().getString("examId");
        String examTitle = getInputData().getString("examTitle");
        String examTime = getInputData().getString("examTime");
        long reminderType = getInputData().getLong("reminderType", 0);

        if (examTitle == null || examTime == null) {
            return Result.failure();
        }

        String message;
        if (reminderType == Constants.REMINDER_24_HOURS) {
            message = "Your exam is scheduled for tomorrow at " + examTime;
        } else if (reminderType == Constants.REMINDER_1_HOUR) {
            message = "Your exam starts in 1 hour! ";
        } else {
            message = "Exam scheduled at " + examTime;
        }

        NotificationUtils.showExamReminder(getApplicationContext(), examTitle, message);

        return Result.success();
    }
}
