package chat21.android.core.messages.handlers;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import chat21.android.core.exception.ChatFieldNotFoundException;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.listeners.ConversationMessagesListener;
import chat21.android.core.messages.listeners.SendMessageListener;
import chat21.android.core.messages.models.Message;
import chat21.android.core.users.models.IChatUser;

/**
 * Created by andrealeo on 05/12/17.
 */

public class ConversationMessagesHandler {
    private static final String TAG = ConversationMessagesHandler.class.getName();

    private List<Message> messages = new ArrayList<Message>(); // messages in memory

    IChatUser currentUser;
    String recipientId;

    DatabaseReference conversationMessagesNode;

    ChildEventListener conversationMessagesChildEventListener;

    List<ConversationMessagesListener> conversationMessagesListeners;

    public ConversationMessagesHandler(String firebaseUrl, String recipientId, String appId, IChatUser currentUser
//            , ConversationsListener conversationMessagesListener
    ) {

        conversationMessagesListeners = new ArrayList<>();

        this.recipientId = recipientId;
        this.currentUser = currentUser;

        this.conversationMessagesNode = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseUrl).child("/apps/" + appId + "/users/" + currentUser.getId() + "/messages/" + recipientId);
        this.conversationMessagesNode.keepSynced(true);
        Log.d(TAG, "conversationMessagesNode : " + conversationMessagesNode.toString());


//        this.conversationMessagesListeners = new ArrayList<ConversationsListener>();
//        this.conversationMessagesListeners.add(conversationMessagesListener);


    }

    public void sendMessage(
            String recipientFullname,
            String type, String text,
            final Map<String, Object> customAttributes, final SendMessageListener sendMessageListener) {
        Log.v(TAG, "sendMessage called");

        // the message to send
        final Message message = new Message();

        message.setSender(currentUser.getId());
        message.setRecipient(this.recipientId);
        message.setStatus(Message.STATUS_SENDING);

        message.setText(text);
        message.setType(type);
        message.setSenderFullname(currentUser.getFullName());
        message.setRecipientFullname(recipientFullname);
//        message.setStatus(Message.STATUS_SENDING);
        message.setTimestamp(new Date().getTime());
        message.setCustomAttributes(customAttributes);

        Log.d(TAG, "sendMessage.message: " + message.toString());

        // generate a message id
        DatabaseReference newMessageReference = conversationMessagesNode.push();
        String messageId = newMessageReference.getKey();

        Log.d(TAG, "Generated messagedId with value : "+ messageId);
        message.setId(messageId); // assign an id to the message

        saveOrUpdateMessageInMemory(message);

//        conversationMessagesNode
//                .push()
        Message messageForSending = createMessageForFirebase(message);
        Log.d(TAG, "sendMessage.messageForSending: " + messageForSending.toString());

        newMessageReference
                .setValue(messageForSending, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Log.v(TAG, "sendMessage.onComplete");

                        if (databaseError != null) {
                            String errorMessage = "sendMessage.onComplete Message not sent. " +
                                    databaseError.getMessage();
                            Log.e(TAG, errorMessage);
                            FirebaseCrash.report(new Exception(errorMessage));
                            if (sendMessageListener != null) {
                                sendMessageListener.onMessageSentComplete(null, new ChatRuntimeException(databaseError.toException()));
                            }

                        } else {
                            Log.d(TAG, "message sent with success to : " + databaseReference.toString());

                            //the cloud code set status to 100 automaticaly
                            //message.setStatus(Message.STATUS_SENT);
                            saveOrUpdateMessageInMemory(message);


//                            databaseReference.child("status").setValue(Message.STATUS_RECEIVED);
                           // databaseReference.child("customAttributes").setValue(customAttributes);
                            //TODO lookup and return the message from the firebase server to retrieve all the fields (timestamp, status, etc)
                            if (sendMessageListener != null) {
                                sendMessageListener.onMessageSentComplete(message, null);
                            }
                        }
                    }
                }); // save message on db


        if (sendMessageListener != null) {
            //set sender and recipiet because MessageListActivity use this message to update the view immediatly and MessageListAdapter use message.sender
            Log.d(TAG, "onBeforeMessageSent called with message : " + message);

            sendMessageListener.onBeforeMessageSent(message, null);
        }

    }

    private Message createMessageForFirebase(Message message)  {
        Message messageForFirebase = (Message)message.clone();
        messageForFirebase.setSender(null);
        messageForFirebase.setRecipient(null);
        messageForFirebase.setStatus(null);
        messageForFirebase.setTimestamp(null);
        return  messageForFirebase;
    }
    // it checks if the message already exists.
    // if the message exists update it, add it otherwise
    private void saveOrUpdateMessageInMemory(Message newMessage) {
        Log.d(TAG, "saveOrUpdateMessageInMemory  for message : " + newMessage);

        // look for the message

        int index = -1;
        for (Message tempMessage : messages) {
            if (tempMessage.equals(newMessage)) {
                index = messages.indexOf(tempMessage);
                break;
            }
        }

        if (index != -1) {
            // message already exists
            messages.set(index, newMessage); // update the existing message
            Log.v(TAG, "message " + newMessage + "updated into messages at position " + index);

        } else {
            // message not exists
            messages.add(newMessage); // insert a new message
            Log.v(TAG, "message " + newMessage + "is not found into messages. The message was added at the end of the list");

        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public ChildEventListener connect() {
        Log.d(TAG, "connecting  for recipientId : " + this.recipientId);


//        final List<ConversationMessagesListener> conversationMessagesListeners = new ArrayList<ConversationMessagesListener>();
//        conversationMessagesListeners.add(conversationMessagesListener);

        if (conversationMessagesChildEventListener==null) {

            Log.d(TAG, "creating a new conversationMessagesChildEventListener");

            conversationMessagesChildEventListener = conversationMessagesNode.orderByChild(Message.TIMESTAMP_FIELD_KEY).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.v(TAG, "ConversationMessagesHandler.connect.onChildAdded");

                    try {
                        Message message = decodeMessageSnapShop(dataSnapshot);
                        Log.d(TAG, "ConversationMessagesHandler.connect.onChildAdded.message : "+ message);


                        if (message.getStatus()< Message.STATUS_RECEIVED_FROM_RECIPIENT_CLIENT
                                && !message.getSender().equals(currentUser.getId())
                                && message.isDirectChannel()){

                            dataSnapshot.getRef().child(Message.STATUS_FIELD_KEY).setValue(Message.STATUS_RECEIVED_FROM_RECIPIENT_CLIENT);
                            Log.d(TAG, "Message with id : " + message.getId() + " is received from the recipient client and the status field of the message has beed set to " + Message.STATUS_RECEIVED_FROM_RECIPIENT_CLIENT);
                        }

                        saveOrUpdateMessageInMemory(message);

                        for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                            conversationMessagesListener.onConversationMessageReceived(message, null);
                        }

                        //TODO settare status a 200 qui

                    }catch (ChatFieldNotFoundException cfnfe) {
                        Log.w(TAG, "Error decoding message on onChildAdded " + cfnfe.getMessage());
                    } catch (Exception e) {
                        for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                            conversationMessagesListener.onConversationMessageReceived(null, new ChatRuntimeException(e));
                        }
                    }
                }

                //for return recepit
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.v(TAG, "ConversationMessagesHandler.connect.onChildChanged");

                    try {
                        Message message = decodeMessageSnapShop(dataSnapshot);

                        Log.d(TAG, "ConversationMessagesHandler.connect.onChildChanged.message : "+ message);

                        saveOrUpdateMessageInMemory(message);

                        for (ConversationMessagesListener conversationMessagesListener : conversationMessagesListeners) {
                            conversationMessagesListener.onConversationMessageChanged(message, null);
                        }

                    } catch (ChatFieldNotFoundException cfnfe) {
                        Log.w(TAG, "Error decoding message on onChildAdded " + cfnfe.getMessage());
                    }  catch (Exception e) {
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

            Log.i(TAG, "connected for recipientId: " + recipientId );


        }else {
            Log.i(TAG, "already connected form recipientId : " + recipientId);
        }

        return conversationMessagesChildEventListener;
    }


    /**
     * @param dataSnapshot the datasnapshot to decode
     * @return the decoded message
     */
    public static Message decodeMessageSnapShop(DataSnapshot dataSnapshot) throws ChatFieldNotFoundException {
        Log.v(TAG, "decodeMessageSnapShop called");

        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

        String messageId = dataSnapshot.getKey();

        String sender = (String) map.get("sender");
        if (sender ==null) {
            throw new ChatFieldNotFoundException("Required sender field is null for message id : "+ messageId);
        }

        String recipient = (String) map.get("recipient");
        if (recipient ==null) {
            throw new ChatFieldNotFoundException("Required recipient field is null for message id : "+ messageId);
        }


        String sender_fullname = (String) map.get("sender_fullname");
        String recipient_fullname = (String) map.get("recipient_fullname");

        Long status = null;
        if (map.containsKey("status")){
            status =  (Long) map.get("status");
        }

        String text = (String) map.get("text");

        Long timestamp = null;
        if (map.containsKey("timestamp")) {
            timestamp = (Long) map.get("timestamp");
        }

        String type = (String) map.get("type");

        String channelType = (String) map.get("channel_type");

        Message message = new Message();

        message.setId(messageId);
        message.setSender(sender);
        message.setSenderFullname(sender_fullname);
        message.setRecipient(recipient);
        message.setRecipientFullname(recipient_fullname);
        message.setStatus(status);
        message.setText(text);
        message.setTimestamp(timestamp);
        message.setType(type);
        message.setChannelType(channelType);

        Log.v(TAG, "decodeMessageSnapShop.message : "+ message);

//        Log.d(TAG, "message >: " + dataSnapshot.toString());

        return message;
    }

    public List<ConversationMessagesListener> getConversationMessagesListener() {
        return conversationMessagesListeners;
    }

    public void setConversationMessagesListeners(List<ConversationMessagesListener> conversationMessagesListeners) {
        this.conversationMessagesListeners = conversationMessagesListeners;
        Log.i(TAG, "  ConversationMessagesListeners setted");

    }

    public void addConversationMessagesListener(ConversationMessagesListener conversationMessagesListener) {
        Log.v(TAG, "  addConversationMessagesListener called");

        this.conversationMessagesListeners.add(conversationMessagesListener);
        Log.i(TAG, "  conversationMessagesListener with hashCode: "+ conversationMessagesListener.hashCode() + " added");

    }

    public void upsertConversationMessagesListener(ConversationMessagesListener conversationMessagesListener) {
        Log.v(TAG, "  upsertConversationMessagesListener called");

        if (conversationMessagesListeners.contains(conversationMessagesListener)) {
            this.conversationMessagesListeners.remove(conversationMessagesListener);
            this.conversationMessagesListeners.add(conversationMessagesListener);
            Log.i(TAG, "  conversationMessagesListener with hashCode: "+ conversationMessagesListener.hashCode() + " updated");
        } else {
            this.conversationMessagesListeners.add(conversationMessagesListener);
            Log.i(TAG, "  conversationMessagesListener with hashCode: "+ conversationMessagesListener.hashCode() + " added");

        }
    }

    public void removeConversationMessagesListener(ConversationMessagesListener conversationMessagesListener) {
        Log.v(TAG, "  removeConversationMessagesListener called");

        this.conversationMessagesListeners.remove(conversationMessagesListener);
        Log.i(TAG, "  conversationMessagesListener with hashCode: "+ conversationMessagesListener.hashCode() + " removed");

    }

    public void removeAllConversationMessagesListeners() {
        this.conversationMessagesListeners = null;
        Log.i(TAG, "Removed all ConversationMessagesListeners");

    }




    public ChildEventListener getConversationMessagesChildEventListener() {
        return conversationMessagesChildEventListener;
    }

    public void disconnect() {
        this.conversationMessagesNode.removeEventListener(conversationMessagesChildEventListener);
        this.removeAllConversationMessagesListeners();
    }


}
