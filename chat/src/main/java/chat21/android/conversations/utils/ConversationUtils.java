
package chat21.android.conversations.utils;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.regex.Pattern;

import chat21.android.core.ChatManager;
import chat21.android.core.conversations.listeners.OnConversationRetrievedCallback;
import chat21.android.core.conversations.listeners.OnConversationTreeChangeListener;
import chat21.android.core.conversations.listeners.OnUnreadConversationCountListener;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.utils.StringUtils;

/**
 * Created by stefanodp91 on 11/01/17.
 */

public class ConversationUtils {
    private static final String TAG = ConversationUtils.class.getName();
    private static final String TAG_NOTIFICATION = "TAG_NOTIFICATION";

    public static void observeMessageTree(String appId, String userId, DatabaseReference node,
                                          OnConversationTreeChangeListener onConversationTreeChangeListener) {
        Log.d(TAG, "observeMessageTree");

        Log.d(TAG, "ConversationUtils.observeMessageTree: node == " + node.toString());

        addOnValueEventListener(node, onConversationTreeChangeListener);
        addOnChildEventListener(appId, userId, node, onConversationTreeChangeListener);
    }

    // observe the conversation list for this node.
    private static void addOnValueEventListener(
            final DatabaseReference node,
            final OnConversationTreeChangeListener onConversationTreeChangeListener) {
        Log.d(TAG, "addOnValueEventListener");

        node.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "addOnValueEventListener.onDataChange");

                onConversationTreeChangeListener.onTreeDataChanged(node,
                        dataSnapshot, (int) dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "addOnValueEventListener.onCancelled");

