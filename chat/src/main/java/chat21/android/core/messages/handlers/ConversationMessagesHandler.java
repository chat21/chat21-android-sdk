package chat21.android.core.messages.handlers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.listeners.ConversationMessagesListener;
import chat21.android.core.messages.listeners.SendMessageListener;
import chat21.android.core.messages.models.Message;
import java.util.Date;
/**
 * Created by andrealeo on 05/12/17.
 */

public class ConversationMessagesHandler {
    private static final String TAG = ConversationMessagesHandler.class.getName();

    String recipientId;
    DatabaseReference conversationMessagesNode;
//    List<ConversationsListener> conversationMessagesListeners;
    ChildEventListener conversationMessagesChildEventListener;

    public ConversationMessagesHandler(String firebaseUrl, String recipientId, String appId, String currentUserId
//            , ConversationsListener conversationMessagesListener
    ) {

        this.recipientId = recipientId;

        this.conversationMessagesNode = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseUrl).child("/apps/"+appId+"/users/"+currentUserId+"/messages/"+recipientId);
        this.conversationMessagesNode.keepSynced(true);


//        this.conversationMessagesListeners = new ArrayList<ConversationsListener>();
//        this.conversationMessagesListeners.add(conversationMessagesListener);

    }

    public void sendMessage(
            String sender,
            String senderFullname,
            String type, String text,
                            final Map<String, Object> customAttributes, final SendMessageListener sendMessageListener) {
        Log.d(TAG, "sendMessage");

        // the message to send
        final Message message = new Message();
        message.setSender(sender);
        message.setRecipient(this.recipientId);
        message.setText(text);
        message.setType(type);
        message.setSender_fullname(senderFullname);
        message.setStatus(Message.STATUS_SENDING);
        message.setTimestamp(new Date().getTime());


        conversationMessagesNode
                .push()
                .setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Log.d(TAG, "sendMessage.onComplete");

                        if (databaseError != null) {
                            String errorMessage = "sendMessage.onComplete Message not sent. " +
                                    databaseError.getMessage();
                            Log.e(TAG, errorMessage);
                            FirebaseCrash.report(new Exception(errorMessage));
                            if (sendMessageListener!=null){
                                sendMessageListener.onResult(null, new ChatRuntimeException(databaseError.toException()));
                            }

                        } else {
                            Log.d(TAG, "message sent with success");
                            Log.d(TAG, databaseReference.toString());
//                            databaseReference.child("status").setValue(Message.STATUS_RECEIVED);
                            databaseReference.child("customAttributes").setValue(customAttributes);
                            //TODO lookup and return the message from the firebase server to retrieve all the fields (timestamp, status, etc)
                            if (sendMessageListener!=null){
                                sendMessageListener.onResult(message, null);
                            }
                        }
                    }
                }); // save message on db
    }


    public ChildEventListener connect(final ConversationMessagesListener conversationMessagesListener) {

        final List<ConversationMessagesListener> conversationMessagesListeners = new ArrayList<ConversationMessagesListener>();
        conversationMessagesListeners.add(conversationMessagesListener);

        conversationMessagesChildEventListener = conversationMessagesNode.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "ConversationMessagesHandler.connect.onChildAdded");

                try {
                    Message message = decodeMessageSnapShop(dataSnapshot);

                    for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                        conversationMessagesListener.onConversationMessageReceived(message, null);
                    }

                    //TODO settare status a 200 qui

                } catch (Exception e) {
                    for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                        conversationMessagesListener.onConversationMessageReceived(null, new ChatRuntimeException(e));
                    }
                }
            }

            //for return recepit
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "ConversationMessagesHandler.connect.onChildChanged");

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
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
//                Log.d(TAG, "observeMessages.onChildMoved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "observeMessages.onCancelled");

            }
        });

        return conversationMessagesChildEventListener;
    }


    /**
     * @param dataSnapshot the datasnapshot to decode
     * @return the decoded message
     */
    public static Message decodeMessageSnapShop(DataSnapshot dataSnapshot) {
        Log.d(TAG, "decodeMessageSnapShop");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

        String messageId = dataSnapshot.getKey();
        String sender_fullname = (String) map.get("sender_fullname");
        String recipient = (String) map.get("recipient");
        long status = (long) map.get("status");
        String text = (String) map.get("text");
        long timestamp = (long) map.get("timestamp");
        String type = (String) map.get("type");
        String sender = (String) map.get("sender");

        Message message = new Message();

        message.setId(messageId);
        message.setSender_fullname(sender_fullname);
        message.setRecipient(recipient);
        message.setStatus((int) status);
        message.setText(text);
        message.setTimestamp(timestamp);
        message.setType(type);
        message.setSender(sender);

//        Log.d(TAG, "message >: " + dataSnapshot.toString());

        return message;
    }



}
