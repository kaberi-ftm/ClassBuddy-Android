package com.classbuddy.app.util;

public class Constants {

    // Firebase Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_CLASSROOMS = "classrooms";
    public static final String COLLECTION_ROUTINES = "routines";
    public static final String COLLECTION_EXAMS = "exams";
    public static final String COLLECTION_NOTICES = "notices";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";

    // User Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_STUDENT = "student";

    // Exam Types
    public static final String EXAM_TYPE_CT = "ct";
    public static final String EXAM_TYPE_FINAL = "final";
    public static final String EXAM_TYPE_LAB_QUIZ = "labquiz";
    public static final String EXAM_TYPE_VIVA = "viva";

    // Routine Types
    public static final String ROUTINE_TYPE_LECTURE = "lecture";
    public static final String ROUTINE_TYPE_LAB = "lab";
    public static final String ROUTINE_TYPE_TUTORIAL = "tutorial";

    // Notice Priority
    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_NORMAL = "normal";
    public static final String PRIORITY_HIGH = "high";
    public static final String PRIORITY_URGENT = "urgent";

    // Notification Types
    public static final String NOTIFICATION_TYPE_EXAM = "exam";
    public static final String NOTIFICATION_TYPE_NOTICE = "notice";
    public static final String NOTIFICATION_TYPE_CLASSROOM = "classroom";
    public static final String NOTIFICATION_TYPE_REMINDER = "reminder";
    public static final String NOTIFICATION_TYPE_GENERAL = "general";
    public static final String NOTIFICATION_TYPE_ROUTINE = "routine";

    // Reminder Times (in milliseconds)
    public static final long REMINDER_24_HOURS = 24 * 60 * 60 * 1000L;
    public static final long REMINDER_1_HOUR = 60 * 60 * 1000L;
    public static final long REMINDER_30_MINUTES = 30 * 60 * 1000L;

    // Days of Week
    public static final String[] DAYS_OF_WEEK = {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    // Limits
    public static final int MAX_CLASSROOMS_PER_ADMIN = 10;
    public static final int MAX_STUDENTS_PER_CLASSROOM = 100;
    public static final int CODE_LENGTH = 6;
    public static final int PASSWORD_LENGTH = 4;
    public static final int CLASSROOM_CODE_LENGTH = 6;

    // SharedPreferences Keys
    public static final String PREF_NAME = "classbuddy_prefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";

    // Intent Extras
    public static final String EXTRA_CLASSROOM_ID = "classroom_id";
    public static final String EXTRA_EXAM_ID = "exam_id";
    public static final String EXTRA_NOTICE_ID = "notice_id";
    public static final String EXTRA_USER_ROLE = "user_role";
}
