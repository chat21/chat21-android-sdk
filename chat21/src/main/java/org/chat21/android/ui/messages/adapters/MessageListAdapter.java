package org.chat21.android.ui.messages.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.adapters.AbstractRecyclerAdapter;
import org.chat21.android.ui.messages.listeners.OnMessageClickListener;
import org.chat21.android.utils.StringUtils;

import java.util.List;

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

        int viewType = -1;

        Message message = getItems().get(position);

        String messageSubType = message.getAttributes() != null &&
                StringUtils.isValid((String) message.getAttributes().get("subtype")) ?
                (String) message.getAttributes().get("subtype") : null;

        sender = message.getSender();

        if (sender.compareTo(loggedUser.getId()) == 0) {
            viewType = R.id.row_sender;
        } else if (message.getType().equals("info") ||
                StringUtils.isValid(messageSubType) && messageSubType.equals("info") ||
                message.getType().equals("info/support") ||
                StringUtils.isValid(messageSubType) && messageSubType.equals("info/support")
                ) {
            viewType = R.id.row_system;
        } else {
            viewType = R.id.row_recipient;
        }

        return viewType;


    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (viewType == R.id.row_sender) {
            return new SenderViewHolder(
                    inflater.inflate(R.layout.row_sender, parent, false));
        } else if (viewType == R.id.row_recipient) {
            return new RecipientViewHolder(
                    inflater.inflate(R.layout.row_recipient, parent, false));
        } else if (viewType == R.id.row_system) {
            return new SystemViewHolder(
                    inflater.inflate(R.layout.row_system, parent, false));
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
        } else if (holder instanceof RecipientViewHolder) {
            ((RecipientViewHolder) holder).bind(previousMessage, message,
                    position, onMessageClickListener);
        } else if (holder instanceof SystemViewHolder) {
            ((SystemViewHolder) holder).bind(message);
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