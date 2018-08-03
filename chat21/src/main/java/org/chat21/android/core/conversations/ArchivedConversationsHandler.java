package org.chat21.android.core.conversations;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.conversations.listeners.ConversationsListener;
import org.chat21.android.core.conversations.listeners.UnreadConversationsListener;
import org.chat21.android.core.conversations.models.Conversation;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by andrealeo on 18/12/17.
 * Modified by stefanodp91 on 19/03/18.
 */

public class ArchivedConversationsHandler {
    private static final String TAG = ArchivedConversationsHandler.class.getName();

    private List<Conversation> conversations;
    private DatabaseReference conversationsNode;
    private String appId;
    private String currentUserId;
    private List<ConversationsListener> conversationsListeners;
    private ChildEventListener conversationsChildEventListener;
    private Comparator<Conversation> conversationComparator;

    private String currentOpenConversationId;

    private List<Conversation> unreadConversations;
    private List<UnreadConversationsListener> unreadConversationsListeners;
    private ValueEventListener unreadConversationsValueEventListener;

    public ArchivedConversationsHandler(String firebaseUrl, String appId, String currentUserId) {
        conversationsListeners = new ArrayList<ConversationsListener>();
        conversations = new ArrayList<>(); // conversations in memory

        unreadConversationsListeners = new ArrayList<>();
        unreadConversations = new ArrayList<>(); // unread conversation in memory

        this.appId = appId;
        this.currentUserId = currentUserId;

        if (StringUtils.isValid(firebaseUrl)) {
            this.conversationsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(firebaseUrl)
                    .child("/apps/" + appId + "/users/" + currentUserId + "/archived_conversations/");
        } else {
            this.conversationsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + appId + "/users/" + currentUserId + "/archived_conversations/");
        }
        this.conversationsNode.keepSynced(true);

        conversationComparator = new Comparator<Conversation>() {
            @Override
            public int compare(Conversation o1, Conversation o2) {
                try {
                    return o2.getTimestampLong().compareTo(o1.getTimestampLong());
                } catch (Exception e) {
                    Log.e(TAG, "ConversationHandler.sortConversationsInMemory: " +
                            "cannot compare conversations timestamp", e);
                    return 0;
                }
            }
        };

//        Log.d(TAG, "ConversationsHandler.conversationsNode == " + conversationsNode.toString());
    }

    public ChildEventListener connect(ConversationsListener conversationsListener) {
        this.upsertConversationsListener(conversationsListener);
        return connect();
    }

    public ChildEventListener connect() {

        if (this.unreadConversationsValueEventListener == null) {
            // count the number of conversation unread
            unreadConversationsValueEventListener = conversationsNode.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        try {
                            // decode the conversation
                            Conversation conversation = decodeConversationFromSnapshot(postSnapshot);

                            // if the conversation is new add it to the unread conversations list
                            // otherwise remove it from the unread conversations list
                            if (conversation.getIs_new()) {
                                if (!unreadConversations.contains(conversation)) {
                                    // add the conversation to the unread conversations list
                                    unreadConversations.add(conversation);
                                } else {
                                    // update the conversation within the conversations list
                                    int index = unreadConversations.indexOf(conversation);
                                    unreadConversations.set(index, conversation);
                                }
                            } else {
                                // if the unread conversations list contains
                                // the conversation remove it
                                if (unreadConversations.contains(conversation)) {
                                    unreadConversations.remove(conversation);
                                }
                            }

                            // notify to all subscribers that the unread conversations list changed
                            for (UnreadConversationsListener listener : unreadConversationsListeners) {
                                listener.onUnreadConversationCounted(unreadConversations.size(), null);
                            }

                        } catch (Exception e) {
                            // notify to all subscribers that an error occurred
                            for (UnreadConversationsListener listener : unreadConversationsListeners) {
                                listener.onUnreadConversationCounted(-1, new ChatRuntimeException(e));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // notify to all subscribers that an error occurred
                    for (UnreadConversationsListener listener : unreadConversationsListeners) {
                        listener.onUnreadConversationCounted(-1,
                                new ChatRuntimeException(databaseError.toException()));
                    }
                }
            });
        } else {
            Log.i(TAG, "ArchivedConversationsHandler.connect.valueEventListener: valueEventListener already connected.");
        }

        // subscribe on conversations add/change/remove
        if (this.conversationsChildEventListener == null) {

            this.conversationsChildEventListener = conversationsNode.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "ArchivedConversationsHandler.connect.onChildAdded");

                    try {
                        Conversation conversation = decodeConversationFromSnapshot(dataSnapshot);

                        // it sets the conversation as read if the person whom are talking to is the current user
                        if (currentUserId.equals(conversation.getSender())) {
                            setConversationRead(conversation.getConversationId());
                        }

                        addConversation(conversation);
                    } catch (Exception e) {
                        notifyConversationAdded(null, new ChatRuntimeException(e));
                    }
                }

                //for return receipt
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "observeMessages.onChildChanged");

                    try {
                        Conversation conversation = decodeConversationFromSnapshot(dataSnapshot);
                        updateConversation(conversation);
                    } catch (Exception e) {
                        notifyConversationChanged(null, new ChatRuntimeException(e));
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "observeMessages.onChildRemoved");

