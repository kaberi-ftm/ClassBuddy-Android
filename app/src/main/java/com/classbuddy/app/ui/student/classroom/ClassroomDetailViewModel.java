package com.classbuddy.app.ui.student.classroom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.ExamRepository;
import com.classbuddy.app.data.repository.NoticeRepository;
import com.classbuddy.app.data.repository.RoutineRepository;
import com.classbuddy.app.util.Resource;

import java.util.Date;
import java.util.List;

public class ClassroomDetailViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final RoutineRepository routineRepository;
    private final ExamRepository examRepository;
    private final NoticeRepository noticeRepository;

    private final MediatorLiveData<Resource<Classroom>> classroom = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> leaveResult = new MediatorLiveData<>();
    private final MutableLiveData<ClassroomStats> stats = new MutableLiveData<>();

    public ClassroomDetailViewModel() {
        classroomRepository = new ClassroomRepository();
        routineRepository = new RoutineRepository();
        examRepository = new ExamRepository();
        noticeRepository = new NoticeRepository();
    }

    public void loadClassroom(String classroomId) {
        LiveData<Resource<Classroom>> source = classroomRepository.getClassroom(classroomId);
        classroom.addSource(source, resource -> {
            classroom.setValue(resource);
            if (resource.isSuccess()) {
                loadStats(classroomId);
            }
        });
    }

    private void loadStats(String classroomId) {
        ClassroomStats classroomStats = new ClassroomStats();

        // Load routine count
        LiveData<Resource<List<Routine>>> routineSource = routineRepository.getRoutinesByClassroom(classroomId);
        classroom.addSource(routineSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                classroomStats.routineCount = resource.data.size();
                stats.setValue(classroomStats);
            }
        });

        // Load upcoming exam count
        LiveData<Resource<List<Exam>>> examSource = examRepository.getExamsByClassroom(classroomId);
        classroom.addSource(examSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                Date now = new Date();
                classroomStats.examCount = (int) resource.data.stream()
                        .filter(exam -> exam.getExamDate() != null && exam.getExamDate().toDate().after(now))
                        .count();
                stats. setValue(classroomStats);
            }
        });

        // Load notice count
        LiveData<Resource<List<Notice>>> noticeSource = noticeRepository.getNoticesByClassroom(classroomId);
        classroom.addSource(noticeSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                classroomStats.noticeCount = resource.data.size();
                stats.setValue(classroomStats);
            }
        });
    }

    public void leaveClassroom(String classroomId) {
        LiveData<Resource<Void>> source = classroomRepository.leaveClassroom(classroomId);
        leaveResult.addSource(source, resource -> {
            leaveResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                leaveResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<Classroom>> getClassroom() {
        return classroom;
    }

    public LiveData<Resource<Void>> getLeaveResult() {
        return leaveResult;
    }

    public LiveData<ClassroomStats> getStats() {
        return stats;
    }

    public static class ClassroomStats {
        public int routineCount = 0;
        public int examCount = 0;
        public int noticeCount = 0;
    }
}
