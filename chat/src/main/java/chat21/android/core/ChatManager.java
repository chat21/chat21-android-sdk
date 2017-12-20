package chat21.android.core;

import android.content.Context;
import android.util.Log;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat21.android.core.conversations.ConversationsHandler;
import chat21.android.core.conversations.listeners.ConversationsListener;
import chat21.android.core.messages.handlers.ConversationMessagesHandler;
import chat21.android.core.messages.listeners.SendMessageListener;
import chat21.android.core.messages.models.Message;
import chat21.android.core.users.models.IChatUser;
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


    private static ChatManager mInstance;

    // bugfix Issue #16
    private static String mPresenceDeviceInstance;

    private Context mContext;

    private List<IChatUser> mContacts;

    private Map<String, ConversationMessagesHandler> conversationMessagesHandlerMap;

    private ConversationsHandler conversationsHandler;


    // private constructor
    private ChatManager() {
        conversationMessagesHandlerMap = new HashMap<String, ConversationMessagesHandler>();
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

//
//    public ConversationsHandler addConversationsListener(ConversationsListener conversationsListener) {
////        ConversationsHandler conversationsHandler = new ConversationsHandler(
//        conversationsHandler = new ConversationsHandler(
//                Configuration.firebaseUrl, this.getTenant(), this.getLoggedUser().getId()
////                , conversationMessagesListener
//        );
//
//        conversationsHandler.connect(conversationsListener);
//
//        return conversationsHandler;
//    }

    public ConversationMessagesHandler getConversationMessagesHandler(String recipientId) {
        if (conversationMessagesHandlerMap.containsKey(recipientId)) {
            return conversationMessagesHandlerMap.get(recipientId);
        } else {
            ConversationMessagesHandler messageHandler = new ConversationMessagesHandler(
                    Configuration.firebaseUrl, recipientId, this.getTenant(), this.getLoggedUser().getId());

            conversationMessagesHandlerMap.put(recipientId, messageHandler);

            return messageHandler;
        }
    }

    public ConversationsHandler getConversationsHandler() {
        if (conversationsHandler != null) {
            return conversationsHandler;
        } else {
            conversationsHandler =
                    new ConversationsHandler(Configuration.firebaseUrl, this.getTenant(), this.getLoggedUser().getId());
            return conversationsHandler;
        }
    }

//    public void addConversationMessagesListener(String recipientId, ConversationMessagesListener conversationMessagesListener){
//
//        ConversationMessagesHandler messageHandler = new ConversationMessagesHandler(
//                Configuration.firebaseUrl, recipientId, this.getTenant(), this.getLoggedUser().getId()
////                , conversationMessagesListener
//        );
//
//        messageHandler.connect(conversationMessagesListener);
//    }

    public void sendTextMessage(String recipient_id, String text, Map customAttributes, SendMessageListener sendMessageListener) {


        getConversationMessagesHandler(recipient_id).sendMessage(getLoggedUser().getId(),
                getLoggedUser().getFullName(),
                Message.TYPE_TEXT, text, customAttributes, sendMessageListener);
    }

    public void sendFileMessage(String recipient_id, String text, URL url, String fileName, Map customAttributes, SendMessageListener sendMessageListener) {

    }


//start configuration

    public static final class Configuration {

        private static final String TAG = Configuration.class.getName();

        public static String appId;
        public static String firebaseUrl;
        public static String storageBucket;

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