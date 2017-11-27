package chat21.android.user.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

/**
 * Created by andrealeo
 */
public class RefreshFirebaseInstanceIdTask extends AsyncTask<Object, Object, Void> {
    private static final String TAG = "LOGIN";

    public RefreshFirebaseInstanceIdTask() {

    }

    @Override
    protected Void doInBackground(Object... params) {
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
            Log.i(TAG, "instanceId deleted with success." );

            // Now manually call onTokenRefresh()
            Log.d(TAG, "Getting new token");
            FirebaseInstanceId.getInstance().getToken();


        } catch (IOException e) {
            Log.e(TAG, "deleteInstanceIdCatch: " + e.getMessage());
        }

        return null;
    }
}