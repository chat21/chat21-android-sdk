package chat21.android.core.messages.handlers;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.listeners.SendMessageListener;
import chat21.android.core.messages.models.Message;

/**
 * Created by andrealeo on 05/12/17.
 */

public class ConversationMessagesHandler {
    private static final String TAG = ConversationMessagesHandler.class.getName();

    String recipientId;
    Context context;
    DatabaseReference conversationMessagesNode;

    public ConversationMessagesHandler(String recipientId, Context context) {

        this.recipientId = recipientId;
        this.context = context;

        this.conversationMessagesNode = bla bla.child(recipientId) ;

    }

    public void sendMessage(String type, String text,
                             final Map<String, Object> customAttributes, final SendMessageListener sendMessageListener) {
        Log.d(TAG, "sendMessage");

        // the message to send
        final Message message = new Message();
        message.setSender(ChatManager.getInstance().getLoggedUser().getId());
        message.setRecipient(this.recipientId);
        message.setText(text);
        message.setType(type);
        message.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
//        message.setConversationId(conversation.getConversationId());

//        NodeDAO nodeDAO = new NodeDAOImpl(context);

        conversationMessagesNode
                .push()
                .setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        Log.d(TAG, "sendMessage.onComplete");

                        if (databaseError != null) {
                            String errorMessage = "sendMessage.onComplete Message not sent. " +
                                    databaseError.getMessage();
                            Log.e(TAG, errorMessage);
                            FirebaseCrash.report(new Exception(errorMessage));
                            sendMessageListener.onResult(null, new ChatRuntimeException(databaseError.toException()));

                        } else {
                            Log.d(TAG, "message sent with success");
                            Log.d(TAG, databaseReference.toString());
                            databaseReference.child("status").setValue(Message.STATUS_RECEIVED);
                            databaseReference.child("customAttributes").setValue(customAttributes);
                            //TODO lookup and return the message from the firebase server to retrieve all the fields (timestamp, status, etc)
                            sendMessageListener.onResult(message, null);
                        }
                    }
                }); // save message on db
    }
}
