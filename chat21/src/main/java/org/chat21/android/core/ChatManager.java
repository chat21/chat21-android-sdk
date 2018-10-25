package org.chat21.android.core;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.chat21.android.core.authentication.ChatAuthentication;
import org.chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import org.chat21.android.core.contacts.listeners.OnContactCreatedCallback;
import org.chat21.android.core.contacts.synchronizers.ContactsSynchronizer;
import org.chat21.android.core.conversations.ArchivedConversationsHandler;
import org.chat21.android.core.conversations.ConversationsHandler;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.messages.handlers.ConversationMessagesHandler;
import org.chat21.android.core.messages.listeners.SendMessageListener;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.presence.MyPresenceHandler;
import org.chat21.android.core.presence.PresenceHandler;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.utils.IOUtils;
import org.chat21.android.utils.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.chat21.android.utils.DebugConstants.DEBUG_LOGIN;

/**
 * Created by stefano on 19/05/2016.
 */
public class ChatManager {
    private static final String TAG = ChatManager.class.getName();
    private static final String TAG_TOKEN = "TAG_TOKEN";

    private static final String _SERIALIZED_CHAT_CONFIGURATION_TENANT =
            "_SERIALIZED_CHAT_CONFIGURATION_TENANT";

    public static final String _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER =
            "_SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER";

    public static final String _DEFAULT_APP_ID_VALUE = "default";

    private static ChatManager mInstance;

    private IChatUser loggedUser;
    private String appId;
    private Context mContext;

    private Map<String, ConversationMessagesHandler> conversationMessagesHandlerMap;
    private ConversationsHandler conversationsHandler;
    private ArchivedConversationsHandler archivedConversationsHandler;
    private MyPresenceHandler myPresenceHandler;
    private Map<String, PresenceHandler> presenceHandlerMap;

    private ContactsSynchronizer contactsSynchronizer;
    private GroupsSyncronizer groupsSyncronizer;

    // private constructor
    private ChatManager() {
        conversationMessagesHandlerMap = new HashMap<String, ConversationMessagesHandler>();

        presenceHandlerMap = new HashMap<>();
    }

    public void setLoggedUser(IChatUser loggedUser) {
        this.loggedUser = loggedUser;
        Log.d(TAG, "ChatManager.setloggedUser: loggedUser == " + loggedUser.toString());
        // serialize on disk
        IOUtils.saveObjectToFile(mContext, _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER, loggedUser);
    }

    public IChatUser getLoggedUser() {
//        Log.v(TAG, "ChatManager.getloggedUser");
        return loggedUser;
    }

    public boolean isUserLogged() {
        Log.d(TAG, "ChatManager.isUserLogged");
        boolean isUserLogged = getLoggedUser() != null ? true : false;
        Log.d(TAG, "ChatManager.isUserLogged: isUserLogged == " + isUserLogged);
        return isUserLogged;
    }

    public String getAppId() {
        Log.d(TAG, "getAppId");

        return this.appId;
    }

