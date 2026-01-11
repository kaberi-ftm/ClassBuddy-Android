package com.classbuddy.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.remote.FirebaseAuthSource;
import com.classbuddy.app.data.remote.FirestoreSource;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.DateTimeUtils;
import com.classbuddy.app.util.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutineRepository {

    private final FirebaseAuthSource authSource;
    private final FirestoreSource firestoreSource;

    public RoutineRepository() {
        this.authSource = new FirebaseAuthSource();
        this.firestoreSource = new FirestoreSource();
    }

    public String getCurrentUserId() {
        return authSource.getCurrentUserId();
    }

    public LiveData<Resource<String>> createRoutine(
            String classroomId,
            String classroomName,
            String subject,
            String faculty,
            String room,
            String dayOfWeek,
            int dayIndex,
            String startTime,
            String endTime,
            String type
    ) {
        String adminId = getCurrentUserId();
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();

        if (adminId == null) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        Routine routine = new Routine(
                classroomId,
                classroomName,
                subject,
                faculty,
                room,
                dayOfWeek,
                dayIndex,
                startTime,
                endTime,
                type,
                adminId
        );

        return firestoreSource.createRoutine(routine);
    }

    public LiveData<Resource<List<Routine>>> getRoutinesByClassroom(String classroomId) {
        return firestoreSource.getRoutinesByClassroom(classroomId);
    }

    public LiveData<Resource<List<Routine>>> getRoutinesByClassrooms(List<String> classroomIds) {
        return firestoreSource.getRoutinesByClassrooms(classroomIds);
    }

    public LiveData<Resource<List<Routine>>> getTodaysRoutine(List<String> classroomIds) {
        int todayIndex = DateTimeUtils.getCurrentDayIndex();
        return firestoreSource.getTodaysRoutine(classroomIds, todayIndex);
    }

    public LiveData<Resource<Void>> updateRoutine(
            String routineId,
            String subject,
            String faculty,
            String room,
            String dayOfWeek,
            int dayIndex,
            String startTime,
            String endTime,
            String type
    ) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("subject", subject);
        updates.put("faculty", faculty);
        updates.put("room", room);
        updates.put("dayOfWeek", dayOfWeek);
        updates.put("dayIndex", dayIndex);
        updates.put("startTime", startTime);
        updates.put("endTime", endTime);
        updates.put("type", type);

        return firestoreSource.updateRoutine(routineId, updates);
    }

    public LiveData<Resource<Void>> deleteRoutine(String routineId) {
        return firestoreSource.deleteRoutine(routineId);
    }

    public LiveData<Resource<Void>> cancelClass(String routineId, String reason, String cancelledDate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isCancelled", true);
        updates.put("cancellationReason", reason);
        updates.put("cancelledDate", cancelledDate);
        return firestoreSource.updateRoutine(routineId, updates);
    }

    public LiveData<Resource<Void>> restoreClass(String routineId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isCancelled", false);
        updates.put("cancellationReason", null);
        updates.put("cancelledDate", null);
        return firestoreSource.updateRoutine(routineId, updates);
    }

    public void notifyStudentsOfClassCancellation(List<String> studentIds, Routine routine, String reason, String date) {
        String title = "Class Cancelled";
        String message = routine.getSubject() + " class on " + date + " has been cancelled.";
        if (reason != null && !reason.isEmpty()) {
            message += "\nReason: " + reason;
        }

        firestoreSource.sendNotificationToStudents(
                studentIds,
                title,
                message,
                Constants.NOTIFICATION_TYPE_CLASS_CANCELLED,
                routine.getId(),
                routine.getClassroomId()
        );
    }

    public void notifyStudentsOfRoutineUpdate(List<String> studentIds, String classroomName) {
        firestoreSource.sendNotificationToStudents(
                studentIds,
                "Routine Updated",
                "Class routine has been updated for " + classroomName,
                Constants.NOTIFICATION_TYPE_ROUTINE,
                null,
                null
        );
    }
}
