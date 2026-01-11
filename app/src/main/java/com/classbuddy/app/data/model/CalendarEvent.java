package com.classbuddy.app.data.model;

import java.util.Date;

public class CalendarEvent {

    private String id;
    private String title;
    private String classroomName;
    private String time;
    private String eventType; // "Class", "Lab", "Exam"
    private String description;
    private Date date;

    public CalendarEvent() {
    }

    public CalendarEvent(String id, String title, String classroomName,
                         String time, String eventType, String description, Date date) {
        this.id = id;
        this.title = title;
        this.classroomName = classroomName;
        this. time = time;
        this. eventType = eventType;
        this.description = description;
        this.date = date;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