    private void setContext(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void createContactFor(String uid, String email,
                                 String firstName, String lastName,
                                 final OnContactCreatedCallback callback) {
        final Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("email", email);
        user.put("firstname", firstName);
        user.put("lastname", lastName);
        user.put("timestamp", new Date().getTime());
        user.put("imageurl", "");

        DatabaseReference contactsNode;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
        } else {
            contactsNode = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("/apps/" + ChatManager.Configuration.appId + "/contacts");
        }

        // save the user on contacts node
        contactsNode.child(uid).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    callback.onContactCreatedSuccess(null);
                } else {
                    callback.onContactCreatedSuccess(new ChatRuntimeException(databaseError.toException()));
                }
            }
        });
    }


    /**
     * It initializes the SDK Anonymously using  DEFAULT appId.
     *
     * @param loginActivity
     */
    public static void startAnonymously(final Activity loginActivity, final ChatAuthentication.OnChatLoginCallback onChatLoginCallback) {
        startAnonymously(loginActivity, _DEFAULT_APP_ID_VALUE, onChatLoginCallback);
    }

    /**
     * It initializes the SDK Anonymously using DEFAULT appId.
     *
     * @param loginActivity
     * @param appId
     */
    public static void startAnonymously(final Activity loginActivity, final String appId, final ChatAuthentication.OnChatLoginCallback onChatLoginCallback) {

        //getting context from loginActivity
        final Context context = loginActivity.getApplicationContext();

        ChatAuthentication.getInstance().signInAnonymously(loginActivity,
                new ChatAuthentication.OnChatLoginCallback() {
                    @Override
                    public void onChatLoginSuccess(IChatUser currentUser) {

                        start(context, appId, currentUser);

                        Log.i(TAG, "chat has been initialized with success");

                        onChatLoginCallback.onChatLoginSuccess(currentUser);
                    }

                    @Override
                    public void onChatLoginError(Exception e) {
                        Log.e(TAG, "onChatLoginError", e);
                        onChatLoginCallback.onChatLoginError(e);

                    }
                });
    }

    public static void startWithEmailAndPassword(
            final Activity loginActivity,
            final String appId,
            String email, String password,
            final ChatAuthentication.OnChatLoginCallback onChatLoginCallback) {

        //getting context from loginActivity
        final Context context = loginActivity.getApplicationContext();

        ChatAuthentication.getInstance()
                .signInWithEmailAndPassword(loginActivity, email, password,
                        new ChatAuthentication.OnChatLoginCallback() {
                            @Override
                            public void onChatLoginSuccess(IChatUser currentUser) {

                                start(context, appId, currentUser);

                                Log.i(TAG, "chat has been initialized with success");

                                onChatLoginCallback.onChatLoginSuccess(currentUser);
                            }

                            @Override
                            public void onChatLoginError(Exception e) {
                                Log.e(TAG, "onChatLoginError", e);
                                onChatLoginCallback.onChatLoginError(e);

                            }
                        });
    }

    /**
     * It initializes the SDK using DEFAULT appId and a current user.
     * It serializes the current user.
     * It serializes the configurations.
     *
     * @param context
     * @param currentUser
     */
    public static void start(Context context, IChatUser currentUser) {
        start(context, _DEFAULT_APP_ID_VALUE, currentUser);
    }

    /**
     * It initializes the SDK specifing appId and the currentUser
     * It serializes the current user.
     * It serializes the configurations.
     *
     * @param context
     * @param appId
     * @param currentUser
     */
    public static void start(Context context, String appId, IChatUser currentUser) {

        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(appId).build();

        start(context, mChatConfiguration, currentUser);
    }

    /**
     * It initializes the SDK passing a configuration object and the current user.
     * It serializes the current user.
     * It serializes the configurations.
     *
     * @param context
     * @param configuration
     * @param currentUser
     */
    public static void start(Context context, Configuration configuration, IChatUser currentUser) {
        Log.i(TAG, "starting");

//        // multidex support
//        // source :
//        // https://forums.xamarin.com/discussion/64234/multi-dex-app-with-a-custom-application-class-that-runs-on-pre-lollipop
//        MultiDex.install(context);

        // create a new chat
        ChatManager chat = new ChatManager(); // create the instance of the chat

        chat.setContext(context);

        mInstance = chat;

        // TODO: 16/01/18 move the emoji provider to chatUI
        // This line needs to be executed before any usage of EmojiTextView, EmojiEditText or EmojiButton.
//        EmojiManager.install(new IosEmojiProvider());
        EmojiManager.install(new IosEmojiProvider());

//        chat.loggedUser = currentUser;
        // serialize the current user
//        IOUtils.saveObjectToFile(context, _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER, currentUser);
        chat.setLoggedUser(currentUser);

        chat.appId = configuration.appId;

        // serialize the appId
        IOUtils.saveObjectToFile(context, _SERIALIZED_CHAT_CONFIGURATION_TENANT, configuration.appId);

        chat.initContactsSyncronizer();

        chat.initGroupsSyncronizer();
    }

    public void initContactsSyncronizer() {
        this.contactsSynchronizer = getContactsSynchronizer();
        this.contactsSynchronizer.connect();
    }

    private void initGroupsSyncronizer() {
        this.groupsSyncronizer = getGroupsSyncronizer();
        this.groupsSyncronizer.connect();
    }

    public void dispose() {

        // dispose myPresenceHandler
        if (myPresenceHandler!=null) {
            myPresenceHandler.dispose(); // disconnect all listeners
        }
        myPresenceHandler = null; // destroy it

        // dispose all presenceHandlerMap
        for (Map.Entry<String, PresenceHandler> entry : presenceHandlerMap.entrySet()) {

            String recipientId = entry.getKey();
            PresenceHandler presenceHandler = entry.getValue();

            presenceHandler.disconnect();
            Log.d(TAG, "presenceHandler for recipientId: " + recipientId + " disposed");
        }

        //dispose conversationsHandler
        if (this.conversationsHandler!=null){
            this.conversationsHandler.disconnect();
        }
        this.conversationsHandler = null;

        //dispose archivedConversationsHandler
        if (this.archivedConversationsHandler!=null) {   //can be already null
            this.archivedConversationsHandler.disconnect();
        }
        this.archivedConversationsHandler = null;

        //dispose all conversationMessagesHandlerMap
        for (Map.Entry<String, ConversationMessagesHandler> entry : conversationMessagesHandlerMap.entrySet()) {

            String recipientId = entry.getKey();
            ConversationMessagesHandler conversationMessagesHandler = entry.getValue();

            conversationMessagesHandler.disconnect();
            Log.d(TAG, "conversationMessagesHandler for recipientId: " + recipientId + " disposed");
        }

        // dispose contactsSynchonizer
        if (contactsSynchronizer != null) {
            this.contactsSynchronizer.disconnect();
        }
        this.contactsSynchronizer = null;

        // dispose groupsSyncronizer
        if (groupsSyncronizer != null) {
            this.groupsSyncronizer.removeAllGroupsListeners();
            this.groupsSyncronizer.disconnect();
        }
        this.groupsSyncronizer = null;

        deleteInstanceId();

        removeLoggedUser();
    }

