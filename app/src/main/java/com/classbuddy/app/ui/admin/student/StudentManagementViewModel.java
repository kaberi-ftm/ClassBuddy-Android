package com.classbuddy.app.ui.admin.student;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentManagementViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;

    private final MediatorLiveData<Resource<List<Classroom>>> classrooms = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<User>>> students = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> removeResult = new MediatorLiveData<>();

    private List<Classroom> allClassrooms = new ArrayList<>();
    private String currentClassroomId;

    // Track current sources for cleanup
    private LiveData<Resource<List<User>>> studentsSource;
    private LiveData<Resource<List<Classroom>>> classroomsSource;
    private LiveData<Resource<Void>> removeSource;

    // Track current student IDs to detect changes
    private List<String> currentStudentIds = new ArrayList<>();

    public StudentManagementViewModel() {
        classroomRepository = new ClassroomRepository();
        loadClassrooms();
    }

    private void loadClassrooms() {
        if (classroomsSource != null) {
            classrooms.removeSource(classroomsSource);
        }

        // Uses real-time listener via getClassroomsByAdmin
        classroomsSource = classroomRepository.getAdminClassrooms();
        classrooms.addSource(classroomsSource, resource -> {
            classrooms.setValue(resource);
            if (resource.isSuccess() && resource.data != null) {
                allClassrooms = resource.data;

                // Auto-refresh students when classroom data changes (e.g., student joins/leaves)
                if (currentClassroomId == null) {
                    loadAllStudents();
                } else {
                    // Find the updated classroom and reload students
                    for (Classroom classroom : allClassrooms) {
                        if (classroom.getId().equals(currentClassroomId)) {
                            loadStudentsForClassroom(classroom);
                            return;
                        }
                    }
                    // If classroom not found (deleted), load all
                    loadAllStudents();
                }
            }
        });
    }

    public void loadAllStudents() {
        currentClassroomId = null;
        students.setValue(Resource.loading(null));

        Set<String> studentIds = new HashSet<>();
        for (Classroom classroom : allClassrooms) {
            if (classroom.getStudentIds() != null) {
                studentIds.addAll(classroom.getStudentIds());
            }
        }

        if (studentIds.isEmpty()) {
            students.setValue(Resource.success(new ArrayList<>()));
            return;
        }

        List<String> newStudentIds = new ArrayList<>(studentIds);

        // Only reload if student IDs changed
        if (!areListsEqual(currentStudentIds, newStudentIds)) {
            currentStudentIds = newStudentIds;
            attachStudentsSource(newStudentIds);
        }
    }

    private boolean areListsEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    public void loadStudentsForClassroom(Classroom classroom) {
        currentClassroomId = classroom.getId();
        students.setValue(Resource.loading(null));

        if (classroom.getStudentIds() == null || classroom.getStudentIds().isEmpty()) {
            students.setValue(Resource.success(new ArrayList<>()));
            currentStudentIds = new ArrayList<>();
            return;
        }

        List<String> newStudentIds = classroom.getStudentIds();

        // Only reload if student IDs changed
        if (!areListsEqual(currentStudentIds, newStudentIds)) {
            currentStudentIds = new ArrayList<>(newStudentIds);
            attachStudentsSource(newStudentIds);
        }
    }

    private void attachStudentsSource(List<String> studentIds) {
        if (studentsSource != null) {
            students.removeSource(studentsSource);
        }

        // Uses real-time listener
        studentsSource = classroomRepository.getClassroomStudents(studentIds);
        students.addSource(studentsSource, students::setValue);
    }

    public void removeStudent(String studentId) {
        if (currentClassroomId == null) {
            removeResult.setValue(Resource.error("Please select a specific classroom", null));
            return;
        }

        // Clean up old source
        if (removeSource != null) {
            removeResult.removeSource(removeSource);
        }

        removeSource = classroomRepository.removeStudent(currentClassroomId, studentId);

        removeResult.addSource(removeSource, resource -> {
            removeResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                removeResult.removeSource(removeSource);
                removeSource = null;
                // No need to manually refresh - real-time listeners will handle it
            }
        });
    }

    public void refreshStudents() {
        // Force reload by clearing current student IDs
        currentStudentIds = new ArrayList<>();

        if (currentClassroomId == null) {
            loadAllStudents();
        } else {
            for (Classroom classroom : allClassrooms) {
                if (classroom.getId().equals(currentClassroomId)) {
                    loadStudentsForClassroom(classroom);
                    return;
                }
            }
            loadAllStudents();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up all sources
        if (classroomsSource != null) classrooms.removeSource(classroomsSource);
        if (studentsSource != null) students.removeSource(studentsSource);
        if (removeSource != null) removeResult.removeSource(removeSource);
    }

    public LiveData<Resource<List<Classroom>>> getClassrooms() {
        return classrooms;
    }

    public LiveData<Resource<List<User>>> getStudents() {
        return students;
    }

    public LiveData<Resource<Void>> getRemoveResult() {
        return removeResult;
    }
}
