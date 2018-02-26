package org.chat21.android.ui.messages.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.vanniktech.emoji.EmojiTextView;

import java.util.Date;
import java.util.Map;

import org.chat21.android.R;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.messages.activities.ImageDetailsActivity;
import org.chat21.android.ui.messages.listeners.OnMessageClickListener;
import org.chat21.android.utils.TimeUtils;
import org.chat21.android.utils.image.ImageUtils;
import org.chat21.android.utils.views.TextViewLinkHandler;

/**
 * Created by stefano on 25/11/2016.
 */
class SenderViewHolder extends RecyclerView.ViewHolder {

    private final EmojiTextView mMessage;
    private final TextView mDate;
    private final TextView mTimestamp;
    private final ConstraintLayout mBackgroundBubble;
    private final ImageView mPreview; // Resolve Issue #32
    private final ImageView mMessageStatus;
    private final ProgressBar mProgressBar;   // Resolve Issue #52

    SenderViewHolder(View itemView) {
        super(itemView);
        mMessage = (EmojiTextView) itemView.findViewById(R.id.message);
        mDate = (TextView) itemView.findViewById(R.id.date);
        mTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
        mPreview = (ImageView) itemView.findViewById(R.id.preview); // Resolve Issue #32
        mBackgroundBubble = itemView.findViewById(R.id.message_group);
        mMessageStatus = (ImageView) itemView.findViewById(R.id.status);
        mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress);   // Resolve Issue #52
    }

    void bind(final Message previousMessage, final Message message,
              int position, OnMessageClickListener onMessageClickListener) {

        if (message.getType().equals(Message.TYPE_IMAGE)) {
            mMessage.setVisibility(View.GONE);
            mPreview.setVisibility(View.VISIBLE);

            setImagePreview(message);

        } else if (message.getType().equals(Message.TYPE_FILE)) {
            mMessage.setVisibility(View.GONE);
            mPreview.setVisibility(View.VISIBLE);

            setFilePreview(message);
        } else if (message.getType().equals(Message.TYPE_TEXT)) {
            mProgressBar.setVisibility(View.GONE);   // Resolve Issue #52
            mMessage.setVisibility(View.VISIBLE);
            mPreview.setVisibility(View.GONE);
            setMessage(message);
        }

        setBubble();

        setDate(previousMessage, message, position);

        setTimestamp(message);

        // message status icon
        setStatus(message.getStatus());

        // click on the item
        setOnMessageClickListener(onMessageClickListener);
    }

    private String getImageUrl(Message message) {
        String imgUrl = "";

        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null) {
            imgUrl = (String) metadata.get("src");
        }

        return imgUrl;
    }

    // Resolve Issue #32
    private void setImagePreview(final Message message) {

        // Resolve Issue #52
        mProgressBar.setVisibility(View.VISIBLE);

        Glide.with(itemView.getContext())
                .load(getImageUrl(message))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(
                            Exception e,
                            String model,
                            Target<GlideDrawable> target,
                            boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            GlideDrawable resource,
                            String model,
                            Target<GlideDrawable> target,
                            boolean isFromMemoryCache,
                            boolean isFirstResource) {
                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mPreview);

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startImagePreviewActivity(message);
            }
        });
    }

    private void setFilePreview(final Message message) {
        Glide.with(itemView.getContext())
                .load(message.getText())
                .placeholder(R.drawable.ic_placeholder_file_recipient_24dp)
                .into(mPreview);


        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 06/09/17 aprire il file in base al mime
            }
        });
    }

    private void startImagePreviewActivity(Message message) {
        Intent intent = new Intent(itemView.getContext(), ImageDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChatUI.BUNDLE_MESSAGE, message);
        itemView.getContext().startActivity(intent);
    }

    private void setMessage(Message message) {
        // set message text
        mMessage.setText(Html.fromHtml(message.getText()));
        // clickable link support

        //        con LinkMovementMethod.getInstance() nn funziona
//        mMessage.setMovementMethod(LinkMovementMethod.getInstance()); // clickable link support
    }

    private void setTimestamp(Message message) {
        mTimestamp.setText(TimeUtils.formatTimestamp(message.getTimestamp(), "HH:mm"));
    }

    private void setDate(Message previousMessage, Message message, int position) {
        Date previousMessageDate = null;
        if (previousMessage != null) {
            previousMessageDate = new Date(previousMessage.getTimestamp());
        }

        Date messageDate = new Date(message.getTimestamp());
        // it's today. show the label "today"
        if (TimeUtils.isDateToday(message.getTimestamp())) {
            mDate.setText(itemView.getContext().getString(R.string.today));
        } else {
            // it's not today. shows the week of day label
            mDate.setText(TimeUtils.getFormattedTimestamp(itemView.getContext(), message.getTimestamp()));
        }

        // hides or shows the date label
        if (previousMessageDate != null && position > 0) {
            if (TimeUtils.getDayOfWeek(messageDate)
                    .equals(TimeUtils.getDayOfWeek(previousMessageDate))) {
                mDate.setVisibility(View.GONE);
            } else {
                mDate.setVisibility(View.VISIBLE);
            }
        } else {
            mDate.setVisibility(View.VISIBLE);
        }
    }

    private void setBubble() {
        // set bubble color and background
        Drawable bubble = ImageUtils.changeDrawableColor(itemView.getContext(),
                R.color.background_bubble_sender, R.drawable.balloon_sender);
        mBackgroundBubble.setBackground(bubble);
    }


    private void setStatus(long status) {

        Drawable drawableStatus;
        boolean visible;

        if (status == Message.STATUS_SENDING) {
            drawableStatus = itemView.getContext()
                    .getResources()
                    .getDrawable(R.drawable.ic_message_sending_16dp);
            visible = true;
        } else if (status == Message.STATUS_SENT) {
            drawableStatus = itemView.getContext()
                    .getResources()
                    .getDrawable(R.drawable.ic_message_sent_16dp);
            visible = true;
        } else if (status == Message.STATUS_RETURN_RECEIPT) {
            drawableStatus = itemView.getContext()
                    .getResources()
                    .getDrawable(R.drawable.ic_message_receipt_16dp);
            visible = true;
        } else {
            // status not valid or undefined
            drawableStatus = null;
            visible = false;
        }

        if (drawableStatus != null && visible) {
            mMessageStatus.setBackground(drawableStatus);
        }
    }


    private void setOnMessageClickListener(final OnMessageClickListener callback) {
        mMessage.setMovementMethod(new TextViewLinkHandler() {
            @Override
            public void onLinkClick(ClickableSpan clickableSpan) {
                callback.onMessageLinkClick(mMessage, clickableSpan);
            }

            //mMessage.setMovementMethod(new TextViewLinkHandler(callback.getUrlToListenTo()) {
//            @Override
//            public void onLinkClick(String url) {
//                callback.onMessageClick(mMessage);
//            }
        });
    }
}