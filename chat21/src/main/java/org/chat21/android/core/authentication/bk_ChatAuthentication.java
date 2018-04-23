//package org.chat21.android.core.authentication;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.support.annotation.NonNull;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.UserProfileChangeRequest;
//import com.google.firebase.crash.FirebaseCrash;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ServerValue;
//import com.google.firebase.iid.FirebaseInstanceId;
//
//import org.chat21.android.R;
//import org.chat21.android.core.authentication.task.GetCustomTokenTask;
//import org.chat21.android.core.authentication.task.OnCustomAuthTokenCallback;
//import org.chat21.android.core.authentication.task.RefreshFirebaseInstanceIdTask;
//import org.chat21.android.core.users.models.ChatUser;
//import org.chat21.android.core.users.models.IChatUser;
//import org.chat21.android.instanceid.receiver.TokenBroadcastReceiver;
//import org.chat21.android.utils.ChatUtils;
//import org.chat21.android.utils.StringUtils;
//
//import java.io.IOException;
//
//import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;
//
///**
// * Created by andrealeo on 27/11/17.
// */
//
//public final class bk_ChatAuthentication {
//
//    //firebase auth START
//
////    doc here
////    https://firebase.google.com/docs/auth/android/custom-auth
//
////    readme here
////    https://github.com/firebase/quickstart-android/tree/master/auth
//
////    example here
////    https://github.com/firebase/quickstart-android/blob/master/auth/app/src/main/java/com/google/firebase/quickstart/auth/CustomAuthActivity.java
//
//    public interface OnChatLoginCallback {
//        void onChatLoginSuccess(IChatUser currentUser);
//
//        void onChatLoginError(Exception e);
//    }
//
//    public interface OnChatLogoutCallback {
//        void onChatLogoutSuccess();
//
//        void onChatLogoutError(Exception e);
//    }
//
//    // auth token
//    private FirebaseAuth mAuth;
//    private FirebaseAuth.AuthStateListener mAuthListener;
//    private String mCustomToken;
//    private TokenBroadcastReceiver mTokenReceiver;
//
//    // auth data
//    private String email;
//    private String fullName;
//    private String tenant;
//    private boolean isEmailUpdated = false;
//    private boolean isUserProfileUpdated = false;
//
//    private static ChatAuthentication authInstance;
//
//
//    private bk_ChatAuthentication() {
//// Prevent form the reflection api.
//        if (authInstance != null) {
//            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
//        }
//    }
//
//    //firebase auth END
//
//    public void removeAuthStateListener() {
//        Log.i(DEBUG_LOGIN, "removeAuthStateListener");
//
//        if (mAuthListener != null) {
//            mAuth.removeAuthStateListener(mAuthListener);
//        }
//    }
//
//    public void registerFirebaseReceiver(Context context) {
//        Log.d(DEBUG_LOGIN, "registerFirebaseReceiver");
//
//        // Create token receiver (for demo purposes only)
//        mTokenReceiver = new TokenBroadcastReceiver() {
//            @Override
//            public void onNewToken(String token) {
//                Log.i(DEBUG_LOGIN, "onNewToken:" + token);
//                setCustomToken(token);
//            }
//        };
//
//        context.registerReceiver(mTokenReceiver, TokenBroadcastReceiver.getFilter());
//    }
//
//    public void createAuthListener() {
//        Log.d(DEBUG_LOGIN, "createAuthListener");
//
//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                Log.d(DEBUG_LOGIN, "onAuthStateChanged");
//
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    // User is signed in
//                    Log.i(DEBUG_LOGIN, "onAuthStateChanged:signed_in:" + user.getUid());
//
//                    if (!isEmailUpdated)
//                        updateUserEmail(getEmail());
//
//                    if (!isUserProfileUpdated)
//                        updateUserProfile(getFullName(), null);
//
//                } else {
//                    // User is signed out
//                    Log.i(DEBUG_LOGIN, "onAuthStateChanged:signed_out");
//                }
//            }
//        };
//    }
//
//    public void signInWithUid(final Activity loginActivity, final String appId,
//                              String uid, final OnChatLoginCallback onChatLoginCallback) {
//        Log.d(DEBUG_LOGIN, "signInWithUid");
//
//        final String userIdNormalized = ChatUtils.normalizeUsername(uid);
//
//        //getting token
//        String generateTokenUrl = loginActivity
//                .getString(R.string.custom_auth_verify_token_url) + userIdNormalized;
//
//        new GetCustomTokenTask(new OnCustomAuthTokenCallback() {
//            @Override
//            public void onCustomAuthRetrievedSuccess(String token) {
//                Log.i(DEBUG_LOGIN, "signInWithUid.onCustomAuthRetrievedSuccess : authToken == " + token);
//
//                createContactNode(appId, userIdNormalized);
//                signInWithToken(loginActivity, token, onChatLoginCallback);
//            }
//
//            @Override
//            public void onCustomAuthRetrievedWithError(Exception e) {
//                Log.e(DEBUG_LOGIN, "signInWithUid.onCustomAuthRetrievedWithError");
//
//                // fix Issue #24
//                onChatLoginCallback.onChatLoginError(e);
//            }
//        }).execute(generateTokenUrl);
//    }
//
//
//    private void createContactNode(String appId, String userId) {
//        Log.d(DEBUG_LOGIN, "createContactNode: userId == " + userId);
//
//        DatabaseReference mNodeContacts = FirebaseDatabase.getInstance().getReference()
//                .child("apps/" + appId + "/contacts/" + userId);
//
//        // add uid
//        mNodeContacts
//                .child("uid")
//                .setValue(userId);
//    }
//
//    public void updateNodeContacts(String appId, String userId, String email, String name, String surname) {
//        Log.d(DEBUG_LOGIN, "updateNodeContacts: userId == " + userId + ", email == "
//                + email + ", name == " + name + ", surname == " + surname);
//
//        // retrieve node contacts
//        DatabaseReference mNodeContacts = FirebaseDatabase.getInstance().getReference()
//                .child("apps/" + appId + "/contacts/" + userId);
//
////            // add uid
////            mNodeContacts
////                    .child("uid")
////                    .setValue(userId);
//
//        // add email
//        mNodeContacts
//                .child("email")
//                .setValue(email);
//
//        // add name
//        mNodeContacts
//                .child("name")
//                .setValue(name);
//
//        // add surname
//        mNodeContacts
//                .child("surname")
//                .setValue(surname);
//
//        // add timestamp
//        mNodeContacts
//                .child("timestamp")
//                .setValue(ServerValue.TIMESTAMP);
//    }
//
//    public void signInAnonymously(final Activity loginActivity, final OnChatLoginCallback onChatLoginCallback) {
//
//        Log.i(DEBUG_LOGIN, "signInWithEmailAndPassword called");
//
//        // bugfix Issue #11
//        // if the google play service is not updated inform the user
//        if (!checkPlayServices(loginActivity.getApplicationContext())) {
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity);
//            builder.setTitle("Google Play Services not updated")
//                    .setMessage("Update the Google Play Service to continue using the app")
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            loginActivity.finish();
//                        }
//                    })
//                    .show();
//        }
//
//        getFirebaseAuth().signInAnonymously()
//                .addOnCompleteListener(loginActivity, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (task.isSuccessful()) {
//                            new RefreshFirebaseInstanceIdTask().execute();
//
//                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
//
//                            IChatUser iChatUser = convertFirebaseUserToChatUser(firebaseUser);
//
//                            Log.d(DEBUG_LOGIN, "calling  onChatLoginCallback.onChatLoginSuccess() iChatUser :  " + iChatUser);
//
//                            onChatLoginCallback.onChatLoginSuccess(iChatUser);
//                        } else {
//                            Log.e(DEBUG_LOGIN, "signInAnonymously", task.getException());
//                            Toast.makeText(loginActivity, "Authentication failed.",
//                                    Toast.LENGTH_LONG).show();
//                            Log.d(DEBUG_LOGIN, "calling  onChatLoginCallback.onChatLoginError()");
//                            onChatLoginCallback.onChatLoginError(task.getException());
//                        }
//                    }
//                });
//    }
//
//
//    private  IChatUser convertFirebaseUserToChatUser (FirebaseUser firebaseUser) {
//        if (firebaseUser!=null){
//            return new ChatUser(firebaseUser.getUid(), firebaseUser.getDisplayName());
//        }else {
//            return null;
//        }
//    }
//
//    public void signInWithEmailAndPassword(final Activity loginActivity, final String email, final String password,
//                                           final OnChatLoginCallback onChatLoginCallback) {
//
//        Log.i(DEBUG_LOGIN, "signInWithEmailAndPassword called");
//
//        // bugfix Issue #11
//        // if the google play service is not updated inform the user
//        if (!checkPlayServices(loginActivity.getApplicationContext())) {
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity);
//            builder.setTitle("Google Play Services not updated")
//                    .setMessage("Update the Google Play Service to continue using the app")
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            loginActivity.finish();
//                        }
//                    })
//                    .show();
//        }
//
//        getFirebaseAuth().signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(loginActivity, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (task.isSuccessful()) {
//                            new RefreshFirebaseInstanceIdTask().execute();
//
//
//                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
//
//                            IChatUser iChatUser = convertFirebaseUserToChatUser(firebaseUser);
//
//                            Log.d(DEBUG_LOGIN, "calling  onChatLoginCallback.onChatLoginSuccess() iChatUser :  " + iChatUser);
//
//                            onChatLoginCallback.onChatLoginSuccess(iChatUser);
//                        } else {
//                            Log.e(DEBUG_LOGIN, "signInWithCustomToken", task.getException());
//                            Toast.makeText(loginActivity, "Authentication failed.",
//                                    Toast.LENGTH_LONG).show();
//                            Log.d(DEBUG_LOGIN, "calling  onChatLoginCallback.onChatLoginError()");
//                            onChatLoginCallback.onChatLoginError(task.getException());
//                        }
//                    }
//                });
//    }
//
//    // https://bitbucket.org/frontiere21/chat21-android-sdk/issues/2/lutente-andrealeo-sembra-non-registrare-pi
//    public void signInWithToken(final Activity loginActivity, String token,
//                                final OnChatLoginCallback onChatLoginCallback) {
//        Log.i(DEBUG_LOGIN, "signInWithToken called");
//
//        // bugfix Issue #11
//        // if the google play service is not updated inform the user
//        if (!checkPlayServices(loginActivity.getApplicationContext())) {
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity);
//            builder.setTitle("Google Play Services not updated")
//                    .setMessage("Update the Google Play Service to continue using the app")
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            loginActivity.finish();
//                        }
//                    })
//                    .show();
//        }
//
//        //setting token
//        setCustomToken(token);
//
//        getFirebaseAuth().signInWithCustomToken(token).addOnCompleteListener(loginActivity, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                Log.i(DEBUG_LOGIN, "signInWithCustomToken:onComplete:" + task.isSuccessful());
//
//                // If sign in fails, display a message to the user. If sign in succeeds
//                // the auth state listener will be notified and logic to handle the
//                // signed in user can be handled in the listener.
//                if (task.isSuccessful()) {
//                    new RefreshFirebaseInstanceIdTask().execute();
//
//                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
//
//                    IChatUser iChatUser = convertFirebaseUserToChatUser(firebaseUser);
//
//                    Log.d(DEBUG_LOGIN, "calling  onChatLoginCallback.onChatLoginSuccess() iChatUser :  " + iChatUser);
//
//                    onChatLoginCallback.onChatLoginSuccess(iChatUser);
//                } else {
//                    Log.e(DEBUG_LOGIN, "signInWithCustomToken", task.getException());
//                    Toast.makeText(loginActivity, "Authentication failed.",
//                            Toast.LENGTH_LONG).show();
//                    Log.d(DEBUG_LOGIN, "calling  onChatLoginCallback.onChatLoginError()");
//                    onChatLoginCallback.onChatLoginError(task.getException());
//                }
//
//            }
//        }).addOnFailureListener(loginActivity, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.e(DEBUG_LOGIN, "signInWithCustomToken.onFailure", e);
//            }
//        });
//    }
//
//    /*
//     * Method to check whether to check Google Play Services is up to date.
//     *
//     * source : https://medium.com/@anshuljain/an-advice-before-using-the-latest-version-of-google-play-services-953dd931b140
//     * @param context
//     * @return false if the play service is not valid, true otherwise
//     */
//    // bugfix Issue #11
//    private boolean checkPlayServices(Context context) {
//        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
//        int result = googleAPI.isGooglePlayServicesAvailable(context);
//        if (result != ConnectionResult.SUCCESS) {
//            //Google Play Services app is not available or version is not up to date. Error the
//            // error condition here
//            return false;
//        }
//        //Google Play Services is available. Return true.
//        return true;
//    }
//
//    public void signOut(final String appId, final OnChatLogoutCallback onChatLogoutCallback) {
//        Log.d(DEBUG_LOGIN, "signOut");
//
//        new AsyncTask<Void, Void, Void>() {
//            Exception logoutException = null;
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//            }
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//
//                    // fix Issue #5
//                    String userId = ChatAuthentication.getInstance()
//                            .getFirebaseAuth()
//                            .getCurrentUser()
//                            .getUid();
//
//                    // fix Issue #5
//                    removeInstanceId(appId, userId);  // fix Issue #23
//
//                    FirebaseInstanceId.getInstance().deleteInstanceId();
//                } catch (IOException e) {
//                    Log.e(DEBUG_LOGIN, "cannot delete instanceId. " + e.getMessage());
//                    logoutException = e;
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//
////                OnPresenceListener onMyPresenceListener = new OnPresenceListener() {
////                    @Override
////                    public void onChanged(boolean imConnected) {
////                        // if connected => disconnect
////                        if (imConnected) {
////                            getFirebaseAuth().signOut();
////                            Log.i(DEBUG_LOGIN, "signed out from firebase with success");
////                            Log.i(DEBUG_MY_PRESENCE, "Chat.onPresenceChange - " +
////                                    "signed out from firebase with success");
////                            onChatLogoutCallback.onChatLogoutSuccess();
////                        }
////                    }
////
////                    @Override
////                    public void onLastOnlineChanged(long lastOnline) {
////                        Log.d(DEBUG_LOGIN, "onLastOnlineChange - lastOnline: " + lastOnline);
////                        Log.d(DEBUG_MY_PRESENCE, "Chat.onLastOnlineChange " +
////                                "- lastOnline: " + lastOnline);
////                    }
////
////                    @Override
////                    public void onError(Exception e) {
////                        Log.e(DEBUG_LOGIN, e.getMessage());
////                        Log.d(DEBUG_MY_PRESENCE, "Chat.onPresenceChangeError" + e.getMessage());
////                        FirebaseCrash.report(e);
////                        onChatLogoutCallback.onChatLogoutError(logoutException);
////                    }
////                };
//
//                if (logoutException == null) {
//                    //always destroy authInstance
//                    destroyInstance();
//                    onChatLogoutCallback.onChatLogoutSuccess();
//
//                } else {
//                    Log.e(DEBUG_LOGIN, "cannot sign outfrom firebase. " + logoutException.getMessage());
//                    onChatLogoutCallback.onChatLogoutError(logoutException);
//                }
//
//                //always destroy authInstance
//                destroyInstance();
//
//            }
//        }.execute();
//    }
//
//    // remove the instanceId for an user on a tenant
//    // fix Issue #5
//    // fix Issue #23
//    private void removeInstanceId(String appId, String userId) {
//        Log.d(DEBUG_LOGIN, "removeInstanceId");
//
//        DatabaseReference firebaseUsersPath = FirebaseDatabase.getInstance().getReference()
//                .child("apps/" + appId + "/users/" + userId + "/instanceId");
//        firebaseUsersPath.removeValue();
//    }
//
//    private void setCustomToken(String token) {
//        Log.d(DEBUG_LOGIN, "setCustomToken: token == " + token);
//
//        mCustomToken = token;
//
//        String status;
//        if (mCustomToken != null) {
//            status = "Token:" + mCustomToken;
//        } else {
//            status = "Token: null";
//        }
//
//        Log.d(DEBUG_LOGIN, "status == " + status);
//    }
//
//    private void updateUserEmail(final String email) {
//        Log.d(DEBUG_LOGIN, "updateUserEmail");
//
//        if (StringUtils.isValid(email)) {
//            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//            user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    Log.d(DEBUG_LOGIN, "updateUserEmail.onCompleteSuccess");
//
//                    if (task.isSuccessful()) {
//                        Log.i(DEBUG_LOGIN, "User email address (" + email + ") " +
//                                "updated with success for user with uid: " + user.getUid());
//                        isEmailUpdated = true;
//                    } else {
//                        task.getException().printStackTrace();
//
//                        String errorMessage = "updateUserEmail.onCompleteError: "
//                                + task.getException().getMessage();
//                        Log.e(DEBUG_LOGIN, errorMessage);
//                        FirebaseCrash.report(new Exception(errorMessage));
//                    }
//                }
//            });
//        }
//    }
//
//    private void updateUserProfile(final String displayName, Uri userPhotoUri) {
//        Log.d(DEBUG_LOGIN, "updateUserProfile: displayName == " + displayName
//                + ", userPhotoUri == " + userPhotoUri);
//
//        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (StringUtils.isValid(displayName)) {
//            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
//                    .setDisplayName(displayName);
//
//            if (userPhotoUri != null)
//                builder.setPhotoUri(userPhotoUri);
//
//            UserProfileChangeRequest profileUpdates = builder.build();
//
//            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    Log.d(DEBUG_LOGIN, "updateUserProfile.onCompleteSuccess");
//
//                    if (task.isSuccessful()) {
//                        Log.i(DEBUG_LOGIN, "User profile (" + displayName + ")" +
//                                " updated with success for user with uid: " + user.getUid());
//                        isUserProfileUpdated = true;
//                    } else {
//                        task.getException().printStackTrace();
//
//                        String errorMessage = "updateUserProfile.onCompleteError: "
//                                + task.getException().getMessage();
//                        Log.e(DEBUG_LOGIN, errorMessage);
//                        FirebaseCrash.report(new Exception(errorMessage));
//                    }
//                }
//            });
//        }
//    }
//
//    public FirebaseAuth getFirebaseAuth() {
//        Log.d(DEBUG_LOGIN, "getFirebaseAuth");
//
//        return mAuth;
//    }
//
//    public String getFirebaseCustomToken() {
//        Log.d(DEBUG_LOGIN, "getFirebaseCustomToken");
//
//        return mCustomToken;
//    }
//
//    public String getEmail() {
//        Log.d(DEBUG_LOGIN, "getEmail");
//
//        return email;
//    }
//
//    public void setEmail(String email) {
//        Log.d(DEBUG_LOGIN, "setEmail: email == " + email);
//
//        this.email = email;
//    }
//
//    public String getFullName() {
//        Log.d(DEBUG_LOGIN, "getFullName");
//
//        return fullName;
//    }
//
//    public void setFullName(String fullName) {
//        Log.d(DEBUG_LOGIN, "setFullName: fullName == " + fullName);
//
//        this.fullName = fullName;
//    }
//
//    public String getTenant() {
//        Log.d(DEBUG_LOGIN, "getTenant");
//
//        return tenant;
//    }
//
//    public void setTenant(String tenant) {
//        this.tenant = tenant;
//    }
//
//    public FirebaseAuth.AuthStateListener getAuthListener() {
//        return mAuthListener;
//    }
//
//    public static ChatAuthentication getInstance() {
//        Log.d(DEBUG_LOGIN, "getInstance");
//
//        if (authInstance == null) {
//            authInstance = new ChatAuthentication();
//            Log.d(DEBUG_LOGIN, "creating new Chat.Authentication instance");
//        } else {
//            Log.d(DEBUG_LOGIN, "Chat.Authentication instance already exists");
//        }
//
//        authInstance.mAuth = FirebaseAuth.getInstance();
//        Log.d(DEBUG_LOGIN, "mAuth.hashCode() : " + authInstance.mAuth.hashCode());
//
//        return authInstance;
//    }
//
//    public void destroyInstance() {
//        authInstance = null;
//        Log.d(DEBUG_LOGIN, "authInstance destroyed");
//    }
//}