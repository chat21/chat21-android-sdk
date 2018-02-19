package org.chat21.android.core.conversations.listeners;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.chat21.android.core.conversations.models.Conversation;

/**
 * Created by stefanodp91 on 11/01/17.
 */

public interface OnConversationTreeChangeListener {
    void onTreeDataChanged(DatabaseReference node, DataSnapshot dataSnapshot, int childrenCount);

    void onTreeChildAdded(DatabaseReference node, DataSnapshot dataSnapshot, Conversation conversation);

    void onTreeChildChanged(DatabaseReference node, DataSnapshot dataSnapshot, Conversation conversation);

    void onTreeChildRemoved();

    void onTreeChildMoved();

    void onTreeCancelled();
}
