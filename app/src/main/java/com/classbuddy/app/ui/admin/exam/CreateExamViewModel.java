package com.classbuddy.app.ui.admin.exam;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.ExamRepository;
import com.classbuddy.app.util.Resource;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateExamViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final ExamRepository examRepository;

    private final MediatorLiveData<Resource<List<Classroom>>> classrooms = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<String>> createResult = new MediatorLiveData<>();
    private Map<String, Classroom> classroomMap = new HashMap<>();

    public CreateExamViewModel() {
        classroomRepository = new ClassroomRepository();
        examRepository = new ExamRepository();

        loadClassrooms();
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

    public void createExam(String classroomId, String classroomName,
                           String courseNo, String courseName,
                           String examType, Timestamp examDate,
                           String startTime, String endTime,
                           String room, int totalMarks, String notes) {

        LiveData<Resource<String>> source = examRepository.createExam(
                classroomId, classroomName, courseNo, courseName,
                examType, examDate, startTime, endTime, room, totalMarks, notes
        );

        // Store exam details for notification
        final String finalCourseNo = courseNo;
        final String finalCourseName = courseName;
        final String finalExamType = examType;
        final Timestamp finalExamDate = examDate;

        createResult.addSource(source, resource -> {
            createResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                createResult.removeSource(source);
                
                // Send notification to students when exam is created successfully
                if (resource.isSuccess() && resource.data != null) {
                    Classroom classroom = classroomMap.get(classroomId);
                    if (classroom != null && classroom.getStudentIds() != null && !classroom.getStudentIds().isEmpty()) {
                        Exam exam = new Exam();
                        exam.setId(resource.data);
                        exam.setClassroomId(classroomId);
                        exam.setClassroomName(classroomName);
                        exam.setCourseNo(finalCourseNo);
                        exam.setCourseName(finalCourseName);
                        exam.setExamType(finalExamType);
                        exam.setExamDate(finalExamDate);
                        examRepository.notifyStudentsOfNewExam(classroom.getStudentIds(), exam);
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
}
