package chat21.android.core.presence.listeners;

/**
 * Created by stefanodp91 on 15/12/17.
 */
public interface OnPresenceListener {
    void onChanged(boolean imConnected);

    void onLastOnlineChanged(long lastOnline);

    void onError(Exception e);
}