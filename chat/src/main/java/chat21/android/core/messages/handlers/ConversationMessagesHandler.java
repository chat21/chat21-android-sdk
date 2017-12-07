package chat21.android.core.messages.handlers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.listeners.ConversationMessagesListener;
import chat21.android.core.messages.listeners.SendMessageListener;
import chat21.android.core.messages.models.Message;
import chat21.android.messages.listeners.OnMessageTreeUpdateListener;

/**
 * Created by andrealeo on 05/12/17.
 */

public class ConversationMessagesHandler {
    private static final String TAG = ConversationMessagesHandler.class.getName();

    String recipientId;
    DatabaseReference conversationMessagesNode;
    List<ConversationMessagesListener> conversationMessagesListeners;

    public ConversationMessagesHandler(String recipientId, String appId, String currentUserId, List<ConversationMessagesListener> conversationMessagesListeners) {

        this.recipientId = recipientId;

        this.conversationMessagesNode = FirebaseDatabase.getInstance().getReference().child("/apps/"+appId+"/users/"+currentUserId+"/messages/"+recipientId);
        this.conversationMessagesNode.keepSynced(true);
        this.conversationMessagesListeners = conversationMessagesListeners;

    }

    ChildEventListener connect(final List<ConversationMessagesListener> conversationMessagesListeners) {

        ChildEventListener childEventListener = conversationMessagesNode.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "observeMessages.onChildAdded");

                try {
                    Message message = decodeMessageSnapShop(dataSnapshot);

                    for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                        conversationMessagesListener.onConversationMessageReceived(message, null);
                    }

                } catch (Exception e) {
                    for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                        conversationMessagesListener.onConversationMessageReceived(null, new ChatRuntimeException(e));
                    }
                }
            }

            //for return recepit
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "observeMessages.onChildChanged");

                try {
                    Message message = decodeMessageSnapShop(dataSnapshot);

                    for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                        conversationMessagesListener.onConversationMessageChanged(message, null);
                    }

                } catch (Exception e) {
                    for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                        conversationMessagesListener.onConversationMessageChanged(null, new ChatRuntimeException(e));
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "observeMessages.onChildRemoved");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "observeMessages.onChildMoved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "observeMessages.onCancelled");

            }
        });

        return childEventListener;
    }


    /**
     * @param dataSnapshot the datasnapshot to decode
     * @return the decoded message
     */
    public static Message decodeMessageSnapShop(DataSnapshot dataSnapshot) {
        Log.d(TAG, "decodeMessageSnapShop");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

        String sender_fullname = (String) map.get("sender_fullname");
        String recipient = (String) map.get("recipient");
        String conversationId = (String) map.get("conversationId");
        long status = (long) map.get("status");
        String text = (String) map.get("text");
        long timestamp = (long) map.get("timestamp");
        String type = (String) map.get("type");
        String sender = (String) map.get("sender");
        String recipientGroupId = (String) map.get("recipientGroupId");

        Message message = new Message();
        message.setSender_fullname(sender_fullname);
        message.setRecipient(recipient);
        message.setConversationId(conversationId);
        message.setStatus((int) status);
        message.setText(text);
        message.setTimestamp(timestamp);
        message.setType(type);
        message.setSender(sender);
        message.setRecipientGroupId(recipientGroupId);

//        Log.d(TAG, "message >: " + dataSnapshot.toString());

        return message;
    }



}
