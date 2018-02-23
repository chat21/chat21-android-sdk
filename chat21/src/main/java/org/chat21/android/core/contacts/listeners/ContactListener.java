package org.chat21.android.core.contacts.listeners;

import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.IChatUser;

/**
 * Created by andrealeo on 06/12/17.
 */

public interface ContactListener {

        public void onContactReceived(IChatUser contact, ChatRuntimeException e);
        public void onContactChanged(IChatUser contact, ChatRuntimeException e);
        public void onContactRemoved(IChatUser contact, ChatRuntimeException e);

}

