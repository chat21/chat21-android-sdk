package chat21.android.user.presence;


import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


import chat21.android.core.ChatManager;
import chat21.android.dao.node.NodeDAO;
import chat21.android.dao.node.NodeDAOImpl;

import static chat21.android.utils.DebugConstants.DEBUG_MY_PRESENCE;

/**
 * Created by stefanodp91 on 07/09/17.
 */
class LoggedUserHandler {
    private static final String TAG = LoggedUserHandler.class.getName();

    void observeMyPresenceChanges(Context context, String userId,
                                  final OnMyPresenceChangesListener onMyPresenceChangesListener) {


        NodeDAO mNodeDAO = new NodeDAOImpl(context);
//        NodeDAO mNodeDAO = Chat.getNodeDao();

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = mNodeDAO.getNodePresenceConnections(userId);

        final DatabaseReference lastOnlineRef = mNodeDAO.getNodePresenceLastOnline(userId);

        mNodeDAO.getNodePresenceConnectedMeta().addValueEventListener(new ValueEventListener() {
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
                Log.e(TAG, errorMessage);
                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onMyPresenceChangesListener != null) {
                    onMyPresenceChangesListener.onMyPresenceChangeError(exception);
                }
            }
        });
    }
}
