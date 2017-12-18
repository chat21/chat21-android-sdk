package chat21.android.core.conversations.listeners;

import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.models.Message;

/**
 * Created by andrealeo on 06/12/17.
 */

public interface ConversationsListener {

        public void onConversationAdded(Conversation conversation, ChatRuntimeException e);
        public void onConversationChanged(Conversation conversation, ChatRuntimeException e);
//        public void onConversationRemoved(Conversation conversation, ChatRuntimeException e);

}

