package org.chat21.android.utils;

public class ObjectUtils {
    public static int objectToInt(Object object) {
        return object != null ? (int) object : 0;
    }

    public static double objectToDouble(Object object) {
        return object != null ? (double) object : 0;
    }

    public static double objectToFloat(Object object) {
        return object != null ? (float) object : 0;
    }

    public static boolean objectToBoolean(Object object) {
        return object != null && (boolean) object;
    }
}