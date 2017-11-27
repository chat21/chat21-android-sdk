package chat21.android.presence;

/**
 * Created by stefanodp91 on 07/09/17.
 */
public interface OnPresenceChangesListener {
    void onPresenceChange(boolean imConnected);

    void onLastOnlineChange(long lastOnline);

    void onPresenceChangeError(Exception e);
}