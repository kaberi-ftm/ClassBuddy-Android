package com.classbuddy.app.ui.student.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Notification;
import com.classbuddy.app.data.repository.NotificationRepository;
import com.classbuddy.app.util.Resource;

import java.util.List;

public class NotificationCenterViewModel extends ViewModel {

    private final NotificationRepository notificationRepository;
    private final MediatorLiveData<Resource<List<Notification>>> notifications = new MediatorLiveData<>();

    public NotificationCenterViewModel() {
        notificationRepository = new NotificationRepository();
        loadNotifications();
    }

    private void loadNotifications() {
        LiveData<Resource<List<Notification>>> source = notificationRepository.getNotifications();
        notifications.addSource(source, notifications:: setValue);
    }

    public void refreshNotifications() {
        loadNotifications();
    }

    public void markAsRead(String notificationId) {
        notificationRepository. markAsRead(notificationId);
    }

    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
    }

    public void deleteNotification(String notificationId) {
        notificationRepository.deleteNotification(notificationId);
    }

    public void clearAllNotifications() {
        notificationRepository.clearAllNotifications();
    }

    public LiveData<Resource<List<Notification>>> getNotifications() {
        return notifications;
    }
}
