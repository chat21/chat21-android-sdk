package org.chat21.android.core.presence;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import org.chat21.android.core.presence.listeners.MyPresenceListener;
import org.chat21.android.utils.StringUtils;

import static org.chat21.android.utils.DebugConstants.DEBUG_MY_PRESENCE;

/**
 * Created by stefanodp91 on 09/01/18.
 */

public class MyPresenceHandler {

    // since I can connect from multiple devices, we store each connection instance separately
    // any time that connectionsRef's value is null (i.e. has no children) I am offline
    private FirebaseDatabase database;
    private DatabaseReference connectionsRef;

    // stores the timestamp of my last disconnect (the last time I was seen online)
    private DatabaseReference lastOnlineRef;

    private DatabaseReference connectedRef;

    private ValueEventListener valueEventListener;

    private List<MyPresenceListener> myPresenceListeners;
    // the device that is currently connected
    String deviceId = null;

    public MyPresenceHandler(String firebaseUrl, String appId, String userId) {
        myPresenceListeners = new ArrayList<>();

        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        database = FirebaseDatabase.getInstance();
        if (StringUtils.isValid(firebaseUrl)) {
            connectionsRef = database.getReferenceFromUrl(firebaseUrl)
                    .child("/apps/" + appId + "/presence/" + userId + "/connections");
        } else {
            connectionsRef = database.getReference()
                    .child("/apps/" + appId + "/presence/" + userId + "/connections");
        }

        // stores the timestamp of my last disconnect (the last time I was seen online)
        if (StringUtils.isValid(firebaseUrl)) {
            lastOnlineRef = database.getReferenceFromUrl(firebaseUrl)
                    .child("/apps/" + appId + "/presence/" + userId + "/lastOnline");
        } else {
            lastOnlineRef = database.getReference()
                    .child("/apps/" + appId + "/presence/" + userId + "/lastOnline");
        }

        // /.info/connected is a boolean value which is not synchronized between clients because
        // the value is dependent on the state of the client.
        // In other words, if one client reads /.info/connected as false,
        // this is no guarantee that a separate client will also read false.
        // On Android, Firebase automatically manages connection state to
        // reduce bandwidth and battery usage.
        // When a client has no active listeners, no pending write or onDisconnect operations,
        // and is not explicitly disconnected by the goOffline method,
        // Firebase closes the connection after 60 seconds of inactivity.
        // source:
        // https://stackoverflow.com/questions/41563120/firebase-database-differentiate-between-online-and-offline-data
        connectedRef = database.getReference(".info/connected");
    }

    public void addPresenceListener(MyPresenceListener myPresenceListener) {
        if (!isListenerAdded(myPresenceListener))
            myPresenceListeners.add(myPresenceListener);
    }

    public void removePresenceListener(MyPresenceListener myPresenceListener) {
        if (isListenerAdded(myPresenceListener))
            myPresenceListeners.remove(myPresenceListener);
    }

    public boolean isListenerAdded(MyPresenceListener myPresenceListener) {
        if (myPresenceListeners == null)
            return false;

        if (myPresenceListeners.size() == 0) {
            return false;
        }
        return myPresenceListeners.contains(myPresenceListener) ? true : false;
    }

    public void upsertPresenceListener(MyPresenceListener myPresenceListener) {
        if (myPresenceListeners.contains(myPresenceListener)) {
            removePresenceListener(myPresenceListener);
            addPresenceListener(myPresenceListener);
            Log.i(DEBUG_MY_PRESENCE, "MyPresenceHandler.upsertPresenceListener: " +
                    "myPresenceListener with hashCode: " +
                    myPresenceListener.hashCode() + " updated");
        } else {
            addPresenceListener(myPresenceListener);
            Log.i(DEBUG_MY_PRESENCE, "MyPresenceHandler.upsertPresenceListener: " +
                    " myPresenceListener with hashCode: " +
                    myPresenceListener.hashCode() + " added");
        }
    }

    public ValueEventListener connect() {
        if (valueEventListener == null) {
            valueEventListener = connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);

                    if (connected) {
                        DatabaseReference con = connectionsRef.push();
                        deviceId = con.getKey();

                        // when this device disconnects, remove it
                        con.onDisconnect().removeValue();

                        // when I disconnect, update the last time I was seen online
                        lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                        // add this device to my connections list
                        // this value could contain info about the device or a timestamp too
                        con.setValue(Boolean.TRUE);
                    }

                    if (myPresenceListeners != null && myPresenceListeners.size() > 0) {
                        for (MyPresenceListener p : myPresenceListeners) {
                            p.isLoggedUserOnline(connected, deviceId);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.i(DEBUG_MY_PRESENCE, "MyPresenceHandler.connect.onCancelled: " +
                            "Listener was cancelled at .info/connected");

                    if (myPresenceListeners != null && myPresenceListeners.size() > 0) {
                        for (MyPresenceListener p : myPresenceListeners) {
                            p.onMyPresenceError(error.toException());
                        }
                    }
                }
            });
        } else {
            Log.d(DEBUG_MY_PRESENCE, "MyPresenceHandler.connect: listener already added.");
        }

        return valueEventListener;
    }

    public void dispose() {
        if (myPresenceListeners != null && myPresenceListeners.size() > 0) {
            myPresenceListeners.clear();
            Log.d(DEBUG_MY_PRESENCE, "MyPresenceHandler.disconnect:" +
                    " myPresenceListeners has been cleared.");
        }

        // when the device disconnects, remove the deviceId connection from the connections list
        if (connectionsRef != null && StringUtils.isValid(deviceId)) {
            connectionsRef.child(deviceId).removeValue();
            Log.d(DEBUG_MY_PRESENCE, "MyPresenceHandler.disconnect: " +
                    "connectionsRef with deviceId: " + deviceId + " has been detached.");
        }

        // when the user is disconnect, update the last time he was seen online
        if (lastOnlineRef != null) {
            lastOnlineRef.setValue(ServerValue.TIMESTAMP);
            Log.d(DEBUG_MY_PRESENCE, "MyPresenceHandler.disconnect:" +
                    " lastOnlineRef has been updated");
        }

        // detach all listeners
        if (connectedRef != null && valueEventListener != null) {
            connectedRef.removeEventListener(valueEventListener);
            valueEventListener = null;
            Log.d(DEBUG_MY_PRESENCE, "MyPresenceHandler.disconnect: " +
                    "connectedRef valueEventListener has been detached");
        }
    }
}