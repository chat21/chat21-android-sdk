
package chat21.android.dao.message;


import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

import chat21.android.conversations.models.Conversation;
import chat21.android.core.ChatManager;
import chat21.android.dao.node.NodeDAO;
import chat21.android.dao.node.NodeDAOImpl;
import chat21.android.core.messages.models.Message;

/**
 * Created by stefanodp91 on 08/09/17.
 */
class UploadMessageOnFirebaseTask {
    private static final String TAG = UploadMessageOnFirebaseTask.class.getName();

    private NodeDAO mNodeDAO;

    UploadMessageOnFirebaseTask(Context context) {
        mNodeDAO = new NodeDAOImpl(context);
    }

    void uploadMessage(String text, Message message, String conversationId, final Map<String, Object> extras) {

        Log.d(TAG, "uploadMessage");

        mNodeDAO.getNodeConversation(conversationId)
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

                        } else {
                            Log.d(TAG, "message sent with success");
                            Log.d(TAG, databaseReference.toString());
                            databaseReference.child("status").setValue(Message.STATUS_RECEIVED);
                            databaseReference.child("extras").setValue(extras);
                        }
                    }
                }); // save message on db

        // update node sender
        updateNodeSender(text, message, conversationId, extras);

        // update node recipient
        updateNodeRecipient(text, message, conversationId, extras);
    }


    private void updateNodeSender(String text,
                                  Message message, String conversationId,
                                  final Map<String, Object> extras) {
        Log.d(TAG, "updateNodeSender");

        // conversation sender
        Conversation cSender = new Conversation();
        cSender.setConvers_with(message.getRecipient());
        cSender.setSender(ChatManager.getInstance().getLoggedUser().getId());
        cSender.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
        cSender.setConvers_with_fullname(message.getRecipient());
        cSender.setRecipient(message.getRecipient());
//        cSender.setRecipientFullName(StringUtils.isValid(conversation.getConvers_with_fullname()) ? conversation.getConvers_with_fullname() : conversation.getConvers_with());
        cSender.getTimestamp();
        cSender.setStatus(ChatManager.CONVERSATION_STATUS_LAST_MESSAGE);
        cSender.setIs_new(true); // the conversation has new messages
        cSender.setLast_message_text(text);

        // save to firebase
        mNodeDAO.getNodeConversations(ChatManager.getInstance().getLoggedUser().getId())
                .child(conversationId).setValue(cSender,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference firebase) {
                        Log.d(TAG, "updateNodeSender.onComplete");

                        // check of there are errors
                        if (databaseError == null) {
                            Log.d(TAG, "extras added with success to node sender");
                            if (extras != null) {
                                // add extras
                                firebase.child("extras").setValue(extras);
                            }
                        } else {
                            String errorMessage = "updateNodeSender.onComplete: Cannot add extras. " +
                                    databaseError.getMessage();
                            Log.e(TAG, errorMessage);
                            FirebaseCrash.report(new Exception(errorMessage));
                        }
                    }
                });
    }

    private void updateNodeRecipient(String text, Message message, String conversationId,
                                     final Map<String, Object> extras) {
        Log.d(TAG, "updateNodeRecipient");

        String conversWithId;
        if (message.getSender().equals(ChatManager.getInstance().getLoggedUser().getId())) {
            conversWithId = message.getRecipient();
        } else {
            conversWithId = message.getSender();
        }

        // conversation recipient
        Conversation cRecipient = new Conversation();
        cRecipient.setConvers_with_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
//        cRecipient.setConvers_with(Chat.Configuration.getLoggedUser().getId());
        cRecipient.setSender(ChatManager.getInstance().getLoggedUser().getId());
        cRecipient.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
        cRecipient.setRecipient(conversWithId);
//        cRecipient.setRecipientFullName(StringUtils.isValid(conversation.getConvers_with_fullname()) ? conversation.getConvers_with_fullname() : conversation.getConvers_with());
        cRecipient.getTimestamp();
        cRecipient.setStatus(ChatManager.CONVERSATION_STATUS_LAST_MESSAGE);
        cRecipient.setIs_new(true);  // the conversation has new messages
        cRecipient.setLast_message_text(text);

        // save to firebase
        mNodeDAO.getNodeConversations(message.getRecipient())
                .child(conversationId).setValue(cRecipient,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference firebase) {
                        Log.d(TAG, "updateNodeRecipient.onComplete");

                        // check of there are errors
                        if (databaseError == null) {
                            Log.d(TAG, "extras added with success to node recipient");
                            if (extras != null) {
                                // add extras
                                firebase.child("extras").setValue(extras);
                            }
                        } else {
                            String errorMessage = "updateNodeRecipient.onComplete: Cannot add extras. " +
                                    databaseError.getMessage();
                            Log.e(TAG, errorMessage);
                            FirebaseCrash.report(new Exception(errorMessage));
                        }
                    }
                });
    }
}