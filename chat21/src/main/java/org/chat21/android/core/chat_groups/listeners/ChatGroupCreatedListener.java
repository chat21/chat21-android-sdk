package org.chat21.android.core.chat_groups.listeners;

import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.exception.ChatRuntimeException;

/**
 * Created by stefanodp91 on 29/01/18.
 */

public interface ChatGroupCreatedListener {
    void onChatGroupCreated(ChatGroup chatGroup, ChatRuntimeException chatException);
}
