package org.chat21.android.core.presence.listeners;

/**
 * Created by stefanodp91 on 09/01/18.
 */

public interface PresenceListener {

    /**
     * Return the user connection status
     *
     * @param isConnected true if the user is online, false otherwise
     */
    void isUserOnline(boolean isConnected);

    /**
     * Return the user last online timestamp
     *
     * @param lastOnline the last online timestamp
     */
    void userLastOnline(long lastOnline);

    void onPresenceError(Exception e);
}