                if (databaseError != null) {
                    String errorMessage = "addOnValueEventListener.onCancelled: " + databaseError.getMessage();
                    Log.e(TAG, errorMessage);
                    FirebaseCrash.report(new Exception(errorMessage));
                    onConversationTreeChangeListener.onTreeCancelled();
                }
            }
        });
    }

    private static void addOnChildEventListener(final String appId, final String userId,
                                                final DatabaseReference node,
                                                final OnConversationTreeChangeListener onConversationTreeChangeListener) {
        Log.d(TAG, "addOnChildEventListener");

        node.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "addOnChildEventListener.onChildAdded");

                Conversation conversation =
                        ConversationUtils.decodeConversationSnapshop(dataSnapshot);

                // it sets the conversation as read if the person whom are talking to is the current user
                if (ChatManager.getInstance().getLoggedUser().getId().equals(conversation.getSender())) {
                    ConversationUtils.setConversationRead(appId, userId, conversation.getConversationId());
                }

                onConversationTreeChangeListener.onTreeChildAdded(node,
                        dataSnapshot, conversation);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "addOnChildEventListener.onChildChanged");

                Conversation conversation =
                        ConversationUtils.decodeConversationSnapshop(dataSnapshot);

                // it sets the conversation as read if the person whom are talking to is the current user
                if (ChatManager.getInstance().getLoggedUser().getId().equals(conversation.getSender())) {
                    ConversationUtils.setConversationRead(appId, userId, conversation.getConversationId());
                }

                onConversationTreeChangeListener.onTreeChildChanged(node,
                        dataSnapshot, conversation);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "addOnChildEventListener.onChildRemoved");

                onConversationTreeChangeListener.onTreeChildRemoved();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "addOnChildEventListener.onChildMoved");

                onConversationTreeChangeListener.onTreeChildMoved();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "addOnChildEventListeneronCancelled");
                if (databaseError != null) {

                    String errorMessage = "addOnChildEventListener.onCancelled: " + databaseError.getMessage();
                    Log.e(TAG, errorMessage);
                    FirebaseCrash.report(new Exception(errorMessage));

                    onConversationTreeChangeListener.onTreeCancelled();
                }
            }
        });
    }

    /**
     * Generate the conversation id of a conversation between two people into a specific tenant
     *
     * @param userId1
     * @param userId2
     * @return the conversation id
     */
    public static String getConversationId(String userId1, String userId2) {
        Log.d(TAG, "getConversationId");

        String[] sortedUsers = StringUtils.sort(userId1, userId2);

        return sortedUsers[0] + "-" + sortedUsers[1];
    }

    public static void setConversationRead(String appId, String userId, final String conversationId) {
        Log.d(TAG, "setConversationRead");

        final DatabaseReference nodeConversations = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/users/" + userId + "/conversations");

        nodeConversations.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // check if the conversation exists to prevent conversation with only "is_new" value
                if (snapshot.hasChild(conversationId)) {
                    // update the state
                    nodeConversations.child(conversationId)
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

    public static Conversation createNewConversation(String conversationId) {
        Log.d(TAG, "createNewConversation");

        // create a new conversation

        // retrieve the conversation users by the conversationId
        String[] users = ConversationUtils.getConversationIdParams(conversationId);
        String firstUser = users[0];
        String secondUser = users[1];

        // retrieve the recipientId
        String recipientId = firstUser.equals(ChatManager.getInstance().getLoggedUser().getId()) ? secondUser : firstUser;

        // create a new conversation
        Conversation conversation = new Conversation();
        conversation.setLast_message_text("");
        conversation.setConvers_with(recipientId);
        conversation.setSender(ChatManager.getInstance().getLoggedUser().getId());
        conversation.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
        conversation.setStatus(Conversation.CONVERSATION_STATUS_LAST_MESSAGE);
        conversation.setRecipient(recipientId);
        conversation.setConversationId(conversationId);

        return conversation;
    }


    public static Conversation createConversationFromBackgroundPush(Intent pushData) {
        Log.d(TAG, "createConversationFromBackgroundPush");

        Conversation conversation = new Conversation();
        conversation.setConvers_with_fullname(pushData.getStringExtra("sender_fullname"));
        conversation.setConvers_with(pushData.getStringExtra("sender"));
        conversation.setIs_new(true);
        conversation.setStatus(Conversation.CONVERSATION_STATUS_LAST_MESSAGE);
        conversation.setLast_message_text(pushData.getStringExtra("text"));
        conversation.setRecipient(pushData.getStringExtra("recipient"));
        conversation.setSender(pushData.getStringExtra("sender"));
        conversation.setSender_fullname(pushData.getStringExtra("sender_fullname"));
        conversation.setConversationId(pushData.getStringExtra("conversationId"));

        // bugfix Issue #36
        // retrieve the group_id
        try {
            String groupId = (String) pushData.getStringExtra("group_id");
            if (StringUtils.isValid(groupId)) {
                conversation.setGroup_id(groupId);
                conversation.setConversationId(groupId);
            } else {
                Log.w(TAG, "group_id is empty or null. ");
            }
        } catch (Exception e) {
            Log.w(TAG, "cannot retrieve group_id. it may not exist" + e.getMessage());
        }

        // bugfix Issue #36
        // retrieve the group_name
        try {
            String groupName = (String) pushData.getStringExtra("group_name");
            if (StringUtils.isValid(groupName)) {
                conversation.setGroup_name(groupName);
            } else {
                Log.w(TAG, "group_name is empty or null. ");
            }
        } catch (Exception e) {
            Log.w(TAG, "cannot retrieve group_name. it may not exist" + e.getMessage());
        }
        //conversation.setStatus();

        Log.i(TAG_NOTIFICATION, "ConverastionUtils.createConversationFromBackgroundPush(intent):conversation: " + conversation.toString());

        return conversation;
    }

    /**
     * Splits the conversationId into tenant, first user and second user.
     * Users are sorted alphabetically.
     *
     * @param conversationId the conversationId to split
     * @return an array of 2 parameters:
     * <p>
     * array[0] is the first user
     * <p>
     * array[1] is the second user
     */
    public static String[] getConversationIdParams(String conversationId) {
        Log.d(TAG, "getConversationIdParams");

        return conversationId.split(Pattern.quote("-"));
    }

    public static void getConversationFromId(String appId, String userId, final String conversationId,
                                             final OnConversationRetrievedCallback callback) {
        Log.d(TAG, "getConversationFromId");

        DatabaseReference nodeConversation = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/users/" + userId + "/conversations/" + conversationId);

        nodeConversation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    Conversation conversation = decodeConversationSnapshop(dataSnapshot);
                    callback.onConversationRetrievedSuccess(conversation);
                } else {
                    callback.onNewConversationCreated(dataSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: 19/10/17
                callback.onConversationRetrievedError(databaseError.toException());
            }
        });
    }

    public static void getUnreadConversationsCount(String appId, String userId,
                                                   final OnUnreadConversationCountListener callback) {
        Log.d(TAG, "getUnreadConversationsCount");

        DatabaseReference nodeConversations = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/users/" + userId + "/conversations");

        nodeConversations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d(TAG, "OnUnreadConversationCountListener.onDataChange");

                Log.d("Count ", "" + snapshot.getChildrenCount());
                //get the conversations list
                int count = 0;
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Conversation conversation = postSnapshot.getValue(Conversation.class);

                    if (conversation.getIs_new()) {
                        count++;
                    }
                }

                callback.onUnreadConversationCounted(count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "OnUnreadConversationCountListener.onCancelled");

                String errorMessage = "getUnreadConversationsCount.onCancelled: " +
                        "Cannot count the unread conversations." + databaseError.getMessage();
                Log.e(TAG, errorMessage);
                FirebaseCrash.report(new Exception(errorMessage));
            }
        });
    }

    public static Conversation decodeConversationSnapshop(DataSnapshot dataSnapshot) {
        Log.d(TAG, "decodeConversationSnapshop");

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

        // group_id
        try {
            String groupId = (String) map.get("group_id");
            if (StringUtils.isValid(groupId)) {
                conversation.setGroup_id(groupId);
            } else {
                Log.w(TAG, "group_id is empty or null. ");
            }
        } catch (Exception e) {
            Log.w(TAG, "cannot retrieve group_id. it may not exist" + e.getMessage());
        }

        // group_name
        try {
            String groupName = (String) map.get("group_name");
            if (StringUtils.isValid(groupName)) {
                conversation.setGroup_name(groupName);
            } else {
                Log.w(TAG, "group_name is empty or null. ");
            }
        } catch (Exception e) {
            Log.w(TAG, "cannot retrieve group_name. it may not exist" + e.getMessage());

            try {
                String groupName = (String) map.get("name");
                if (StringUtils.isValid(groupName)) {
                    conversation.setGroup_name(groupName);
                } else {
                    Log.w(TAG, "group_name is empty or null. ");
                }
            } catch (Exception e1) {
                Log.w(TAG, "cannot retrieve name. it may not exist" + e1.getMessage());
            }
        }

        // convers with
        if (!StringUtils.isValid(conversation.getGroup_id())) {
            if (conversation.getRecipient().equals(ChatManager.getInstance().getLoggedUser().getId())) {
                conversation.setConvers_with(conversation.getSender());
                conversation.setConvers_with_fullname(conversation.getSender_fullname());
            } else {
                conversation.setConvers_with(conversation.getRecipient());
                conversation.setConvers_with_fullname(conversation.getRecipientFullName());
            }
        }

        return conversation;
    }

    public static void uploadConversationOnFirebase(String appId, String groupId, String userIdNode,
                                                    Conversation conversation) {
        Log.d(TAG, "uploadConversationOnFirebase");

        DatabaseReference nodeConversations = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/users/" + userIdNode + "/conversations");

        nodeConversations.child(groupId).setValue(conversation);
    }
}