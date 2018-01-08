package chat21.android.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.io.Serializable;

import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.contacts.listeners.OnContactClickListener;
import chat21.android.ui.contacts.listeners.OnCreateGroupClickListener;
import chat21.android.ui.conversations.activities.ConversationListActivity;
import chat21.android.ui.conversations.fragments.ConversationListFragment;
import chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import chat21.android.ui.messages.activities.MessageListActivity;
import chat21.android.ui.messages.listeners.OnAttachClickListener;
import chat21.android.ui.messages.listeners.OnAttachDocumentsClickListener;
import chat21.android.ui.messages.listeners.OnMessageClickListener;

/**
 * Created by andrealeo on 04/12/17.
 */

public class ChatUI implements Serializable {

    private static final String TAG = ChatUI.class.getName();

    public static final String INTENT_BUNDLE_IS_FROM_NOTIFICATION = "INTENT_BUNDLE_IS_FROM_NOTIFICATION";
    //    public static final String INTENT_BUNDLE_CONVERSATION = "INTENT_BUNDLE_CONVERSATION";
    public static final String INTENT_BUNDLE_RECIPIENT = "INTENT_BUNDLE_RECIPIENT";
    public static final String INTENT_BUNDLE_CONTACT_FULL_NAME = "INTENT_BUNDLE_CONTACT_FULL_NAME";
    // target class to be called in listeners (such as OnProfileClickListener)
    public static final String INTENT_BUNDLE_CALLING_ACTIVITY = "INTENT_BUNDLE_CALLING_ACTIVITY";
    public static final String INTENT_BUNDLE_MESSAGE = "INTENT_BUNDLE_MESSAGE";
    public static final String INTENT_BUNDLE_GROUP = "INTENT_BUNDLE_GROUP";
    public static final String INTENT_BUNDLE_GROUP_ID = "INTENT_BUNDLE_GROUP_ID";
    public static final String INTENT_BUNDLE_PARENT_ACTIVITY = "INTENT_BUNDLE_PARENT_ACTIVITY";

    public static final String INTENT_BUNDLE_SIGNED_UP_USER = "INTENT_BUNDLE_SIGNED_UP_USER";
    public static final String INTENT_BUNDLE_SIGNED_UP_USER_EMAIL = "INTENT_BUNDLE_SIGNED_UP_USER_EMAIL";
    public static final String INTENT_BUNDLE_SIGNED_UP_USER_PASSWORD = "INTENT_BUNDLE_SIGNED_UP_USER_PASSWORD";

    // request constants
    public static final int _REQUEST_CODE_CREATE_GROUP = 100;
    public static final int _REQUEST_CODE_GROUP_ADMIN_PANEL_ACTIVITY = 200;
    public static final int REQUEST_CODE_SIGNUP_ACTIVITY = 300;

    private Context mContext;
    private OnNewConversationClickListener onNewConversationClickListener;
    private OnMessageClickListener onMessageClickListener;
    private OnAttachDocumentsClickListener onAttachDocumentsClickListener;
    private OnAttachClickListener onAttachClickListener;
    private OnContactClickListener onContactClickListener;
    private OnCreateGroupClickListener onCreateGroupClickListener;
    private boolean groupsEnabled = false;

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

    public OnAttachClickListener getOnAttachClickListener() {
        Log.d(TAG, "getOnAttachClickListener");
        return onAttachClickListener;
    }

    public void setOnAttachClickListener(OnAttachClickListener onAttachClickListener) {
        Log.d(TAG, "setOnAttachClickListener");
        this.onAttachClickListener = onAttachClickListener;
    }


    public OnContactClickListener getOnContactClickListener() {
        Log.d(TAG, "getOnContactClickListener");
        return onContactClickListener;
    }

    public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
        Log.d(TAG, "setOnContactClickListener");
        this.onContactClickListener = onContactClickListener;
    }

    public void setOnNewConversationClickListener(OnNewConversationClickListener onNewConversationClickListener) {
        Log.d(TAG, "setOnNewConversationClickListener");
        this.onNewConversationClickListener = onNewConversationClickListener;
    }

    public OnNewConversationClickListener getOnNewConversationClickListener() {
        Log.d(TAG, "getOnNewConversationClickListener");
        return onNewConversationClickListener;
    }

    public void setOnCreateGroupClickListener(OnCreateGroupClickListener onCreateGroupClickListener) {
        Log.d(TAG, "setOnCreateGroupClickListener");
        this.onCreateGroupClickListener = onCreateGroupClickListener;
    }

    public OnCreateGroupClickListener getOnCreateGroupClickListener() {
        Log.d(TAG, "getOnCreateGroupClickListener");
        return onCreateGroupClickListener;
    }

    public void enableGroups(boolean groupsEnabled) {
        this.groupsEnabled = groupsEnabled;
    }

    public boolean areGroupsEnabled() {
        return groupsEnabled;
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
    public void showDirectConversationActivity(IChatUser recipient) {

//        IChatUser iChatUser = ChatManager.getInstance().getLoggedUser();
//        // generate the conversationId
//        String conversationId = ConversationUtils.getConversationId(iChatUser.getId(), contactId);

        // launch the chat
        Intent intent = new Intent(mContext, MessageListActivity.class);
        intent.putExtra(INTENT_BUNDLE_RECIPIENT, recipient);
        intent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
        // extras to be sent in messages or in the conversation
//        intent.putExtra(Chat.INTENT_BUNDLE_EXTRAS, (Serializable) mConfiguration.getExtras());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }
}
