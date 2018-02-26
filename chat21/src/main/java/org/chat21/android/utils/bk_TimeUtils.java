//package org.chat21.android.utils;
//
//import org.ocpsoft.prettytime.PrettyTime;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
///**
// * Created by stefano on 05/09/2015.
// */
//public class bk_TimeUtils {
////    private static final String TAG = TimeUtils.class.getName();
//
//    private static Long ONE_DAY_IN_MILLIS = Long.valueOf(86400000);  // 1 day = 86400000 millis
//
//    /**
//     * Convert a Long timestamp into a string
//     *
//     * @param timestamp the timestamp
//     * @param locale    the locale format
//     * @return the date string
//     */
//    public static String getFormattedDate(Long timestamp, Locale locale) {
////        Log.d(TAG, "getFormattedDate");
//
//        Long currentTime = getCurrentTime(locale);
//        Long timeDiff = currentTime - timestamp;
//        Long onedayInMillis = ONE_DAY_IN_MILLIS;
//
//        /*
//         * if the difference between the current time and the timestamp is
//         * 1 day or more than show the compete timestamp else show the timestamp as
//         * HH:mm
//         */
//        DateFormat dateFormat;
//        if (timeDiff <= onedayInMillis) {
//            dateFormat = new SimpleDateFormat("HH:mm");
//        } else {
//            dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
//        }
//        return dateFormat.format(timestamp);
//    }
//
//    // return the current time in millis
//    private static Long getCurrentTime(Locale locale) {
////        Log.d(TAG, "getCurrentTime");
//        return Calendar.getInstance(locale).getTimeInMillis();
//    }
//
//    public static String fixPrettyTimeFutureMessage(String convertedTimestamp) {
////        Log.d(TAG, "fixPrettyTimeFutureMessage");
//        String fixedTimestamp = convertedTimestamp;
//        if (convertedTimestamp.compareTo("fra poco") == 0) {
//            fixedTimestamp = "adesso";
//        }
//        return fixedTimestamp;
//    }
//
//    public static String getFormattedTimestamp(long timestampLong) {
////        Log.d(TAG, "getFormattedTimestamp");
//        PrettyTime p = new PrettyTime();
//        String timestamp =
//                p.format(new Date((timestampLong)));
//        timestamp = bk_TimeUtils.fixPrettyTimeFutureMessage(timestamp);
//        return timestamp;
//    }
//
//    // resolve Issue #38
//    public static String timestampToHour(long timestamp) {
//        Date date = new Date(timestamp);
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
//        return sdf.format(date);
//    }
//
//    public static String timestampToStrDate(long timestamp) {
//        int currentYear = getCurrentYear();
//        int timestampYear = getYearFromTimestamp(timestamp);
//
//        // if it is the current year hide it, otherwise show it
//        String pattern = "dd MMMM yyyy";
//        if (timestampYear == currentYear) {
//            pattern = "dd MMMM";
//        }
//
//        // timestamp date
//        Date timestampDate = new Date(timestamp);
//        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
//        return sdf.format(timestampDate);
//    }
//
//    // returns the date of week for the date
//    public static String getDayOfWeek(Date date) {
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(date);
//        return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
//    }
//
//    // check if a timestamp is the current date
//    public static boolean isDateToday(long milliSeconds) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(milliSeconds);
//
//        Date getDate = calendar.getTime();
//
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//
//        Date startDate = calendar.getTime();
//
//        return getDate.compareTo(startDate) > 0;
//    }
//
//    // returns the year of the current date
//    private static int getCurrentYear() {
//        Calendar cal = Calendar.getInstance();
//        return cal.get(Calendar.YEAR);
//    }
//
//    // returns the year from the timestamp
//    private static int getYearFromTimestamp(long timestamp) {
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(timestamp);
//        return cal.get(Calendar.YEAR);
//    }
//}