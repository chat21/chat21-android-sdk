package chat21.android.core.presence;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat21.android.core.presence.listeners.PresenceListener;

import static chat21.android.utils.DebugConstants.DEBUG_USER_PRESENCE;

/**
 * Created by stefanodp91 on 09/01/18.
 */

public class PresenceHandler {

    // since I can connect from multiple devices, we store each connection instance separately
    // any time that connectionsRef's value is null (i.e. has no children) I am offline
    private FirebaseDatabase database;

    private DatabaseReference userPresenceRef;

//    private DatabaseReference connectionsRef;

//    // stores the timestamp of user last disconnect (the last time I was seen online)
//    private DatabaseReference lastOnlineRef;

    private ValueEventListener valueEventListener;

    private List<PresenceListener> presenceListeners;
//    private String firebase;
//    private String appId;

    public PresenceHandler(String firebase, String appId, String userId) {
//        this.firebase = firebase;
//        this.appId = appId;
        presenceListeners = new ArrayList<>();

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        database = FirebaseDatabase.getInstance();

        userPresenceRef = database.getReferenceFromUrl(firebase).child("/apps/" + appId + "/presence/" + userId);
//        connectionsRef = userPresenceRef.child("/connections");

//        // stores the timestamp of last disconnect (the last time I was seen online)
//        lastOnlineRef = userPresenceRef.child("/lastOnline");
    }

    public void addPresenceListener(PresenceListener presenceListener) {
        if (!isListenerAdded(presenceListener))
            presenceListeners.add(presenceListener);
    }

    public void removePresenceListener(PresenceListener presenceListener) {
        if (isListenerAdded(presenceListener))
            presenceListeners.remove(presenceListener);
    }

    public boolean isListenerAdded(PresenceListener presenceListener) {
        if (presenceListeners == null)
            return false;

        if (presenceListeners.size() == 0) {
            return false;
        }
        return presenceListeners.contains(presenceListener) ? true : false;
    }

    public void upsertPresenceListener(PresenceListener presenceListener) {
        if (presenceListeners.contains(presenceListener)) {
            removePresenceListener(presenceListener);
            addPresenceListener(presenceListener);
            Log.i(DEBUG_USER_PRESENCE, "PresenceHandler.upsertPresenceListener: " +
                    "PresenceListener with hashCode: " + presenceListener.hashCode() + " updated");
        } else {
            addPresenceListener(presenceListener);
            Log.i(DEBUG_USER_PRESENCE, "PresenceHandler.upsertPresenceListener: " +
                    " PresenceListener with hashCode: " + presenceListener.hashCode() + " added");
        }
    }

    public ValueEventListener connect() {
//        if (valueEventListener == null) {

        valueEventListener = userPresenceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(DEBUG_USER_PRESENCE, "PresenceHandler.connect.onDataChange: dataSnapshot == " + dataSnapshot);

                // full datasnapshot example
//                    DataSnapshot {
//                        key = etWruToogVdIyLztne1tBu3VR902,
//                                value = {
//                                        lastOnline = 1515509219023,
//                                        connections = {
//                                                -L2QbracGqMhxa5HAkBb = true
//                                        }
//                                }
//                    }

                long lastOnline = -1;
                boolean online = false;
                Map<String, Boolean> connections = new HashMap<>();

                if (dataSnapshot.getValue() != null) {
                    // retrieve datasnapshot value
                    Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();

                    // retrieve last online
                    try {
                        lastOnline = (long) value.get("lastOnline");
                    } catch (Exception e) {
                        Log.w(DEBUG_USER_PRESENCE, "PresenceHandler.connect.onDataChange: Cannot retrieve lastOnline " + e.toString());
                    }

                    // retrieve connections
                    try {
                        connections = (Map<String, Boolean>) value.get("connections");
                    } catch (Exception e) {
                        Log.w(DEBUG_USER_PRESENCE, "PresenceHandler.connect.onDataChange: Cannot retrieve connections " + e.toString());
                    }

                    // if exists at least one connection, the user is marked as online
                    if (connections != null && connections.size() > 0) {
                        online = true;
                    }
                }

                // notify presence changes
                if (presenceListeners != null && presenceListeners.size() > 0) {
                    for (PresenceListener p : presenceListeners) {
                        p.isUserOnline(online);
                    }

                    for (PresenceListener p : presenceListeners) {
                        p.userLastOnline(lastOnline);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(DEBUG_USER_PRESENCE, "PresenceHandler.connect.onCancelled: " + databaseError.toString());

                for (PresenceListener p : presenceListeners) {
                    p.onPresenceError(databaseError.toException());
                }
            }
        });

//        } else {
//            Log.d(DEBUG_USER_PRESENCE, "PresenceHandler.connect: listener already added.");
//        }

        return valueEventListener;
    }

    public void disconnect() {
        if (presenceListeners != null && presenceListeners.size() > 0) {
            presenceListeners.clear();
        }

        if (userPresenceRef != null && valueEventListener != null) {
            userPresenceRef.removeEventListener(valueEventListener);
        }
    }
}