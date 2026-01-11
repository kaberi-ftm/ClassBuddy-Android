package com.classbuddy.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.remote.FCMService;
import com.classbuddy.app.data.remote.FirebaseAuthSource;
import com.classbuddy.app.data.remote.FirestoreSource;
import com.classbuddy.app.util.Resource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private final FirebaseAuthSource authSource;
    private final FirestoreSource firestoreSource;

    public AuthRepository() {
        this. authSource = new FirebaseAuthSource();
        this.firestoreSource = new FirestoreSource();
    }

    public FirebaseUser getCurrentUser() {
        return authSource.getCurrentUser();
    }

    public String getCurrentUserId() {
        return authSource.getCurrentUserId();
    }

    public boolean isLoggedIn() {
        return authSource. isLoggedIn();
    }

    public LiveData<Resource<User>> login(String email, String password) {
        MediatorLiveData<Resource<User>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        LiveData<Resource<AuthResult>> authResult = authSource.login(email, password);
        result.addSource(authResult, authResource -> {
            if (authResource.isSuccess() && authResource.data != null) {
                result.removeSource(authResult); // Remove source to prevent memory leak
                String userId = authResource.data.getUser().getUid();

                // Get user data from Firestore
                LiveData<Resource<User>> userResult = firestoreSource.getUser(userId);
                result.addSource(userResult, userResource -> {
                    if (userResource.isSuccess()) {
                        result.removeSource(userResult); // Remove source to prevent memory leak
                        // Update FCM token
                        updateFcmToken(userId);
                        result.setValue(userResource);
                    } else if (userResource.isError()) {
                        result.removeSource(userResult); // Remove source to prevent memory leak
                        result.setValue(userResource);
                    }
                });
            } else if (authResource.isError()) {
                result.removeSource(authResult); // Remove source to prevent memory leak
                result.setValue(Resource.error(authResource.message, null));
            }
        });

        return result;
    }

    public LiveData<Resource<User>> register(String email, String password, String fullName, String role) {
        MediatorLiveData<Resource<User>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        LiveData<Resource<AuthResult>> authResult = authSource.register(email, password);
        result.addSource(authResult, authResource -> {
            if (authResource.isSuccess() && authResource.data != null) {
                result.removeSource(authResult); // Remove source to prevent memory leak
                String userId = authResource.data.getUser().getUid();

                // Create user in Firestore
                User user = new User(email, fullName, role);
                user.setId(userId);

                LiveData<Resource<Void>> createResult = firestoreSource.createUser(user);
                result.addSource(createResult, createResource -> {
                    if (createResource.isSuccess()) {
                        result.removeSource(createResult); // Remove source to prevent memory leak
                        // Update FCM token
                        updateFcmToken(userId);
                        result.setValue(Resource.success(user));
                    } else if (createResource.isError()) {
                        result.removeSource(createResult); // Remove source to prevent memory leak
                        result.setValue(Resource.error(createResource.message, null));
                    }
                });
            } else if (authResource.isError()) {
                result.removeSource(authResult); // Remove source to prevent memory leak
                result.setValue(Resource.error(authResource.message, null));
            }
        });

        return result;
    }

    public void logout() {
        authSource.logout();
    }

    public LiveData<Resource<Void>> sendPasswordResetEmail(String email) {
        return authSource.sendPasswordResetEmail(email);
    }

    private void updateFcmToken(String userId) {
        FCMService.getToken(token -> {
            if (token != null) {
                firestoreSource.updateFcmToken(userId, token);
            }
        });
    }
}
