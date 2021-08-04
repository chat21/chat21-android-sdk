package org.chat21.android.core.authentication.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

import org.chat21.android.core.ChatManager;
import org.chat21.android.utils.ChatUtils;
import org.chat21.android.utils.StringUtils;

/**
 * Created by andrealeo
 */
public class RefreshFirebaseInstanceIdTask extends AsyncTask<Object, Object, Void> {
    private static final String TAG_TOKEN = "TAG_TOKEN";

    public RefreshFirebaseInstanceIdTask() {
        Log.d(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask");
    }

    @Override
    protected Void doInBackground(Object... params) {
        FirebaseInstallations.getInstance().delete();
//            FirebaseInstanceId.getInstance().deleteInstanceId();
        Log.i(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask.doInBackground: instanceId deleted with success.");

        // Now manually call onTokenRefresh()
        Log.d(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask.doInBackground: Getting new token");

        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(task -> {
            try {
                if (!task.isSuccessful()) {
                    return;
                }

                InstallationTokenResult r = task.getResult();
                String token = r != null ? r.getToken() : null;
//                    String token = FirebaseInstanceId.getInstance().getToken();
                Log.i(TAG_TOKEN, "RefreshFirebaseInstanceIdTask: token == " + token);

            } catch (Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });

        return null;
    }
}