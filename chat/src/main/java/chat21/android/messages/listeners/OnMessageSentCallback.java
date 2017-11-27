package chat21.android.messages.listeners;

/**
 * Created by stefanodp91 on 24/11/17.
 */

public interface OnMessageSentCallback {
    void onMessageSentSuccess(String messageId);

    void onMessageSentError(Exception e);
}
