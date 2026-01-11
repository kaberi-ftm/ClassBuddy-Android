package com.classbuddy.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.remote.FirebaseAuthSource;
import com.classbuddy.app.data.remote.FirestoreSource;
import com.classbuddy.app.util.CodeGenerator;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassroomRepository {

    private final FirebaseAuthSource authSource;
    private final FirestoreSource firestoreSource;

    public ClassroomRepository() {
        this.authSource = new FirebaseAuthSource();
        this.firestoreSource = new FirestoreSource();
    }

    public String getCurrentUserId() {
        return authSource.getCurrentUserId();
    }

    // =======================
    // Admin Operations
    // =======================

    public LiveData<Resource<String>> createClassroom(
            String name,
            String description,
            String section,
            String department,
            String adminName
    ) {
        String adminId = getCurrentUserId();
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();

        if (adminId == null) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        Classroom classroom = new Classroom(
                name,
                description,
                section,
                department,
                adminId,
                adminName
        );

        classroom.setCode(CodeGenerator.generateClassroomCode());
        classroom.setPassword(CodeGenerator.generatePassword(4));

        return firestoreSource.createClassroom(classroom);
    }

    public LiveData<Resource<List<Classroom>>> getAdminClassrooms() {
        String adminId = getCurrentUserId();
        MediatorLiveData<Resource<List<Classroom>>> result = new MediatorLiveData<>();

        if (adminId == null) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        return firestoreSource.getClassroomsByAdmin(adminId);
    }

    public LiveData<Resource<Void>> updateClassroom(
            String classroomId,
            String name,
            String description,
            String section,
            String department
    ) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", description);
        updates.put("section", section);
        updates.put("department", department);

        return firestoreSource.updateClassroom(classroomId, updates);
    }

    public LiveData<Resource<Void>> deleteClassroom(String classroomId) {
        return firestoreSource.deleteClassroom(classroomId);
    }

    public LiveData<Resource<String>> regenerateClassroomCode(String classroomId) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        String newCode = CodeGenerator.generateClassroomCode();
        Map<String, Object> updates = new HashMap<>();
        updates.put("code", newCode);

        LiveData<Resource<Void>> updateResult =
                firestoreSource.updateClassroom(classroomId, updates);

        result.addSource(updateResult, resource -> {
            if (resource.isSuccess()) {
                result.setValue(Resource.success(newCode));
            } else if (resource.isError()) {
                result.setValue(Resource.error(resource.message, null));
            }
        });

        return result;
    }

    public LiveData<Resource<List<User>>> getClassroomStudents(List<String> studentIds) {
        return firestoreSource.getStudentsInClassroom(studentIds);
    }

    public LiveData<Resource<Void>> removeStudent(String classroomId, String studentId) {
        return firestoreSource.removeStudentFromClassroom(classroomId, studentId);
    }

    // =======================
    // Student Operations
    // =======================

    public LiveData<Resource<List<Classroom>>> getStudentClassrooms(List<String> classroomIds) {
        return firestoreSource.getClassroomsByStudent(classroomIds);
    }

    public LiveData<Resource<Classroom>> getClassroomByCode(String code) {
        return firestoreSource.getClassroomByCode(code);
    }

    public LiveData<Resource<Void>> joinClassroom(
            String classroomId,
            String password,
            Classroom classroom
    ) {
        MediatorLiveData<Resource<Void>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        String studentId = getCurrentUserId();
        if (studentId == null) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        if (!classroom.getPassword().equals(password)) {
            result.setValue(Resource.error("Wrong password", null));
            return result;
        }

        if (classroom.getStudentIds() != null &&
                classroom.getStudentIds().contains(studentId)) {
            result.setValue(Resource.error("You have already joined this classroom", null));
            return result;
        }

        LiveData<Resource<Void>> addResult =
                firestoreSource.addStudentToClassroom(classroomId, studentId);

        result.addSource(addResult, resource -> {
            if (resource.isSuccess()) {

                firestoreSource.sendNotificationToStudents(
                        java.util.Collections.singletonList(studentId),
                        "Welcome to " + classroom.getName(),
                        "You have successfully joined " + classroom.getName(),
                        Constants.NOTIFICATION_TYPE_CLASSROOM,
                        classroomId,
                        classroomId
                );

                result.setValue(Resource.success(null));
            } else if (resource.isError()) {
                result.setValue(resource);
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> leaveClassroom(String classroomId) {
        String studentId = getCurrentUserId();
        MediatorLiveData<Resource<Void>> result = new MediatorLiveData<>();

        if (studentId == null) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        return firestoreSource.removeStudentFromClassroom(classroomId, studentId);
    }

    public LiveData<Resource<Classroom>> getClassroom(String classroomId) {
        return firestoreSource.getClassroom(classroomId);
    }
}
