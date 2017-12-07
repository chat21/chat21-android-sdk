package chat21.android.dao.message;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.messages.models.Message;
import chat21.android.dao.node.NodeDAO;
import chat21.android.messages.listeners.OnMessageTreeUpdateListener;

/**
 * Created by stefanodp91 on 08/09/17.
 */
public class MessageDAOImpl extends MessageDAOAbstract {
    private static final String TAG = MessageDAOImpl.class.getName();

    private DatabaseReference mNode;
    private ChildEventListener mChildEventListener;
    private NodeDAO mNodeDAO;


    public MessageDAOImpl(Context context) {
        super(context);

        mNodeDAO = new NodeDAO(ChatManager.getInstance().getTenant());
    }


    @Override
    public void observeMessageTree(String conversationId,
                                   OnMessageTreeUpdateListener onTreeUpdateListener) {
        Log.d(TAG, "observeMessageTree");

        mNode = mNodeDAO.getNodeConversation(conversationId);
        ObserveMessagesTask observeMessagesTask = new ObserveMessagesTask(mNode);
        mChildEventListener = observeMessagesTask.observeMessages(onTreeUpdateListener);
    }

    @Override
    public void detachObserveMessageTree(OnDetachObserveMessageTree callback) {
        Log.d(TAG, "detachObserveMessageTree");

        mNode.removeEventListener(mChildEventListener);

        callback.onDetachedObserveMessageTree();
    }

    @Override
    public void sendMessage(String text, String type,
                            Conversation conversation, Map<String, Object> extras) {
        Log.d(TAG, "sendMessage");

        // the message to send
        Message message = new Message();
        message.setSender(ChatManager.getInstance().getLoggedUser().getId());
        message.setRecipient(conversation.getConvers_with());
        message.setText(text);
        message.getTimestamp();
        message.setType(type);
        message.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
        message.setStatus(Message.STATUS_SENT);
        message.setConversationId(conversation.getConversationId());

        // upload message on firebase
        new UploadMessageOnFirebaseTask()
                .uploadMessage(text, message, conversation.getConversationId(), extras);
    }

    @Override
    public void sendGroupMessage(String text,
                                 String type, Conversation conversation) {
        Log.d(TAG, "sendGroupMessage");

        // the message to send
        Message message = new Message();
        message.setConversationId(conversation.getGroup_id());
        message.setRecipientGroupId(conversation.getGroup_id());
        message.setSender(ChatManager.getInstance().getLoggedUser().getId());
        message.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
        message.setStatus(Message.STATUS_SENT);
        message.setText(text);
        message.getTimestamp();
        message.setType(type);

        // upload message on firebase
        new UploadGroupMessageOnFirebaseTask(getContext())
                .uploadMessage(text, message, conversation, conversation.getConversationId());
    }
}
