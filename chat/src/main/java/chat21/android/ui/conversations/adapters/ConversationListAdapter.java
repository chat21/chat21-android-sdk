package chat21.android.ui.conversations.adapters;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import chat21.android.R;
import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.core.ChatManager;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.ui.conversations.listeners.OnConversationClickListener;
import chat21.android.ui.conversations.listeners.OnConversationLongClickListener;
import chat21.android.utils.StringUtils;


/**
 * Created by stefano on 13/10/2015.
 */
public class ConversationListAdapter extends FirebaseRecyclerAdapter<Conversation,
        ViewHolder> {

    private OnConversationClickListener onConversationClickListener;
    private OnConversationLongClickListener onConversationLongClickListener;

    public ConversationListAdapter(DatabaseReference ref) {
        // order child by timestamp
        super(Conversation.class, R.layout.row_conversation, ViewHolder.class,
                ref.orderByChild("timestamp"));
    }

    public OnConversationClickListener getOnConversationClickListener() {
        return onConversationClickListener;
    }

    public void setOnConversationClickListener(OnConversationClickListener onConversationClickListener) {
        this.onConversationClickListener = onConversationClickListener;
    }

    public OnConversationLongClickListener getOnConversationLongClickListener() {
        return onConversationLongClickListener;
    }

    public void setOnConversationLongClickListener(OnConversationLongClickListener onConversationLongClickListener) {
        this.onConversationLongClickListener = onConversationLongClickListener;
    }

    @Override
    protected void populateViewHolder(final ViewHolder holder, Conversation model, int position) {
        final Conversation conversation = getItem(position);

//        if (!StringUtils.isValid(conversation.getGroup_id())) {
//            // update the convers_with user
//            setConversWith(conversation);
//        }

        // update the conversationId
//        setConversationId(conversation);

        holder.bind(conversation, position, getOnConversationClickListener(),
                getOnConversationLongClickListener());
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

//    private void setConversationId(Conversation conversation) {
//        if (StringUtils.isValid(conversation.getGroup_id())) {
//            conversation.setConversationId(conversation.getGroup_id());
//        } else {
//            conversation.setConversationId(ConversationUtils
//                    .getConversationId(ChatManager.getInstance().getLoggedUser().getId(),
//                            conversation.getConvers_with()));
//        }
//    }
}