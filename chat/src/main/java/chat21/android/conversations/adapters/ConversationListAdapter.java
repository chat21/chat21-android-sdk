package chat21.android.conversations.adapters;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import chat21.android.R;
import chat21.android.adapters.AbstractRecyclerAdapter;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.core.ChatManager;
import chat21.android.utils.StringUtils;


/**
 * Created by stefano on 13/10/2015.
 */
public class ConversationListAdapter extends FirebaseRecyclerAdapter<Conversation,
        ViewHolder> {

    private AbstractRecyclerAdapter.OnRecyclerItemClickListener mOnRecyclerItemClickListener;
    private AbstractRecyclerAdapter.OnRecyclerItemLongClickListener mOnRecyclerItemLongClickListener;

    public ConversationListAdapter(DatabaseReference ref) {
        // order child by timestamp
        super(Conversation.class, R.layout.row_conversation, ViewHolder.class,
                ref.orderByChild("timestamp"));
    }

    public void addOnRecyclerItemClickListener(
            AbstractRecyclerAdapter
                    .OnRecyclerItemClickListener<Conversation> onRecyclerItemClickListener) {
        mOnRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public AbstractRecyclerAdapter.OnRecyclerItemClickListener getOnRecyclerItemClickListener() {
        return mOnRecyclerItemClickListener;
    }

    public void addOnRecyclerItemLongClickListener(
            AbstractRecyclerAdapter
                    .OnRecyclerItemLongClickListener<Conversation> onRecyclerItemLongClickListener) {
        mOnRecyclerItemLongClickListener = onRecyclerItemLongClickListener;
    }

    public AbstractRecyclerAdapter.OnRecyclerItemLongClickListener getOnRecyclerItemLongClickListener() {
        return mOnRecyclerItemLongClickListener;
    }

    @Override
    protected void populateViewHolder(final ViewHolder holder, Conversation model, int position) {
        final Conversation conversation = getItem(position);

        if (!StringUtils.isValid(conversation.getGroup_id())) {
            // update the convers_with user
            setConversWith(conversation);
        }

        // update the conversationId
        setConversationId(conversation);

        holder.bind(conversation, position, getOnRecyclerItemClickListener(),
                getOnRecyclerItemLongClickListener());
    }

    private void setConversWith(Conversation conversation) {
        if (conversation.getSender().equals(ChatManager.getInstance().getLoggedUser().getId())) {
            conversation.setConvers_with(conversation.getRecipient());
            conversation.setConvers_with_fullname(conversation.getRecipientFullName());
        } else {
            conversation.setConvers_with(conversation.getSender());
            conversation.setConvers_with_fullname(conversation.getSender_fullname());
        }
    }

    private void setConversationId(Conversation conversation) {
        if (StringUtils.isValid(conversation.getGroup_id())) {
            conversation.setConversationId(conversation.getGroup_id());
        } else {
            conversation.setConversationId(ConversationUtils
                    .getConversationId(ChatManager.getInstance().getLoggedUser().getId(),
                            conversation.getConvers_with()));
        }
    }
}