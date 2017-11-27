package chat21.android.presence;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import chat21.android.dao.node.NodeDAO;
import chat21.android.dao.node.NodeDAOImpl;

import static chat21.android.utils.DebugConstants.DEBUG_PRESENCE;

/**
 * Created by stefanodp91 on 07/09/17.
 */
class UserHandler {
    private static final String TAG = UserHandler.class.getName();

    void observeUserPresenceChanges(Context context, String userId,
                                    final OnPresenceChangesListener onPresenceChangesListener) {

        NodeDAO mNodeDAO = new NodeDAOImpl(context);

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = mNodeDAO.getNodePresenceConnections(userId);

        final DatabaseReference lastOnlineRef = mNodeDAO.getNodePresenceLastOnline(userId);

        // subscriber for presence changes
        myConnectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.i(DEBUG_PRESENCE, "UserHandler.observeUserPresenceChanges" +
                        ".myConnectionsRef.addValueEventListener.onDataChange - snapshot: " + snapshot);

                if (onPresenceChangesListener != null) {
                    onPresenceChangesListener.onPresenceChange(snapshot.hasChildren());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "observeForPresenceChanges.onCancelled. "
                        + databaseError.getMessage();
                Log.e(TAG, "UserHandler.observeUserPresenceChanges" +
                        ".myConnectionsRef.addValueEventListener. " + errorMessage);

                Exception exception = new Exception(errorMessage);
                FirebaseCrash.report(exception);

                if (onPresenceChangesListener != null) {
                    onPresenceChangesListener.onPresenceChangeError(exception);
                }
            }
        });

        // subscribe for last online changes
        lastOnlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                try {
                    long timestamp = (long) snapshot.getValue();

                    Log.i(DEBUG_PRESENCE, "UserHandler.observeUserPresenceChanges" +
                            ".lastOnlineRef.addValueEventListener.onDataChange - snapshot: " + snapshot);
                    Log.i(DEBUG_PRESENCE, "UserHandler.observeUserPresenceChanges" +
                            ".lastOnlineRef.addValueEventListener.onDataChange - timestamp: " + timestamp);


                    if (onPresenceChangesListener != null) {
                        onPresenceChangesListener.onLastOnlineChange(timestamp);
                    }
                } catch (Exception exception) {
                    Log.e(TAG, "observeUserPresenceChanges.lastOnlineRef.onDataChange: " +
                            "cannot retrieve last online. " + exception.getMessage());
                    onPresenceChangesListener.onPresenceChangeError(exception);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "observeForPresenceChanges.onCancelled. " + databaseError.getMessage();
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