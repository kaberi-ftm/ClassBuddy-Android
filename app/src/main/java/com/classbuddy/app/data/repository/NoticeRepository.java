package com.classbuddy.app.data.repository;

import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.data.remote.FirebaseAuthSource;
import com.classbuddy.app.data.remote.FirestoreSource;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.Resource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeRepository {

    private final FirebaseAuthSource authSource;
    private final FirestoreSource firestoreSource;

    public NoticeRepository() {
        authSource = new FirebaseAuthSource();
        firestoreSource = new FirestoreSource();
    }

    // Create notice WITHOUT image (Storage not available)
    public LiveData<Resource<String>> createNotice(String classroomId, String classroomName,
                                                   String title, String content, String priority,
                                                   String adminName, Uri imageUri) {
        String currentUserId = authSource.getCurrentUserId();
        if (currentUserId == null) {
            androidx.lifecycle.MutableLiveData<Resource<String>> result = new androidx.lifecycle.MutableLiveData<>();
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        Notice notice = new Notice();
        notice.setClassroomId(classroomId);
        notice.setClassroomName(classroomName);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPriority(priority);
        notice.setAdminId(currentUserId);
        notice.setAdminName(adminName);
        notice.setImageUrl(null); // No image support
        notice.setPinned(false);
        notice.setReadByStudentIds(new ArrayList<>());
        notice.setCreatedAt(Timestamp.now());
        notice.setUpdatedAt(Timestamp.now());

        return firestoreSource.createNotice(notice);
    }

    public LiveData<Resource<List<Notice>>> getNoticesByClassroom(String classroomId) {
        return firestoreSource.getNoticesByClassroom(classroomId);
    }

    public LiveData<Resource<List<Notice>>> getNoticesByClassrooms(List<String> classroomIds) {
        if (classroomIds == null || classroomIds.isEmpty()) {
            androidx.lifecycle.MutableLiveData<Resource<List<Notice>>> result = new androidx.lifecycle.MutableLiveData<>();
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }
        return firestoreSource.getNoticesByClassrooms(classroomIds);
    }

    public LiveData<Resource<List<Notice>>> getRecentNotices(List<String> classroomIds, int limit) {
        if (classroomIds == null || classroomIds.isEmpty()) {
            androidx.lifecycle.MutableLiveData<Resource<List<Notice>>> result = new androidx.lifecycle.MutableLiveData<>();
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }
        return firestoreSource.getRecentNotices(classroomIds, limit);
    }

    public LiveData<Resource<Void>> togglePinNotice(String noticeId, boolean isPinned) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isPinned", isPinned);
        updates.put("updatedAt", Timestamp.now());
        return firestoreSource.updateNotice(noticeId, updates);
    }

    public LiveData<Resource<Void>> deleteNotice(String noticeId) {
        return firestoreSource.deleteNotice(noticeId);
    }

    public void markAsRead(String noticeId) {
        String currentUserId = authSource.getCurrentUserId();
        if (currentUserId == null) return;
        firestoreSource.markNoticeAsRead(noticeId, currentUserId);
    }

    public void notifyStudentsOfNewNotice(List<String> studentIds, Notice notice) {
        String title = "New Notice: " + notice.getTitle();
        String message = notice.getContent();
        if (message.length() > 100) {
            message = message.substring(0, 97) + "...";
        }

        firestoreSource.sendNotificationToStudents(
                studentIds,
                title,
                message,
                Constants.NOTIFICATION_TYPE_NOTICE,
                notice.getId(),
                notice.getClassroomId()
        );
    }

    public void notifyStudentsOfNoticeUpdate(List<String> studentIds, Notice notice) {
        String title = "Notice Updated: " + notice.getTitle();
        String message = "A notice has been updated in " + notice.getClassroomName();

        firestoreSource.sendNotificationToStudents(
                studentIds,
                title,
                message,
                Constants.NOTIFICATION_TYPE_NOTICE_UPDATE,
                notice.getId(),
                notice.getClassroomId()
        );
    }
}
