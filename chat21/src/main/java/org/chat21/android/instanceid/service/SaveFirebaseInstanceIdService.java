package org.chat21.android.instanceid.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.chat21.android.core.ChatManager;
import org.chat21.android.utils.ChatUtils;
import org.chat21.android.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/*
 * Created by stefanodp91 on 15/01/18.
 */

//https://github.com/MahmoudAlyuDeen/FirebaseIM/blob/master/app/src/main/java/afterapps/com/firebaseim/login/LoginActivity.java
public class SaveFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG_TOKEN = "TAG_TOKEN";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Log.d(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh");

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh:" +
                " called with instanceId: " + token);
        Log.i(TAG_TOKEN, "SaveFirebaseInstanceIdService: token == " + token);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String appId = ChatManager.Configuration.appId;

        if (firebaseUser != null && StringUtils.isValid(appId)) {

            DatabaseReference root;
            if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
                root = FirebaseDatabase.getInstance()
                        .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl);
            } else {
                root = FirebaseDatabase.getInstance().getReference();
            }

            DatabaseReference firebaseUsersPath = root
                    .child("apps/" + ChatManager.Configuration.appId +
                            "/users/" + firebaseUser.getUid() + "/instances/" + token);

            Map<String, Object> device = new HashMap<>();
            device.put("device_model", ChatUtils.getDeviceModel());
            device.put("platform", "Android");
            device.put("platform_version", ChatUtils.getSystemVersion());
            device.put("language", ChatUtils.getSystemLanguage(getResources()));

            firebaseUsersPath.setValue(device); // placeholder value

            Log.i(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh: " +
                    "saved with token: " + token +
                    ", appId: " + appId + ", firebaseUsersPath: " + firebaseUsersPath);
        } else {
            Log.i(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh:" +
                    "user is null. token == " + token + ", appId == " + appId);
        }
    }
}
