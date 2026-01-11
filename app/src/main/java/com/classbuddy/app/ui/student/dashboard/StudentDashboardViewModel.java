package com.classbuddy.app.ui.student.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.ExamRepository;
import com.classbuddy.app.data.repository.NoticeRepository;
import com.classbuddy.app.data.repository.RoutineRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentDashboardViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final RoutineRepository routineRepository;
    private final ExamRepository examRepository;
    private final NoticeRepository noticeRepository;

    private final MediatorLiveData<Resource<User>> currentUser = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Classroom>>> joinedClassrooms = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Routine>>> todaysRoutine = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Exam>>> upcomingExams = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Notice>>> recentNotices = new MediatorLiveData<>();

    // Track current sources for cleanup
    private LiveData<Resource<User>> userSource;
    private LiveData<Resource<List<Classroom>>> classroomsSource;
    private LiveData<Resource<List<Routine>>> routineSource;
    private LiveData<Resource<List<Exam>>> examSource;
    private LiveData<Resource<List<Notice>>> noticeSource;

    // Track current classroom IDs to detect changes
    private List<String> currentClassroomIds = new ArrayList<>();

    public StudentDashboardViewModel() {
        userRepository = new UserRepository();
        classroomRepository = new ClassroomRepository();
        routineRepository = new RoutineRepository();
        examRepository = new ExamRepository();
        noticeRepository = new NoticeRepository();

        loadUserData();
    }

    private void loadUserData() {
        // Clean up old user source
        if (userSource != null) {
            currentUser.removeSource(userSource);
        }

        // Use real-time listener for user data
        userSource = userRepository.getCurrentUser();
        currentUser.addSource(userSource, resource -> {
            currentUser.setValue(resource);

            if (resource.isSuccess() && resource.data != null) {
                List<String> newClassroomIds = resource.data.getJoinedClassrooms();
                if (newClassroomIds == null) {
                    newClassroomIds = new ArrayList<>();
                }

                // Check if classroom IDs have changed
                if (!areListsEqual(currentClassroomIds, newClassroomIds)) {
                    currentClassroomIds = new ArrayList<>(newClassroomIds);
                    loadDashboardData(newClassroomIds);
                }
            }
        });
    }

    private boolean areListsEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    private void loadDashboardData(List<String> classroomIds) {
        if (classroomIds == null || classroomIds.isEmpty()) {
            todaysRoutine.setValue(Resource.success(new ArrayList<>()));
            upcomingExams.setValue(Resource.success(new ArrayList<>()));
            recentNotices.setValue(Resource.success(new ArrayList<>()));
            joinedClassrooms.setValue(Resource.success(new ArrayList<>()));
            return;
        }

        loadClassrooms(classroomIds);
        loadTodaysRoutine(classroomIds);
        loadUpcomingExams(classroomIds);
        loadRecentNotices(classroomIds);
    }

    private void loadClassrooms(List<String> classroomIds) {
        // Clean up old source
        if (classroomsSource != null) {
            joinedClassrooms.removeSource(classroomsSource);
        }

        classroomsSource = classroomRepository.getStudentClassrooms(classroomIds);
        joinedClassrooms.addSource(classroomsSource, joinedClassrooms::setValue);
    }

    private void loadTodaysRoutine(List<String> classroomIds) {
        // Clean up old source
        if (routineSource != null) {
            todaysRoutine.removeSource(routineSource);
        }

        routineSource = routineRepository.getTodaysRoutine(classroomIds);
        todaysRoutine.addSource(routineSource, todaysRoutine::setValue);
    }

    private void loadUpcomingExams(List<String> classroomIds) {
        // Clean up old source
        if (examSource != null) {
            upcomingExams.removeSource(examSource);
        }

        examSource = examRepository.getUpcomingExams(classroomIds);
        upcomingExams.addSource(examSource, upcomingExams::setValue);
    }

    private void loadRecentNotices(List<String> classroomIds) {
        // Clean up old source
        if (noticeSource != null) {
            recentNotices.removeSource(noticeSource);
        }

        noticeSource = noticeRepository.getRecentNotices(classroomIds, 5);
        recentNotices.addSource(noticeSource, recentNotices::setValue);
    }

    public void refreshData() {
        // Force reload by clearing tracked classroom IDs
        currentClassroomIds = new ArrayList<>();
        loadUserData();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up all sources
        if (userSource != null) currentUser.removeSource(userSource);
        if (classroomsSource != null) joinedClassrooms.removeSource(classroomsSource);
        if (routineSource != null) todaysRoutine.removeSource(routineSource);
        if (examSource != null) upcomingExams.removeSource(examSource);
        if (noticeSource != null) recentNotices.removeSource(noticeSource);
    }

    public LiveData<Resource<User>> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Resource<List<Classroom>>> getJoinedClassrooms() {
        return joinedClassrooms;
    }

    public LiveData<Resource<List<Routine>>> getTodaysRoutine() {
        return todaysRoutine;
    }

    public LiveData<Resource<List<Exam>>> getUpcomingExams() {
        return upcomingExams;
    }

    public LiveData<Resource<List<Notice>>> getRecentNotices() {
        return recentNotices;
    }
}
