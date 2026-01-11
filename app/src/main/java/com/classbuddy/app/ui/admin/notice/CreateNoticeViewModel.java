package com.classbuddy.app.ui.admin.notice;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.NoticeRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateNoticeViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    private final MediatorLiveData<Resource<List<Classroom>>> classrooms = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<String>> createResult = new MediatorLiveData<>();
    private final MutableLiveData<String> adminName = new MutableLiveData<>();
    private Map<String, Classroom> classroomMap = new HashMap<>();

    public CreateNoticeViewModel() {
        classroomRepository = new ClassroomRepository();
        noticeRepository = new NoticeRepository();
        userRepository = new UserRepository();

        loadClassrooms();
        loadAdminName();
    }

    private void loadClassrooms() {
        LiveData<Resource<List<Classroom>>> source = classroomRepository.getAdminClassrooms();
        classrooms.addSource(source, resource -> {
            classrooms.setValue(resource);
            if (resource.isSuccess() && resource.data != null) {
                classroomMap.clear();
                for (Classroom classroom : resource.data) {
                    classroomMap.put(classroom.getId(), classroom);
                }
            }
        });
    }

    private void loadAdminName() {
        LiveData<Resource<User>> userSource = userRepository.getCurrentUser();
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

        final String finalTitle = title;
        final String finalContent = content;
        
        createResult.addSource(source, resource -> {
            createResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                createResult.removeSource(source);
                
                // Send notification to students when notice is created successfully
                if (resource.isSuccess() && resource.data != null) {
                    Classroom classroom = classroomMap.get(classroomId);
                    if (classroom != null && classroom.getStudentIds() != null && !classroom.getStudentIds().isEmpty()) {
                        Notice notice = new Notice();
                        notice.setId(resource.data);
                        notice.setTitle(finalTitle);
                        notice.setContent(finalContent);
                        notice.setClassroomId(classroomId);
                        notice.setClassroomName(classroomName);
                        noticeRepository.notifyStudentsOfNewNotice(classroom.getStudentIds(), notice);
                    }
                }
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
