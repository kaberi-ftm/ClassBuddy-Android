package com.classbuddy.app.ui.admin.routine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.repository.RoutineRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminRoutineViewModel extends ViewModel {

    private final RoutineRepository routineRepository;

    private final MediatorLiveData<Resource<List<Routine>>> filteredRoutines = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> deleteResult = new MediatorLiveData<>();
    private final MutableLiveData<Integer> selectedDayIndex = new MutableLiveData<>(0);

    private String classroomId;
    private List<Routine> allRoutines = new ArrayList<>();

    public AdminRoutineViewModel() {
        routineRepository = new RoutineRepository();
    }

    public void setClassroomId(String classroomId) {
        this.classroomId = classroomId;
        loadRoutines();
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

    public LiveData<Resource<List<Routine>>> getFilteredRoutines() {
        return filteredRoutines;
    }

    public LiveData<Resource<Void>> getDeleteResult() {
        return deleteResult;
    }
}
