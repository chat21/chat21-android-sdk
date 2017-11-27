package chat21.android.user.presence;


import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import chat21.android.dao.node.NodeDAO;
import chat21.android.dao.node.NodeDAOImpl;
import chat21.android.presence.OnPresenceChangesListener;

import static chat21.android.utils.DebugConstants.DEBUG_MY_PRESENCE;

/**
 * Created by stefanodp91 on 07/09/17.
 */
class LogoutHandler {
    private static final String TAG = LogoutHandler.class.getName();

    void signOut(Context context, String userId,
                 String presenceDeviceInstanceId,
                 final OnPresenceChangesListener onPresenceChangesListener) {

        NodeDAO mNodeDAO = new NodeDAOImpl(context);
//        NodeDAO mNodeDAO = Chat.getNodeDao();


        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = mNodeDAO.getNodePresenceConnections(userId)
                .child(presenceDeviceInstanceId);

        final DatabaseReference lastOnlineRef = mNodeDAO.getNodePresenceLastOnline(userId);

        mNodeDAO.getNodePresenceConnectedMeta().addValueEventListener(new ValueEventListener() {
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

                if (onPresenceChangesListener != null) {
                    onPresenceChangesListener.onPresenceChange(imConnected);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "signOut.onCancelled. " + databaseError.getMessage();
                Log.e(TAG, errorMessage);
                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onPresenceChangesListener != null) {
                    onPresenceChangesListener.onPresenceChangeError(exception);
                }
            }
        });
    }
}