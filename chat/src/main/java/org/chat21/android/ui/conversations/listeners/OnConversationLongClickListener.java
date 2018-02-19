package org.chat21.android.ui.conversations.listeners;

import org.chat21.android.core.conversations.models.Conversation;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public interface OnConversationLongClickListener {
    void onConversationLongClicked(Conversation conversation, int position);
}
