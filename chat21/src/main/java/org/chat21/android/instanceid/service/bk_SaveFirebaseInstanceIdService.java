//package org.chat21.android.instanceid.service;
//
//import android.util.Log;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.iid.FirebaseInstanceId;
//import com.google.firebase.iid.FirebaseInstanceIdService;
//
//import org.chat21.android.core.ChatManager;
//import org.chat21.android.utils.ChatUtils;
//import org.chat21.android.utils.StringUtils;
//
//import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;
//
///*
// * Created by stefanodp91 on 15/01/18.
// */
//
////https://github.com/MahmoudAlyuDeen/FirebaseIM/blob/master/app/src/main/java/afterapps/com/firebaseim/login/LoginActivity.java
//public class bk_SaveFirebaseInstanceIdService extends FirebaseInstanceIdService {
//    private static final String TAG_TOKEN = "TAG_TOKEN";
//
//    @Override
//    public void onTokenRefresh() {
//        super.onTokenRefresh();
//
//        Log.d(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh");
//
//        String instanceId = FirebaseInstanceId.getInstance().getToken();
//        Log.d(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh: called with instanceId: " + instanceId);
//        Log.i(TAG_TOKEN, "SaveFirebaseInstanceIdService: token == " + instanceId);
//
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
////        String tenant = ChatAuthentication.getInstance().getTenant();
////        Log.i(DEBUG_LOGIN, "authTenant == " + tenant);
//
//        String appId = ChatManager.Configuration.appId;
//
//        if (firebaseUser != null && StringUtils.isValid(appId)) {
//
////            DatabaseReference root;
////            if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
////                root = FirebaseDatabase.getInstance().getReferenceFromUrl(ChatManager.Configuration.firebaseUrl);
////            } else {
////                root = FirebaseDatabase.getInstance().getReference();
////            }
////
////            DatabaseReference firebaseUsersPath = root
////                    .child("apps/" + appId + "/users/" + ChatUtils.normalizeUsername(firebaseUser.getUid()) + "/instanceId/" + instanceId);
////            firebaseUsersPath.setValue(instanceId);
//
////            DatabaseReference firebaseUsersPath = FirebaseDatabase.getInstance().getReference()
////                    .child("apps")
////                    .child(appId)
////                    .child("users")
//////                    .child(tenant + "-" + userId)
////                    .child(ChatUtils.normalizeUsername(firebaseUser.getUid()))
////                    .child("instanceId");
////            firebaseUsersPath.setValue(instanceId);
//
//            DatabaseReference root;
//            if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
//                root = FirebaseDatabase.getInstance().getReferenceFromUrl(ChatManager.Configuration.firebaseUrl);
//            } else {
//                root = FirebaseDatabase.getInstance().getReference();
//            }
//
//            // remove the instanceId for the logged user
//            DatabaseReference firebaseUsersPath = root
//                    .child("apps/" + ChatManager.Configuration.appId + "/users/" + ChatUtils.normalizeUsername(firebaseUser.getUid()) + "/instanceId");
//            firebaseUsersPath.setValue(instanceId);
//
//            Log.i(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh:  saved with instanceId: " + instanceId +
//                    ", appId: " + appId + ", firebaseUsersPath: " + firebaseUsersPath);
//        } else {
//            Log.i(DEBUG_LOGIN, "SaveFirebaseInstanceIdService.onTokenRefresh: user is null. instanceId == " + instanceId + ", appId == " + appId);
//        }
//    }
//}
