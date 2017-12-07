package chat21.android.core.messages.listeners;

import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.models.Message;

/**
 * Created by andrealeo on 06/12/17.
 */

public interface ConversationMessagesListener {

        public void onConversationMessageReceived(Message message, ChatRuntimeException e);
        public void onConversationMessageChanged(Message message, ChatRuntimeException e);

}

