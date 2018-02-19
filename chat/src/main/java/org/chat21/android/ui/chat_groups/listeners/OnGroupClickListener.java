package org.chat21.android.ui.chat_groups.listeners;

import org.chat21.android.core.chat_groups.models.ChatGroup;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public interface OnGroupClickListener {
    void onGroupClicked(ChatGroup chatGroup, int position);
}
