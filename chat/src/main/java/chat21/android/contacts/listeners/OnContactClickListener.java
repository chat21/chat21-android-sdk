package chat21.android.contacts.listeners;

import java.io.Serializable;

import chat21.android.user.models.IChatUser;

/**
 * Created by stefanodp91 on 29/03/17.
 */
public interface OnContactClickListener extends Serializable {
    void onContactClick(IChatUser contact);
}
