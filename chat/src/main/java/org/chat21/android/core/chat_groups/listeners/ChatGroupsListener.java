package org.chat21.android.core.chat_groups.listeners;

import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.chat_groups.models.ChatGroup;

/**
 * Created by stefanodp91 on 24/01/18.
 */

public interface ChatGroupsListener {

    void onGroupAdded(ChatGroup chatGroup, ChatRuntimeException e);

    void onGroupChanged(ChatGroup chatGroup, ChatRuntimeException e);

    void onGroupRemoved(ChatRuntimeException e);
}
