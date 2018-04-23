package org.chat21.android.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;


/**
 * Created by stefano on 25/08/2015.
 */
public class ChatUtils {
//    private static final String TAG = ChatUtils.class.getName();

//    // check if the group creation enabled or disabled
//    public static boolean areGroupsEnabled(Context context) {
//        Log.d(TAG, "areGroupsEnabled");
//        boolean isEnabled = false;
//        if (context.getString(R.string.enable_groups).trim().compareToIgnoreCase("true") == 0) {
//            isEnabled = true;
//        } else if (context.getString(R.string.enable_groups).trim().compareToIgnoreCase("false") == 0) {
//            isEnabled = false;
//        }
//        return isEnabled;
//    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getVersionCode(Context context) {
//        Log.d(TAG, "getVersionCode");
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's version name from the {@code PackageManager}.
     */
    public static String getVersionName(Context context) {
//        Log.d(TAG, "getVersionName");
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

//    /**
//     * Returns the device language code
//     *
//     * @return
//     */
//    public static String getLanguage() {
//        Log.d(TAG, "getLanguage");
////		Locale locale = Locale.getDefault();
//        String lang = Locale.getDefault().getLanguage();
//        Log.d(TAG, "Language: " + lang);
//        return lang;
//    }

    public static String getDeviceModel() {
        return android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
    }

    public static String getSystemVersion() {
        String release = Build.VERSION.RELEASE;
//        int sdkVersion = Build.VERSION.SDK_INT;
//        return release + " - " + sdkVersion;
        return release;
    }

    public static String getSystemLanguage(Resources resources) {
        return resources.getConfiguration().locale.toString();
    }

    @Deprecated
    public static String normalizeUsername(String username) {
        return username.replace(".", "_");
    }

    @Deprecated
    public static String deNormalizeUsername(String username) {
        return username.replace("_", ".");
    }
}