package com.classbuddy.app.data.remote;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.classbuddy.app.data.model.Classroom;
import com.classbuddy.app.data.model.Exam;
import com.classbuddy.app.data.model.Notice;
import com.classbuddy.app.data.model.Notification;
import com.classbuddy.app.data.model.Routine;
import com.classbuddy.app.data.model.User;
import com.classbuddy.app.util.Constants;
import com.classbuddy.app.util.Resource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirestoreSource {

    private final FirebaseFirestore firestore;
    private static final long FIRESTORE_TIMEOUT_MS = 30000; // 30 seconds timeout

    public FirestoreSource() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    private String getFirestoreErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            if (lowerMessage.contains("network") || lowerMessage.contains("connection")) {
                return "No internet connection. Please check your network and try again.";
            } else if (lowerMessage.contains("timeout") || lowerMessage.contains("timed out") || 
                       lowerMessage.contains("deadline") || lowerMessage.contains("unavailable")) {
                return "Connection timed out. Please check your internet connection and try again.";
            }
        }
        return message != null ? message : "An error occurred. Please try again.";
    }

    // ==================== USER OPERATIONS ====================

    public LiveData<Resource<Void>> createUser(User user) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        Handler handler = new Handler(Looper.getMainLooper());
        
        // Set timeout
        Runnable timeoutRunnable = () -> {
            if (!isCompleted.getAndSet(true)) {
                result.setValue(Resource.error("Connection timed out. Please check your internet connection and try again.", null));
            }
        };
        handler.postDelayed(timeoutRunnable, FIRESTORE_TIMEOUT_MS);

        firestore.collection(Constants.COLLECTION_USERS)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    if (!isCompleted.getAndSet(true)) {
                        handler.removeCallbacks(timeoutRunnable);
                        result.setValue(Resource.success(null));
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isCompleted.getAndSet(true)) {
                        handler.removeCallbacks(timeoutRunnable);
                        result.setValue(Resource.error(getFirestoreErrorMessage(e), null));
                    }
                });

        return result;
    }

    public LiveData<Resource<User>> getUser(String userId) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        
        AtomicBoolean isCompleted = new AtomicBoolean(false);
        Handler handler = new Handler(Looper.getMainLooper());
        
        // Set timeout
        Runnable timeoutRunnable = () -> {
            if (!isCompleted.getAndSet(true)) {
                result.setValue(Resource.error("Connection timed out. Please check your internet connection and try again.", null));
            }
        };
        handler.postDelayed(timeoutRunnable, FIRESTORE_TIMEOUT_MS);

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isCompleted.getAndSet(true)) {
                        handler.removeCallbacks(timeoutRunnable);
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            result.setValue(Resource.success(user));
                        } else {
                            result.setValue(Resource.error("User not found", null));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isCompleted.getAndSet(true)) {
                        handler.removeCallbacks(timeoutRunnable);
                        result.setValue(Resource.error(getFirestoreErrorMessage(e), null));
                    }
                });

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
                        classrooms.sort((c1, c2) -> {
                            if (c1.getCreatedAt() != null && c2.getCreatedAt() != null) {
                                return c2.getCreatedAt().compareTo(c1.getCreatedAt());
                            }
                            return 0;
                        });
                        result.setValue(Resource.success(classrooms));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Classroom>>> getClassroomsByStudent(List<String> classroomIds) {
        MutableLiveData<Resource<List<Classroom>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result.setValue(Resource.loading(null));

        // Firestore whereIn has a limit of 10 items, so we need to batch
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < classroomIds.size(); i += 10) {
            batches.add(classroomIds.subList(i, Math.min(i + 10, classroomIds.size())));
        }

        List<Classroom> allClassrooms = new ArrayList<>();
        int[] completedBatches = {0};
        int[] errorCount = {0};

        for (List<String> batch : batches) {
            firestore.collection(Constants.COLLECTION_CLASSROOMS)
                    .whereIn(FieldPath.documentId(), batch)
                    .addSnapshotListener((querySnapshot, error) -> {
                        if (error != null) {
                            errorCount[0]++;
                            if (errorCount[0] == batches.size()) {
                                result.setValue(Resource.error(error.getMessage(), null));
                            }
                            return;
                        }
                        if (querySnapshot != null) {
                            // Remove old classrooms from this batch and add new ones
                            synchronized (allClassrooms) {
                                // Remove classrooms that were from this batch
                                allClassrooms.removeIf(c -> batch.contains(c.getId()));

                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    Classroom classroom = doc.toObject(Classroom.class);
                                    if (classroom != null) {
                                        allClassrooms.add(classroom);
                                    }
                                }

                                completedBatches[0]++;
                                if (completedBatches[0] >= batches.size()) {
                                    result.setValue(Resource.success(new ArrayList<>(allClassrooms)));
                                }
                            }
                        }
                    });
        }

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

        result.setValue(Resource.loading(null));

        // Use real-time listener for immediate updates when students join/leave
        firestore.collection(Constants.COLLECTION_USERS)
                .whereIn(FieldPath.documentId(), studentIds)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<User> students = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                students.add(user);
                            }
                        }
                        result.setValue(Resource.success(students));
                    }
                });

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
                        routines.sort((r1, r2) -> {
                            if (r1.getDayIndex() != r2.getDayIndex()) {
                                return Integer.compare(r1.getDayIndex(), r2.getDayIndex());
                            }
                            return r1.getStartTime().compareTo(r2.getStartTime());
                        });
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
                        routines.sort((r1, r2) -> {
                            if (r1.getDayIndex() != r2.getDayIndex()) {
                                return Integer.compare(r1.getDayIndex(), r2.getDayIndex());
                            }
                            return r1.getStartTime().compareTo(r2.getStartTime());
                        });
                        result.setValue(Resource.success(routines));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Routine>>> getTodaysRoutine(List<String> classroomIds, int dayIndex) {
        MutableLiveData<Resource<List<Routine>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result.setValue(Resource.loading(null));

        // Use real-time listener for immediate updates
        firestore.collection(Constants.COLLECTION_ROUTINES)
                .whereIn("classroomId", classroomIds)
                .whereEqualTo("dayIndex", dayIndex)
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
                                routines.add(routine);
                            }
                        }
                        routines.sort((r1, r2) -> r1.getStartTime().compareTo(r2.getStartTime()));
                        result.setValue(Resource.success(routines));
                    }
                });

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
                        exams.sort((e1, e2) -> {
                            if (e1.getExamDate() != null && e2.getExamDate() != null) {
                                return e1.getExamDate().compareTo(e2.getExamDate());
                            }
                            return 0;
                        });
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
                        exams.sort((e1, e2) -> {
                            if (e1.getExamDate() != null && e2.getExamDate() != null) {
                                return e1.getExamDate().compareTo(e2.getExamDate());
                            }
                            return 0;
                        });
                        result.setValue(Resource.success(exams));
                    }
                });

        return result;
    }

    public LiveData<Resource<List<Exam>>> getUpcomingExams(List<String> classroomIds) {
        MutableLiveData<Resource<List<Exam>>> result = new MutableLiveData<>();

        if (classroomIds == null || classroomIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result.setValue(Resource.loading(null));

        com.google.firebase.Timestamp now = com.google.firebase.Timestamp.now();

        // Use real-time listener for immediate updates
        firestore.collection(Constants.COLLECTION_EXAMS)
                .whereIn("classroomId", classroomIds)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error.getMessage(), null));
                        return;
                    }
                    if (querySnapshot != null) {
                        List<Exam> exams = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Exam exam = doc.toObject(Exam.class);
                            if (exam != null && exam.getExamDate() != null && exam.getExamDate().compareTo(now) >= 0) {
                                exams.add(exam);
                            }
                        }
                        exams.sort((e1, e2) -> {
                            if (e1.getExamDate() != null && e2.getExamDate() != null) {
                                return e1.getExamDate().compareTo(e2.getExamDate());
                            }
                            return 0;
                        });
                        if (exams.size() > 10) {
                            exams = exams.subList(0, 10);
                        }
                        result.setValue(Resource.success(exams));
                    }
                });

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
                        notices.sort((n1, n2) -> {
                            if (n1.isPinned() && !n2.isPinned()) return -1;
                            if (!n1.isPinned() && n2.isPinned()) return 1;
                            if (n1.getCreatedAt() != null && n2.getCreatedAt() != null) {
                                return n2.getCreatedAt().compareTo(n1.getCreatedAt());
                            }
                            return 0;
                        });
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
                        notices.sort((n1, n2) -> {
                            if (n1.isPinned() && !n2.isPinned()) return -1;
                            if (!n1.isPinned() && n2.isPinned()) return 1;
                            if (n1.getCreatedAt() != null && n2.getCreatedAt() != null) {
                                return n2.getCreatedAt().compareTo(n1.getCreatedAt());
                            }
                            return 0;
                        });
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

        result.setValue(Resource.loading(null));

        // Use real-time listener for immediate updates
        firestore.collection(Constants.COLLECTION_NOTICES)
                .whereIn("classroomId", classroomIds)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        result.setValue(Resource.error(error.getMessage(), null));
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
                        notices.sort((n1, n2) -> {
                            if (n1.getCreatedAt() != null && n2.getCreatedAt() != null) {
                                return n2.getCreatedAt().compareTo(n1.getCreatedAt());
                            }
                            return 0;
                        });
                        if (notices.size() > limit) {
                            notices = notices.subList(0, limit);
                        }
                        result.setValue(Resource.success(notices));
                    }
                });

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
                        notifications.sort((n1, n2) -> {
                            if (n1.getCreatedAt() != null && n2.getCreatedAt() != null) {
                                return n2.getCreatedAt().compareTo(n1.getCreatedAt());
                            }
                            return 0;
                        });
                        if (notifications.size() > 50) {
                            notifications = notifications.subList(0, 50);
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
