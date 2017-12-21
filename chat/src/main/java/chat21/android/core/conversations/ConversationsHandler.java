package chat21.android.core.conversations;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.conversations.listeners.ConversationsListener;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.exception.ChatRuntimeException;

/**
 * Created by andrealeo on 18/12/17.
 */

public class ConversationsHandler {

    private static final String TAG = ConversationsHandler.class.getName();

    DatabaseReference conversationsNode;
    String appId;
    String currentUserId;

    public ConversationsHandler(String firebaseUrl, String appId, String currentUserId) {
        this.appId = appId;
        this.currentUserId = currentUserId;
        this.conversationsNode = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseUrl).child("/apps/" + appId + "/users/" + currentUserId + "/conversations/");
        this.conversationsNode.keepSynced(true);


//        Log.d(TAG, "ConversationsHandler.conversationsNode == " + conversationsNode.toString());
    }

    public ChildEventListener connect(final ConversationsListener conversationsListener) {
        final List<ConversationsListener> conversationsListeners = new ArrayList<ConversationsListener>();
        conversationsListeners.add(conversationsListener);

        ChildEventListener childEventListener = conversationsNode.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "ConversationsHandler.connect.onChildAdded");

                try {
                    Conversation conversation = decodeConversationFromSnapshot(dataSnapshot);

                    // it sets the conversation as read if the person whom are talking to is the current user
                    if (currentUserId.equals(conversation.getSender())) {
                        setConversationRead(appId, currentUserId, conversation.getConversationId());
                    }


                    for (ConversationsListener conversationsListener : conversationsListeners) {
                        conversationsListener.onConversationAdded(conversation, null);
                    }

                } catch (Exception e) {
                    for (ConversationsListener conversationsListener : conversationsListeners) {
                        conversationsListener.onConversationAdded(null, new ChatRuntimeException(e));
                    }
                }
            }

            //for return recepit
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "observeMessages.onChildChanged");

                try {
                    Conversation conversation = decodeConversationFromSnapshot(dataSnapshot);

                    for (ConversationsListener conversationsListener : conversationsListeners) {
                        conversationsListener.onConversationChanged(conversation, null);
                    }

                } catch (Exception e) {
                    for (ConversationsListener conversationsListener : conversationsListeners) {
                        conversationsListener.onConversationChanged(null, new ChatRuntimeException(e));
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "observeMessages.onChildRemoved");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
//                Log.d(TAG, "observeMessages.onChildMoved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "observeMessages.onCancelled");

            }
        });

        return childEventListener;
    }


    public static Conversation decodeConversationFromSnapshot(DataSnapshot dataSnapshot) {
        Log.d(TAG, "decodeConversationFromSnapshop");

        Conversation conversation = new Conversation();

        // conversationId
        conversation.setConversationId(dataSnapshot.getKey());
        Log.d(TAG, "ConversationUtils.decodeConversationSnapshop: conversationId = " + conversation.getConversationId());

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

        // is_new
        try {
            boolean is_new = (boolean) map.get("is_new");
            conversation.setIs_new(is_new);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve is_new");
        }

        // last_message_text
        try {
            String last_message_text = (String) map.get("last_message_text");
            conversation.setLast_message_text(last_message_text);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve last_message_text");
        }

        // recipient
        try {
            String recipient = (String) map.get("recipient");
            conversation.setRecipient(recipient);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve recipient");
        }

        // rrecipient_fullname
        try {
            String recipientFullName = (String) map.get("recipient_fullname");
            conversation.setRecipientFullName(recipientFullName);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve recipient_fullname");
        }

        // sender
        try {
            String sender = (String) map.get("sender");
            conversation.setSender(sender);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve sender");
        }

        // sender_fullname
        try {
            String sender_fullname = (String) map.get("sender_fullname");
            conversation.setSender_fullname(sender_fullname);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve sender_fullname");
        }

        // status
        try {
            long status = (long) map.get("status");
            conversation.setStatus((int) status);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve status");
        }

        // timestamp
        try {
            long timestamp = (long) map.get("timestamp");
            conversation.setTimestamp(timestamp);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve timestamp");
        }


        // convers with
        if (conversation.getRecipient().equals(ChatManager.getInstance().getLoggedUser().getId())) {
            conversation.setConvers_with(conversation.getSender());
            conversation.setConvers_with_fullname(conversation.getSender_fullname());
        } else {
            conversation.setConvers_with(conversation.getRecipient());
            conversation.setConvers_with_fullname(conversation.getRecipientFullName());
        }


        return conversation;
    }


    public void setConversationRead(String appId, String userId, final String recipientId) {
        Log.d(TAG, "setConversationRead");


        conversationsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // check if the conversation exists to prevent conversation with only "is_new" value
                if (snapshot.hasChild(recipientId)) {
                    // update the state
                    conversationsNode.child(recipientId)
                            .child("is_new")
                            .setValue(false); // the conversation has been read
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = "cannot mark the conversation as read.: " + databaseError.getMessage();
                Log.e(TAG, errorMessage);
                FirebaseCrash.report(new Exception(errorMessage));
            }
        });
    }


    public DatabaseReference getConversationsNode() {
        return conversationsNode;
    }
}
