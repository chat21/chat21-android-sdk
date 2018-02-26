package org.chat21.android.utils;

import android.content.Context;

import org.chat21.android.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by stefano on 05/09/2015.
 */
public class TimeUtils {

    public static boolean isDateToday(long milliSeconds) {
        Calendar toCheck = Calendar.getInstance();
        toCheck.setTimeInMillis(milliSeconds);

        Date toCheckDate = toCheck.getTime();

        toCheck.setTimeInMillis(System.currentTimeMillis());
        toCheck.set(Calendar.HOUR_OF_DAY, 0);
        toCheck.set(Calendar.MINUTE, 0);
        toCheck.set(Calendar.SECOND, 0);

        Date startDate = toCheck.getTime();

        return toCheckDate.compareTo(startDate) > 0;
    }

    public static boolean isYesterday(long milliSeconds) {

        Calendar toCheck = Calendar.getInstance();
        toCheck.setTimeInMillis(milliSeconds); // your date

        Calendar yesterday = Calendar.getInstance(); // today
        yesterday.add(Calendar.DAY_OF_YEAR, -1); // yesterday

        boolean isYesterday = yesterday.get(Calendar.YEAR) == toCheck.get(Calendar.YEAR)
                && yesterday.get(Calendar.DAY_OF_YEAR) == toCheck.get(Calendar.DAY_OF_YEAR);

        return isYesterday ? true : false;
    }

    public static boolean isDateInCurrentWeek(long milliSeconds) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);

        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTimeInMillis(milliSeconds);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);

        return week == targetWeek && year == targetYear;
    }

    // https://www.ibm.com/support/knowledgecenter/en/SSMKHH_9.0.0/com.ibm.etools.mft.doc/ak05616_.htm
    public static String getFormattedTimestamp(Context context, long milliSeconds) {
        if (isDateToday(milliSeconds)) {
            return formatTimestamp(milliSeconds, "HH:mm");
        } else if (isYesterday(milliSeconds)) {
            return context.getString(R.string.yesterday);
        } else if (isDateInCurrentWeek(milliSeconds)) {
            return formatTimestamp(milliSeconds, "EEEE");
        } else {
            return formatTimestamp(milliSeconds, "dd/MM/yy");
        }
    }

    public static String formatTimestamp(Long timestamp, String pattern) {
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(timestamp);
    }

    public static String getDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }
}