package chat21.android.dao.message;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import chat21.android.core.ChatManager;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.messages.models.Message;

/**
 * Created by stefanodp91 on 08/09/17.
 */
class UploadGroupMessageOnFirebaseTask {
    private static final String TAG = UploadGroupMessageOnFirebaseTask.class.getName();

    void uploadMessage(String appId, String text, Message message,
                       Conversation conversation, String conversationId) {
        Log.d(TAG, "uploadMessage");

        mNodeDAO.getNodeConversation(conversationId)
                .push()
                .setValue(message);

        updateNodesFromGroupMessage(appId, text, conversation, conversationId);
    }

    private void updateNodesFromGroupMessage(final String appId, String text,
                                             final Conversation conversation,
                                             final String conversationId) {
        Log.d(TAG, "updateNodesFromGroupMessage");

        conversation.setSender(ChatManager.getInstance().getLoggedUser().getId());
        conversation.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
        conversation.setLast_message_text(text);
        // bugfix Issue #66
        conversation.setStatus(Conversation.CONVERSATION_STATUS_LAST_MESSAGE);
        conversation.setIs_new(true);

        DatabaseReference nodeMembers = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/groups/" + conversation.getGroup_id() + "/members");

        nodeMembers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String member = snapshot.getKey();
                    Log.d(TAG, "member ==" + member);

                    FirebaseDatabase.getInstance().getReference()
                            .child("apps/" + appId + "/users/" + member + "/conversations/" + conversationId).setValue(conversation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: 29/06/17
            }
        });
    }
}