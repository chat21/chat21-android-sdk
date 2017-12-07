package chat21.android.core.messages.listeners;

import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.models.Message;

/**
 * Created by andrealeo on 24/11/17.
 */

public interface SendMessageListener {

    void onResult(Message message, ChatRuntimeException chatException);


}