//    private void deleteInstanceId() {
//
//        DatabaseReference root;
//        if (StringUtils.isValid(Configuration.firebaseUrl)) {
//            root = FirebaseDatabase.getInstance().getReferenceFromUrl(Configuration.firebaseUrl);
//        } else {
//            root = FirebaseDatabase.getInstance().getReference();
//        }
//
//        // remove the instanceId for the logged user
//        DatabaseReference firebaseUsersPath = root
//                .child("apps/" + ChatManager.Configuration.appId + "/users/" +
//                        loggedUser.getId() + "/instanceId");
//        firebaseUsersPath.removeValue();
//
//        try {
//            FirebaseInstanceId.getInstance().deleteInstanceId();
//        } catch (IOException e) {
//            Log.e(DEBUG_LOGIN, "cannot delete instanceId. " + e.getMessage());
//            return;
//        }
//    }

    private void deleteInstanceId() {

        DatabaseReference root;
        if (StringUtils.isValid(Configuration.firebaseUrl)) {
            root = FirebaseDatabase.getInstance().getReferenceFromUrl(Configuration.firebaseUrl);
        } else {
            root = FirebaseDatabase.getInstance().getReference();
        }

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG_TOKEN, "ChatManager.deleteInstanceId: token ==  " + token);

        // remove the instanceId for the logged user
        DatabaseReference firebaseUsersPath = root
                .child("apps/" + ChatManager.Configuration.appId + "/users/" +
                        loggedUser.getId() + "/instances/" + token);
        firebaseUsersPath.removeValue();

        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            Log.e(DEBUG_LOGIN, "cannot delete instanceId. " + e.getMessage());
            return;
        }
    }

    private void removeLoggedUser() {
        // clear all logged user data
        IOUtils.deleteObject(mContext, _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER);
    }

    /**
     * Return the instance of the Chat
     *
     * @return the instance
     */
    public static ChatManager getInstance() {
//        Log.v(TAG, "getInstance");
        if (mInstance == null) {
            throw new RuntimeException("instance cannot be null. call start first.");
        }
        return mInstance;
    }

