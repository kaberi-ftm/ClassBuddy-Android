package com.classbuddy.app.ui.admin.routine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.RoutineRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminRoutineViewModel extends ViewModel {

    private final RoutineRepository routineRepository;
    private final ClassroomRepository classroomRepository;

    private final MediatorLiveData<Resource<List<Routine>>> filteredRoutines = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> deleteResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> cancelResult = new MediatorLiveData<>();
    private final MutableLiveData<Integer> selectedDayIndex = new MutableLiveData<>(0);

    private String classroomId;
    private List<Routine> allRoutines = new ArrayList<>();
    private Classroom currentClassroom;

    public AdminRoutineViewModel() {
        routineRepository = new RoutineRepository();
        classroomRepository = new ClassroomRepository();
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
        loadClassroomDetails();
        loadRoutines();
    }

    private void loadClassroomDetails() {
        if (classroomId == null) return;
        LiveData<Resource<List<Classroom>>> source = classroomRepository.getAdminClassrooms();
        filteredRoutines.addSource(source, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                for (Classroom classroom : resource.data) {
                    if (classroom.getId().equals(classroomId)) {
                        currentClassroom = classroom;
                        break;
                    }
                }
            }
        });
    }

    public void loadRoutines() {
        if (classroomId == null) return;

        filteredRoutines.setValue(Resource.loading(null));

        LiveData<Resource<List<Routine>>> source = routineRepository.getRoutinesByClassroom(classroomId);
        filteredRoutines.addSource(source, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                allRoutines = resource. data;
                applyDayFilter();
            } else {
                filteredRoutines.setValue(resource);
            }
        });
    }

    public void filterByDay(int dayIndex) {
        selectedDayIndex.setValue(dayIndex);
        applyDayFilter();
    }

    private void applyDayFilter() {
        Integer dayIndex = selectedDayIndex.getValue();
        if (dayIndex == null) dayIndex = 0;

        final int filterDay = dayIndex;
        List<Routine> filtered = allRoutines.stream()
                .filter(routine -> routine. getDayIndex() == filterDay)
                .collect(Collectors.toList());

        filteredRoutines.setValue(Resource.success(filtered));
    }

    public void deleteRoutine(String routineId) {
        LiveData<Resource<Void>> source = routineRepository.deleteRoutine(routineId);
        deleteResult.addSource(source, resource -> {
            deleteResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                deleteResult.removeSource(source);
                if (resource.isSuccess()) {
                    loadRoutines(); // Refresh list
                }
            }
        });
    }

    public void cancelClass(Routine routine, String reason, String date) {
        LiveData<Resource<Void>> source = routineRepository.cancelClass(routine.getId(), reason, date);
        cancelResult.addSource(source, resource -> {
            cancelResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                cancelResult.removeSource(source);
                if (resource.isSuccess()) {
                    // Notify students
                    if (currentClassroom != null && currentClassroom.getStudentIds() != null) {
                        routineRepository.notifyStudentsOfClassCancellation(
                                currentClassroom.getStudentIds(), routine, reason, date
                        );
                    }
                    loadRoutines();
                }
            }
        });
    }

    public void restoreClass(String routineId) {
        LiveData<Resource<Void>> source = routineRepository.restoreClass(routineId);
        cancelResult.addSource(source, resource -> {
            cancelResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                cancelResult.removeSource(source);
                if (resource.isSuccess()) {
                    loadRoutines();
                }
            }
        });
    }

    public LiveData<Resource<List<Routine>>> getFilteredRoutines() {
        return filteredRoutines;
    }

    public LiveData<Resource<Void>> getDeleteResult() {
        return deleteResult;
    }

    public LiveData<Resource<Void>> getCancelResult() {
        return cancelResult;
    }
}
