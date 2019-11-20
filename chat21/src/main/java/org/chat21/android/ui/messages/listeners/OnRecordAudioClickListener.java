package org.chat21.android.ui.messages.listeners;

import androidx.fragment.app.FragmentActivity;

import org.chat21.android.core.users.models.IChatUser;

import java.io.Serializable;

public interface OnRecordAudioClickListener<T> extends Serializable {
    void onRecordAudioClicked(IChatUser recipient, String channelType, FragmentActivity activity);
}
