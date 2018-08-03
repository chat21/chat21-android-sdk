package org.chat21.android.ui.archived_conversations.listeners;

import org.chat21.android.core.conversations.models.Conversation;

public interface OnSwipeMenuReopenClickListener {
    void onSwipeMenuReopened(Conversation conversation, int position);
}
