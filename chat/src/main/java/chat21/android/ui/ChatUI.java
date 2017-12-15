package chat21.android.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.io.Serializable;

import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.core.ChatManager;
import chat21.android.ui.messages.activities.MessageListActivity;
import chat21.android.ui.messages.listeners.OnAttachDocumentsClickListener;
import chat21.android.ui.messages.listeners.OnMessageClickListener;
import chat21.android.ui.contacts.listeners.OnContactClickListener;
import chat21.android.ui.conversations.activities.ConversationListActivity;
import chat21.android.ui.conversations.fragments.ConversationListFragment;
import chat21.android.core.users.models.IChatUser;

/**
 * Created by andrealeo on 04/12/17.
 */

public class ChatUI implements Serializable {

    private static final String TAG = ChatUI.class.getName();


    // singleton
    // source : https://android.jlelse.eu/how-to-make-the-perfect-singleton-de6b951dfdb0
    private static volatile ChatUI instance;

    //private constructor.
    private ChatUI() {

        // Prevent form the reflection api.
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static ChatUI getInstance() {
        if (instance == null) { //if there is no instance available... create new one
            synchronized (ChatUI.class) {
                if (instance == null) instance = new ChatUI();
            }
        }

        return instance;
    }

    // Make singleton from serialize and deserialize operation.
    protected ChatUI readResolve() {
        return getInstance();
    }
    // end singleton


    private Context mContext;

    private OnMessageClickListener onMessageClickListener;
    private OnAttachDocumentsClickListener onAttachDocumentsClickListener;
    private OnContactClickListener onContactClickListener;


    // contact
    public static final String INTENT_BUNDLE_CONTACT_ID = "username"; // FIXME: 17/10/16 NOT EDIT
    public static final String _INTENT_BUNDLE_CONTACT = "_INTENT_BUNDLE_CONTACT";
    public static final String INTENT_BUNDLE_CONTACT_DISPLAY_NAME = "INTENT_BUNDLE_CONTACT_DISPLAY_NAME";

    // target class to be called in listeners (such as OnProfileClickListener)
    public static final String INTENT_BUNDLE_CALLING_ACTIVITY = "INTENT_BUNDLE_CALLING_ACTIVITY";

    // conversation object
    public static final String _INTENT_BUNDLE_CONVERSATION_ID = "_INTENT_BUNDLE_CONVERSATION_ID";

    public static final String INTENT_BUNDLE_IS_FROM_NOTIFICATION = "INTENT_BUNDLE_IS_FROM_NOTIFICATION";

    // message object
    public static final String _INTENT_EXTRAS_MESSAGE = "_INTENT_EXTRAS_MESSAGE";

    // extras
    public static final String INTENT_BUNDLE_EXTRAS = "INTENT_BUNDLE_EXTRAS";

    // group conversation object
    public static final String _INTENT_BUNDLE_GROUP = "_INTENT_BUNDLE_GROUP";
    public static final String _INTENT_EXTRAS_GROUP_ID = "_INTENT_EXTRAS_GROUP_ID";

    public static final String _INTENT_EXTRAS_PARENT_ACTIVITY = "_INTENT_EXTRAS_PARENT_ACTIVITY";


    // request constants
    public static final int _REQUEST_CODE_CREATE_GROUP = 100;
    public static final int _REQUEST_CODE_GROUP_ADMIN_PANEL_ACTIVITY = 200;


    public OnMessageClickListener getOnMessageClickListener() {
        Log.d(TAG, "getOnMessageClickListener");
        return onMessageClickListener;
    }

    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        Log.d(TAG, "setOnMessageClickListener");
        this.onMessageClickListener = onMessageClickListener;
    }

    public OnAttachDocumentsClickListener getOnAttachDocumentsClickListener() {
        Log.d(TAG, "getOnAttachDocumentsClickListener");
        return onAttachDocumentsClickListener;
    }

    public void setOnAttachDocumentsClickListener(OnAttachDocumentsClickListener onAttachDocumentsClickListener) {
        Log.d(TAG, "setOnAttachDocumentsClickListener");
        this.onAttachDocumentsClickListener = onAttachDocumentsClickListener;
    }

    public OnContactClickListener getOnContactClickListener() {
        Log.d(TAG, "getOnContactClickListener");
        return onContactClickListener;
    }

    public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
        Log.d(TAG, "setOnContactClickListener");
        this.onContactClickListener = onContactClickListener;
    }


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
        intent.putExtra(_INTENT_BUNDLE_CONVERSATION_ID, conversationId);
        intent.putExtra(INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
        // extras to be sent in messages or in the conversation
//        intent.putExtra(Chat.INTENT_BUNDLE_EXTRAS, (Serializable) mConfiguration.getExtras());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
