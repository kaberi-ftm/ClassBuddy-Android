package com.classbuddy.app.ui.admin.classroom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.util.Resource;

import java.util.List;

public class AdminClassroomViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final MediatorLiveData<Resource<List<Classroom>>> classrooms = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> deleteResult = new MediatorLiveData<>();

    public AdminClassroomViewModel() {
        classroomRepository = new ClassroomRepository();
        loadClassrooms();
    }

    private void loadClassrooms() {
        LiveData<Resource<List<Classroom>>> source = classroomRepository.getAdminClassrooms();
        classrooms.addSource(source, classrooms:: setValue);
    }

    public void refreshClassrooms() {
        loadClassrooms();
    }

    public void deleteClassroom(String classroomId) {
        LiveData<Resource<Void>> source = classroomRepository.deleteClassroom(classroomId);
        deleteResult.addSource(source, resource -> {
            deleteResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                deleteResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<List<Classroom>>> getClassrooms() {
        return classrooms;
    }

    public LiveData<Resource<Void>> getDeleteResult() {
        return deleteResult;
    }
}
