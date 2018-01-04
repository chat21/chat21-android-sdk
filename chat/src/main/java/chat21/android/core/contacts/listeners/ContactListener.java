package chat21.android.core.contacts.listeners;

import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.models.Message;
import chat21.android.core.users.models.IChatUser;

/**
 * Created by andrealeo on 06/12/17.
 */

public interface ContactListener {

        public void onContactReceived(IChatUser contact, ChatRuntimeException e);
        public void onContactChanged(IChatUser contact, ChatRuntimeException e);
        public void onContactRemoved(IChatUser contact, ChatRuntimeException e);

}

