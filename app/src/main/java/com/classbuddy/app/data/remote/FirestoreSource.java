package com.classbuddy.app.data.remote;


import androidx.lifecycle. LiveData;
import androidx.lifecycle.MutableLiveData;

import com.classbuddy.app.data.model.Classroom;
import com. classbuddy.app.data.model. Exam;
import com.classbuddy.app.data.model.Notice;
import com. classbuddy.app.data.model.Notification;
import com. classbuddy.app.data.model.Routine;
import com. classbuddy.app.data.model.User;
import com. classbuddy.app.util.Constants;
import com. classbuddy.app.util.Resource;
import com.google.firebase.firestore.DocumentReference;
import com. google.firebase.firestore.DocumentSnapshot;
import com.google.firebase. firestore. FieldValue;
import com.google.firebase. firestore.FirebaseFirestore;
import com.google. firebase.firestore. Query;
import com. google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java. util.HashMap;
import java. util.List;
import java.util. Map;

public class FirestoreSource {

    private final FirebaseFirestore firestore;

    public FirestoreSource() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ==================== USER OPERATIONS ====================

    public LiveData<Resource<Void>> createUser(User user) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants. COLLECTION_USERS)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<User>> getUser(String userId) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource. loading(null));

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot. toObject(User. class);
                        result.setValue(Resource.success(user));
                    } else {
                        result.setValue(Resource. error("User not found", null));
                    }
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<User>> getUserRealtime(String userId) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource. loading(null));

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot. toObject(User. class);
                        result.setValue(Resource.success(user));
                    }
                });

        return result;
    }

    public LiveData<Resource<Void>> updateUser(String userId, Map<String, Object> updates) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> updateFcmToken(String userId, String token) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);
        return updateUser(userId, updates);
    }

    // ==================== CLASSROOM OPERATIONS ====================

    public LiveData<Resource<String>> createClassroom(Classroom classroom) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        DocumentReference docRef = firestore. collection(Constants. COLLECTION_CLASSROOMS).document();
        classroom.setId(docRef.getId());

        docRef.set(classroom)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(docRef.getId())))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Classroom>> getClassroom(String classroomId) {
        MutableLiveData<Resource<Classroom>> result = new MutableLiveData<>();
        result.setValue(Resource. loading(null));

        firestore.collection(Constants.COLLECTION_CLASSROOMS)
                .document(classroomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Classroom classroom = documentSnapshot.toObject(Classroom.class);
                        result. setValue(Resource.success(classroom));
                    } else {
                        result.setValue(Resource.error("Classroom not found", null));
                    }
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e. getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Classroom>> getClassroomByCode(String code) {
        MutableLiveData<Resource<Classroom>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants. COLLECTION_CLASSROOMS)
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (! querySnapshot.isEmpty()) {
                        Classroom classroom = querySnapshot.getDocuments().get(0).toObject(Classroom.class);
                        result. setValue(Resource.success(classroom));
                    } else {
                        result.setValue(Resource.error("Invalid classroom code", null));
                    }
                })
                .addOnFailureListener(e -> result. setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<List<Classroom>>> getClassroomsByAdmin(String adminId) {
        MutableLiveData<Resource<List<Classroom>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_CLASSROOMS)
                .whereEqualTo("adminId", adminId)
                .orderBy("createdAt", Query. Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource. error(error.getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Classroom> classrooms = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Classroom classroom = doc. toObject(Classroom.class);
                            if (classroom != null) {
                                classrooms.add(classroom);
                            }
                        }
                        result.setValue(Resource.success(classrooms));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Classroom>>> getClassroomsByStudent(List<String> classroomIds) {
        MutableLiveData<Resource<List<Classroom>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds.isEmpty()) {
            result. setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result.setValue(Resource. loading(null));

        firestore.collection(Constants.COLLECTION_CLASSROOMS)
                .whereIn("id", classroomIds)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error. getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Classroom> classrooms = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot. getDocuments()) {
                            Classroom classroom = doc.toObject(Classroom. class);
                            if (classroom != null) {
                                classrooms.add(classroom);
                            }
                        }
                        result. setValue(Resource.success(classrooms));
                    }
                });

        return result;
    }

    public LiveData<Resource<Void>> updateClassroom(String classroomId, Map<String, Object> updates) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_CLASSROOMS)
                .document(classroomId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result. setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource. error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> deleteClassroom(String classroomId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_CLASSROOMS)
                .document(classroomId)
                .delete()
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> addStudentToClassroom(String classroomId, String studentId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, Object> updates = new HashMap<>();
        updates.put("studentIds", FieldValue.arrayUnion(studentId));
        updates.put("studentCount", FieldValue. increment(1));

        firestore.collection(Constants.COLLECTION_CLASSROOMS)
                .document(classroomId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Also update user's joined classrooms
                    Map<String, Object> userUpdates = new HashMap<>();
                    userUpdates.put("joinedClassrooms", FieldValue. arrayUnion(classroomId));
                    firestore.collection(Constants.COLLECTION_USERS)
                            .document(studentId)
                            . update(userUpdates);
                    result. setValue(Resource.success(null));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> removeStudentFromClassroom(String classroomId, String studentId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, Object> updates = new HashMap<>();
        updates.put("studentIds", FieldValue.arrayRemove(studentId));
        updates.put("studentCount", FieldValue.increment(-1));

        firestore.collection(Constants.COLLECTION_CLASSROOMS)
                .document(classroomId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Also update user's joined classrooms
                    Map<String, Object> userUpdates = new HashMap<>();
                    userUpdates.put("joinedClassrooms", FieldValue. arrayRemove(classroomId));
                    firestore. collection(Constants. COLLECTION_USERS)
                            . document(studentId)
                            .update(userUpdates);
                    result.setValue(Resource.success(null));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<List<User>>> getStudentsInClassroom(List<String> studentIds) {
        MutableLiveData<Resource<List<User>>> result = new MutableLiveData<>();

        if (studentIds == null || studentIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result.setValue(Resource. loading(null));

        firestore.collection(Constants.COLLECTION_USERS)
                .whereIn("id", studentIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> students = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            students. add(user);
                        }
                    }
                    result.setValue(Resource.success(students));
                })
                .addOnFailureListener(e -> result.setValue(Resource. error(e.getMessage(), null)));

        return result;
    }

    // ==================== ROUTINE OPERATIONS ====================

    public LiveData<Resource<String>> createRoutine(Routine routine) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        DocumentReference docRef = firestore.collection(Constants.COLLECTION_ROUTINES).document();
        routine.setId(docRef. getId());

        docRef.set(routine)
                .addOnSuccessListener(aVoid -> result.setValue(Resource. success(docRef.getId())))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<List<Routine>>> getRoutinesByClassroom(String classroomId) {
        MutableLiveData<Resource<List<Routine>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore. collection(Constants. COLLECTION_ROUTINES)
                .whereEqualTo("classroomId", classroomId)
                .orderBy("dayIndex")
                .orderBy("startTime")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error. getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Routine> routines = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot. getDocuments()) {
                            Routine routine = doc.toObject(Routine. class);
                            if (routine != null) {
                                routines.add(routine);
                            }
                        }
                        result. setValue(Resource.success(routines));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Routine>>> getRoutinesByClassrooms(List<String> classroomIds) {
        MutableLiveData<Resource<List<Routine>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds. isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result. setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_ROUTINES)
                .whereIn("classroomId", classroomIds)
                .orderBy("dayIndex")
                .orderBy("startTime")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Routine> routines = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Routine routine = doc.toObject(Routine.class);
                            if (routine != null) {
                                routines. add(routine);
                            }
                        }
                        result.setValue(Resource.success(routines));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Routine>>> getTodaysRoutine(List<String> classroomIds, int dayIndex) {
        MutableLiveData<Resource<List<Routine>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds. isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result. setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_ROUTINES)
                .whereIn("classroomId", classroomIds)
                .whereEqualTo("dayIndex", dayIndex)
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Routine> routines = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot. getDocuments()) {
                        Routine routine = doc.toObject(Routine. class);
                        if (routine != null) {
                            routines.add(routine);
                        }
                    }
                    result. setValue(Resource.success(routines));
                })
                .addOnFailureListener(e -> result. setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> updateRoutine(String routineId, Map<String, Object> updates) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_ROUTINES)
                .document(routineId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result. setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> deleteRoutine(String routineId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore. collection(Constants. COLLECTION_ROUTINES)
                .document(routineId)
                .delete()
                .addOnSuccessListener(aVoid -> result. setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource. error(e.getMessage(), null)));

        return result;
    }

    // ==================== EXAM OPERATIONS ====================

    public LiveData<Resource<String>> createExam(Exam exam) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource. loading(null));

        DocumentReference docRef = firestore.collection(Constants.COLLECTION_EXAMS).document();
        exam.setId(docRef.getId());

        docRef.set(exam)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(docRef.getId())))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<List<Exam>>> getExamsByClassroom(String classroomId) {
        MutableLiveData<Resource<List<Exam>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants. COLLECTION_EXAMS)
                .whereEqualTo("classroomId", classroomId)
                .orderBy("examDate", Query.Direction. ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error. getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Exam> exams = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot. getDocuments()) {
                            Exam exam = doc.toObject(Exam. class);
                            if (exam != null) {
                                exams.add(exam);
                            }
                        }
                        result. setValue(Resource.success(exams));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Exam>>> getExamsByClassrooms(List<String> classroomIds) {
        MutableLiveData<Resource<List<Exam>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result.setValue(Resource.loading(null));

        firestore. collection(Constants. COLLECTION_EXAMS)
                .whereIn("classroomId", classroomIds)
                .orderBy("examDate", Query.Direction. ASCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Exam> exams = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Exam exam = doc.toObject(Exam.class);
                            if (exam != null) {
                                exams. add(exam);
                            }
                        }
                        result.setValue(Resource.success(exams));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Exam>>> getUpcomingExams(List<String> classroomIds) {
        MutableLiveData<Resource<List<Exam>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds. isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result. setValue(Resource.loading(null));

        com.google.firebase. Timestamp now = com.google.firebase. Timestamp.now();

        firestore.collection(Constants.COLLECTION_EXAMS)
                .whereIn("classroomId", classroomIds)
                .whereGreaterThanOrEqualTo("examDate", now)
                .orderBy("examDate", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Exam> exams = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Exam exam = doc.toObject(Exam.class);
                        if (exam != null) {
                            exams. add(exam);
                        }
                    }
                    result.setValue(Resource.success(exams));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e. getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> updateExam(String examId, Map<String, Object> updates) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_EXAMS)
                .document(examId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> deleteExam(String examId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_EXAMS)
                .document(examId)
                .delete()
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    // ==================== NOTICE OPERATIONS ====================

    public LiveData<Resource<String>> createNotice(Notice notice) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        DocumentReference docRef = firestore. collection(Constants. COLLECTION_NOTICES).document();
        notice.setId(docRef.getId());

        docRef.set(notice)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(docRef.getId())))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<List<Notice>>> getNoticesByClassroom(String classroomId) {
        MutableLiveData<Resource<List<Notice>>> result = new MutableLiveData<>();
        result.setValue(Resource. loading(null));

        firestore.collection(Constants.COLLECTION_NOTICES)
                .whereEqualTo("classroomId", classroomId)
                .orderBy("isPinned", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction. DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result. setValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Notice> notices = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Notice notice = doc.toObject(Notice.class);
                            if (notice != null) {
                                notices.add(notice);
                            }
                        }
                        result.setValue(Resource.success(notices));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Notice>>> getNoticesByClassrooms(List<String> classroomIds) {
        MutableLiveData<Resource<List<Notice>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds. isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result. setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_NOTICES)
                .whereIn("classroomId", classroomIds)
                .orderBy("isPinned", Query.Direction.DESCENDING)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource. error(error.getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Notice> notices = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Notice notice = doc. toObject(Notice. class);
                            if (notice != null) {
                                notices.add(notice);
                            }
                        }
                        result.setValue(Resource.success(notices));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Notice>>> getRecentNotices(List<String> classroomIds, int limit) {
        MutableLiveData<Resource<List<Notice>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result.setValue(Resource. loading(null));

        firestore.collection(Constants.COLLECTION_NOTICES)
                .whereIn("classroomId", classroomIds)
                .orderBy("createdAt", Query.Direction. DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Notice> notices = new ArrayList<>();
                    for (DocumentSnapshot doc :  querySnapshot.getDocuments()) {
                        Notice notice = doc.toObject(Notice.class);
                        if (notice != null) {
                            notices.add(notice);
                        }
                    }
                    result. setValue(Resource.success(notices));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> updateNotice(String noticeId, Map<String, Object> updates) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore. collection(Constants. COLLECTION_NOTICES)
                .document(noticeId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(Resource. success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e. getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> markNoticeAsRead(String noticeId, String studentId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, Object> updates = new HashMap<>();
        updates.put("readByStudentIds", FieldValue. arrayUnion(studentId));

        firestore.collection(Constants.COLLECTION_NOTICES)
                .document(noticeId)
                .update(updates)
                . addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> deleteNotice(String noticeId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_NOTICES)
                .document(noticeId)
                .delete()
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    // ==================== NOTIFICATION OPERATIONS ====================

    public LiveData<Resource<String>> createNotification(Notification notification) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        DocumentReference docRef = firestore. collection(Constants. COLLECTION_NOTIFICATIONS).document();
        notification.setId(docRef.getId());

        docRef.set(notification)
                .addOnSuccessListener(aVoid -> result.setValue(Resource. success(docRef.getId())))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<List<Notification>>> getNotificationsByUser(String userId) {
        MutableLiveData<Resource<List<Notification>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants. COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query. Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error. getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Notification> notifications = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) {
                                notifications.add(notification);
                            }
                        }
                        result.setValue(Resource. success(notifications));
                    }
                });

        return result;
    }

    public LiveData<Resource<Void>> markNotificationAsRead(String notificationId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        Map<String, Object> updates = new HashMap<>();
        updates.put("isRead", true);

        firestore.collection(Constants. COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> markAllNotificationsAsRead(String userId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc :  querySnapshot.getDocuments()) {
                        doc.getReference().update("isRead", true);
                    }
                    result. setValue(Resource.success(null));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> deleteNotification(String notificationId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> result.setValue(Resource. success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e. getMessage(), null)));

        return result;
    }

    public LiveData<Resource<Void>> deleteAllNotifications(String userId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    result. setValue(Resource.success(null));
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));

        return result;
    }

    // ==================== BATCH NOTIFICATION CREATION ====================

    public void sendNotificationToStudents(List<String> studentIds, String title,
                                           String message, String type,
                                           String referenceId, String classroomId) {
        for (String studentId :  studentIds) {
            Notification notification = new Notification(
                    studentId, title, message, type, referenceId, classroomId
            );
            DocumentReference docRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS).document();
            notification.setId(docRef.getId());
            docRef.set(notification);
        }
    }
}