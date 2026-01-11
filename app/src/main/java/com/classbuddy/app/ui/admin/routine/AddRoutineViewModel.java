package com.classbuddy.app.ui.admin.routine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.RoutineRepository;
import com.classbuddy.app.util.Resource;

public class AddRoutineViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final RoutineRepository routineRepository;

    private final MutableLiveData<String> classroomName = new MutableLiveData<>();
    private final MutableLiveData<Routine> routineData = new MutableLiveData<>();
    private final MediatorLiveData<Resource<String>> saveResult = new MediatorLiveData<>();

    private String classroomId;

    public AddRoutineViewModel() {
        classroomRepository = new ClassroomRepository();
        routineRepository = new RoutineRepository();
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
        loadClassroomName();
    }

    private void loadClassroomName() {
        LiveData<Resource<Classroom>> source = classroomRepository.getClassroom(classroomId);
        saveResult.addSource(source, resource -> {
            if (resource. isSuccess() && resource.data != null) {
                classroomName.setValue(resource.data.getName());
            }
        });
    }

    public void loadRoutine(String routineId) {
        // In a real implementation, you would fetch the routine from Firebase
        // For now, we'll rely on the routines already loaded in the list
    }

    public void createRoutine(String subject, String faculty, String room,
                              String dayOfWeek, int dayIndex,
                              String startTime, String endTime, String type) {

        String name = classroomName.getValue();
        if (name == null) name = "";

        LiveData<Resource<String>> source = routineRepository.createRoutine(
                classroomId, name, subject, faculty, room,
                dayOfWeek, dayIndex, startTime, endTime, type
        );

        saveResult.addSource(source, resource -> {
            saveResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                saveResult. removeSource(source);
            }
        });
    }

    public void updateRoutine(String routineId, String subject, String faculty, String room,
                              String dayOfWeek, int dayIndex,
                              String startTime, String endTime, String type) {

        LiveData<Resource<Void>> source = routineRepository.updateRoutine(
                routineId, subject, faculty, room,
                dayOfWeek, dayIndex, startTime, endTime, type
        );

        saveResult. addSource(source, resource -> {
            if (resource.isSuccess()) {
                saveResult.setValue(Resource.success(routineId));
            } else if (resource.isError()) {
                saveResult.setValue(Resource. error(resource.message, null));
            }
            if (resource.isSuccess() || resource.isError()) {
                saveResult.removeSource(source);
            }
        });
    }

    public LiveData<String> getClassroomName() {
        return classroomName;
    }

    public LiveData<Routine> getRoutineData() {
        return routineData;
    }

    public LiveData<Resource<String>> getSaveResult() {
        return saveResult;
    }
}
