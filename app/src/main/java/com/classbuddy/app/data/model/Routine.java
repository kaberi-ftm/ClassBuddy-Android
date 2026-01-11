package com.classbuddy.app.data.model;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;

public class Routine implements Serializable {

    @DocumentId
    private String id;
    private String classroomId;
    private String classroomName;
    private String subject;
    private String faculty;
    private String room;
    private String dayOfWeek; // Monday, Tuesday, etc.
    private int dayIndex; // 0 = Sunday, 1 = Monday, etc.
    private String startTime; // "09:00"
    private String endTime; // "10:00"
    private String type; // "lecture", "lab", "tutorial"
    private String adminId;

    @ServerTimestamp
    private Timestamp createdAt;

    @ServerTimestamp
    private Timestamp updatedAt;

    public Routine() {}

    public Routine(String classroomId, String classroomName, String subject,
                   String faculty, String room, String dayOfWeek, int dayIndex,
                   String startTime, String endTime, String type, String adminId) {
        this.classroomId = classroomId;
        this.classroomName = classroomName;
        this.subject = subject;
        this.faculty = faculty;
        this.room = room;
        this. dayOfWeek = dayOfWeek;
        this.dayIndex = dayIndex;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.adminId = adminId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getDayIndex() { return dayIndex; }
    public void setDayIndex(int dayIndex) { this.dayIndex = dayIndex; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getTimeSlot() {
        return startTime + " - " + endTime;
    }
}
