package com.classbuddy.app.ui.splash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.AuthRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

public class SplashViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    public SplashViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public LiveData<Resource<User>> getCurrentUser() {
        return userRepository. getCurrentUser();
    }
}
