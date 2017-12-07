package  chat21.android.dao.message;

import java.util.Map;

import chat21.android.core.conversations.models.Conversation;
import  chat21.android.messages.listeners.OnMessageTreeUpdateListener;

/**
 * Created by stefanodp91 on 08/09/17.
 */

public interface MessageDAO {

    void observeMessageTree(String conversationId,
                            OnMessageTreeUpdateListener onTreeUpdateListener);

    void detachObserveMessageTree(OnDetachObserveMessageTree callbac);

    void sendMessage(String text, String type,
                     Conversation conversation, Map<String, Object> extras);

    void sendGroupMessage(String text, String type,
                          Conversation conversation);
}