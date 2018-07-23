package org.chat21.android.ui.conversations.listeners;

import org.chat21.android.core.conversations.models.Conversation;

public interface OnSwipeMenuCloseClickListener {
    void onSwipeMenuClosed(Conversation conversation, int position);
}
