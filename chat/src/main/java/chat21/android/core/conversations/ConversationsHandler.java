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
import java.util.Collections;
import java.util.Comparator;
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

    private List<Conversation> conversations;

    DatabaseReference conversationsNode;
    String appId;
    String currentUserId;

    List<ConversationsListener> conversationsListeners;

    ChildEventListener conversationsChildEventListener;

    public ConversationsHandler(String firebaseUrl, String appId, String currentUserId) {
        conversationsListeners = new ArrayList<ConversationsListener>();
        conversations = new ArrayList<>(); // conversations in memory

        this.appId = appId;
        this.currentUserId = currentUserId;
        this.conversationsNode = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseUrl).child("/apps/" + appId + "/users/" + currentUserId + "/conversations/");
        this.conversationsNode.keepSynced(true);

//        Log.d(TAG, "ConversationsHandler.conversationsNode == " + conversationsNode.toString());
    }

    public ChildEventListener connect() {

        if (this.conversationsChildEventListener==null) {

            this.conversationsChildEventListener = conversationsNode.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "ConversationsHandler.connect.onChildAdded");

                    try {
                        Conversation conversation = decodeConversationFromSnapshot(dataSnapshot);

                        // it sets the conversation as read if the person whom are talking to is the current user
                        if (currentUserId.equals(conversation.getSender())) {
                            setConversationRead(conversation.getConversationId());
                        }

                        saveOrUpdateConversationInMemory(conversation);
                        sortConversationsInMemory();

                        for (ConversationsListener conversationsListener : conversationsListeners) {
                            conversationsListener.onConversationAdded(conversation, null);
                        }

                    } catch (Exception e) {
                        for (ConversationsListener conversationsListener : conversationsListeners) {
                            conversationsListener.onConversationAdded(null, new ChatRuntimeException(e));
                        }
                    }
                }

                //for return receipt
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "observeMessages.onChildChanged");

                    try {
                        Conversation conversation = decodeConversationFromSnapshot(dataSnapshot);

                        saveOrUpdateConversationInMemory(conversation);
                        sortConversationsInMemory();

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
                    Log.d(TAG, "observeMessages.onChildRemoved");

//                Log.d(TAG, "observeMessages.onChildRemoved: dataSnapshot == " + dataSnapshot.toString());

//                try {
//                    Conversation conversation = decodeConversationFromSnapshot(dataSnapshot);
//
//                    deleteConversationFromMemory(conversation);
//                    sortConversationsInMemory();
//
//                    for (ConversationsListener conversationsListener : conversationsListeners) {
//                        conversationsListener.onConversationRemoved(null);
//                    }
//
//                } catch (Exception e) {
//                    for (ConversationsListener conversationsListener : conversationsListeners) {
//                        conversationsListener.onConversationRemoved(new ChatRuntimeException(e));
//                    }
//                }
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
        }else {
            Log.i(TAG, "already connected : " );
        }

        return conversationsChildEventListener;
    }

    public List<Conversation> getConversations() {
        sortConversationsInMemory(); // ensure to return a sorted list
        return conversations;
    }

    // it checks if the conversation already exists.
    // if the conversation exists update it, add it otherwise
    private void saveOrUpdateConversationInMemory(Conversation newConversation) {

        // look for the conversation
        int index = -1;
        for (Conversation tempConversation : conversations) {
            if (tempConversation.equals(newConversation)) {
                index = conversations.indexOf(tempConversation);
                break;
            }
        }

        if (index != -1) {
            // conversation already exists
            conversations.set(index, newConversation); // update the existing conversation
        } else {
            // conversation not exists
            conversations.add(newConversation); // insert a new conversation
        }
    }

    // it checks if the conversation already exists.
    // if the conversation exists delete it
    private void deleteConversationFromMemory(Conversation conversationToDelete) {
        // look for the conversation
        int index = -1;
        for (Conversation tempConversation : conversations) {
            if (tempConversation.equals(conversationToDelete)) {
                index = conversations.indexOf(tempConversation);
                break;
            }
        }

        if (index != -1) {
            // conversation already exists
            conversations.remove(index); // delete existing conversation
        }
    }

    private void sortConversationsInMemory() {

        // TODO: 20/12/17 study if is better to create the comparator in the ConversationHandler constructor
        Log.d(TAG, "ConversationHandler.sortConversationsInMemory");

        if (conversations.size() > 1) {
            Collections.sort(conversations, new Comparator<Conversation>() {
                @Override
                public int compare(Conversation o1, Conversation o2) {
                    try {
                        return o2.getTimestampLong().compareTo(o1.getTimestampLong());
                    } catch (Exception e) {
                        Log.e(TAG, "ConversationHandler.sortConversationsInMemory: cannot compare conversations timestamp", e);
                        return 0;
                    }
                }
            });
        } else {
            // 1 item is already sorted
        }
    }


    public static Conversation decodeConversationFromSnapshot(DataSnapshot dataSnapshot) {
        Log.d(TAG, "ConversationHandler.decodeConversationFromSnapshop");

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


    public void setConversationRead(final String recipientId) {
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


    public List<ConversationsListener> getConversationsListener() {
        return conversationsListeners;
    }

//    public void setConversationsListener(List<ConversationsListener> conversationsListeners) {
//        this.conversationsListeners = conversationsListeners;
//    }

    public void addConversationsListener(ConversationsListener conversationsListener) {
        Log.v(TAG, "  addConversationsListener called");

        this.conversationsListeners.add(conversationsListener);

        Log.i(TAG, "  conversationsListener with hashCode: "+ conversationsListener.hashCode() + " added");

    }

    public void removeConversationsListener(ConversationsListener conversationsListener) {
        Log.v(TAG, "  removeConversationsListener called");

        this.conversationsListeners.remove(conversationsListener);

        Log.i(TAG, "  conversationsListener with hashCode: "+ conversationsListener.hashCode() + " removed");

    }

    public void upsertConversationsListener(ConversationsListener conversationsListener) {
        Log.v(TAG, "  upsertConversationsListener called");

        if (conversations.contains(conversationsListener)) {
            this.removeConversationsListener(conversationsListener);
            this.addConversationsListener(conversationsListener);
            Log.i(TAG, "  conversationsListener with hashCode: "+ conversationsListener.hashCode() + " updated");

        } else {
            this.addConversationsListener(conversationsListener);
            Log.i(TAG, "  conversationsListener with hashCode: "+ conversationsListener.hashCode() + " added");

        }
    }

    public void removeAllConversationsListeners() {
        this.conversationsListeners = null;
        Log.i(TAG, "Removed all ConversationsListeners");

    }




    public ChildEventListener getConversationsChildEventListener() {
        return conversationsChildEventListener;
    }

    public void disconnect() {
        this.conversationsNode.removeEventListener(this.conversationsChildEventListener);
        this.removeAllConversationsListeners();
    }


//    public void deleteConversation(String recipientId, final ConversationsListener conversationsListener) {
//        DatabaseReference.CompletionListener onConversationRemoved
//                = new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//                if (databaseError == null) {
//                    // conversation deleted with success
//                    conversationsListener.onConversationRemoved(null);
//                } else {
//                    // there are error
//                    // conversation not deleted
//                    conversationsListener.onConversationRemoved(new ChatRuntimeException(databaseError.toException()));
//                }
//            }
//        };
//
//        // remove the conversation with recipientId
//        this.conversationsNode.child(recipientId).removeValue(onConversationRemoved);
//    }


//    public DatabaseReference getConversationsNode() {
//        return conversationsNode;
//    }
}
