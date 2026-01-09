package com.classbuddy.app.data.model;


import com.google. firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google. firebase.firestore. ServerTimestamp;

import java.io. Serializable;

public class Notification implements Serializable {

    public enum NotificationType {
        NOTICE("notice"),
        EXAM("exam"),
        ROUTINE("routine"),
        CLASSROOM("classroom"),
        CUSTOM("custom");

        private final String value;

        NotificationType(String value) {
            this. value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @DocumentId
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type;
    private String referenceId; // ID of related entity (notice, exam, etc.)
    private String classroomId;
    private boolean isRead;

    @ServerTimestamp
    private Timestamp createdAt;

    public Notification() {
        this.isRead = false;
    }

    public Notification(String userId, String title, String message,
                        String type, String referenceId, String classroomId) {
        this.userId = userId;
        this.title = title;
        this. message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.classroomId = classroomId;
        this.isRead = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}