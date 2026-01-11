package com.classbuddy.app.ui.student.classroom;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.util.Resource;

public class JoinClassroomViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final MediatorLiveData<Resource<Void>> joinResult = new MediatorLiveData<>();

    private LiveData<Resource<Classroom>> currentClassroomSource;
    private LiveData<Resource<Void>> currentJoinSource;

    public JoinClassroomViewModel() {
        classroomRepository = new ClassroomRepository();
    }

    public void joinClassroom(String code, String password) {
        // Clean up any existing sources to prevent memory leaks
        cleanupSources();

        joinResult.setValue(Resource.loading(null));

        currentClassroomSource = classroomRepository.getClassroomByCode(code);
        joinResult.addSource(currentClassroomSource, classroomResource -> {
            // Only process non-loading states
            if (classroomResource.status == Resource.Status.LOADING) {
                return; // Keep showing loading
            }

            // Remove this source immediately after getting result
            joinResult.removeSource(currentClassroomSource);
            currentClassroomSource = null;

            if (classroomResource.isSuccess() && classroomResource.data != null) {
                Classroom classroom = classroomResource.data;

                currentJoinSource = classroomRepository.joinClassroom(
                        classroom.getId(), password, classroom);

                joinResult.addSource(currentJoinSource, resource -> {
                    // Only process non-loading states from join operation
                    if (resource.status == Resource.Status.LOADING) {
                        return; // Keep showing loading
                    }

                    joinResult.setValue(resource);

                    // Clean up after completion
                    if (currentJoinSource != null) {
                        joinResult.removeSource(currentJoinSource);
                        currentJoinSource = null;
                    }
                });
            } else if (classroomResource.isError()) {
                joinResult.setValue(Resource.error(classroomResource.message != null ?
                        classroomResource.message : "Failed to find classroom", null));
            } else {
                // Handle case where data is null but not explicitly error
                joinResult.setValue(Resource.error("Classroom not found", null));
            }
        });
    }

    private void cleanupSources() {
        if (currentClassroomSource != null) {
            joinResult.removeSource(currentClassroomSource);
            currentClassroomSource = null;
        }
        if (currentJoinSource != null) {
            joinResult.removeSource(currentJoinSource);
            currentJoinSource = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleanupSources();
    }

    public LiveData<Resource<Void>> getJoinResult() {
        return joinResult;
    }
}
