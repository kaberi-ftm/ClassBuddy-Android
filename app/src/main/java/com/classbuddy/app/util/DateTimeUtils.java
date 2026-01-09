package com.classbuddy.app.util;

import com.google.firebase. Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java. util.Date;
import java.util. Locale;
import java.util. concurrent.TimeUnit;

public class DateTimeUtils {

    private static final String DATE_FORMAT = "dd MMM yyyy";
    private static final String TIME_FORMAT = "hh:mm a";
    private static final String TIME_FORMAT_24H = "HH: mm";
    private static final String DATE_TIME_FORMAT = "dd MMM yyyy, hh:mm a";
    private static final String DAY_DATE_FORMAT = "EEEE, dd MMM yyyy";
    private static final String MONTH_YEAR_FORMAT = "MMMM yyyy";

    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        return formatDate(timestamp. toDate());
    }

    public static String formatTime(String time24h) {
        try {
            SimpleDateFormat input = new SimpleDateFormat(TIME_FORMAT_24H, Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            Date date = input.parse(time24h);
            return date != null ? output. format(date) : time24h;
        } catch (ParseException e) {
            return time24h;
        }
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale. getDefault());
        return sdf.format(date);
    }

    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return formatDateTime(timestamp.toDate());
    }

    public static String formatDayDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(DAY_DATE_FORMAT, Locale. getDefault());
        return sdf.format(date);
    }

    public static String formatMonthYear(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(MONTH_YEAR_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    public static String getRelativeTime(Timestamp timestamp) {
        if (timestamp == null) return "";

        long now = System.currentTimeMillis();
        long time = timestamp.toDate().getTime();
        long diff = now - time;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Just now";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit. MILLISECONDS.toMinutes(diff);
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (diff < TimeUnit.DAYS. toMillis(1)) {
            long hours = TimeUnit. MILLISECONDS.toHours(diff);
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (diff < TimeUnit. DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS. toDays(diff);
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            return formatDate(timestamp);
        }
    }

    public static String getCountdown(Timestamp examDate) {
        if (examDate == null) return "";

        long now = System.currentTimeMillis();
        long examTime = examDate. toDate().getTime();
        long diff = examTime - now;

        if (diff <= 0) {
            return "Completed";
        }

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit. MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS. toMinutes(diff) % 60;

        if (days > 0) {
            return days + "d " + hours + "h left";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m left";
        } else {
            return minutes + " minutes left";
        }
    }

    public static int getCurrentDayIndex() {
        Calendar calendar = Calendar.getInstance();
        return calendar. get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static String getCurrentDayName() {
        return Constants. DAYS_OF_WEEK[getCurrentDayIndex()];
    }

    public static boolean isToday(Timestamp timestamp) {
        if (timestamp == null) return false;

        Calendar today = Calendar.getInstance();
        Calendar check = Calendar.getInstance();
        check.setTime(timestamp.toDate());

        return today.get(Calendar. YEAR) == check.get(Calendar. YEAR)
                && today. get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar. YEAR) == cal2.get(Calendar. YEAR)
                && cal1.get(Calendar. DAY_OF_YEAR) == cal2.get(Calendar. DAY_OF_YEAR);
    }

    public static boolean isTomorrow(Timestamp timestamp) {
        if (timestamp == null) return false;

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        Calendar check = Calendar.getInstance();
        check.setTime(timestamp.toDate());

        return tomorrow.get(Calendar.YEAR) == check.get(Calendar.YEAR)
                && tomorrow.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isThisWeek(Timestamp timestamp) {
        if (timestamp == null) return false;

        Calendar now = Calendar.getInstance();
        Calendar check = Calendar.getInstance();
        check.setTime(timestamp.toDate());

        return now.get(Calendar. YEAR) == check.get(Calendar. YEAR)
                && now.get(Calendar. WEEK_OF_YEAR) == check.get(Calendar. WEEK_OF_YEAR);
    }

    public static Timestamp createTimestamp(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar. MILLISECOND, 0);
        return new Timestamp(calendar. getTime());
    }

    public static Timestamp createTimestamp(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTime());
    }

    public static long getMillisFromTime(String time24h) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_24H, Locale.getDefault());
            Date date = sdf.parse(time24h);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar. setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, timeCalendar. get(Calendar. MINUTE));
                calendar.set(Calendar. SECOND, 0);
                calendar.set(Calendar. MILLISECOND, 0);
                return calendar.getTimeInMillis();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getDayOfWeekFromDate(Date date) {
        if (date == null) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return Constants. DAYS_OF_WEEK[dayIndex];
    }

    public static int getDayIndexFromName(String dayName) {
        for (int i = 0; i < Constants. DAYS_OF_WEEK.length; i++) {
            if (Constants. DAYS_OF_WEEK[i].equalsIgnoreCase(dayName)) {
                return i;
            }
        }
        return -1;
    }

    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar. MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar. getInstance();
        calendar.setTime(date);
        calendar.set(Calendar. HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}