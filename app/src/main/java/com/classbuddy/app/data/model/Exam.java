package com.classbuddy.app.data.model;



import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Exam implements Serializable {

    public enum ExamType {
        CT("CT"),
        FINAL("Final"),
        LAB_QUIZ("Lab Quiz"),
        VIVA("Viva");

        private final String displayName;

        ExamType(String displayName) {
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
    private String courseNo;
    private String courseName;
    private String examType; // "ct", "final", "labquiz", "viva"
    private Timestamp examDate;
    private String startTime;
    private String endTime;
    private String room;
    private int totalMarks;
    private String notes;
    private String adminId;

    @ServerTimestamp
    private Timestamp createdAt;

    @ServerTimestamp
    private Timestamp updatedAt;

    public Exam() {}

    public Exam(String classroomId, String classroomName, String courseNo,
                String courseName, String examType, Timestamp examDate,
                String startTime, String endTime, String room,
                int totalMarks, String notes, String adminId) {
        this.classroomId = classroomId;
        this.classroomName = classroomName;
        this.courseNo = courseNo;
        this.courseName = courseName;
        this.examType = examType;
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this. totalMarks = totalMarks;
        this.notes = notes;
        this. adminId = adminId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getCourseNo() { return courseNo; }
    public void setCourseNo(String courseNo) { this.courseNo = courseNo; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public Timestamp getExamDate() { return examDate; }
    public void setExamDate(Timestamp examDate) { this.examDate = examDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isUpcoming() {
        if (examDate == null) return false;
        return examDate.toDate().after(new Date());
    }

    public String getExamTypeDisplay() {
        if (examType == null) return "";
        switch (examType. toLowerCase()) {
            case "ct": return "CT";
            case "final": return "Final";
            case "labquiz": return "Lab Quiz";
            case "viva": return "Viva";
            default:  return examType;
        }
    }
}
