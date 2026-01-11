package com.classbuddy.app.util;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT_12H = new SimpleDateFormat("hh:mm a", Locale. getDefault());
    private static final SimpleDateFormat TIME_FORMAT_24H = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale. getDefault());
    private static final SimpleDateFormat DAY_DATE_FORMAT = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("EEEE", Locale.getDefault());

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        return DATE_FORMAT.format(timestamp. toDate());
    }

    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    public static String formatTime(String time24h) {
        if (time24h == null || time24h.isEmpty()) return "";
        try {
            Date date = TIME_FORMAT_24H.parse(time24h);
            return TIME_FORMAT_12H.format(date);
        } catch (ParseException e) {
            return time24h;
        }
    }

    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return DATE_TIME_FORMAT.format(timestamp.toDate());
    }

    public static String formatDayDate(Date date) {
        if (date == null) return "";
        return DAY_DATE_FORMAT.format(date);
    }

    public static String formatMonthYear(Date date) {
        if (date == null) return "";
        return MONTH_YEAR_FORMAT.format(date);
    }

    public static String getRelativeTime(Timestamp timestamp) {
        if (timestamp == null) return "";

        long now = System.currentTimeMillis();
        long time = timestamp.toDate().getTime();
        long diff = now - time;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Just now";
        } else if (diff < TimeUnit.HOURS. toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + "m ago";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit. MILLISECONDS.toHours(diff);
            return hours + "h ago";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + "d ago";
        } else {
            return DATE_FORMAT.format(timestamp.toDate());
        }
    }

    public static String getCountdown(Timestamp timestamp) {
        if (timestamp == null) return "";

        long now = System.currentTimeMillis();
        long examTime = timestamp.toDate().getTime();
        long diff = examTime - now;

        if (diff <= 0) {
            return "Completed";
        }

        long days = TimeUnit. MILLISECONDS.toDays(diff);

        if (days == 0) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            if (hours == 0) {
                return "Less than 1 hour";
            }
            return hours + " hour" + (hours > 1 ? "s" : "") + " left";
        } else if (days == 1) {
            return "Tomorrow";
        } else if (days < 7) {
            return days + " days left";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " week" + (weeks > 1 ? "s" : "") + " left";
        } else {
            return DATE_FORMAT.format(timestamp.toDate());
        }
    }

    public static int getCurrentDayIndex() {
        Calendar calendar = Calendar. getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Convert to 0-indexed (Sunday = 0, Monday = 1, etc.)
        return dayOfWeek - 1;
    }

    public static String getCurrentDayName() {
        return Constants.DAYS_OF_WEEK[getCurrentDayIndex()];
    }

    public static String getDayOfWeekFromDate(Date date) {
        if (date == null) return "";
        return DAY_FORMAT.format(date);
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar. getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    public static boolean isTomorrow(Date date) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        return isSameDay(date, tomorrow.getTime());
    }

    public static boolean isPast(Timestamp timestamp) {
        if (timestamp == null) return false;
        return timestamp.toDate().before(new Date());
    }

    public static boolean isFuture(Timestamp timestamp) {
        if (timestamp == null) return false;
        return timestamp.toDate().after(new Date());
    }

    public static long getMillisFromTime(String time24h, Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            String[] parts = time24h.split(":");
            if (parts. length >= 2) {
                calendar. set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }

            return calendar. getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getDayShortName(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < Constants.DAYS_OF_WEEK.length) {
            return Constants.DAYS_OF_WEEK[dayIndex]. substring(0, 3);
        }
        return "";
    }
}
