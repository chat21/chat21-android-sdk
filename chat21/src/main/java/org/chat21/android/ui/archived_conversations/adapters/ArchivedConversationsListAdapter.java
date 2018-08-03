package org.chat21.android.ui.archived_conversations.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;
import com.vanniktech.emoji.EmojiTextView;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.conversations.models.Conversation;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.adapters.AbstractRecyclerAdapter;
import org.chat21.android.ui.archived_conversations.listeners.OnSwipeMenuReopenClickListener;
import org.chat21.android.ui.conversations.listeners.OnConversationClickListener;
import org.chat21.android.ui.conversations.listeners.OnConversationLongClickListener;
import org.chat21.android.ui.conversations.listeners.OnSwipeMenuUnreadClickListener;
import org.chat21.android.utils.StringUtils;
import org.chat21.android.utils.TimeUtils;
import org.chat21.android.utils.image.CropCircleTransformation;

import java.util.List;

/**
 * Created by stefano on 02/08/2018.
 */
public class ArchivedConversationsListAdapter extends AbstractRecyclerAdapter<Conversation,
        ArchivedConversationsListAdapter.ViewHolder> {
    private static final String TAG = ArchivedConversationsListAdapter.class.getName();

    private OnConversationClickListener onConversationClickListener;
    private OnConversationLongClickListener onConversationLongClickListener;
    private OnSwipeMenuReopenClickListener onSwipeMenuReopenClickListener;

    public OnConversationClickListener getOnConversationClickListener() {
        return onConversationClickListener;
    }

    public void setOnConversationClickListener(OnConversationClickListener onConversationClickListener) {
        this.onConversationClickListener = onConversationClickListener;
    }

    public OnConversationLongClickListener getOnConversationLongClickListener() {
        return onConversationLongClickListener;
    }

    public void setOnConversationLongClickListener(
            OnConversationLongClickListener onConversationLongClickListener) {
        this.onConversationLongClickListener = onConversationLongClickListener;
    }

    public void setOnSwipeMenuReopenClickListener(OnSwipeMenuReopenClickListener onSwipeMenuReopenClickListener) {
        this.onSwipeMenuReopenClickListener = onSwipeMenuReopenClickListener;
    }

    public OnSwipeMenuReopenClickListener getOnSwipeMenuReopenClickListener() {
        return onSwipeMenuReopenClickListener;
    }

    public ArchivedConversationsListAdapter(Context context, List<Conversation> conversations) {
        super(context, conversations);
    }

    @Override
    public void setList(List<Conversation> mList) {
        super.setList(mList);
    }

    @Override
    public ArchivedConversationsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_archived_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ArchivedConversationsListAdapter.ViewHolder holder, final int position) {
        final Conversation conversation = getItem(position);

        setRecipientPicture(holder, conversation);

        setRecipientDisplayName(holder, conversation.getConvers_with_fullname(), conversation.getConvers_with());

        setGroupSenderName(holder, conversation);

        setLastMessageText(holder, conversation);

        setTimestamp(holder, conversation.getIs_new(), conversation.getTimestampLong());

        setConversationCLickAction(holder, conversation, position);

        setConversationLongCLickAction(holder, conversation, position);

        setOnReopenClickListener(holder, conversation, position);
    }

    private void setRecipientPicture(ViewHolder holder, Conversation conversation) {
        String picture = "";
        if (conversation.isDirectChannel()) {

            // retrieve the contact
            IChatUser contact = ChatManager.getInstance()
                    .getContactsSynchronizer()
                    .findById(conversation.getConvers_with());

            // retrieve the contact picture
            if (contact != null) {
                picture = contact.getProfilePictureUrl();
            }

            // show the contact picture
            Glide.with(holder.itemView.getContext())
                    .load(picture)
                    .placeholder(R.drawable.ic_person_avatar)
                    .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                    .into(holder.recipientPicture);
        } else if (conversation.isGroupChannel()) {

            // retrieve the group
            ChatGroup chatGroup = ChatManager.getInstance()
                    .getGroupsSyncronizer()
                    .getById(conversation.getConversationId());

            // retrieve the group picture
            if (chatGroup != null) {
                picture = chatGroup.getIconURL();
            }

            Glide.with(holder.itemView.getContext())
                    .load(picture)
                    .placeholder(R.drawable.ic_group_avatar)
                    .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                    .into(holder.recipientPicture);
        } else {
            Toast.makeText(holder.itemView.getContext(),
                    "channel type is undefined",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // set the recipient display name whom are talking with
    private void setRecipientDisplayName(ViewHolder holder, String recipientFullName, String recipientId) {
        String displayName = StringUtils.isValid(recipientFullName) ? recipientFullName : recipientId;
        holder.recipientDisplayName.setText(displayName);
    }

    // show te last message text
    private void setLastMessageText(ViewHolder holder, Conversation conversation) {

        // default text message
        String lastMessageText = StringUtils.isValid(conversation.getLast_message_text()) ?
                conversation.getLast_message_text() : "";

        if (conversation.getIs_new()) {
            // show bold text
            holder.lastTextMessage.setText(Html.fromHtml("<b>" + lastMessageText + "</b>"));
        } else {
            // not not bold text
            holder.lastTextMessage.setText(lastMessageText);
        }
    }

    private void setGroupSenderName(ViewHolder holder, Conversation conversation) {
        if (conversation.isGroupChannel()) {

            // retrieve the sender
            String sender = conversation.getSender();
            if (conversation.getSender() != null && conversation.getSender()
                    .equals(ChatManager.getInstance().getLoggedUser().getId())) {
                sender = holder.itemView.getContext().getString(R.string.activity_conversation_list_adapter_you_label);
            } else {
                if (StringUtils.isValid(conversation.getSender())) {
                    if (!conversation.getSender().equals("system")) {
                        sender = conversation.getSender_fullname();
                    }
                }
            }

            if (sender != null) {
                holder.senderDisplayName.setText(sender + ": "); // set it
                holder.senderDisplayName.setVisibility(View.VISIBLE); // show it
            } else {
                holder.senderDisplayName.setVisibility(View.GONE); // hide it
            }
        } else {
            holder.senderDisplayName.setVisibility(View.GONE);
        }
    }

    // show the last sent message timestamp
    private void setTimestamp(ViewHolder holder, boolean hasNewMessages, long timestamp) {
        // format the timestamp to a pretty visible format
        String formattedTimestamp = TimeUtils.getFormattedTimestamp(holder.itemView.getContext(), timestamp);

        if (hasNewMessages) {
            // show bold text
            holder.lastMessageTimestamp.setText(Html.fromHtml("<b>" + formattedTimestamp + "</b>"));
        } else {
            // not not bold text
            holder.lastMessageTimestamp.setText(formattedTimestamp);
        }
    }

    // set on row click listener
    private void setConversationCLickAction(ViewHolder holder,
                                            final Conversation conversation, final int position) {
        // use the swipe item click listener to solve the open/close issue
        // more details at https://github.com/daimajia/AndroidSwipeLayout/issues/403
        holder.swipeItem.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getOnConversationClickListener() != null) {
                    getOnConversationClickListener().onConversationClicked(conversation, position);
                } else {
                    Log.w(TAG, "ArchivedConversationsListAdapter.setConversationCLickAction:" +
                            " getOnConversationClickListener() is null. " +
                            "set it with setOnConversationClickListener method. ");
                }
            }
        });
    }

    // set on row long click listener
    private void setConversationLongCLickAction(ViewHolder holder,
                                                final Conversation conversation, final int position) {
        // use the swipe item click listener to solve the open/close issue
        // more details at https://github.com/daimajia/AndroidSwipeLayout/issues/403
        holder.swipeItem.getSurfaceView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (getOnConversationLongClickListener() != null) {
                    getOnConversationLongClickListener().onConversationLongClicked(conversation, position);

                    // source :
                    // https://stackoverflow.com/questions/18911290/perform-both-the-normal-click-and-long-click-at-button
                    return true; // event triggered
                } else {
                    Log.w(TAG, "ArchivedConversationsListAdapter.setConversationLongCLickAction:" +
                            " getOnConversationLongClickListener is null. " +
                            "set it with setOnConversationLongClickListener method. ");
                }

                return false; // event not triggered
            }
        });
    }

    private void setOnReopenClickListener(final ViewHolder holder, final Conversation conversation, final int position) {
        holder.reopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getOnSwipeMenuReopenClickListener().onSwipeMenuReopened(conversation, position);
            }
        });
    }

    /**
     * Dismiss the swipe menu for the view at position
     * @param position the position of the item to dismiss
     */
    public void dismissSwipeMenu(RecyclerView recyclerView, int position) {
        // retrieve the viewholder at position
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);

        // check if the viewholder is an instance of ConversationListAdapter.ViewHolder
        if(viewHolder instanceof ViewHolder) {

            // cast the holder to ConversationListAdapter.ViewHolder
            ViewHolder holder = (ViewHolder) viewHolder;

            // dismiss the menu
            holder.swipeItem.close();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView recipientPicture;
        private TextView recipientDisplayName;
        private TextView senderDisplayName;
        private EmojiTextView lastTextMessage;
        private TextView lastMessageTimestamp;
        private TextView reopen;
        private SwipeLayout swipeItem;

        public ViewHolder(View itemView) {
            super(itemView);

            recipientPicture = itemView.findViewById(R.id.recipient_picture);
            recipientDisplayName = itemView.findViewById(R.id.recipient_display_name);
            senderDisplayName = itemView.findViewById(R.id.sender_display_name);
            lastTextMessage = itemView.findViewById(R.id.last_text_message);
            lastMessageTimestamp = itemView.findViewById(R.id.last_message_timestamp);

            reopen = itemView.findViewById(R.id.reopen);

            swipeItem = itemView.findViewById(R.id.swipe_item);
            swipeItem.setShowMode(SwipeLayout.ShowMode.PullOut);
//            swipeItem.addDrag(SwipeLayout.DragEdge.Left, swipeItem.findViewById(R.id.swipe_left));
            swipeItem.addDrag(SwipeLayout.DragEdge.Right, swipeItem.findViewById(R.id.swipe_right));
        }
    }
}