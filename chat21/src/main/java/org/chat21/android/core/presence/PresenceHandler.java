package org.chat21.android.core.presence;

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

import org.chat21.android.core.presence.listeners.PresenceListener;
import org.chat21.android.utils.StringUtils;

import static org.chat21.android.utils.DebugConstants.DEBUG_USER_PRESENCE;

/**
 * Created by stefanodp91 on 09/01/18.
 */

public class PresenceHandler {

    public static final long LAST_ONLINE_UNDEFINED = -1;

    // since I can connect from multiple devices, we store each connection instance separately
    // any time that connectionsRef's value is null (i.e. has no children) I am offline
    private FirebaseDatabase database;

    private DatabaseReference userPresenceRef;

    private ValueEventListener valueEventListener;

    private List<PresenceListener> presenceListeners;

    public PresenceHandler(String firebaseUrl, String appId, String userId) {
        presenceListeners = new ArrayList<>();

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        database = FirebaseDatabase.getInstance();

        if (StringUtils.isValid(firebaseUrl)) {
            userPresenceRef = database.getReferenceFromUrl(firebaseUrl)
                    .child("/apps/" + appId + "/presence/" + userId);
        } else {
            userPresenceRef = database.getReference()
                    .child("/apps/" + appId + "/presence/" + userId);
        }
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

        valueEventListener = userPresenceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(DEBUG_USER_PRESENCE, "PresenceHandler.connect.onDataChange:" +
                        " dataSnapshot == " + dataSnapshot);

                long lastOnline = LAST_ONLINE_UNDEFINED;
                boolean online = false;
                Map<String, Boolean> connections = new HashMap<>();

                if (dataSnapshot.getValue() != null) {
                    // retrieve datasnapshot value
                    Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();

                    // retrieve last online
                    try {
                        lastOnline = (long) value.get("lastOnline");
                    } catch (Exception e) {
                        Log.w(DEBUG_USER_PRESENCE, "PresenceHandler.connect.onDataChange:" +
                                " Cannot retrieve lastOnline " + e.toString());
                    }

                    // retrieve connections
                    try {
                        connections = (Map<String, Boolean>) value.get("connections");
                    } catch (Exception e) {
                        Log.w(DEBUG_USER_PRESENCE, "PresenceHandler.connect.onDataChange:" +
                                " Cannot retrieve connections " + e.toString());
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
                Log.w(DEBUG_USER_PRESENCE, "PresenceHandler.connect.onCancelled: " +
                        databaseError.toString());

                for (PresenceListener p : presenceListeners) {
                    p.onPresenceError(databaseError.toException());
                }
            }
        });

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