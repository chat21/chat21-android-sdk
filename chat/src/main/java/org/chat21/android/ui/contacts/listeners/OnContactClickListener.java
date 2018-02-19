package org.chat21.android.ui.contacts.listeners;

import java.io.Serializable;

import org.chat21.android.core.users.models.IChatUser;

/**
 * Created by stefanodp91 on 29/03/17.
 */
public interface OnContactClickListener extends Serializable {
    void onContactClicked(IChatUser contact, int position);
}
