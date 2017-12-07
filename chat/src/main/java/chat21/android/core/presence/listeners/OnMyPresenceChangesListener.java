package chat21.android.core.presence.listeners;

/**
 * Created by stefanodp91 on 07/09/17.
 */
public interface OnMyPresenceChangesListener {
    void onMyPresenceChange(boolean imConnected);

    void onMyLastOnlineChange(long lastOnline);

    void onMyPresenceChangeError(Exception e);
}