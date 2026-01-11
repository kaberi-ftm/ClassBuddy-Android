package com.classbuddy.app.ui.student.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.CalendarEvent;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.ExamRepository;
import com.classbuddy.app.data.repository.RoutineRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.DateTimeUtils;
import com.classbuddy.app.util.Resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;
    private final ExamRepository examRepository;

    private final MediatorLiveData<Resource<List<CalendarEvent>>> eventsForSelectedDate = new MediatorLiveData<>();

    // Track current sources for cleanup
    private LiveData<Resource<User>> userSource;
    private LiveData<Resource<List<Routine>>> routineSource;
    private LiveData<Resource<List<Exam>>> examSource;

    private List<String> classroomIds = new ArrayList<>();
    private List<Routine> allRoutines = new ArrayList<>();
    private List<Exam> allExams = new ArrayList<>();
    private Date selectedDate = new Date();

    // Track current classroom IDs to detect changes
    private List<String> currentClassroomIds = new ArrayList<>();

    public CalendarViewModel() {
        userRepository = new UserRepository();
        routineRepository = new RoutineRepository();
        examRepository = new ExamRepository();

        loadUserData();
    }

    private void loadUserData() {
        // Clean up old source
        if (userSource != null) {
            eventsForSelectedDate.removeSource(userSource);
        }

        userSource = userRepository.getCurrentUser();
        eventsForSelectedDate.addSource(userSource, userResource -> {
            if (userResource.isSuccess() && userResource.data != null) {
                List<String> newClassroomIds = userResource.data.getJoinedClassrooms();
                if (newClassroomIds == null) {
                    newClassroomIds = new ArrayList<>();
                }

                // Check if classroom IDs changed
                if (!areListsEqual(currentClassroomIds, newClassroomIds)) {
                    currentClassroomIds = new ArrayList<>(newClassroomIds);
                    classroomIds = newClassroomIds;

                    if (!classroomIds.isEmpty()) {
                        loadRoutines();
                        loadExams();
                    } else {
                        allRoutines = new ArrayList<>();
                        allExams = new ArrayList<>();
                        eventsForSelectedDate.setValue(Resource.success(new ArrayList<>()));
                    }
                }
            }
        });
    }

    private boolean areListsEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    private void loadRoutines() {
        // Clean up old source
        if (routineSource != null) {
            eventsForSelectedDate.removeSource(routineSource);
        }

        routineSource = routineRepository.getRoutinesByClassrooms(classroomIds);
        eventsForSelectedDate.addSource(routineSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                allRoutines = resource.data;
                generateEventsForDate(selectedDate);
            }
        });
    }

    private void loadExams() {
        // Clean up old source
        if (examSource != null) {
            eventsForSelectedDate.removeSource(examSource);
        }

        examSource = examRepository.getExamsByClassrooms(classroomIds);
        eventsForSelectedDate.addSource(examSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                allExams = resource.data;
                generateEventsForDate(selectedDate);
            }
        });
    }

    public void loadEventsForDate(Date date) {
        this.selectedDate = date;
        generateEventsForDate(date);
    }

    public void refreshEvents() {
        // Force reload by clearing tracked classroom IDs
        currentClassroomIds = new ArrayList<>();
        loadUserData();
    }

    private void generateEventsForDate(Date date) {
        List<CalendarEvent> events = new ArrayList<>();

        // Get day of week for routines
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String dayName = DateTimeUtils.getDayOfWeekFromDate(date);
        
        // Format the selected date for comparison
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDateStr = sdf.format(date);

        // Add routines for this day
        for (Routine routine : allRoutines) {
            boolean shouldAdd = false;
            boolean isCancelledForThisDate = false;
            
            // Check if this is a specific date class
            if (routine.getSpecificDate() != null && !routine.getSpecificDate().isEmpty()) {
                // One-time class - check if it matches the selected date
                if (routine.getSpecificDate().equals(selectedDateStr)) {
                    shouldAdd = true;
                    isCancelledForThisDate = routine.isCancelled();
                }
            } else {
                // Recurring weekly class - check day of week
                if (routine.getDayIndex() == dayIndex) {
                    shouldAdd = true;
                    // Check if cancelled for this specific date
                    if (routine.isCancelled() && routine.getCancelledDate() != null) {
                        isCancelledForThisDate = routine.getCancelledDate().equals(selectedDateStr);
                    }
                }
            }
            
            if (shouldAdd) {
                String title = routine.getSubject();
                if (isCancelledForThisDate) {
                    title = routine.getSubject() + " (CANCELLED)";
                }
                
                CalendarEvent event = new CalendarEvent(
                        routine.getId(),
                        title,
                        routine.getClassroomName(),
                        DateTimeUtils.formatTime(routine.getStartTime()) + " - " +
                                DateTimeUtils.formatTime(routine.getEndTime()),
                        routine.getType().equalsIgnoreCase("lab") ? "Lab" : "Class",
                        "Room: " + routine.getRoom() + " | " + routine.getFaculty(),
                        date,
                        isCancelledForThisDate
                );
                events.add(event);
            }
        }

        // Add exams for this date
        for (Exam exam : allExams) {
            if (exam.getExamDate() != null &&
                    DateTimeUtils.isSameDay(exam.getExamDate().toDate(), date)) {
                
                String title = exam.getExamTypeDisplay() + ": " + exam.getCourseName();
                if (exam.isCancelled()) {
                    title = title + " (CANCELLED)";
                }
                
                CalendarEvent event = new CalendarEvent(
                        exam.getId(),
                        title,
                        exam.getClassroomName(),
                        DateTimeUtils.formatTime(exam.getStartTime()),
                        "Exam",
                        "Course: " + exam.getCourseNo() + " | Room: " + exam.getRoom(),
                        date,
                        exam.isCancelled()
                );
                events.add(event);
            }
        }

        // Sort by time
        events.sort((e1, e2) -> e1.getTime().compareTo(e2.getTime()));

        eventsForSelectedDate.setValue(Resource.success(events));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up all sources
        if (userSource != null) eventsForSelectedDate.removeSource(userSource);
        if (routineSource != null) eventsForSelectedDate.removeSource(routineSource);
        if (examSource != null) eventsForSelectedDate.removeSource(examSource);
    }

    public LiveData<Resource<List<CalendarEvent>>> getEventsForSelectedDate() {
        return eventsForSelectedDate;
    }
}
