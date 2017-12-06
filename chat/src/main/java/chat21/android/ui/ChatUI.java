package chat21.android.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import chat21.android.conversations.activities.ConversationListActivity;
import chat21.android.conversations.fragments.ConversationListFragment;
import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.core.ChatManager;
import chat21.android.messages.activites.MessageListActivity;
import chat21.android.user.models.IChatUser;

/**
 * Created by andrealeo on 04/12/17.
 */

public class ChatUI {

    private Context mContext;

    public void showConversationsListFragment(FragmentManager fragmentManager,
                                              @IdRes int containerId) {
        Fragment fragment = ConversationListFragment.newInstance();
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(containerId, fragment)
                    .commitAllowingStateLoss();
        }
    }

    public void showConversationsListActivity() {
        Intent intent = new Intent(mContext, ConversationListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    // TODO: 24/11/17 showChatWith(user)
    // TODO: 24/11/17 add extras here
    public void showDirectConversationActivity(String contactId) {

        IChatUser iChatUser = ChatManager.getInstance().getLoggedUser();
        // generate the conversationId
        String conversationId = ConversationUtils.getConversationId(iChatUser.getId(), contactId);

        // launch the chat
        Intent intent = new Intent(mContext, MessageListActivity.class);
        intent.putExtra(ChatManager._INTENT_BUNDLE_CONVERSATION_ID, conversationId);
        intent.putExtra(ChatManager.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
        // extras to be sent in messages or in the conversation
//        intent.putExtra(Chat.INTENT_BUNDLE_EXTRAS, (Serializable) mConfiguration.getExtras());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
