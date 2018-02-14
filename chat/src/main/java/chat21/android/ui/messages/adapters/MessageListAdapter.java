package chat21.android.ui.messages.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.messages.models.Message;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.adapters.AbstractRecyclerAdapter;
import chat21.android.ui.messages.listeners.OnMessageClickListener;

/**
 * Created by stefano on 31/08/2015.
 */
public class MessageListAdapter extends AbstractRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private IChatUser loggedUser;
    private String sender;
    private OnMessageClickListener onMessageClickListener;

    private static final String TAG = MessageListAdapter.class.getName();

    public MessageListAdapter(Context context, List<Message> items) {
        super(context, items);
        loggedUser = ChatManager.getInstance().getLoggedUser();
    }

    /**
     * Callback called when a message is clicked.
     * Returns the control to the calling activity
     *
     * @param onMessageClickListener the listener
     */
    public void setMessageClickListener(OnMessageClickListener onMessageClickListener) {
        this.onMessageClickListener = onMessageClickListener;
    }


    @Override
    public int getItemViewType(int position) {
//        Log.d(TAG, "position: " + position);

        Message message = getItems().get(position);
//        Log.d(TAG, "message.id: " + message.getId());
//        Log.d(TAG, "message.sender: " + message.getSender());


        sender = message.getSender();

//        if(loggedUser != null) {
//            Log.d(TAG, "loggedUser: " + loggedUser.toString());
//        } else {
//            Log.d(TAG, "loggedUser is null");
//        }


        if (sender.compareTo(loggedUser.getId()) == 0) {
            return R.id.row_sender;
        } else {
            return R.id.row_recipient;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (viewType == R.id.row_sender) {
            return new SenderViewHolder(
                    inflater.inflate(R.layout.row_sender, parent, false));
        } else if (viewType == R.id.row_recipient) {
//            return new RecipientViewHolder(
//                    inflater.inflate(R.layout.row_recipient, parent, false));
            return new RecipientConstraintViewHolder(
                    inflater.inflate(R.layout.row_recipient_constraint, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Message message = getItems().get(position);

        // retrieve the previous message, if exists
        Message previousMessage = null;
        if (position > 0) {
            previousMessage = getItem(position - 1);
        }

        if (holder instanceof SenderViewHolder) {
            ((SenderViewHolder) holder).bind(previousMessage, message,
                    position, onMessageClickListener);
//        } else if (holder instanceof RecipientViewHolder) {
//            ((RecipientViewHolder) holder).bind(previousMessage, message,
//                    position, onMessageClickListener);
//        }

        } else if (holder instanceof RecipientConstraintViewHolder) {
            ((RecipientConstraintViewHolder) holder).bind(previousMessage, message,
                    position, onMessageClickListener);
        }
    }

    /**
     * Update only a single message.
     *
     * @param message the message to update
     */
    public void updateMessage(Message message) {
        List<Message> messageList = getItems();

        int messagePosition = messageList.indexOf(message);

        if (messagePosition >= 0 && messagePosition < messageList.size()) {
            messageList.set(messagePosition, message);
        } else {
            messageList.add(message);
//            int lastPosition = messageList.size() - 1;

//            messageList.add(lastPosition, message);
        }
        notifyDataSetChanged();
    }
}