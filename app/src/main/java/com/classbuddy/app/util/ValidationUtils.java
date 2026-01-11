package com.classbuddy.app.util;


import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return ! TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    public static boolean isValidClassroomCode(String code) {
        return !TextUtils.isEmpty(code) && code.length() == Constants.CLASSROOM_CODE_LENGTH;
    }

    public static boolean isValidClassroomPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 4;
    }

    public static boolean passwordsMatch(String password, String confirmPassword) {
        return ! TextUtils.isEmpty(password) && password.equals(confirmPassword);
    }

    public static boolean isNotEmpty(String value) {
        return !TextUtils.isEmpty(value) && ! value.trim().isEmpty();
    }

    public static boolean isValidCourseNo(String courseNo) {
        return ! TextUtils.isEmpty(courseNo) && courseNo.trim().length() >= 2;
    }

    public static boolean isValidMarks(int marks) {
        return marks > 0 && marks <= 100;
    }

    public static boolean isValidTime(String time) {
        if (TextUtils.isEmpty(time)) return false;
        return time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    public static boolean isEndTimeAfterStartTime(String startTime, String endTime) {
        if (! isValidTime(startTime) || !isValidTime(endTime)) return false;

        String[] startParts = startTime.split(":");
        String[] endParts = endTime. split(":");

        int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
        int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);

        return endMinutes > startMinutes;
    }

    public static String getEmailError(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }
        if (! Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email";
        }
        return null;
    }

    public static String getPasswordError(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        return null;
    }

    public static String getNameError(String name) {
        if (TextUtils. isEmpty(name)) {
            return "Name is required";
        }
        if (name.trim().length() < 2) {
            return "Name must be at least 2 characters";
        }
        return null;
    }

    public static String getConfirmPasswordError(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return "Please confirm your password";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        return null;
    }
}
