package chat21.android.messages.listeners;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import chat21.android.core.messages.models.Message;

/**
 * Created by stefanodp91 on 11/01/17.
 */
public interface OnMessageTreeUpdateListener {
    void onTreeChildAdded(DatabaseReference node, DataSnapshot dataSnapshot, Message message);

    void onTreeChildChanged(DatabaseReference node, DataSnapshot dataSnapshot, Message message);

    void onTreeChildRemoved();

    void onTreeChildMoved();

    void onTreeCancelled();
}
