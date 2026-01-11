package com.classbuddy.app.ui.student.classroom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.List;

public class MyClassroomsViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    private final MediatorLiveData<Resource<List<Classroom>>> classrooms = new MediatorLiveData<>();

    // Track current sources for cleanup
    private LiveData<Resource<User>> userSource;
    private LiveData<Resource<List<Classroom>>> classroomSource;

    // Track current classroom IDs to detect changes
    private List<String> currentClassroomIds = new ArrayList<>();

    public MyClassroomsViewModel() {
        userRepository = new UserRepository();
        classroomRepository = new ClassroomRepository();

        loadClassrooms();
    }

    private void loadClassrooms() {
        // Clean up old source
        if (userSource != null) {
            classrooms.removeSource(userSource);
        }

        userSource = userRepository.getCurrentUser();
        classrooms.addSource(userSource, userResource -> {
            if (userResource.isSuccess() && userResource.data != null) {
                List<String> newClassroomIds = userResource.data.getJoinedClassrooms();
                if (newClassroomIds == null) {
                    newClassroomIds = new ArrayList<>();
                }

                // Check if classroom IDs changed
                if (!areListsEqual(currentClassroomIds, newClassroomIds)) {
                    currentClassroomIds = new ArrayList<>(newClassroomIds);

                    if (!newClassroomIds.isEmpty()) {
                        loadClassroomsFromIds(newClassroomIds);
                    } else {
                        classrooms.setValue(Resource.success(new ArrayList<>()));
                    }
                }
            } else if (userResource.isError()) {
                classrooms.setValue(Resource.error(userResource.message, null));
            } else if (userResource.isLoading()) {
                classrooms.setValue(Resource.loading(null));
            }
        });
    }

    private boolean areListsEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    private void loadClassroomsFromIds(List<String> classroomIds) {
        // Clean up old source
        if (classroomSource != null) {
            classrooms.removeSource(classroomSource);
        }

        classroomSource = classroomRepository.getStudentClassrooms(classroomIds);
        classrooms.addSource(classroomSource, classrooms::setValue);
    }

    public void refreshClassrooms() {
        // Force reload by clearing tracked classroom IDs
        currentClassroomIds = new ArrayList<>();
        loadClassrooms();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up all sources
        if (userSource != null) classrooms.removeSource(userSource);
        if (classroomSource != null) classrooms.removeSource(classroomSource);
    }

    public LiveData<Resource<List<Classroom>>> getClassrooms() {
        return classrooms;
    }
}
