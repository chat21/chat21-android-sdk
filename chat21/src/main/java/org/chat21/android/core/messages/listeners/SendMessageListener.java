package org.chat21.android.core.messages.listeners;

import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.messages.models.Message;

/**
 * Created by andrealeo on 24/11/17.
 */

public interface SendMessageListener {

    void onBeforeMessageSent(Message message, ChatRuntimeException chatException);

    void onMessageSentComplete(Message message, ChatRuntimeException chatException);
}