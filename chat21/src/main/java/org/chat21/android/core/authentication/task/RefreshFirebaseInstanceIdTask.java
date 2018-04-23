package org.chat21.android.core.authentication.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

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
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
            Log.i(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask.doInBackground: instanceId deleted with success.");

            // Now manually call onTokenRefresh()
            Log.d(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask.doInBackground: Getting new token");
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.i(TAG_TOKEN, "RefreshFirebaseInstanceIdTask: token == " + token);

        } catch (IOException e) {
            Log.e(DEBUG_LOGIN, "RefreshFirebaseInstanceIdTask.doInBackground: deleteInstanceIdCatch: " + e.getMessage());
        }

        return null;
    }
}