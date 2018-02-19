package org.chat21.android.core.presence.listeners;

/**
 * Created by stefanodp91 on 09/01/18.
 */

public interface MyPresenceListener {

    /**
     * Return the user connection status
     *
     * @param isConnected true if the user is online, false otherwise
     * @param deviceId    the id associated to the connected device
     */
    void isLoggedUserOnline(boolean isConnected, String deviceId);

    void onMyPresenceError(Exception e);
}
