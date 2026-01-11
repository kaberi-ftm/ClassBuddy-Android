package com.classbuddy.app.ui.admin.notice;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.NoticeRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

import java.util.List;

public class CreateNoticeViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    private final MediatorLiveData<Resource<List<Classroom>>> classrooms = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<String>> createResult = new MediatorLiveData<>();
    private final MutableLiveData<String> adminName = new MutableLiveData<>();

    public CreateNoticeViewModel() {
        classroomRepository = new ClassroomRepository();
        noticeRepository = new NoticeRepository();
        userRepository = new UserRepository();

        loadClassrooms();
        loadAdminName();
    }

    private void loadClassrooms() {
        LiveData<Resource<List<Classroom>>> source = classroomRepository.getAdminClassrooms();
        classrooms.addSource(source, classrooms:: setValue);
    }

    private void loadAdminName() {
        LiveData<Resource<User>> userSource = userRepository. getCurrentUser();
        createResult.addSource(userSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                adminName.setValue(resource.data.getFullName());
            }
        });
    }

    public void createNotice(String classroomId, String classroomName,
                             String title, String content, String priority,
                             boolean isPinned, Uri imageUri) {

        String admin = adminName.getValue();
        if (admin == null) admin = "Admin";

        LiveData<Resource<String>> source = noticeRepository.createNotice(
                classroomId, classroomName, title, content, priority, admin, imageUri
        );

        createResult.addSource(source, resource -> {
            createResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                createResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<List<Classroom>>> getClassrooms() {
        return classrooms;
    }

    public LiveData<Resource<String>> getCreateResult() {
        return createResult;
    }

    public LiveData<String> getAdminName() {
        return adminName;
    }
}