//                Log.d(TAG, "observeMessages.onChildRemoved: dataSnapshot == " + dataSnapshot.toString());

                    try {
                        Conversation conversation = decodeConversationFromSnapshot(dataSnapshot);
                        deleteConversationFromMemory(conversation.getConversationId());
                    } catch (Exception e) {
                        notifyConversationRemoved(new ChatRuntimeException(e));
                    }
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
        } else {
            Log.i(TAG, "already connected : ");
        }

        return conversationsChildEventListener;
    }

    public String getCurrentOpenConversationId() {
        return currentOpenConversationId;
    }

    public void setCurrentOpenConversationId(String currentOpenConversationId) {
        this.currentOpenConversationId = currentOpenConversationId;
    }

    public List<Conversation> getConversations() {
//        sortConversationsInMemory(); // ensure to return a sorted list
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

//    // it checks if the conversation already exists.
//    // if the conversation exists delete it
//    private void deleteConversationFromMemory(Conversation conversationToDelete) {
//        // look for the conversation
//        int index = -1;
//        for (Conversation tempConversation : conversations) {
//            if (tempConversation.equals(conversationToDelete)) {
//                index = conversations.indexOf(tempConversation);
//                break;
//            }
//        }
//
//        if (index != -1) {
//            // conversation already exists
//            conversations.remove(index); // delete existing conversation
//        }
//    }

    // it checks if the conversation already exists through its conversationId
    // if the conversation exists delete it
    public void deleteConversationFromMemory(String conversationId) {
        int index = -1;
        for(Conversation tempConversation : conversations) {
            if(tempConversation.getConversationId().equals(conversationId)) {
                index = conversations.indexOf(tempConversation);
                break;
            }
        }

        if(index != -1) {
            conversations.remove(index);
            sortConversationsInMemory();
            notifyConversationRemoved(null);
        }

    }

    private void sortConversationsInMemory() {
        Log.d(TAG, "ConversationHandler.sortConversationsInMemory");

        // check if the list has al least 1 item.
        // 1 item is already sorted
        if (conversations.size() > 1) {
            Collections.sort(conversations, conversationComparator);
        }
    }

    public void addConversation(Conversation conversation) {

        try {
            saveOrUpdateConversationInMemory(conversation);
            sortConversationsInMemory();
            notifyConversationAdded(conversation, null);
        } catch (Exception e) {
            notifyConversationAdded(null, new ChatRuntimeException(e));
        }
    }

    public void updateConversation(Conversation conversation) {

        try {
            saveOrUpdateConversationInMemory(conversation);
            sortConversationsInMemory();
            notifyConversationChanged(conversation, null);
        } catch (Exception e) {
            notifyConversationChanged(null, new ChatRuntimeException(e));
        }
    }

    private void notifyConversationAdded(Conversation conversation, ChatRuntimeException exception) {
        if (conversationsListeners != null) {
            for (ConversationsListener conversationsListener : conversationsListeners) {
                conversationsListener.onConversationAdded(conversation, exception);
            }
        }
    }

    private void notifyConversationChanged(Conversation conversation, ChatRuntimeException exception) {
        if (conversationsListeners != null) {
            for (ConversationsListener conversationsListener : conversationsListeners) {
                conversationsListener.onConversationChanged(conversation, exception);
            }
        }
    }

    private void notifyConversationRemoved(ChatRuntimeException exception) {
        if (conversationsListeners != null) {
            for (ConversationsListener conversationsListener : conversationsListeners) {
                conversationsListener.onConversationRemoved(exception);
            }
        }
    }

    public static Conversation decodeConversationFromSnapshot(DataSnapshot dataSnapshot) {
        Log.d(TAG, "ConversationHandler.decodeConversationFromSnapshop");

        Conversation conversation = new Conversation();

        // conversationId
        conversation.setConversationId(dataSnapshot.getKey());
        Log.d(TAG, "ArchivedConversationsHandler.decodeConversationSnapshop: conversationId = " +
                conversation.getConversationId());

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

        // is_new
        try {
            boolean is_new = (boolean) map.get("is_new");
            conversation.setIs_new(is_new);
        } catch (Exception e) {
            Log.e(TAG, "ArchivedConversationsHandler.decodeConversationSnapshop:" +
                    " cannot retrieve is_new");
        }

        // last_message_text
        try {
            String last_message_text = (String) map.get("last_message_text");
            conversation.setLast_message_text(last_message_text);
        } catch (Exception e) {
            Log.e(TAG, "ArchivedConversationsHandler.decodeConversationSnapshop: " +
                    "cannot retrieve last_message_text");
        }

        // recipient
        try {
            String recipient = (String) map.get("recipient");
            conversation.setRecipient(recipient);
        } catch (Exception e) {
            Log.e(TAG, "ArchivedConversationsHandler.decodeConversationSnapshop:" +
                    " cannot retrieve recipient");
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

        try {
            String channelType = (String) map.get("channel_type");
            conversation.setChannelType(channelType);
        } catch (Exception e) {
            Log.e(TAG, "cannot retrieve channel_type");
        }


        // convers with
        if (conversation.getRecipient()
                .equals(ChatManager.getInstance().getLoggedUser().getId())) {
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

        Conversation conversation = getById(recipientId);
        // check if the conversation is new
        // if it is new set the conversation as read (false), do nothing otherwise
        if (conversation != null && conversation.getIs_new()) {
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
                    String errorMessage = "cannot mark the conversation as read: " +
                            databaseError.getMessage();
                    Log.e(TAG, errorMessage);
                    FirebaseCrash.report(new Exception(errorMessage));
                }
            });
        }
    }

    public void toggleConversationRead(final String recipientId) {

        // retrieve the conversation by the conversationId
        Conversation conversation = getById(recipientId);

        // toggle the conversation status
        boolean status = !conversation.getIs_new();

        if (conversation != null) {
            final boolean finalStatus = status;
            conversationsNode.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    // check if the conversation exists to prevent conversation with only "is_new" value
                    if (snapshot.hasChild(recipientId)) {
                        // update the state
                        conversationsNode.child(recipientId)
                                .child("is_new")
                                .setValue(finalStatus);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    String errorMessage = "cannot toggle the conversation read: " +
                            databaseError.getMessage();
                    Log.e(TAG, errorMessage);
                    FirebaseCrash.report(new Exception(errorMessage));
                }
            });
        }
    }

    public List<UnreadConversationsListener> getUnreadConversationsListeners() {
        return unreadConversationsListeners;
    }

    public List<ConversationsListener> getConversationsListener() {
        return conversationsListeners;
    }

