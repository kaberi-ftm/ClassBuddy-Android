package com.classbuddy.app.data.model;

import java.io.Serializable;
import java.util. Date;

public class CalendarEvent implements Serializable {

    public enum EventType {
        CLASS("class"),
        EXAM_CT("exam_ct"),
        EXAM_FINAL("exam_final"),
        EXAM_LAB_QUIZ("exam_labquiz"),
        EXAM_VIVA("exam_viva"),
        LAB("lab"),
        NOTICE("notice");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private String id;
    private String title;
    private String description;
    private Date date;
    private String time;
    private String eventType;
    private String classroomId;
    private String classroomName;
    private String referenceId;

    public CalendarEvent() {}

    public CalendarEvent(String id, String title, String description, Date date,
                         String time, String eventType, String classroomId,
                         String classroomName, String referenceId) {
        this.id = id;
        this. title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.eventType = eventType;
        this.classroomId = classroomId;
        this.classroomName = classroomName;
        this. referenceId = referenceId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public int getColorForType() {
        switch (eventType) {
            case "class": return 0xFF1565C0; // Blue
            case "exam_ct": return 0xFFE53935; // Red
            case "exam_final": return 0xFFD81B60; // Pink
            case "exam_labquiz":  return 0xFF8E24AA; // Purple
            case "exam_viva": return 0xFF5E35B1; // Deep Purple
            case "lab": return 0xFF43A047; // Green
            case "notice": return 0xFFFF8F00; // Orange
            default: return 0xFF757575; // Grey
        }
    }
}