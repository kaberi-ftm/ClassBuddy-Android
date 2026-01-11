package com.classbuddy.app.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.AuthRepository;
import com.classbuddy.app.util.Resource;

public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MediatorLiveData<Resource<User>> loginResult = new MediatorLiveData<>();

    public LoginViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Resource<User>> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        LiveData<Resource<User>> source = authRepository.login(email, password);
        loginResult.addSource(source, resource -> {
            loginResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                loginResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<Void>> sendPasswordResetEmail(String email) {
        return authRepository.sendPasswordResetEmail(email);
    }
}
