package chat21.android.ui.messages.listeners;

import android.view.View;

import java.io.Serializable;

import chat21.android.core.messages.models.Message;

/**
 * Created by stefanodp91 on 29/03/17.
 */
public interface OnMessageLongClickListener extends Serializable {
    void onMessageLongClick(View view, Message message, int position);
}
