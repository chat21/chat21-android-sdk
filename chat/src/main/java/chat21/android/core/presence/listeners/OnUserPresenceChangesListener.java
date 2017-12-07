package chat21.android.core.presence.listeners;

/**
 * Created by stefanodp91 on 07/09/17.
 */
public interface OnUserPresenceChangesListener {
    void onUserPresenceChange(boolean imConnected);

    void onUserLastOnlineChange(long lastOnline);

    void onUserPresenceChangeError(Exception e);
}