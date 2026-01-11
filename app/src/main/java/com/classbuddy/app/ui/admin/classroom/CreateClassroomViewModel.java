package com.classbuddy.app.ui.admin.classroom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

public class CreateClassroomViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    private final MediatorLiveData<Resource<String>> createResult = new MediatorLiveData<>();
    private final MutableLiveData<String> adminName = new MutableLiveData<>();

    public CreateClassroomViewModel() {
        classroomRepository = new ClassroomRepository();
        userRepository = new UserRepository();

        loadAdminName();
    }

    private void loadAdminName() {
        LiveData<Resource<User>> userSource = userRepository.getCurrentUser();
        createResult.addSource(userSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                adminName.setValue(resource.data.getFullName());
            }
        });
    }

    public void createClassroom(String name, String description, String section, String department) {
        String admin = adminName.getValue();
        if (admin == null) admin = "Admin";

        LiveData<Resource<String>> source = classroomRepository.createClassroom(
                name, description, section, department, admin);

        createResult.addSource(source, resource -> {
            createResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                createResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<String>> getCreateResult() {
        return createResult;
    }

    public LiveData<String> getAdminName() {
        return adminName;
    }
}
