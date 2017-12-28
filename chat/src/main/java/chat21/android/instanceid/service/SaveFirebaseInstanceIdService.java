package chat21.android.instanceid.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import chat21.android.core.authentication.ChatAuthentication;
import chat21.android.utils.ChatUtils;
import chat21.android.utils.StringUtils;

import static chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/*
 * Created by Mahmoud on 3/13/2017.
 */

//https://github.com/MahmoudAlyuDeen/FirebaseIM/blob/master/app/src/main/java/afterapps/com/firebaseim/login/LoginActivity.java
public class SaveFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Log.d(DEBUG_LOGIN, "onTokenRefresh");

        String instanceId = FirebaseInstanceId.getInstance().getToken();
        Log.d(DEBUG_LOGIN, "onTokenRefresh called with instanceId: " + instanceId);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String tenant = ChatAuthentication.getInstance().getTenant();
        Log.i(DEBUG_LOGIN, "authTenant == " + tenant);

        if (firebaseUser != null && StringUtils.isValid(tenant)) {

//            Log.d(TAG, "firebaseUser.getEmail() " + firebaseUser.getEmail());
//            Log.d(TAG, "firebaseUser.getDisplayName() " + firebaseUser.getDisplayName());

//            String tenant = Chat.Authentication
//                    .getInstance()
//                    .getTenant();
//
////            Log.d(TAG, "authTenant == " + tenant);


//            String userId = getNormalizedUserId(ChatUtils.normalizeUsername(firebaseUser.getUid()));

            // TODO: 15/09/17  
            // fix Issue #23
            DatabaseReference firebaseUsersPath = FirebaseDatabase.getInstance().getReference()
                    .child("apps")
                    .child(tenant)
                    .child("users")
//                    .child(tenant + "-" + userId)
                    .child(ChatUtils.normalizeUsername(firebaseUser.getUid()))
                    .child("instanceId");
            firebaseUsersPath.setValue(instanceId);


            Log.i(DEBUG_LOGIN, "onTokenRefresh saved with instanceId: " + instanceId +
                    ", tenant: " + tenant + ", firebaseUsersPath: " + firebaseUsersPath);
        } else {
            Log.i(DEBUG_LOGIN, "user is null. instanceId == " + instanceId + ", authTenant == " + tenant);
        }
    }


//    // The signInWithUid method adds the tenant as a prefix and it
//    // is preceded by the "_" character.
//    // For this reason a splitting on the username returned by firebase
//    // is done by excluding the initial part of the tenant
//    private String getNormalizedUserId(String extendedUsername) {
//        String[] temp = StringUtils.splitByChar(extendedUsername, "_");
////        String splittedTenant = temp[0];
//        String splittedUsername = "";
//
//        for (int index = 1, size = temp.length; index < size; index++) {
//            splittedUsername += ("_" + temp[index]);
//        }
//
//        // remove the first char if it is "_"
//        if (splittedUsername.startsWith("_")) {
//            splittedUsername = splittedUsername.substring(1, splittedUsername.length());
//        }
//
//        // remove the last char if it is "_"
//        if (splittedUsername.endsWith("_")) {
//            splittedUsername = splittedUsername.substring(0, splittedUsername.length() - 1);
//        }
//
////        Log.d(TAG, "splittedUsername: " + splittedUsername);
//
//        return ChatUtils.normalizeUsername(splittedUsername);
//    }
}
