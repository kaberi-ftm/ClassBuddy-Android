package com.classbuddy.app.ui.admin.exam;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.ExamRepository;
import com.classbuddy.app.util.Resource;
import com.google.firebase.Timestamp;

import java.util.List;

public class CreateExamViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final ExamRepository examRepository;

    private final MediatorLiveData<Resource<List<Classroom>>> classrooms = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<String>> createResult = new MediatorLiveData<>();

    public CreateExamViewModel() {
        classroomRepository = new ClassroomRepository();
        examRepository = new ExamRepository();

        loadClassrooms();
    }

    private void loadClassrooms() {
        LiveData<Resource<List<Classroom>>> source = classroomRepository.getAdminClassrooms();
        classrooms.addSource(source, classrooms::setValue);
    }

    public void createExam(String classroomId, String classroomName,
                           String courseNo, String courseName,
                           String examType, Timestamp examDate,
                           String startTime, String endTime,
                           String room, int totalMarks, String notes) {

        LiveData<Resource<String>> source = examRepository.createExam(
                classroomId, classroomName, courseNo, courseName,
                examType, examDate, startTime, endTime, room, totalMarks, notes
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
}
