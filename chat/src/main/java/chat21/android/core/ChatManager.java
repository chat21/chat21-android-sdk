package chat21.android.core;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.util.List;

import chat21.android.contacts.listeners.OnContactClickListener;
import chat21.android.conversations.activities.ConversationListActivity;
import chat21.android.conversations.fragments.ConversationListFragment;
import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.messages.activites.MessageListActivity;
import chat21.android.messages.listeners.OnAttachDocumentsClickListener;
import chat21.android.messages.listeners.OnMessageClickListener;
import chat21.android.user.models.IChatUser;
import chat21.android.utils.IOUtils;

import static chat21.android.utils.DebugConstants.DEBUG_MY_PRESENCE;

/**
 * Created by stefano on 19/05/2016.
 */
public class ChatManager {
    private static final String TAG = ChatManager.class.getName();

    private static final String _SERIALIZED_CHAT_CONFIGURATION_TENANT =
            "_SERIALIZED_CHAT_CONFIGURATION_TENANT";

    private static final String _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER =
            "_SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER";

    // contact
    public static final String INTENT_BUNDLE_CONTACT_ID = "username"; // FIXME: 17/10/16 NOT EDIT
    public static final String _INTENT_BUNDLE_CONTACT = "_INTENT_BUNDLE_CONTACT";
    public static final String INTENT_BUNDLE_CONTACT_DISPLAY_NAME = "INTENT_BUNDLE_CONTACT_DISPLAY_NAME";

    // target class to be called in listeners (such as OnProfileClickListener)
    public static final String INTENT_BUNDLE_CALLING_ACTIVITY = "INTENT_BUNDLE_CALLING_ACTIVITY";

    // conversation object
    public static final String _INTENT_BUNDLE_CONVERSATION_ID = "_INTENT_BUNDLE_CONVERSATION_ID";
    public static final String _INTENT_BUNDLE_GROUP_NAME = "_INTENT_BUNDLE_GROUP_NAME";

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

    /**** conversation status ****/
    public static final int CONVERSATION_STATUS_FAILED = 0; // non andato a buon fine
    //creo una conversazione (NON chiaro) - usato solo per conversazioni di gruppo
    public static final int CONVERSATION_STATUS_JUST_CREATED = 1;
    // la conversazione contiene l'ultimo messaggio inviato
    public static final int CONVERSATION_STATUS_LAST_MESSAGE = 2;

    private static ChatManager mInstance;

    // bugfix Issue #16
    private static String mPresenceDeviceInstance;

    private Context mContext;

    private List<IChatUser> mContacts;

    private OnMessageClickListener onMessageClickListener;
    private OnAttachDocumentsClickListener onAttachDocumentsClickListener;
    private OnContactClickListener onContactClickListener;

    // private constructor
    private ChatManager() {
    }

    // bugfix Issue #16
    public static void setPresenceDeviceInstance(String presenceDeviceInstance) {
        mPresenceDeviceInstance = presenceDeviceInstance;
        Log.i(DEBUG_MY_PRESENCE, "Chat.setPresenceDeviceInstance");
    }

    // bugfix Issue #16
    public static String getPresenceDeviceInstance() {
        Log.i(DEBUG_MY_PRESENCE, "Chat.getPresenceDeviceInstance");
        return mPresenceDeviceInstance;
    }

    public IChatUser getLoggedUser() {
        IChatUser loggedUser = (IChatUser) IOUtils.getObjectFromFile(mContext, _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER);

        if (loggedUser != null)
            Log.d(TAG, "serialized_logged_user: " + loggedUser.toString());
        else
            Log.d(TAG, "serialized_logged_user is null");

        return loggedUser;
    }

    public String getTenant() {
        Log.d(TAG, "getTenant");

        String tenant = (String) IOUtils.getObjectFromFile(mContext, _SERIALIZED_CHAT_CONFIGURATION_TENANT);

        Log.d(TAG, "serialize_tenant: " + tenant);

        return tenant;
    }

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

    private void setContext(Context context) {
        mContext = context;
    }


    /**
     * It initializes the SDK.
     * It serializes the current user.
     * It serializes the configurations.
     *
     * @param context
     * @param configuration
     * @param currentUser
     */
    public static void start(Context context, Configuration configuration, IChatUser currentUser) {
        Log.i(TAG, "Chat.start");

//        // multidex support
//        // source :
//        // https://forums.xamarin.com/discussion/64234/multi-dex-app-with-a-custom-application-class-that-runs-on-pre-lollipop
//        MultiDex.install(context);

        // create a new chat
        ChatManager chat = new ChatManager(); // create the instance of the chat

        chat.setContext(context);

        mInstance = chat;

        // This line needs to be executed before any usage of EmojiTextView, EmojiEditText or EmojiButton.
        EmojiManager.install(new IosEmojiProvider());

        // serialize the current user
        IOUtils.saveObjectToFile(context, _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER, currentUser);

        // serialize the tenant
        IOUtils.saveObjectToFile(context, _SERIALIZED_CHAT_CONFIGURATION_TENANT, configuration.appId);
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

        // generate the conversationId
        String conversationId = ConversationUtils.getConversationId(getLoggedUser().getId(), contactId);

        // launch the chat
        Intent intent = new Intent(mContext, MessageListActivity.class);
        intent.putExtra(ChatManager._INTENT_BUNDLE_CONVERSATION_ID, conversationId);
        intent.putExtra(ChatManager.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
        // extras to be sent in messages or in the conversation
//        intent.putExtra(Chat.INTENT_BUNDLE_EXTRAS, (Serializable) mConfiguration.getExtras());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * Return the instance of the Chat
     *
     * @return the instance
     */
    public static ChatManager getInstance() {
        Log.d(TAG, "getInstance");
        if (mInstance == null) {
            throw new RuntimeException("instance cannot be null. call start first.");
        }
        return mInstance;
    }

    public void setContacts(List<IChatUser> contacts) {
        mContacts = contacts;
    }

    public List<IChatUser> getContacts() {
        return mContacts;
    }





//start configuration

    public static final class Configuration {

        private static final String TAG = Configuration.class.getName();

        public static String appId;
        public String firebaseUrl;
        public String storageBucket;

        public Configuration(Builder builder) {
            Log.d(TAG, ">>>>>> Configuration <<<<<<");

            this.appId = builder.mAppId;
            this.firebaseUrl = builder.mFirebaseUrl;
            this.storageBucket = builder.mStorageBucket;
        }

        /**
         * Creates a configuration object
         */
        public static final class Builder {
            private static final String TAG = Builder.class.getName();

            private String mAppId;
            private String mFirebaseUrl;
            private String mStorageBucket;

            public Builder(String appId) {
                Log.d(TAG, "Configuration.Builder: appId = " + appId);

                mAppId = appId;
            }

            public Builder firebaseUrl(String firebaseUrl) {
                Log.d(TAG, "Configuration.Builder.firebaseUrl: firebaseUrl = " + firebaseUrl);

                mFirebaseUrl = firebaseUrl;

                return this;
            }

            public Builder storageBucket(String storageBucket) {
                Log.d(TAG, "Configuration.Builder.storageReference: storageBucket = " + storageBucket);

                mStorageBucket = storageBucket;

                return this;
            }

            public Configuration build() {
                Log.d(TAG, "Configuration.build");

                return new Configuration(this);
            }
        }
    }
//end configuration


}