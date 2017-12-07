package  chat21.android.conversations.listeners;

import chat21.android.core.conversations.models.Conversation;

/**
 * Created by stefanodp91 on 19/10/17.
 */

public interface OnConversationRetrievedCallback {

    void onConversationRetrievedSuccess(Conversation conversation);

    void onNewConversationCreated(String conversationId);

    void onConversationRetrievedError(Exception e);
}
