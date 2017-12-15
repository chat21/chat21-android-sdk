package chat21.android.ui.messages.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.messages.models.Message;
import chat21.android.ui.messages.listeners.OnMessageClickListener;
import chat21.android.ui.adapters.AbstractRecyclerAdapter;

/**
 * Created by stefano on 31/08/2015.
 */
public class MessageListAdapter extends AbstractRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private String sender;
    private OnMessageClickListener onMessageClickListener;

    public MessageListAdapter(Context context, List<Message> items) {
        super(context, items);
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
        Message message = getItems().get(position);
        sender = message.getSender();

        if (sender.compareTo(ChatManager.getInstance().getLoggedUser().getId()) == 0) {
            return R.id.row_sender_no_profile_picture;
        } else {
            return R.id.row_recipient_no_profile_picture;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (viewType == R.id.row_sender_no_profile_picture) {
            return new SenderViewHolder(
                    inflater.inflate(R.layout.row_sender, parent, false));
        } else if (viewType == R.id.row_recipient_no_profile_picture) {
            return new RecipientViewHolder(
                    inflater.inflate(R.layout.row_recipient, parent, false));
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
        }
    }

    /**
     * Update only a single message.
     *
     * @param message the message to update
     */
    public void updateMessage(Message message) {
        List<Message> messageList = getItems();

        int position = messageList.size() - 1;

        messageList.remove(position);
        messageList.add(message);
        notifyDataSetChanged();
    }
}