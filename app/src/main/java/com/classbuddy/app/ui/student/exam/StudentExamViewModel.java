package com.classbuddy.app.ui.student.exam;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.ExamRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class StudentExamViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final ExamRepository examRepository;

    private final MediatorLiveData<Resource<List<Exam>>> upcomingExams = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Exam>>> pastExams = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> isEmpty = new MutableLiveData<>(false);

    // Track current sources for cleanup
    private LiveData<Resource<User>> userSource;
    private LiveData<Resource<List<Exam>>> examSource;

    // Track current classroom IDs to detect changes
    private List<String> currentClassroomIds = new ArrayList<>();

    public StudentExamViewModel() {
        userRepository = new UserRepository();
        examRepository = new ExamRepository();
    }

    public void loadAllExams() {
        upcomingExams.setValue(Resource.loading(null));

        // Clean up old source
        if (userSource != null) {
            upcomingExams.removeSource(userSource);
        }

        userSource = userRepository.getCurrentUser();
        upcomingExams.addSource(userSource, userResource -> {
            if (userResource.isSuccess() && userResource.data != null) {
                List<String> newClassroomIds = userResource.data.getJoinedClassrooms();
                if (newClassroomIds == null) {
                    newClassroomIds = new ArrayList<>();
                }

                // Check if classroom IDs changed
                if (!areListsEqual(currentClassroomIds, newClassroomIds)) {
                    currentClassroomIds = new ArrayList<>(newClassroomIds);

                    if (!newClassroomIds.isEmpty()) {
                        loadExamsForClassrooms(newClassroomIds);
                    } else {
                        upcomingExams.setValue(Resource.success(new ArrayList<>()));
                        pastExams.setValue(Resource.success(new ArrayList<>()));
                        isEmpty.setValue(true);
                    }
                }
            } else if (userResource.isError()) {
                upcomingExams.setValue(Resource.error(userResource.message, null));
            }
        });
    }

    private boolean areListsEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    private void loadExamsForClassrooms(List<String> classroomIds) {
        // Clean up old source
        if (examSource != null) {
            upcomingExams.removeSource(examSource);
        }

        examSource = examRepository.getExamsByClassrooms(classroomIds);
        upcomingExams.addSource(examSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                separateExams(resource.data);
            } else if (resource.isError()) {
                upcomingExams.setValue(resource);
            }
        });
    }

    public void loadExamsForClassroom(String classroomId) {
        upcomingExams.setValue(Resource.loading(null));

        // Clean up old source
        if (examSource != null) {
            upcomingExams.removeSource(examSource);
        }

        examSource = examRepository.getExamsByClassroom(classroomId);
        upcomingExams.addSource(examSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                separateExams(resource.data);
            } else {
                upcomingExams.setValue(resource);
            }
        });
    }

    private void separateExams(List<Exam> allExams) {
        Date now = new Date();

        List<Exam> upcoming = allExams.stream()
                .filter(exam -> exam.getExamDate() != null && exam.getExamDate().toDate().after(now))
                .collect(Collectors.toList());

        List<Exam> past = allExams.stream()
                .filter(exam -> exam.getExamDate() != null && exam.getExamDate().toDate().before(now))
                .collect(Collectors.toList());

        upcomingExams.setValue(Resource.success(upcoming));
        pastExams.setValue(Resource.success(past));
        isEmpty.setValue(upcoming.isEmpty() && past.isEmpty());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up all sources
        if (userSource != null) upcomingExams.removeSource(userSource);
        if (examSource != null) upcomingExams.removeSource(examSource);
    }

    public LiveData<Resource<List<Exam>>> getUpcomingExams() {
        return upcomingExams;
    }

    public LiveData<Resource<List<Exam>>> getPastExams() {
        return pastExams;
    }

    public LiveData<Boolean> getIsEmpty() {
        return isEmpty;
    }
}
