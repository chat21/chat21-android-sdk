package chat21.android.ui.conversations.listeners;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.google.firebase.crash.FirebaseCrash;


import chat21.android.R;
import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.core.ChatManager;
import chat21.android.ui.conversations.listeners.OnContactListClickListener;
import chat21.android.ui.messages.activities.MessageListActivity;
import chat21.android.ui.ChatUI;

/**
 * Created by frontiere21 on 08/11/16.
 */
public class OnSupportContactListClickListener implements View.OnClickListener {
    private static final String TAG = OnContactListClickListener.class.getName();
    private Context context;

    public OnSupportContactListClickListener(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "OnContactListClickListener");

        // se l'utente correntemente loggato è il supporto, mostra la lista di contatti
        // altrimenti avvia la conversazione con il supporto

        if (ChatManager.getInstance().getLoggedUser().getId().compareTo(getContext().getResources()
                .getString(R.string.chat_support_account_id)) == 0) {
            try {
                // targetClass MUST NOT BE NULL
                Class<?> targetClass = Class.forName(
                        getContext().getString(R.string.target_contact_list_activity_class));
                Intent intent = new Intent(getContext(), targetClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
                getContext().startActivity(intent);
            } catch (ClassNotFoundException e) {
                String errorMessage = "cannot retrieve the user list activity target class. \n" + e.getMessage();
                Log.e(TAG, errorMessage);
                FirebaseCrash.report(new Exception(errorMessage));
            }
        } else {
            String conversationId = ConversationUtils.getConversationId(
            ChatManager.getInstance().getLoggedUser().getId(), getContext().getResources().getString(R.string.chat_support_account_id));

            Intent intent = new Intent(getContext(), MessageListActivity.class);
            intent.putExtra(ChatUI._INTENT_BUNDLE_CONVERSATION_ID, conversationId);
            intent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
            getContext().startActivity(intent);
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}