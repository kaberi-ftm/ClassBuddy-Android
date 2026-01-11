package com.classbuddy.app.ui.admin.exam;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.ExamRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminExamViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final ExamRepository examRepository;

    private final MediatorLiveData<Resource<List<Exam>>> exams = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> cancelResult = new MediatorLiveData<>();
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>(null);

    private List<String> classroomIds = new ArrayList<>();
    private List<Exam> allExams = new ArrayList<>();
    private Map<String, Classroom> classroomMap = new HashMap<>();

    public AdminExamViewModel() {
        classroomRepository = new ClassroomRepository();
        examRepository = new ExamRepository();

        loadClassrooms();
    }

    private void loadClassrooms() {
        LiveData<Resource<List<Classroom>>> classroomSource = classroomRepository.getAdminClassrooms();
        exams.addSource(classroomSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                classroomIds.clear();
                classroomMap.clear();
                for (Classroom classroom : resource.data) {
                    classroomIds.add(classroom.getId());
                    classroomMap.put(classroom.getId(), classroom);
                }
                loadExams();
            }
        });
    }

    private void loadExams() {
        if (classroomIds.isEmpty()) {
            exams.setValue(Resource.success(new ArrayList<>()));
            return;
        }

        LiveData<Resource<List<Exam>>> examSource = examRepository.getExamsByClassrooms(classroomIds);
        exams.addSource(examSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                allExams = resource.data;
                applyFilter();
            } else {
                exams.setValue(resource);
            }
        });
    }

    public void setFilter(String filter) {
        currentFilter.setValue(filter);
        applyFilter();
    }

    private void applyFilter() {
        String filter = currentFilter.getValue();
        if (filter == null || filter.isEmpty()) {
            exams.setValue(Resource.success(allExams));
        } else {
            List<Exam> filtered = allExams. stream()
                    .filter(exam -> exam.getExamType().equalsIgnoreCase(filter))
                    .collect(Collectors.toList());
            exams.setValue(Resource.success(filtered));
        }
    }

    public void refreshExams() {
        loadClassrooms();
    }

    public void cancelExam(Exam exam, String reason) {
        LiveData<Resource<Void>> source = examRepository.cancelExam(exam.getId(), reason);
        cancelResult.addSource(source, resource -> {
            cancelResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                cancelResult.removeSource(source);
                if (resource.isSuccess()) {
                    // Notify students
                    Classroom classroom = classroomMap.get(exam.getClassroomId());
                    if (classroom != null && classroom.getStudentIds() != null) {
                        examRepository.notifyStudentsOfExamCancellation(
                                classroom.getStudentIds(), exam, reason
                        );
                    }
                    loadExams();
                }
            }
        });
    }

    public void restoreExam(String examId) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("isCancelled", false);
        updates.put("cancellationReason", null);
        
        LiveData<Resource<Void>> source = new com.classbuddy.app.data.remote.FirestoreSource().updateExam(examId, updates);
        cancelResult.addSource(source, resource -> {
            cancelResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                cancelResult.removeSource(source);
                if (resource.isSuccess()) {
                    loadExams();
                }
            }
        });
    }

    public LiveData<Resource<List<Exam>>> getExams() {
        return exams;
    }

    public LiveData<Resource<Void>> getCancelResult() {
        return cancelResult;
    }
}
