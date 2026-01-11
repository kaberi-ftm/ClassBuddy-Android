package com.classbuddy.app.ui.student.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.AuthRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

public class StudentProfileViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    private final MediatorLiveData<Resource<User>> currentUser = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> updateResult = new MediatorLiveData<>();

    public StudentProfileViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();

        loadUser();
    }

    private void loadUser() {
        LiveData<Resource<User>> source = userRepository.getCurrentUser();
        currentUser.addSource(source, currentUser::setValue);
    }

    // REMOVED: uploadProfileImage method - Storage not available

    public void updateProfile(String fullName) {
        LiveData<Resource<Void>> source = userRepository.updateProfile(fullName);
        updateResult.addSource(source, resource -> {
            updateResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                updateResult.removeSource(source);
                if (resource.isSuccess()) {
                    loadUser(); // Refresh user data
                }
            }
        });
    }

    public void updateNotificationSettings(boolean enabled) {
        userRepository.updateNotificationSettings(enabled);
    }

    public void logout() {
        authRepository. logout();
    }

    public LiveData<Resource<User>> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Resource<Void>> getUpdateResult() {
        return updateResult;
    }
}
