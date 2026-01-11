package com.classbuddy.app.data.model;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Classroom implements Serializable {

    @DocumentId
    private String id;
    private String name;
    private String description;
    private String section;
    private String department;
    private String code; // Unique join code
    private String password;
    private String adminId;
    private String adminName;
    private List<String> studentIds;
    private int studentCount;

    @ServerTimestamp
    private Timestamp createdAt;

    @ServerTimestamp
    private Timestamp updatedAt;

    public Classroom() {
        this. studentIds = new ArrayList<>();
        this.studentCount = 0;
    }

    public Classroom(String name, String description, String section,
                     String department, String adminId, String adminName) {
        this. name = name;
        this.description = description;
        this.section = section;
        this.department = department;
        this.adminId = adminId;
        this.adminName = adminName;
        this.studentIds = new ArrayList<>();
        this.studentCount = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public List<String> getStudentIds() { return studentIds; }
    public void setStudentIds(List<String> studentIds) { this.studentIds = studentIds; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public void addStudent(String studentId) {
        if (!studentIds.contains(studentId)) {
            studentIds. add(studentId);
            studentCount = studentIds.size();
        }
    }

    public void removeStudent(String studentId) {
        studentIds.remove(studentId);
        studentCount = studentIds.size();
    }
}
