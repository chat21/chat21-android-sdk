package chat21.android.dao.message;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

import chat21.android.messages.listeners.OnMessageTreeUpdateListener;
import chat21.android.core.messages.models.Message;

/**
 * Created by stefanodp91 on 08/09/17.
 */

class ObserveMessagesTask {
    private static final String TAG = ObserveMessagesTask.class.getName();

    private DatabaseReference mNode;

    ObserveMessagesTask(DatabaseReference node) {
        mNode = node;
    }

//    ChildEventListener observeMessages(final OnMessageTreeUpdateListener onTreeUpdateListener) {
//        ChildEventListener childEventListener = mNode.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "observeMessages.onChildAdded");
//
//                Message message = decodeMessageSnapShop(dataSnapshot);
////                if (message != null)
//                onTreeUpdateListener.onTreeChildAdded(mNode, dataSnapshot, message);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "observeMessages.onChildChanged");
//
//                Message message = decodeMessageSnapShop(dataSnapshot);
//
////                if (message != null)
//                onTreeUpdateListener.onTreeChildChanged(mNode, dataSnapshot, message);
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "observeMessages.onChildRemoved");
//
//                onTreeUpdateListener.onTreeChildRemoved();
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "observeMessages.onChildMoved");
//
//                onTreeUpdateListener.onTreeChildMoved();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "observeMessages.onCancelled");
//
//                if (databaseError != null) {
//                    String errorMessage = "databaseError: " + databaseError.getMessage();
//                    Log.e(TAG, errorMessage);
//                    FirebaseCrash.report(new Exception(errorMessage));
//
//                    onTreeUpdateListener.onTreeCancelled();
//                }
//            }
//        });
//
//        return childEventListener;
//    }

//    /**
//     * @param dataSnapshot the datasnapshot to decode
//     * @return the decoded message
//     */
//    public static Message decodeMessageSnapShop(DataSnapshot dataSnapshot) {
//        Log.d(TAG, "decodeMessageSnapShop");
//
//        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//        String sender_fullname = (String) map.get("sender_fullname");
//        String recipient = (String) map.get("recipient");
//        String conversationId = (String) map.get("conversationId");
//        long status = (long) map.get("status");
//        String text = (String) map.get("text");
//        long timestamp = (long) map.get("timestamp");
//        String type = (String) map.get("type");
//        String sender = (String) map.get("sender");
//        String recipientGroupId = (String) map.get("recipientGroupId");
//
//        Message message = new Message();
//        message.setSender_fullname(sender_fullname);
//        message.setRecipient(recipient);
//        message.setConversationId(conversationId);
//        message.setStatus((int) status);
//        message.setText(text);
//        message.setTimestamp(timestamp);
//        message.setType(type);
//        message.setSender(sender);
//        message.setRecipientGroupId(recipientGroupId);
//
////        Log.d(TAG, "message >: " + dataSnapshot.toString());
//
//        return message;
//    }
}
