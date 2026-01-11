package com.classbuddy.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.classbuddy.app.data.model.Notification;
import com.classbuddy.app.data.remote.FirebaseAuthSource;
import com.classbuddy.app.data.remote.FirestoreSource;
import com.classbuddy.app.util.Resource;

import java.util.List;

public class NotificationRepository {

    private final FirebaseAuthSource authSource;
    private final FirestoreSource firestoreSource;

    public NotificationRepository() {
        this.authSource = new FirebaseAuthSource();
        this.firestoreSource = new FirestoreSource();
    }

    public String getCurrentUserId() {
        return authSource.getCurrentUserId();
    }

    public LiveData<Resource<List<Notification>>> getNotifications() {
        String userId = getCurrentUserId();
        if (userId == null) {
            MediatorLiveData<Resource<List<Notification>>> result = new MediatorLiveData<>();
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }
        return firestoreSource.getNotificationsByUser(userId);
    }

    public LiveData<Resource<Void>> markAsRead(String notificationId) {
        return firestoreSource.markNotificationAsRead(notificationId);
    }

    public LiveData<Resource<Void>> markAllAsRead() {
        String userId = getCurrentUserId();
        if (userId == null) {
            MediatorLiveData<Resource<Void>> result = new MediatorLiveData<>();
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }
        return firestoreSource.markAllNotificationsAsRead(userId);
    }

    public LiveData<Resource<Void>> deleteNotification(String notificationId) {
        return firestoreSource.deleteNotification(notificationId);
    }

    public LiveData<Resource<Void>> clearAllNotifications() {
        String userId = getCurrentUserId();
        if (userId == null) {
            MediatorLiveData<Resource<Void>> result = new MediatorLiveData<>();
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }
        return firestoreSource.deleteAllNotifications(userId);
    }
}
