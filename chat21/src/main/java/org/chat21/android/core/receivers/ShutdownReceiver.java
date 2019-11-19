package org.chat21.android.core.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.conversations.ConversationsHandler;
import org.chat21.android.core.presence.MyPresenceHandler;

/**
 * Created by stefanodp91 on 19/02/18.
 */

public class ShutdownReceiver extends BroadcastReceiver {
    private static final String TAG = ShutdownReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            Log.i(TAG, "System shutting down");

            ChatManager chatManager = ChatManager.getInstance();

            // disconnect the current user when the phone shutdown
            if (chatManager != null && chatManager.isStarted()) {
                MyPresenceHandler presenceHandler = chatManager.getMyPresenceHandler();

                if (presenceHandler != null) {
                    presenceHandler.dispose();
                }

                ConversationsHandler conversationsHandler = chatManager.getConversationsHandler();

                if (conversationsHandler != null) {
                    conversationsHandler.removeAllConversationsListeners();
                }
            }
        }
    }
}
