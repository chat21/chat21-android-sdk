package chat21.android.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;

import java.util.Locale;

import chat21.android.R;


/**
 * Created by stefano on 25/08/2015.
 */
public class ChatUtils {
    private static final String TAG = ChatUtils.class.getName();

    // check if the support account is enabled or disabled
    public static boolean isChatSupportAccountEnabled(Context context) {
        Log.d(TAG, "isChatSupportAccountEnabled");
        boolean isEnabled = false;
        if (context.getString(R.string.enable_chat_support_account).trim().compareToIgnoreCase("true") == 0) {
            isEnabled = true;
        } else if (context.getString(R.string.enable_chat_support_account).trim().compareToIgnoreCase("false") == 0) {
            isEnabled = false;
        }
        return isEnabled;
    }

    // check if the group creation enabled or disabled
    public static boolean areGroupsEnabled(Context context) {
        Log.d(TAG, "areGroupsEnabled");
        boolean isEnabled = false;
        if (context.getString(R.string.enable_groups).trim().compareToIgnoreCase("true") == 0) {
            isEnabled = true;
        } else if (context.getString(R.string.enable_groups).trim().compareToIgnoreCase("false") == 0) {
            isEnabled = false;
        }
        return isEnabled;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        Log.d(TAG, "getAppVersion");
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
     * Returns the device language code
     *
     * @return
     */
    public static String getLanguage() {
        Log.d(TAG, "getLanguage");
//		Locale locale = Locale.getDefault();
        String lang = Locale.getDefault().getLanguage();
        Log.d(TAG, "Language: " + lang);
        return lang;
    }

    public static String normalizeUsername(String username) {
        return username.replace(".", "_");
    }

    public static String deNormalizeUsername(String username) {
        return username.replace("_", ".");
    }
}