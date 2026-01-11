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
import java.util.List;
import java.util.stream.Collectors;

public class AdminExamViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final ExamRepository examRepository;

    private final MediatorLiveData<Resource<List<Exam>>> exams = new MediatorLiveData<>();
    private final MutableLiveData<String> currentFilter = new MutableLiveData<>(null);

    private List<String> classroomIds = new ArrayList<>();
    private List<Exam> allExams = new ArrayList<>();

    public AdminExamViewModel() {
        classroomRepository = new ClassroomRepository();
        examRepository = new ExamRepository();

        loadClassrooms();
    }

    private void loadClassrooms() {
        LiveData<Resource<List<Classroom>>> classroomSource = classroomRepository.getAdminClassrooms();
        exams.addSource(classroomSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                classroomIds. clear();
                for (Classroom classroom :  resource.data) {
                    classroomIds.add(classroom.getId());
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

    public LiveData<Resource<List<Exam>>> getExams() {
        return exams;
    }
}
