package com.classbuddy.app.ui.student.notice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.data.repository.NoticeRepository;
import com.classbuddy.app.data.repository.UserRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StudentNoticeViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;

    private final MediatorLiveData<Resource<List<Notice>>> filteredNotices = new MediatorLiveData<>();
    private final MutableLiveData<String> currentPriorityFilter = new MutableLiveData<>(null);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    // Track current sources for cleanup
    private LiveData<Resource<User>> userSource;
    private LiveData<Resource<List<Notice>>> noticeSource;

    // Track current classroom IDs to detect changes
    private List<String> currentClassroomIds = new ArrayList<>();

    private List<Notice> allNotices = new ArrayList<>();

    public StudentNoticeViewModel() {
        userRepository = new UserRepository();
        noticeRepository = new NoticeRepository();
    }

    public void loadAllNotices() {
        filteredNotices.setValue(Resource.loading(null));

        // Clean up old source
        if (userSource != null) {
            filteredNotices.removeSource(userSource);
        }

        userSource = userRepository.getCurrentUser();
        filteredNotices.addSource(userSource, userResource -> {
            if (userResource.isSuccess() && userResource.data != null) {
                List<String> newClassroomIds = userResource.data.getJoinedClassrooms();
                if (newClassroomIds == null) {
                    newClassroomIds = new ArrayList<>();
                }

                // Check if classroom IDs changed
                if (!areListsEqual(currentClassroomIds, newClassroomIds)) {
                    currentClassroomIds = new ArrayList<>(newClassroomIds);

                    if (!newClassroomIds.isEmpty()) {
                        loadNoticesForClassrooms(newClassroomIds);
                    } else {
                        allNotices = new ArrayList<>();
                        filteredNotices.setValue(Resource.success(new ArrayList<>()));
                    }
                }
            } else if (userResource.isError()) {
                filteredNotices.setValue(Resource.error(userResource.message, null));
            }
        });
    }

    private boolean areListsEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    private void loadNoticesForClassrooms(List<String> classroomIds) {
        // Clean up old source
        if (noticeSource != null) {
            filteredNotices.removeSource(noticeSource);
        }

        noticeSource = noticeRepository.getNoticesByClassrooms(classroomIds);
        filteredNotices.addSource(noticeSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                allNotices = resource.data;
                applyFilters();
            } else {
                filteredNotices.setValue(resource);
            }
        });
    }

    public void loadNoticesForClassroom(String classroomId) {
        filteredNotices.setValue(Resource.loading(null));

        // Clean up old source
        if (noticeSource != null) {
            filteredNotices.removeSource(noticeSource);
        }

        noticeSource = noticeRepository.getNoticesByClassroom(classroomId);
        filteredNotices.addSource(noticeSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                allNotices = resource.data;
                applyFilters();
            } else {
                filteredNotices.setValue(resource);
            }
        });
    }

    public void filterByPriority(String priority) {
        currentPriorityFilter.setValue(priority);
        applyFilters();
    }

    public void searchNotices(String query) {
        searchQuery.setValue(query);
        applyFilters();
    }

    public void markAsRead(String noticeId) {
        noticeRepository.markAsRead(noticeId);
    }

    private void applyFilters() {
        String priority = currentPriorityFilter.getValue();
        String query = searchQuery.getValue();

        List<Notice> filtered = allNotices;

        // Apply priority filter
        if (priority != null && !priority.isEmpty()) {
            filtered = filtered.stream()
                    .filter(notice -> notice.getPriority().equalsIgnoreCase(priority))
                    .collect(Collectors.toList());
        }

        // Apply search filter
        if (query != null && !query.isEmpty()) {
            String lowerQuery = query.toLowerCase();
            filtered = filtered.stream()
                    .filter(notice ->
                            notice.getTitle().toLowerCase().contains(lowerQuery) ||
                                    notice.getContent().toLowerCase().contains(lowerQuery) ||
                                    notice.getClassroomName().toLowerCase().contains(lowerQuery))
                    .collect(Collectors.toList());
        }

        filteredNotices.setValue(Resource.success(filtered));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up all sources
        if (userSource != null) filteredNotices.removeSource(userSource);
        if (noticeSource != null) filteredNotices.removeSource(noticeSource);
    }

    public LiveData<Resource<List<Notice>>> getFilteredNotices() {
        return filteredNotices;
    }
}
