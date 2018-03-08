package org.chat21.android.core.contacts.listeners;

import org.chat21.android.core.exception.ChatRuntimeException;

/**
 * Created by stefanodp91 on 28/02/18.
 */

public interface OnContactCreatedCallback {
    void onContactCreatedSuccess(ChatRuntimeException exception);
}
