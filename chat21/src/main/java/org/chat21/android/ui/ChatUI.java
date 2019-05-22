package org.chat21.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.chat21.android.core.ChatManager;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.contacts.activites.ContactListActivity;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;
import org.chat21.android.ui.contacts.listeners.OnCreateGroupClickListener;
import org.chat21.android.ui.conversations.activities.ConversationListActivity;
import org.chat21.android.ui.conversations.fragments.ConversationListFragment;
import org.chat21.android.ui.conversations.listeners.OnNewConversationClickListener;
import org.chat21.android.ui.messages.activities.MessageListActivity;
import org.chat21.android.ui.messages.listeners.OnAttachClickListener;
import org.chat21.android.ui.messages.listeners.OnAttachDocumentsClickListener;
import org.chat21.android.ui.messages.listeners.OnMessageClickListener;

import java.io.Serializable;

/**
 * Created by andrealeo on 04/12/17.
 */

public class ChatUI implements Serializable {

    private static final String TAG = ChatUI.class.getName();

    public static final String BUNDLE_RECIPIENT = "BUNDLE_RECIPIENT";
    // target class to be called in listeners (such as OnProfileClickListener)
    public static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
    public static final String BUNDLE_CHAT_GROUP = "BUNDLE_CHAT_GROUP";
    public static final String BUNDLE_GROUP_ID = "BUNDLE_GROUP_ID";
    public static final String BUNDLE_CHANNEL_TYPE = "BUNDLE_CHANNEL_TYPE";

    public static final String BUNDLE_SIGNED_UP_USER_EMAIL = "BUNDLE_SIGNED_UP_USER_EMAIL";
    public static final String BUNDLE_SIGNED_UP_USER_PASSWORD = "BUNDLE_SIGNED_UP_USER_PASSWORD";

    // request constants
    public static final int REQUEST_CODE_CREATE_GROUP = 100;
    public static final int REQUEST_CODE_SIGNUP_ACTIVITY = 200;

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

        //set the default mContext value equals to ChatManager.getInstance().getContext() Use ChatUI.getIntance().setContext to use another context
        mContext = ChatManager.getInstance().getContext();

        // This line needs to be executed before any usage of EmojiTextView, EmojiEditText or EmojiButton.
        // EmojiManager.install(new IosEmojiProvider());
        EmojiManager.install(new IosEmojiProvider());

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
        Log.d(TAG, "getOnChatGroupClickListener");
        return onContactClickListener;
    }

    public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
        Log.d(TAG, "setOnChatGroupClickListener");
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
        // launch the chat
        Intent intent = new Intent(mContext, MessageListActivity.class);
        intent.putExtra(BUNDLE_RECIPIENT, recipient);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public void openChatWithGroup(String groupId, String groupName) {
        openChatWithGroup(new ChatUser(groupId, groupName));
    }

    public void openChatWithGroup(IChatUser group) {
        Intent intent = new Intent(mContext, MessageListActivity.class);
        intent.putExtra(BUNDLE_RECIPIENT, group);
        intent.putExtra(BUNDLE_CHANNEL_TYPE, Message.GROUP_CHANNEL_TYPE);
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
//        key == google.sent_time   	values == 1518088363536
//        key == google.ttl   		    values == 2419200
//        key == sender 				values == mVFL2ncvuhNYwlFHXhg0AZAioav2
//        key == sender_fullname		values == Sundar Pichai
//        key == channel_type			values == group
//        key == from					values == 77360455507
//        key == text					values == test background 2
//        key == timestamp			    values == 1518088363486
//        key == google.message_id	    values == 0:1518088363542657%90a182f990a182f9
//        key == recipient_fullname	    values == ChromeOS recap
//        key == recipient 			    values == -L4omVgWKAqZOAkJQpLe
//        key == collapse_key 		    values == chat21.android.demo

        Bundle extras = notificationIntent.getExtras();
        if (extras != null) {
//            for (String key : extras.keySet()) {
//                Log.d(DEBUG_NOTIFICATION, "ChatUI.processRemoteNotification:" +
//                        " key == " + key + " - values == " + extras.get(key).toString());
//            }

            if (extras.containsKey("channel_type")) {
                String channel = extras.getString("channel_type");

                if (channel.equals(Message.DIRECT_CHANNEL_TYPE)) {
                    processDirectNotification(extras, channel);
                } else if (channel.equals(Message.GROUP_CHANNEL_TYPE)) {
                    processGroupNotification(extras, channel);
                } else {
                    // default case
                    processDirectNotification(extras, channel);
                }
            }
        }
    }

    private void processDirectNotification(Bundle extras, String channel) {
        if (extras.containsKey("sender") && extras.containsKey("sender_fullname")) {
            String sender = extras.getString("sender");
            String senderFullName = extras.getString("sender_fullname");
            IChatUser contact = new ChatUser(sender, senderFullName);

            startMessageListActivity(contact, channel);
        }
    }

    private void processGroupNotification(Bundle extras, String channel) {
        if (extras.containsKey("recipient") && extras.containsKey("recipient_fullname")) {
            String recipient = extras.getString("recipient");
            String recipientFullName = extras.getString("recipient_fullname");
            IChatUser contact = new ChatUser(recipient, recipientFullName);

            startMessageListActivity(contact, channel);
        }
    }

    private void startMessageListActivity(IChatUser contact, String channel) {
        Intent intent = new Intent(mContext, MessageListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, contact);
        intent.putExtra(BUNDLE_CHANNEL_TYPE, channel);
        mContext.startActivity(intent);
    }
}
