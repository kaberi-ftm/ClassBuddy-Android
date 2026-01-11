package com.classbuddy.app.ui.student.routine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.RoutineRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.DateTimeUtils;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentRoutineViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;

    private final MediatorLiveData<Resource<List<Routine>>> filteredRoutines = new MediatorLiveData<>();
    private final MutableLiveData<Integer> selectedDayIndex = new MutableLiveData<>(DateTimeUtils.getCurrentDayIndex());

    // Track current sources for cleanup
    private LiveData<Resource<User>> userSource;
    private LiveData<Resource<List<Routine>>> routineSource;

    // Track current classroom IDs to detect changes
    private List<String> currentClassroomIds = new ArrayList<>();

    private List<Routine> allRoutines = new ArrayList<>();

    public StudentRoutineViewModel() {
        userRepository = new UserRepository();
        routineRepository = new RoutineRepository();
    }

    public void loadAllRoutines() {
        filteredRoutines.setValue(Resource.loading(null));

        // Clean up old source
        if (userSource != null) {
            filteredRoutines.removeSource(userSource);
        }

        userSource = userRepository.getCurrentUser();
        filteredRoutines.addSource(userSource, userResource -> {
            if (userResource.isSuccess() && userResource.data != null) {
                List<String> newClassroomIds = userResource.data.getJoinedClassrooms();
                if (newClassroomIds == null) {
                    newClassroomIds = new ArrayList<>();
                }

                // Check if classroom IDs changed
                if (!areListsEqual(currentClassroomIds, newClassroomIds)) {
                    currentClassroomIds = new ArrayList<>(newClassroomIds);

                    if (!newClassroomIds.isEmpty()) {
                        loadRoutinesForClassrooms(newClassroomIds);
                    } else {
                        allRoutines = new ArrayList<>();
                        filteredRoutines.setValue(Resource.success(new ArrayList<>()));
                    }
                }
            } else if (userResource.isError()) {
                filteredRoutines.setValue(Resource.error(userResource.message, null));
            }
        });
    }

    private boolean areListsEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    private void loadRoutinesForClassrooms(List<String> classroomIds) {
        // Clean up old source
        if (routineSource != null) {
            filteredRoutines.removeSource(routineSource);
        }

        routineSource = routineRepository.getRoutinesByClassrooms(classroomIds);
        filteredRoutines.addSource(routineSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                allRoutines = resource.data;
                applyDayFilter();
            } else {
                filteredRoutines.setValue(resource);
            }
        });
    }

    public void loadRoutinesForClassroom(String classroomId) {
        filteredRoutines.setValue(Resource.loading(null));

        // Clean up old source
        if (routineSource != null) {
            filteredRoutines.removeSource(routineSource);
        }

        routineSource = routineRepository.getRoutinesByClassroom(classroomId);
        filteredRoutines.addSource(routineSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                allRoutines = resource.data;
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
        if (dayIndex == null) dayIndex = DateTimeUtils.getCurrentDayIndex();

        final int filterDay = dayIndex;
        List<Routine> filtered = allRoutines.stream()
                .filter(routine -> routine.getDayIndex() == filterDay)
                .sorted((r1, r2) -> {
                    // Sort by classroom name first, then by start time
                    int classroomCompare = r1.getClassroomName().compareTo(r2.getClassroomName());
                    if (classroomCompare != 0) return classroomCompare;
                    return r1.getStartTime().compareTo(r2.getStartTime());
                })
                .collect(Collectors.toList());

        filteredRoutines.setValue(Resource.success(filtered));
    }

    /**
     * Get routines grouped by classroom for better display
     */
    public Map<String, List<Routine>> getRoutinesGroupedByClassroom() {
        Map<String, List<Routine>> grouped = new LinkedHashMap<>();
        for (Routine routine : allRoutines) {
            String classroomName = routine.getClassroomName();
            if (!grouped.containsKey(classroomName)) {
                grouped.put(classroomName, new ArrayList<>());
            }
            grouped.get(classroomName).add(routine);
        }
        return grouped;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up all sources
        if (userSource != null) filteredRoutines.removeSource(userSource);
        if (routineSource != null) filteredRoutines.removeSource(routineSource);
    }

    public LiveData<Resource<List<Routine>>> getFilteredRoutines() {
        return filteredRoutines;
    }
}
