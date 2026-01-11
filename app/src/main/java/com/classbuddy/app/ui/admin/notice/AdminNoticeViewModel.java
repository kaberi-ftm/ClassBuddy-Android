package com.classbuddy.app.ui.admin.notice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.data.repository.ClassroomRepository;
import com.classbuddy.app.data.repository.NoticeRepository;
import com.classbuddy.app.util.Resource;

import java.util.ArrayList;
import java.util.List;

public class AdminNoticeViewModel extends ViewModel {

    private final ClassroomRepository classroomRepository;
    private final NoticeRepository noticeRepository;

    private final MediatorLiveData<Resource<List<Notice>>> notices = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Void>> actionResult = new MediatorLiveData<>();

    private List<String> classroomIds = new ArrayList<>();

    public AdminNoticeViewModel() {
        classroomRepository = new ClassroomRepository();
        noticeRepository = new NoticeRepository();

        loadClassrooms();
    }

    private void loadClassrooms() {
        LiveData<Resource<List<Classroom>>> classroomSource = classroomRepository.getAdminClassrooms();
        notices.addSource(classroomSource, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                classroomIds.clear();
                for (Classroom classroom :  resource.data) {
                    classroomIds.add(classroom.getId());
                }
                loadNotices();
            }
        });
    }

    private void loadNotices() {
        if (classroomIds.isEmpty()) {
            notices.setValue(Resource.success(new ArrayList<>()));
            return;
        }

        LiveData<Resource<List<Notice>>> noticeSource = noticeRepository.getNoticesByClassrooms(classroomIds);
        notices.addSource(noticeSource, notices:: setValue);
    }

    public void refreshNotices() {
        loadClassrooms();
    }

    public void togglePin(String noticeId, boolean isPinned) {
        LiveData<Resource<Void>> source = noticeRepository. togglePinNotice(noticeId, isPinned);
        actionResult.addSource(source, resource -> {
            actionResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                actionResult.removeSource(source);
            }
        });
    }

    public void deleteNotice(String noticeId) {
        LiveData<Resource<Void>> source = noticeRepository.deleteNotice(noticeId);
        actionResult.addSource(source, resource -> {
            actionResult.setValue(resource);
            if (resource.isSuccess() || resource.isError()) {
                actionResult.removeSource(source);
            }
        });
    }

    public LiveData<Resource<List<Notice>>> getNotices() {
        return notices;
    }

    public LiveData<Resource<Void>> getActionResult() {
        return actionResult;
    }
}
