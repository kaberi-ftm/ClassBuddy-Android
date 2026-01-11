package com.classbuddy.app.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    @DocumentId
    private String id;
    private String email;
    private String fullName;
    private String profileImageUrl;
    private String role; // "admin" or "student"
    private String fcmToken;
    private List<String> joinedClassrooms;
    private boolean notificationsEnabled;

    @ServerTimestamp
    private Timestamp createdAt;

    @ServerTimestamp
    private Timestamp updatedAt;

    public User() {
        this.joinedClassrooms = new ArrayList<>();
        this.notificationsEnabled = true;
    }

    public User(String email, String fullName, String role) {
        this. email = email;
        this.fullName = fullName;
        this.role = role;
        this.joinedClassrooms = new ArrayList<>();
        this.notificationsEnabled = true;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public List<String> getJoinedClassrooms() { return joinedClassrooms; }
    public void setJoinedClassrooms(List<String> joinedClassrooms) {
        this.joinedClassrooms = joinedClassrooms;
    }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "student".equalsIgnoreCase(role);
    }
}
