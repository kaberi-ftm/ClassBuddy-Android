package com.classbuddy.app.data.remote;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.classbuddy.app.util.Resource;
import com. google.firebase.auth.AuthResult;
import com. google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth. FirebaseUser;

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

    public LiveData<Resource<AuthResult>> login(String email, String password) {
        MutableLiveData<Resource<AuthResult>> result = new MutableLiveData<>();
        result.setValue(Resource. loading(null));

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> result.setValue(Resource. success(authResult)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<AuthResult>> register(String email, String password) {
        MutableLiveData<Resource<AuthResult>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> result.setValue(Resource. success(authResult)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

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
                    . addOnFailureListener(e -> result. setValue(Resource.error(e.getMessage(), null)));
        } else {
            result.setValue(Resource. error("User not logged in", null));
        }

        return result;
    }
}