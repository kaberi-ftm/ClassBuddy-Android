package com.classbuddy.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.remote.FirebaseAuthSource;
import com.classbuddy.app.data.remote.FirestoreSource;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.DateTimeUtils;
import com.classbuddy.app.util.Resource;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamRepository {

    private final FirebaseAuthSource authSource;
    private final FirestoreSource firestoreSource;

    public ExamRepository() {
        this.authSource = new FirebaseAuthSource();
        this.firestoreSource = new FirestoreSource();
    }

    public String getCurrentUserId() {
        return authSource.getCurrentUserId();
    }

    public LiveData<Resource<String>> createExam(
            String classroomId,
            String classroomName,
            String courseNo,
            String courseName,
            String examType,
            Timestamp examDate,
            String startTime,
            String endTime,
            String room,
            int totalMarks,
            String notes
    ) {
        String adminId = getCurrentUserId();
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();

        if (adminId == null) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        Exam exam = new Exam(
                classroomId,
                classroomName,
                courseNo,
                courseName,
                examType,
                examDate,
                startTime,
                endTime,
                room,
                totalMarks,
                notes,
                adminId
        );

        return firestoreSource.createExam(exam);
    }

    public LiveData<Resource<List<Exam>>> getExamsByClassroom(String classroomId) {
        return firestoreSource.getExamsByClassroom(classroomId);
    }

    public LiveData<Resource<List<Exam>>> getExamsByClassrooms(List<String> classroomIds) {
        return firestoreSource.getExamsByClassrooms(classroomIds);
    }

    public LiveData<Resource<List<Exam>>> getUpcomingExams(List<String> classroomIds) {
        return firestoreSource.getUpcomingExams(classroomIds);
    }

    public LiveData<Resource<Void>> updateExam(
            String examId,
            String courseNo,
            String courseName,
            String examType,
            Timestamp examDate,
            String startTime,
            String endTime,
            String room,
            int totalMarks,
            String notes
    ) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("courseNo", courseNo);
        updates.put("courseName", courseName);
        updates.put("examType", examType);
        updates.put("examDate", examDate);
        updates.put("startTime", startTime);
        updates.put("endTime", endTime);
        updates.put("room", room);
        updates.put("totalMarks", totalMarks);
        updates.put("notes", notes);

        return firestoreSource.updateExam(examId, updates);
    }

    public LiveData<Resource<Void>> deleteExam(String examId) {
        return firestoreSource.deleteExam(examId);
    }

    public void notifyStudentsOfNewExam(List<String> studentIds, Exam exam) {
        String title = "New " + exam.getExamTypeDisplay() + " Scheduled";
        String message = exam.getCourseName() + " on " +
                DateTimeUtils.formatDate(exam.getExamDate());

        firestoreSource.sendNotificationToStudents(
                studentIds,
                title,
                message,
                Constants.NOTIFICATION_TYPE_EXAM,
                exam.getId(),
                exam.getClassroomId()
        );
    }
}
