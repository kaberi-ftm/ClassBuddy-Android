package com.classbuddy.app.data.remote;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.classbuddy.app.util.Resource;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.atomic.AtomicBoolean;

public class FirebaseAuthSource {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthSource() {
        this. firebaseAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    private static final long AUTH_TIMEOUT_MS = 30000; // 30 seconds timeout

    public LiveData<Resource<AuthResult>> login(String email, String password) {
        MutableLiveData<Resource<AuthResult>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        Handler handler = new Handler(Looper.getMainLooper());
        
        // Set timeout
        Runnable timeoutRunnable = () -> {
            if (!isCompleted.getAndSet(true)) {
                result.setValue(Resource.error("Connection timed out. Please check your internet connection and try again.", null));
            }
        };
        handler.postDelayed(timeoutRunnable, AUTH_TIMEOUT_MS);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (!isCompleted.getAndSet(true)) {
                        handler.removeCallbacks(timeoutRunnable);
                        result.setValue(Resource.success(authResult));
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isCompleted.getAndSet(true)) {
                        handler.removeCallbacks(timeoutRunnable);
                        result.setValue(Resource.error(getAuthErrorMessage(e), null));
                    }
                });

        return result;
    }

    public LiveData<Resource<AuthResult>> register(String email, String password) {
        MutableLiveData<Resource<AuthResult>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        Handler handler = new Handler(Looper.getMainLooper());
        
        // Set timeout
        Runnable timeoutRunnable = () -> {
            if (!isCompleted.getAndSet(true)) {
                result.setValue(Resource.error("Connection timed out. Please check your internet connection and try again.", null));
            }
        };
        handler.postDelayed(timeoutRunnable, AUTH_TIMEOUT_MS);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (!isCompleted.getAndSet(true)) {
                        handler.removeCallbacks(timeoutRunnable);
                        result.setValue(Resource.success(authResult));
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isCompleted.getAndSet(true)) {
                        handler.removeCallbacks(timeoutRunnable);
                        result.setValue(Resource.error(getAuthErrorMessage(e), null));
                    }
                });

        return result;
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public LiveData<Resource<Void>> sendPasswordResetEmail(String email) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> result. setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource. error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> updatePassword(String newPassword) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                    . addOnFailureListener(e -> result. setValue(Resource.error(getAuthErrorMessage(e), null)));
        } else {
            result.setValue(Resource. error("User not logged in", null));
        }

        return result;
    }

    private String getAuthErrorMessage(Exception e) {
        if (e instanceof FirebaseNetworkException) {
            return "No internet connection. Please check your network and try again.";
        } else if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Password is too weak. Please use at least 6 characters.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Invalid email or password. Please try again.";
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            return "An account already exists with this email address.";
        } else {
            String message = e.getMessage();
            if (message != null) {
                String lowerMessage = message.toLowerCase();
                if (lowerMessage.contains("network") || lowerMessage.contains("connection")) {
                    return "No internet connection. Please check your network and try again.";
                } else if (lowerMessage.contains("timeout") || lowerMessage.contains("timed out") || 
                           lowerMessage.contains("deadline") || lowerMessage.contains("unavailable")) {
                    return "Connection timed out. Please check your internet connection and try again.";
                }
            }
            return message != null ? message : "An error occurred. Please try again.";
        }
    }
}
