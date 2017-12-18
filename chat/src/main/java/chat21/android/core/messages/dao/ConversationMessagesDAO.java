//package chat21.android.core.messages.dao;
//
//import android.util.Log;
//
//import com.google.firebase.crash.FirebaseCrash;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.List;
//import java.util.Map;
//
//import chat21.android.core.ChatManager;
//import chat21.android.core.exception.ChatRuntimeException;
//import chat21.android.core.messages.listeners.ConversationsListener;
//import chat21.android.core.messages.listeners.SendMessageListener;
//import chat21.android.core.messages.models.Message;
//
///**
// * Created by andrealeo on 05/12/17.
// */
//
//public class ConversationMessagesDAO {
//    private static final String TAG = ConversationMessagesDAO.class.getName();
//
//    String recipientId;
//    DatabaseReference conversationMessagesNode;
//
//    public ConversationMessagesDAO(String firebaseUrl, String recipientId, String appId, String currentUserId) {
//
//        this.recipientId = recipientId;
//
//        this.conversationMessagesNode = FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseUrl).child("/apps/"+appId+"/users/"+currentUserId+"/messages/"+recipientId);
//        this.conversationMessagesNode.keepSynced(true);
//    }
//
//
//
//
//    public void sendMessage(String type, String text,
//                             final Map<String, Object> customAttributes, final SendMessageListener sendMessageListener) {
//        Log.d(TAG, "sendMessage");
//
//        // the message to send
//        final Message message = new Message();
//        message.setSender(ChatManager.getInstance().getLoggedUser().getId());
//        message.setRecipient(this.recipientId);
//        message.setText(text);
//        message.setType(type);
//        message.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
//
//
//        conversationMessagesNode
//                .push()
//                .setValue(message, new DatabaseReference.CompletionListener() {
//                    @Override
//                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                        Log.d(TAG, "sendMessage.onComplete");
//
//                        if (databaseError != null) {
//                            String errorMessage = "sendMessage.onComplete Message not sent. " +
//                                    databaseError.getMessage();
//                            Log.e(TAG, errorMessage);
//                            FirebaseCrash.report(new Exception(errorMessage));
//                            if (sendMessageListener!=null){
//                                sendMessageListener.onResult(null, new ChatRuntimeException(databaseError.toException()));
//                            }
//
//                        } else {
//                            Log.d(TAG, "message sent with success");
//                            Log.d(TAG, databaseReference.toString());
////                            databaseReference.child("status").setValue(Message.STATUS_RECEIVED);
//                            databaseReference.child("customAttributes").setValue(customAttributes);
//                            //TODO lookup and return the message from the firebase server to retrieve all the fields (timestamp, status, etc)
//                            if (sendMessageListener!=null){
//                                sendMessageListener.onResult(message, null);
//                            }
//                        }
//                    }
//                }); // save message on db
//    }
//
//
//
//}
