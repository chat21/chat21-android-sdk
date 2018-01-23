package chat21.android.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import chat21.android.core.ChatManager;
import chat21.android.core.users.models.ChatUser;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.contacts.activites.ContactListActivity;
import chat21.android.ui.contacts.listeners.OnContactClickListener;
import chat21.android.ui.contacts.listeners.OnCreateGroupClickListener;
import chat21.android.ui.conversations.activities.ConversationListActivity;
import chat21.android.ui.conversations.fragments.ConversationListFragment;
import chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import chat21.android.ui.messages.activities.MessageListActivity;
import chat21.android.ui.messages.listeners.OnAttachClickListener;
import chat21.android.ui.messages.listeners.OnAttachDocumentsClickListener;
import chat21.android.ui.messages.listeners.OnMessageClickListener;
import chat21.android.utils.StringUtils;

import static chat21.android.utils.DebugConstants.DEBUG_NOTIFICATION;

/**
 * Created by andrealeo on 04/12/17.
 */

public class ChatUI implements Serializable {

    private static final String TAG = ChatUI.class.getName();

    public static final String BUNDLE_IS_FROM_NOTIFICATION = "BUNDLE_IS_FROM_NOTIFICATION";
    public static final String BUNDLE_RECIPIENT = "BUNDLE_RECIPIENT";
    // target class to be called in listeners (such as OnProfileClickListener)
    public static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
    public static final String BUNDLE_GROUP = "BUNDLE_GROUP";
    public static final String BUNDLE_GROUP_ID = "BUNDLE_GROUP_ID";
    public static final String BUNDLE_PARENT_ACTIVITY = "BUNDLE_PARENT_ACTIVITY";
    public static final String BUNDLE_CHANNEL_TYPE = "BUNDLE_CHANNEL_TYPE";

    public static final String BUNDLE_SIGNED_UP_USER_EMAIL = "BUNDLE_SIGNED_UP_USER_EMAIL";
    public static final String BUNDLE_SIGNED_UP_USER_PASSWORD = "BUNDLE_SIGNED_UP_USER_PASSWORD";

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

        //set the default mContext value equals to ChatManager.getInstance(). Use ChatUI.getIntance().setContext to use another context
        mContext = ChatManager.getInstance().getContext();


        //default init for onNewConversationClickListener
        setDefaultOnNewConversationClickListener();

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
    // What is???
//    protected ChatUI readResolve() {
//        return getInstance();
//    }
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

    public void setDefaultOnNewConversationClickListener() {
        this.onNewConversationClickListener = new OnNewConversationClickListener() {
            @Override
            public void onNewConversationClicked() {

                Intent intent = new Intent(mContext, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context

                mContext.startActivity(intent);

            }
        };
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

    public void openConversationsListFragment(FragmentManager fragmentManager,
                                              @IdRes int containerId) {
        Fragment fragment = ConversationListFragment.newInstance();
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(containerId, fragment)
                    .commitAllowingStateLoss();
        }
    }

    public void openConversationsListActivity() {
        Intent intent = new Intent(mContext, ConversationListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    public void openConversationMessagesActivity(String recipientId, String recipientFullName) {
        this.openConversationMessagesActivity(new ChatUser(recipientId, recipientFullName));
    }

    // TODO: 24/11/17 showChatWith(user)
    // TODO: 24/11/17 add extras here
    public void openConversationMessagesActivity(IChatUser recipient) {

//        IChatUser iChatUser = ChatManager.getInstance().getLoggedUser();
//        // generate the conversationId
//        String conversationId = ConversationUtils.getConversationId(iChatUser.getId(), contactId);

        // launch the chat
        Intent intent = new Intent(mContext, MessageListActivity.class);
        intent.putExtra(BUNDLE_RECIPIENT, recipient);
        intent.putExtra(ChatUI.BUNDLE_IS_FROM_NOTIFICATION, false);
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

    public void processRemoteNotification(Intent notificationIntent) {
        Log.d(DEBUG_NOTIFICATION, "ChatUI.processRemoteNotification: notificationIntent == " + notificationIntent.toString());

        if (StringUtils.isValid(notificationIntent.getStringExtra("sender")) &&
                StringUtils.isValid(notificationIntent.getStringExtra("sender_fullname"))) {
            String contactId = notificationIntent.getStringExtra("sender");
            Log.d(DEBUG_NOTIFICATION, "ChatUI.processRemoteNotification: contactId == " + contactId);

            String contactFullName = notificationIntent.getStringExtra("sender_fullname");
            Log.d(DEBUG_NOTIFICATION, "ChatUI.processRemoteNotification: contactFullName == " + contactFullName);

            String channelType = notificationIntent.getStringExtra("channel_type");

            // create the recipient from background notification data
            IChatUser recipient = new ChatUser(contactId, contactFullName);

            Intent intent = new Intent(mContext, MessageListActivity.class);
            intent.putExtra(BUNDLE_RECIPIENT, recipient);
            intent.putExtra(BUNDLE_IS_FROM_NOTIFICATION, true);
            intent.putExtra(BUNDLE_CHANNEL_TYPE, channelType);
            // start from outside of an activity context
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            // clear activity stack
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);

        }
    }
}
