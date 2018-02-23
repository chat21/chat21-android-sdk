package org.chat21.android.ui.messages.listeners;

import android.text.style.ClickableSpan;
import android.widget.TextView;

import java.io.Serializable;

/**
 * Created by stefanodp91 on 29/03/17.
 */
public interface OnMessageClickListener extends Serializable {
    void onMessageLinkClick(TextView message,ClickableSpan clickableSpan);
//    void onMessageLinkClick(TextView message);

//    String getUrlToListenTo();
}
