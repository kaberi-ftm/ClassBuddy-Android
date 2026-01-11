package com.classbuddy.app.ui.admin.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.AuthRepository;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.ExamRepository;
import com.classbuddy.app.data.repository.NoticeRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ExamRepository examRepository;
    private final NoticeRepository noticeRepository;

    private final MediatorLiveData<Resource<User>> currentUser = new MediatorLiveData<>();
    private final MutableLiveData<Integer> totalClassrooms = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalStudents = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> upcomingEventsCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> activeNoticesCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private List<String> classroomIds = new ArrayList<>();

    public AdminDashboardViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        classroomRepository = new ClassroomRepository();
        examRepository = new ExamRepository();
        noticeRepository = new NoticeRepository();

        loadData();
    }

    private void loadData() {
        isLoading.setValue(true);

        // Load user data
        LiveData<Resource<User>> userSource = userRepository.getCurrentUser();
        currentUser.addSource(userSource, resource -> {
            currentUser.setValue(resource);
            if (resource.isSuccess()) {
                loadAdminClassrooms();
            }
        });
    }

    private void loadAdminClassrooms() {
        LiveData<Resource<List<Classroom>>> classroomSource = classroomRepository.getAdminClassrooms();
        currentUser.addSource(classroomSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                List<Classroom> classrooms = resource.data;
                totalClassrooms.setValue(classrooms.size());

                // Calculate total students
                int students = 0;
                classroomIds.clear();
                for (Classroom classroom : classrooms) {
                    students += classroom.getStudentCount();
                    classroomIds.add(classroom.getId());
                }
                totalStudents.setValue(students);

                // Load exams and notices count
                loadUpcomingEvents();
                loadActiveNotices();
            }
            isLoading.setValue(false);
        });
    }

    private void loadUpcomingEvents() {
        if (classroomIds.isEmpty()) {
            upcomingEventsCount.setValue(0);
            return;
        }

        LiveData<Resource<List<Exam>>> examSource = examRepository. getUpcomingExams(classroomIds);
        currentUser.addSource(examSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                upcomingEventsCount.setValue(resource. data.size());
            }
        });
    }

    private void loadActiveNotices() {
        if (classroomIds.isEmpty()) {
            activeNoticesCount.setValue(0);
            return;
        }

        LiveData<Resource<List<Notice>>> noticeSource = noticeRepository.getRecentNotices(classroomIds, 100);
        currentUser.addSource(noticeSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                activeNoticesCount. setValue(resource.data.size());
            }
        });
    }

    public void refreshData() {
        loadData();
    }

    public void logout() {
        authRepository.logout();
    }

    public LiveData<Resource<User>> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Integer> getTotalClassrooms() {
        return totalClassrooms;
    }

    public LiveData<Integer> getTotalStudents() {
        return totalStudents;
    }

    public LiveData<Integer> getUpcomingEventsCount() {
        return upcomingEventsCount;
    }

    public LiveData<Integer> getActiveNoticesCount() {
        return activeNoticesCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
