package com.classbuddy.app.data.model;

import com.google. firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google. firebase.firestore. ServerTimestamp;

import java.io. Serializable;
import java.util. ArrayList;
import java. util.List;

public class Notice implements Serializable {

    public enum Priority {
        LOW("Low"),
        NORMAL("Normal"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @DocumentId
    private String id;
    private String classroomId;
    private String classroomName;
    private String title;
    private String content;
    private String priority; // "low", "normal", "high", "urgent"
    private String imageUrl;
    private boolean isPinned;
    private String adminId;
    private String adminName;
    private List<String> readByStudentIds;

    @ServerTimestamp
    private Timestamp createdAt;

    @ServerTimestamp
    private Timestamp updatedAt;

    public Notice() {
        this.readByStudentIds = new ArrayList<>();
        this.isPinned = false;
    }

    public Notice(String classroomId, String classroomName, String title,
                  String content, String priority, String adminId, String adminName) {
        this. classroomId = classroomId;
        this.classroomName = classroomName;
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.adminId = adminId;
        this.adminName = adminName;
        this.readByStudentIds = new ArrayList<>();
        this.isPinned = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public List<String> getReadByStudentIds() { return readByStudentIds; }
    public void setReadByStudentIds(List<String> readByStudentIds) {
        this.readByStudentIds = readByStudentIds;
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isReadByStudent(String studentId) {
        return readByStudentIds != null && readByStudentIds. contains(studentId);
    }

    public void markAsRead(String studentId) {
        if (readByStudentIds == null) {
            readByStudentIds = new ArrayList<>();
        }
        if (! readByStudentIds.contains(studentId)) {
            readByStudentIds. add(studentId);
        }
    }
}