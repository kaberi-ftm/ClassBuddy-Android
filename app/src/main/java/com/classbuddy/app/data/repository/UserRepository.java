package com.classbuddy.app.data.repository;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.remote.FirebaseAuthSource;
import com.classbuddy.app.data.remote.FirestoreSource;
import com.classbuddy.app.util.Resource;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final FirebaseAuthSource authSource;
    private final FirestoreSource firestoreSource;

    public UserRepository() {
        authSource = new FirebaseAuthSource();
        firestoreSource = new FirestoreSource();
    }

    /**
     * Get current user with real-time updates.
     * This uses a snapshot listener to automatically update when user data changes.
     */
    public LiveData<Resource<User>> getCurrentUser() {
        FirebaseUser firebaseUser = authSource.getCurrentUser();
        if (firebaseUser == null) {
            MutableLiveData<Resource<User>> result = new MutableLiveData<>();
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }
        // Use real-time listener for instant updates when user data changes
        return firestoreSource.getUserRealtime(firebaseUser.getUid());
    }

    /**
     * Get current user with one-time fetch (non-realtime).
     * Use this when you only need the current state once.
     */
    public LiveData<Resource<User>> getCurrentUserOnce() {
        FirebaseUser firebaseUser = authSource.getCurrentUser();
        if (firebaseUser == null) {
            MutableLiveData<Resource<User>> result = new MutableLiveData<>();
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }
        return firestoreSource.getUser(firebaseUser.getUid());
    }

    public LiveData<Resource<User>> getUser(String userId) {
        return firestoreSource.getUser(userId);
    }

    /**
     * Get user with real-time updates
     */
    public LiveData<Resource<User>> getUserRealtime(String userId) {
        return firestoreSource.getUserRealtime(userId);
    }

    // REMOVED: uploadProfileImage method - Storage not available
    // Profile images will not be supported

    public LiveData<Resource<String>> uploadProfileImage(Uri imageUri) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        // Return error since storage is not enabled
        result.setValue(Resource.error("Profile image upload is not available", null));
        return result;
    }

    public LiveData<Resource<Void>> updateProfile(String fullName) {
        FirebaseUser firebaseUser = authSource.getCurrentUser();
        if (firebaseUser == null) {
            MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        return firestoreSource.updateUser(firebaseUser.getUid(), updates);
    }

    public void updateNotificationSettings(boolean enabled) {
        FirebaseUser firebaseUser = authSource.getCurrentUser();
        if (firebaseUser == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("notificationsEnabled", enabled);

        firestoreSource.updateUser(firebaseUser.getUid(), updates);
    }

    public void updateFcmToken(String token) {
        FirebaseUser firebaseUser = authSource.getCurrentUser();
        if (firebaseUser == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);

        firestoreSource.updateUser(firebaseUser.getUid(), updates);
    }
}
