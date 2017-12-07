package chat21.android.core.presence;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import chat21.android.core.ChatManager;
import chat21.android.core.presence.listeners.OnMyPresenceChangesListener;
import chat21.android.core.presence.listeners.OnUserPresenceChangesListener;

import static chat21.android.utils.DebugConstants.DEBUG_MY_PRESENCE;
import static chat21.android.utils.DebugConstants.DEBUG_USER_PRESENCE;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public class PresenceManger {

    public static void observeMyPresenceChanges(final OnMyPresenceChangesListener onMyPresenceChangesListener) {
        String userId = ChatManager.getInstance().getLoggedUser().getId();


        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + ChatManager.getInstance().getTenant() +
                        "/presence/" + userId + "/connections");

        // user last online time
        final DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + ChatManager.getInstance().getTenant() +
                        "/presence/" + userId + "/lastOnline");

        // firebase virtual meta data for user connection
        FirebaseDatabase.getInstance().getReference()
                .child("/.info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.i(DEBUG_MY_PRESENCE, "LoggedUserHandler.observeMyPresenceChanges" +
                        ".connectedRef.addValueEventListener.onDataChange " +
                        "- connected (or reconnected after connection loss");
                Log.i(DEBUG_MY_PRESENCE, "LoggedUserHandler.observeMyPresenceChanges" +
                        ".connectedRef.addValueEventListener.onDataChange - snapshot: " + snapshot);

                final boolean imConnected = (boolean) snapshot.getValue();
                Log.i(DEBUG_MY_PRESENCE, "LoggedUserHandler.observeMyPresenceChanges" +
                        ".connectedRef.addValueEventListener.onDataChange - imConnected: " + imConnected);

                if (imConnected) {
                    // connection established (or I've reconnected after a loss of connection)

                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp instead of just true
                    DatabaseReference autoId = myConnectionsRef.push();

                    // save this device instance
                    ChatManager.setPresenceDeviceInstance(autoId.getKey());

                    autoId.setValue(true);

                    // when this device disconnects, remove it
                    autoId.onDisconnect().removeValue();

                    // when I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP); // bugfix Issue #54
                }

                if (onMyPresenceChangesListener != null) {
                    onMyPresenceChangesListener.onMyPresenceChange(imConnected);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "observeMyPresenceChanges.onCancelled. " + databaseError.getMessage();
                Log.e(DEBUG_MY_PRESENCE, errorMessage);
                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onMyPresenceChangesListener != null) {
                    onMyPresenceChangesListener.onMyPresenceChangeError(exception);
                }
            }
        });
    }

    public static void logout(String presenceDeviceInstanceId,
                              final OnMyPresenceChangesListener onMyPresenceChangesListener) {

        String userId = ChatManager.getInstance().getLoggedUser().getId();

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + ChatManager.getInstance().getTenant() +
                        "/presence/" + userId + "/connections/" + presenceDeviceInstanceId);

        // user last online time
        final DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + ChatManager.getInstance().getTenant() +
                        "/presence/" + userId + "/lastOnline");

        // firebase virtual meta data for user connection
        FirebaseDatabase.getInstance().getReference()
                .child("/.info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.i(DEBUG_MY_PRESENCE, "LogoutHandler.signOut.connectedRef" +
                        ".addValueEventListener.onDataChange " +
                        "- connected (or reconnected after connection loss");
                Log.i(DEBUG_MY_PRESENCE, "LogoutHandler.signOut.connectedRef" +
                        ".addValueEventListener.onDataChange - snapshot: " + snapshot.toString());

                final boolean imConnected = (boolean) snapshot.getValue();
                Log.i(DEBUG_MY_PRESENCE, "LogoutHandler.signOut.connectedRef" +
                        ".addValueEventListener.onDataChange - imConnected: " + imConnected);

                if (imConnected) {
                    // when this device disconnects, remove it
                    myConnectionsRef.removeValue();

                    // when I disconnect, update the last time I was seen online
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                }

                if (onMyPresenceChangesListener != null) {
                    onMyPresenceChangesListener.onMyPresenceChange(imConnected);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "signOut.onCancelled. " + databaseError.getMessage();
                Log.e(DEBUG_MY_PRESENCE, errorMessage);
                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onMyPresenceChangesListener != null) {
                    onMyPresenceChangesListener.onMyPresenceChangeError(exception);
                }
            }
        });
    }

    public static void observeUserPresenceChanges(String userId, final OnUserPresenceChangesListener onUserPresenceChangesListener) {

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + ChatManager.getInstance().getTenant() + "/presence/" + userId + "/connections");

        // user last online time
        DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + ChatManager.getInstance().getTenant() + "/presence/" + userId + "/lastOnline");

        // subscriber for presence changes
        myConnectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.i(DEBUG_USER_PRESENCE, "UserHandler.observeUserPresenceChanges" +
                        ".myConnectionsRef.addValueEventListener.onDataChange - snapshot: " + snapshot);

                if (onUserPresenceChangesListener != null) {
                    onUserPresenceChangesListener.onUserPresenceChange(snapshot.hasChildren());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "observeForPresenceChanges.onCancelled. "
                        + databaseError.getMessage();
                Log.e(DEBUG_USER_PRESENCE, "UserHandler.observeUserPresenceChanges" +
                        ".myConnectionsRef.addValueEventListener. " + errorMessage);

                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onUserPresenceChangesListener != null) {
                    onUserPresenceChangesListener.onUserPresenceChangeError(exception);
                }
            }
        });

        // subscribe for last online changes
        lastOnlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                try {
                    long timestamp = (long) snapshot.getValue();

                    Log.i(DEBUG_USER_PRESENCE, "UserHandler.observeUserPresenceChanges" +
                            ".lastOnlineRef.addValueEventListener.onDataChange - snapshot: " + snapshot);
                    Log.i(DEBUG_USER_PRESENCE, "UserHandler.observeUserPresenceChanges" +
                            ".lastOnlineRef.addValueEventListener.onDataChange - timestamp: " + timestamp);


                    if (onUserPresenceChangesListener != null) {
                        onUserPresenceChangesListener.onUserLastOnlineChange(timestamp);
                    }
                } catch (Exception exception) {
                    Log.e(DEBUG_USER_PRESENCE, "observeUserPresenceChanges.lastOnlineRef.onDataChange: " +
                            "cannot retrieve last online. " + exception.getMessage());
                    onUserPresenceChangesListener.onUserPresenceChangeError(exception);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "observeForPresenceChanges.onCancelled. " + databaseError.getMessage();
                Log.e(DEBUG_USER_PRESENCE, errorMessage);
                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onUserPresenceChangesListener != null) {
                    onUserPresenceChangesListener.onUserPresenceChangeError(exception);
                }
            }
        });
    }
}