//    public void setConversationsListener(List<ConversationsListener> conversationsListeners) {
//        this.conversationsListeners = conversationsListeners;
//    }

    public void addConversationsListener(ConversationsListener conversationsListener) {
        Log.v(TAG, "  addGroupsListener called");

        this.conversationsListeners.add(conversationsListener);

        Log.i(TAG, "  conversationsListener with hashCode: " +
                conversationsListener.hashCode() + " added");
    }

    public void addUnreadConversationsListener(UnreadConversationsListener unreadConversationsListener) {
        Log.v(TAG, "ArchivedConversationsHandler.addGroupsListener: called");

        this.unreadConversationsListeners.add(unreadConversationsListener);

        Log.i(TAG, "ArchivedConversationsHandler.addUnreadConversationsListener: unreadConversationsListener with hashCode: " +
                unreadConversationsListener.hashCode() + " added");
    }

    public void removeConversationsListener(ConversationsListener conversationsListener) {
        Log.v(TAG, "  removeGroupsListener called");

        if (conversationsListeners != null)
            this.conversationsListeners.remove(conversationsListener);

        Log.i(TAG, "  conversationsListener with hashCode: " +
                conversationsListener.hashCode() + " removed");
    }

    public void removeUnreadConversationsListener(UnreadConversationsListener unreadConversationsListener) {
        Log.v(TAG, "ArchivedConversationsHandler.removeUnreadConversationsListener: called");

        if (unreadConversationsListener != null)
            this.unreadConversationsListeners.remove(unreadConversationsListener);

        Log.i(TAG, "ArchivedConversationsHandler.removeUnreadConversationsListener: unreadConversationsListener with hashCode: " +
                unreadConversationsListener.hashCode() + " removed");
    }

    public void upsertConversationsListener(ConversationsListener conversationsListener) {
        Log.v(TAG, "  upsertGroupsListener called");

        if (conversationsListeners.contains(conversationsListener)) {
            this.removeConversationsListener(conversationsListener);
            this.addConversationsListener(conversationsListener);
            Log.i(TAG, "  conversationsListener with hashCode: " +
                    conversationsListener.hashCode() + " updated");

        } else {
            this.addConversationsListener(conversationsListener);
            Log.i(TAG, "  conversationsListener with hashCode: " +
                    conversationsListener.hashCode() + " added");
        }
    }

    public void upsetUnreadConversationsListener(UnreadConversationsListener unreadConversationsListener) {
        Log.v(TAG, "ArchivedConversationsHandler.upsetUnreadConversationsListener: called");

        if (unreadConversationsListeners.contains(unreadConversationsListener)) {
            this.removeUnreadConversationsListener(unreadConversationsListener);
            this.addUnreadConversationsListener(unreadConversationsListener);
            Log.i(TAG, "ArchivedConversationsHandler.upsetUnreadConversationsListener: unreadConversationsListener with hashCode: " +
                    unreadConversationsListener.hashCode() + " updated");

        } else {
            this.addUnreadConversationsListener(unreadConversationsListener);
            Log.i(TAG, "ArchivedConversationsHandler.upsetUnreadConversationsListener: unreadConversationsListener with hashCode: " +
                    unreadConversationsListener.hashCode() + " added");
        }
    }

    public void removeAllConversationsListeners() {
        this.conversationsListeners = null;
        Log.i(TAG, "Removed all ConversationsListeners");
    }

    public void removeAllUnreadConversationsListeners() {
        if (unreadConversationsListeners != null) unreadConversationsListeners.clear();
        unreadConversationsListeners = null;
        Log.i(TAG, "ArchivedConversationsHandler.removeAllUnreadConversationsListeners: Removed all ConversationsListeners");
    }

    public ChildEventListener getConversationsChildEventListener() {
        return conversationsChildEventListener;
    }

    public ValueEventListener getUnreadConversationsValueEventListener() {
        return unreadConversationsValueEventListener;
    }

    public void disconnect() {
        this.conversationsNode.removeEventListener(this.conversationsChildEventListener);
        this.removeAllConversationsListeners();

        this.conversationsNode.removeEventListener(unreadConversationsValueEventListener);
        this.removeAllUnreadConversationsListeners();
    }

    public void deleteConversation(final String conversationId, final ConversationsListener conversationsListener) {

        // the node of the conversation with conversationId
        DatabaseReference nodeConversation = conversationsNode.child(conversationId);

        nodeConversation.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError == null) {
                    deleteConversationFromMemory(conversationId);
                    conversationsListener.onConversationRemoved(null);
                } else {
                    conversationsListener.onConversationRemoved(new ChatRuntimeException(databaseError.toException()));
                }
            }
        });
    }

    /**
     * It looks for the conversation with {@code conversationId}
     *
     * @param conversationId the group id to looking for
     * @return the conversation if exists, null otherwise
     */
    public Conversation getById(String conversationId) {
        for (Conversation conversation : conversations) {
            if (conversation.getConversationId().equals(conversationId)) {
                return conversation;
            }
        }
        return null;
    }
}