//
//    public ConversationsHandler addGroupsListener(ConversationsListener conversationsListener) {
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

    public ConversationMessagesHandler getConversationMessagesHandler(String recipientId, String recipientFullName) {

        IChatUser chatUser = new ChatUser(recipientId, recipientFullName);

        return getConversationMessagesHandler(chatUser);
    }

    public ConversationMessagesHandler getConversationMessagesHandler(IChatUser recipient) {
        String recipientId = recipient.getId();
        Log.d(TAG, "Getting ConversationMessagesHandler for recipientId " + recipientId);

        if (conversationMessagesHandlerMap.containsKey(recipientId)) {
            Log.i(TAG, "ConversationMessagesHandler for recipientId " + recipientId + " already inizialized. Return it");

            return conversationMessagesHandlerMap.get(recipientId);
        } else {
            ConversationMessagesHandler messageHandler = new ConversationMessagesHandler(
                    Configuration.firebaseUrl, this.getAppId(), this.getLoggedUser(), recipient);
            conversationMessagesHandlerMap.put(recipientId, messageHandler);

            Log.i(TAG, "ConversationMessagesHandler for recipientId " + recipientId + " created.");

            return messageHandler;
        }
    }

    public PresenceHandler getPresenceHandler(String recipientId) {
        Log.d(TAG, "Getting PresenceHandler for recipientId " + recipientId);

        if (presenceHandlerMap.containsKey(recipientId)) {
            Log.i(TAG, "PresenceHandler for recipientId " + recipientId + " already inizialized. Return it");

            return presenceHandlerMap.get(recipientId);
        } else {
            PresenceHandler presenceHandler =
                    new PresenceHandler(Configuration.firebaseUrl, this.getAppId(), recipientId);

            presenceHandlerMap.put(recipientId, presenceHandler);

            Log.i(TAG, "PresenceHandler for recipientId " + recipientId + " created.");

            return presenceHandler;
        }
    }

    public ConversationsHandler getConversationsHandler() {
        if (this.conversationsHandler != null) {
            return this.conversationsHandler;
        } else {
            this.conversationsHandler = new ConversationsHandler(Configuration.firebaseUrl,
                    this.getAppId(), this.getLoggedUser().getId());
            return conversationsHandler;
        }
    }

  public ArchivedConversationsHandler getArchivedConversationsHandler() {
        if (this.archivedConversationsHandler != null) {
            return this.archivedConversationsHandler;
        } else {
            this.archivedConversationsHandler = new ArchivedConversationsHandler(Configuration.firebaseUrl,
                    this.getAppId(), this.getLoggedUser().getId());
            return archivedConversationsHandler;
        }
    }

    public ContactsSynchronizer getContactsSynchronizer() {
        if (this.contactsSynchronizer != null) {
            return this.contactsSynchronizer;
        } else {
            this.contactsSynchronizer =
                    new ContactsSynchronizer(Configuration.firebaseUrl, this.getAppId());
            return this.contactsSynchronizer;
        }
    }

    public GroupsSyncronizer getGroupsSyncronizer() {
        if (this.groupsSyncronizer != null) {
            return this.groupsSyncronizer;
        } else {
            this.groupsSyncronizer =
                    new GroupsSyncronizer(Configuration.firebaseUrl, this.getAppId(), loggedUser.getId());
            return this.groupsSyncronizer;
        }
    }

    public MyPresenceHandler getMyPresenceHandler() {
        if (this.myPresenceHandler != null) {
            return this.myPresenceHandler;
        } else {
            this.myPresenceHandler = new MyPresenceHandler(Configuration.firebaseUrl,
                    getAppId(), getLoggedUser().getId());
            return myPresenceHandler;
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
//
//    public void sendTextMessage(String recipient_id, String text, Map customAttributes, SendMessageListener sendMessageListener) {
//
//        Log.d(TAG, "sending text message to recipientId : " + recipient_id + " with text : " + text + " and customAttributes : " + customAttributes);
//
//        getConversationMessagesHandler(recipient_id).sendMessage(
//                "fullnameDAELIMINAREANDROID",
//                Message.TYPE_TEXT, text, customAttributes, sendMessageListener);
//    }

    public void sendTextMessage(String recipientId, String recipientFullName, String text) {
        sendTextMessage(recipientId, recipientFullName, text,
                null, null);
    }

    public void sendTextMessage(String recipientId, String recipientFullName, String text, String typeChannel) {
        sendTextMessage(recipientId, recipientFullName, text,
                typeChannel, null);
    }

    public void sendTextMessage(String recipientId, String recipientFullName, String text, String channelType,
                                SendMessageListener sendMessageListener) {
        sendTextMessage(recipientId, recipientFullName,
                text, null, sendMessageListener);
    }

    public void sendTextMessage(String recipientId, String recipientFullName, String text, String channelType,
                                Map metadata, SendMessageListener sendMessageListener) {

        Log.d(TAG, "sending text message to recipientId : " + recipientId +
                ", recipientFullName: " + recipientFullName + " with text : " +
                text + " and metadata : " + metadata);

        getConversationMessagesHandler(recipientId, recipientFullName).sendMessage(
                Message.TYPE_TEXT, text, channelType, metadata, sendMessageListener);
    }

    public void sendImageMessage(String recipientId, String recipientFullName, String text, String channelType,
                                 Map metadata, SendMessageListener sendMessageListener) {

        Log.d(TAG, "sending image message to recipientId : " + recipientId +
                ", recipientFullName: " + recipientFullName + " with text : " +
                text + " and metadata : " + metadata);

        getConversationMessagesHandler(recipientId, recipientFullName).sendMessage(
                Message.TYPE_IMAGE, text, channelType, metadata, sendMessageListener);
    }

    public void sendFileMessage(String recipient_id, String text, String channelType, URL url, String fileName,
                                Map metadata, SendMessageListener sendMessageListener) {

    }


//start configuration

    public static final class Configuration {

        private static final String TAG = Configuration.class.getName();

        public static String appId;
        public static String firebaseUrl;
        public static String storageBucket;

        public Configuration(Builder builder) {
            Log.v(TAG, "Configuration constructor called");

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