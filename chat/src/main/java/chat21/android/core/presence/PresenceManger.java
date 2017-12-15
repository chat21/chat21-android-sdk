package chat21.android.core.presence;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import chat21.android.core.presence.listeners.OnPresenceListener;

import static chat21.android.utils.DebugConstants.DEBUG_USER_PRESENCE;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public class PresenceManger {


    public static void observeUserPresenceChanges(String appId, String userId, final OnPresenceListener onPresenceListener) {

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/presence/" + userId + "/connections");

        // user last online time
        DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/presence/" + userId + "/lastOnline");

        // subscriber for presence changes
        myConnectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(DEBUG_USER_PRESENCE, "PresenceManger.observeUserPresenceChanges" +
                        ".myConnectionsRef.onDataChange: snapshot == " + snapshot.toString());

                if (onPresenceListener != null) {
                    onPresenceListener.onChanged(snapshot.hasChildren());
                } else {
                    Log.w(DEBUG_USER_PRESENCE, "PresenceManger.observeUserPresenceChanges" +
                            ".myConnectionsRef.onDataChange : onPresenceListener is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "PresenceManger.observeUserPresenceChanges" +
                        ".myConnectionsRef.onCancelled: " + databaseError.toString();

                Log.e(DEBUG_USER_PRESENCE, errorMessage);

                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onPresenceListener != null) {
                    onPresenceListener.onError(exception);
                } else {
                    Log.w(DEBUG_USER_PRESENCE, "PresenceManger.observeUserPresenceChanges" +
                            ".myConnectionsRef.onCancelled : onPresenceListener is null");
                }
            }
        });

        // subscribe for last online changes
        lastOnlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(DEBUG_USER_PRESENCE, "PresenceManger.observeUserPresenceChanges" +
                        ".lastOnlineRef.onDataChange: snapshot == " + snapshot.toString());

                try {
                    long timestamp = (long) snapshot.getValue();

                    Log.d(DEBUG_USER_PRESENCE, "PresenceManger.observeUserPresenceChanges" +
                            ".lastOnlineRef.onDataChange: timestamp == " + timestamp);

                    if (onPresenceListener != null) {
                        onPresenceListener.onLastOnlineChanged(timestamp);
                    } else {
                        Log.w(DEBUG_USER_PRESENCE, "PresenceManger.observeUserPresenceChanges" +
                                ".lastOnlineRef.onDataChange : onPresenceListener is null");
                    }
                } catch (Exception exception) {
                    String errorMessage = "PresenceManger.observeUserPresenceChanges" +
                            ".lastOnlineRef.onDataChange: " + exception.toString();

                    Log.e(DEBUG_USER_PRESENCE, errorMessage);

                    FirebaseCrash.report(exception);

                    onPresenceListener.onError(exception);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "PresenceManger.observeUserPresenceChanges" +
                        ".lastOnlineRef.onCancelled: " + databaseError.toString();

                Log.e(DEBUG_USER_PRESENCE, errorMessage);

                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onPresenceListener != null) {
                    onPresenceListener.onError(exception);
                } else {
                    Log.w(DEBUG_USER_PRESENCE, "PresenceManger.observeUserPresenceChanges" +
                            ".lastOnlineRef.onCancelled : onPresenceListener is null");
                }
            }
        });
    }

    public static void logout(String appId, String userId, String presenceDeviceInstanceId, final OnPresenceListener onPresenceListener) {

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId +
                        "/presence/" + userId + "/connections/" + presenceDeviceInstanceId);

        // user last online time
        final DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId +
                        "/presence/" + userId + "/lastOnline");

        // firebase virtual meta data for user connection
        FirebaseDatabase.getInstance().getReference()
                .child("/.info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Log.d(DEBUG_USER_PRESENCE, "PresenceManger.logout" +
                        "./.info/connected.onDataChange: snapshot == " + snapshot.toString());


                final boolean imConnected = (boolean) snapshot.getValue();
                Log.d(DEBUG_USER_PRESENCE, "PresenceManger.logout" +
                        "./.info/connected.onDataChange: imConnected == " + imConnected);

                if (imConnected) {
                    // when this device disconnects, remove it
                    myConnectionsRef.removeValue();

                    // when I disconnect, update the last time I was seen online
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                }

                if (onPresenceListener != null) {
                    onPresenceListener.onChanged(imConnected);
                } else {
                    Log.w(DEBUG_USER_PRESENCE, "PresenceManger.logout" +
                            "./.info/connected.onDataChange: onPresenceListener is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "PresenceManger.logout " +
                        "./.info/connected.onCancelled: " + databaseError.toString();

                Log.e(DEBUG_USER_PRESENCE, errorMessage);

                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);


                if (onPresenceListener != null) {
                    onPresenceListener.onError(exception);
                } else {
                    Log.w(DEBUG_USER_PRESENCE, "PresenceManger.logout" +
                            "./.info/connected.onCancelled: onPresenceListener is null");
                }
            }
        });
    }
}