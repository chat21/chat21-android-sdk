package org.chat21.android.core.conversations.listeners;

import org.chat21.android.core.conversations.models.Conversation;
import org.chat21.android.core.exception.ChatRuntimeException;

/**
 * Created by andrealeo on 06/12/17.
 */

public interface UnreadConversationsListener {

    void onUnreadConversationCounted(int count, ChatRuntimeException e);

}

