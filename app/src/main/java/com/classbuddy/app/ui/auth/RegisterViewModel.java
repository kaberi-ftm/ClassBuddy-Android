package com.classbuddy.app.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.AuthRepository;
import com.classbuddy.app.util.Resource;

public class RegisterViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MediatorLiveData<Resource<User>> registerResult = new MediatorLiveData<>();

    public RegisterViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Resource<User>> getRegisterResult() {
        return registerResult;
    }

    public void register(String email, String password, String fullName, String role) {
        LiveData<Resource<User>> source = authRepository.register(email, password, fullName, role);
        registerResult.addSource(source, resource -> {
            registerResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                registerResult.removeSource(source);
            }
        });
    }
}
