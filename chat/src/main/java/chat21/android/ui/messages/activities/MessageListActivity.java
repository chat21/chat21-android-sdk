package chat21.android.ui.messages.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiImageView;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import java.io.File;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.messages.handlers.ConversationMessagesHandler;
import chat21.android.core.messages.listeners.ConversationMessagesListener;
import chat21.android.core.messages.listeners.SendMessageListener;
import chat21.android.core.messages.models.Message;
import chat21.android.core.presence.PresenceHandler;
import chat21.android.core.presence.listeners.PresenceListener;
import chat21.android.core.users.models.ChatUser;
import chat21.android.core.users.models.IChatUser;
import chat21.android.storage.OnUploadedCallback;
import chat21.android.storage.StorageHandler;
import chat21.android.ui.ChatUI;
import chat21.android.ui.messages.adapters.MessageListAdapter;
import chat21.android.ui.messages.fragments.BottomSheetAttach;
import chat21.android.ui.messages.listeners.OnMessageClickListener;
import chat21.android.ui.users.activities.PublicProfileActivity;
import chat21.android.utils.ChatUtils;
import chat21.android.utils.StringUtils;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.image.CropCircleTransformation;

import static chat21.android.utils.DebugConstants.DEBUG_NOTIFICATION;
import static chat21.android.utils.DebugConstants.DEBUG_USER_PRESENCE;

/**
 * Created by stefano on 31/08/2015.
 */
public class MessageListActivity extends AppCompatActivity implements ConversationMessagesListener, PresenceListener {
    private static final String TAG = MessageListActivity.class.getName();

    public static final int _INTENT_ACTION_GET_PICTURE = 853;

    private PresenceHandler presenceHandler;
    private ConversationMessagesHandler conversationMessagesHandler;
    private boolean conversWithOnline = false;
    private long conversWithLastOnline = -1;

//    // check if this activity is called from a background notification
//    private boolean isFromBackgroundNotification = false;
//    // check if this activity is called from a foreground notification
//    private boolean isFromForegroundNotification = false;

    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MessageListAdapter messageListAdapter;
    private Toolbar toolbar;

    private ImageView mPictureView;
    private TextView mTitleTextView;
    private TextView mSubTitleTextView;
    private RelativeLayout mNoMessageLayout;

    private EmojiPopup emojiPopup;
    private EmojiEditText editText;
    private ViewGroup rootView;
    private ImageView emojiButton;
    private ImageView attachButton;
    private ImageView sendButton;
    private LinearLayout mEmojiBar;

    // retrieved data
    private IChatUser recipient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_NOTIFICATION, "MessageListActivity.onCreate");

        setContentView(R.layout.activity_message_list);

        registerViews();

        // it comes from other activities or from a foreground notification
        recipient = (IChatUser) getIntent().getSerializableExtra(ChatUI.INTENT_BUNDLE_RECIPIENT);

        if (recipient == null) {
            // it comes from background notification
            recipient = getRecipientFromBackgroundNotification();
            Log.d(DEBUG_NOTIFICATION, "MessageListActivity.onCreate: recipient == " + recipient.toString());
        }

        ChatManager.getInstance().setActiveConversation(recipient.getId(), true);

        conversationMessagesHandler = ChatManager.getInstance()
                .getConversationMessagesHandler(recipient);
        Log.d(DEBUG_NOTIFICATION, "MessageListActivity.onCreate: conversationMessagesHandler got with hash == " + conversationMessagesHandler.hashCode());
        conversationMessagesHandler.upsertConversationMessagesListener(this);
        Log.d(TAG, "MessageListActivity.onCreate: conversationMessagesHandler attached");
        Log.d(DEBUG_NOTIFICATION, "MessageListActivity.onCreate: conversationMessagesHandler attached");
        conversationMessagesHandler.connect();
        Log.d(TAG, "MessageListActivity.onCreate: conversationMessagesHandler connected");
        Log.d(DEBUG_NOTIFICATION, "MessageListActivity.onCreate: conversationMessagesHandler connected");

        presenceHandler = ChatManager.getInstance().getPresenceHandler(recipient.getId());
        presenceHandler.upsertPresenceListener(this);
        presenceHandler.connect();

        initRecyclerView();

        // panel which contains the edittext, the emoji button and the attach button
        initInputPanel();

        initToolbar(recipient);

        // create a conversation object
//        if (isFromBackgroundNotification) {
//            onConversationRetrievedSuccess(ConversationUtils.createConversationFromBackgroundPush(getIntent()));
//        } else {
//            ConversationUtils.getConversationFromId(ChatManager.getInstance().getTenant(),
//                    ChatManager.getInstance().getLoggedUser().getId(),
//                    recipientId, this);
//        }
//        observeUserPresence();
    }

    @Override
    protected void onPause() {
        ChatManager.getInstance().setActiveConversation(recipient.getId(), false);

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "  MessageListActivity.onStop");

        // dismiss the emoji panel
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        ChatManager.getInstance().setActiveConversation(recipient.getId(), false);

        // detach the conversation messages listener
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "  MessageListActivity.onDestroy");

        ChatManager.getInstance().setActiveConversation(recipient.getId(), false);

        presenceHandler.removePresenceListener(this);
        Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.onDestroy: presenceHandler detached");

        conversationMessagesHandler.removeConversationMessagesListener(this);
    }

//    @Override
//    protected void onStart() {
//        Log.d(TAG, "  MessageListActivity.onStart");
//        super.onStart();
//
////        conversationMessagesHandler.upsertConversationMessagesListener(this);
////        Log.d(TAG, "  MessageListActivity.onStart: conversationMessagesHandler attached");
//    }
//
//    private String getRecipientId() {
//        Log.d(TAG, "getRecipientId");
//
//        String recipientId;
//
//        if (getIntent().getSerializableExtra(INTENT_BUNDLE_RECIPIENT_ID) != null) {
//            // retrieve conversationId
//            isFromBackgroundNotification = false;
//            isFromForegroundNotification = false;
//            recipientId = getIntent().getStringExtra(INTENT_BUNDLE_RECIPIENT_ID);
//            // check if the activity has been called from foreground notification
//            try {
//                isFromForegroundNotification = getIntent().getExtras().getBoolean(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION);
//
//            } catch (Exception e) {
//                Log.e(TAG, "MessageListActivity.getConversationId: cannot retrieve 'is from notification extra' " + e.getMessage());
//                isFromForegroundNotification = false;
//            }
//        } else {
//            //from background notification
//            isFromBackgroundNotification = true;
//            recipientId = getIntent().getStringExtra("recipient");
//
//        }
//
//        Log.d(TAG_NOTIFICATION, "MessageListActivity.recipientId: recipientId: " + recipientId);
//
//        return recipientId;
//    }

    private void registerViews() {
        Log.d(TAG, "registerViews");

        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mPictureView = (ImageView) findViewById(R.id.toolbar_picture);
        mTitleTextView = (TextView) findViewById(R.id.toolbar_title);
        mSubTitleTextView = (TextView) findViewById(R.id.toolbar_subtitle);

        mNoMessageLayout = (RelativeLayout) findViewById(R.id.no_messages_layout);

        editText = (EmojiEditText) findViewById(R.id.main_activity_chat_bottom_message_edittext);
        rootView = (ViewGroup) findViewById(R.id.main_activity_root_view);
        emojiButton = (ImageView) findViewById(R.id.main_activity_emoji);
        attachButton = (ImageView) findViewById(R.id.main_activity_attach);
        sendButton = (ImageView) findViewById(R.id.main_activity_send);
        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
        mEmojiBar = (LinearLayout) findViewById(R.id.main_activity_emoji_bar);
    }

    private IChatUser getRecipientFromBackgroundNotification() {
        IChatUser recipient = null;
        if (StringUtils.isValid(getIntent().getStringExtra("sender")) &&
                StringUtils.isValid(getIntent().getStringExtra("sender_fullname"))) {
            String contactId = getIntent().getStringExtra("sender");
            Log.d(DEBUG_NOTIFICATION, "MessageListActivity.onCreate.fromNotification: contactId == " + contactId);

            String contactFullName = getIntent().getStringExtra("sender_fullname");
            Log.d(DEBUG_NOTIFICATION, "MessageListActivity.onCreate.fromNotification: contactFullName == " + contactFullName);

            // create the recipient from background notification data
            recipient = new ChatUser(contactId, contactFullName);
        }
        return recipient;
    }

    private void initToolbar(IChatUser recipient) {
        Log.d(TAG, "initToolbar");

        // setup the toolbar with conversations data
        if (recipient != null) {
            initDirectToolbar(recipient.getProfilePictureUrl(), recipient.getId(), recipient.getFullName());
        }
//        else
        //TODO for group

//            if (conversation.isDirectChannel()) {
//                // its a one to one conversation
//                initDirectToolbar("", conversation.getConvers_with(), conversation.getConvers_with_fullname());
//            } else if (conversation.isGroupChannel()) {
//                // its a group conversation
//                initGroupToolbar("", conversation.getRecipient(), conversation.getRecipientFullName());
//            } else {
//                Toast.makeText(this, "channel type is undefined", Toast.LENGTH_SHORT).show();
//            }
//        }


        // minimal settings
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initDirectToolbar(String pictureUrl, String conversWith, String conversWithFullName) {
        Log.d(TAG, "initDirectToolbar");

        // toolbar picture
        setPicture(pictureUrl, R.drawable.ic_person_avatar);

        // toolbar recipient display name
        String deNormalizedUsername = ChatUtils.deNormalizeUsername(conversWith);
        String recipientDisplayName = StringUtils.isValid(conversWithFullName) ?
                conversWithFullName : deNormalizedUsername;
        mTitleTextView.setText(recipientDisplayName);

        // toolbar click listener
//        OnProfilePictureClickListener onProfilePictureClickListener =
//                new OnProfilePictureClickListener(this, conversWith);
//        onProfilePictureClickListener.setContactDisplayName(conversWithFullName);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageListActivity.this, PublicProfileActivity.class);

                intent.putExtra(ChatUI.INTENT_BUNDLE_RECIPIENT, recipient);
//                intent.putExtra(INTENT_BUNDLE_CALLING_ACTIVITY, targetClass);
                startActivity(intent);
            }
        });
    }

//    private void initGroupToolbar(String pictureUrl, String recipient, String recipientFullName) {
//        Log.d(TAG, "initGroupToolbar");
//
//        // toolbar picture
//        setPicture(pictureUrl, R.drawable.ic_group_avatar);
//
//        // toolbar recipient display name
//        String recipientDisplayName = StringUtils.isValid(recipientFullName) ?
//                recipientFullName : recipient;
//        mTitleTextView.setText(recipientDisplayName);
//
//        // toolbar subtitle
//        displayGroupMembersInSubtitle();
//
//        // toolbar click listener
//        View.OnClickListener onToolbarClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onToolbarClickListener.onClick");
//
//                starGroupDetailsActivity();
//            }
//        };
//
//        toolbar.setOnClickListener(onToolbarClickListener); // shows the group information
//    }
//
//    // bugfix Issue #31
//    private void displayGroupMembersInSubtitle() {
//        mSubTitleTextView.setText(getString(R.string.activity_message_list_group_info_label));
//
//        GroupUtils.subscribeOnGroupsChanges(ChatManager.getInstance().getAppId(), recipient.getId(),
//                new GroupUtils.OnGroupsChangeListener() {
//                    @Override
//                    public void onGroupChanged(Group group, String groupId) {
//
//                        String members;
//                        if (group != null && group.getMembers() != null) {
//                            members = GroupUtils.getGroupMembersAsList(group.getMembers());
//                        } else {
//                            Log.e(TAG, "displayGroupMembersInSubtitle" +
//                                    ".subscribeOnGroupsChanges.onGroupChanged: group is null.");
//                            members = getString(R.string.activity_message_list_group_info_you_label);
//                        }
//
//                        mSubTitleTextView.setText(members);
//                    }
//
//                    @Override
//                    public void onGroupCancelled(String errorMessage) {
//                        Log.e(TAG, errorMessage);
//                    }
//                });
//    }

    private void setPicture(String pictureUrl, @DrawableRes int placeholder) {
        Glide.with(getApplicationContext())
                .load(StringUtils.isValid(pictureUrl) ? pictureUrl : "")
                .placeholder(placeholder)
                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .into(mPictureView);
    }

//    private void starGroupDetailsActivity() {
//        Log.d(TAG, "starGroupDetailsActivity");
//
////        if (recipient == null)
////            return;
//
//        Intent intent = new Intent(this, GroupAdminPanelActivity.class);
//        intent.putExtra(GroupAdminPanelActivity.EXTRAS_GROUP_NAME, recipient.getFullName());
//        intent.putExtra(GroupAdminPanelActivity.EXTRAS_GROUP_ID, recipient.getId());
//        startActivityForResult(intent, ChatUI._REQUEST_CODE_GROUP_ADMIN_PANEL_ACTIVITY);
//    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView");

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);  // put adding from bottom
        recyclerView.setLayoutManager(mLinearLayoutManager);
        initRecyclerViewAdapter(recyclerView);
    }

    private void initRecyclerViewAdapter(RecyclerView recyclerView) {
        Log.d(TAG, "initRecyclerViewAdapter");

        Log.d(TAG, "conversationMessagesHandler.getMessages().size() is " + conversationMessagesHandler.getMessages().size());

        messageListAdapter = new MessageListAdapter(this, conversationMessagesHandler.getMessages());
        messageListAdapter.setMessageClickListener(this.onMessageClickListener);
        recyclerView.setAdapter(messageListAdapter);

        // scroll to last position
        if (messageListAdapter.getItemCount() > 0) {
            int position = messageListAdapter.getItemCount() - 1;
            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    /**
     * Listener called when a message is clicked.
     */
    public OnMessageClickListener onMessageClickListener =
            new OnMessageClickListener() {
                @Override
                public void onMessageLinkClick(TextView messageView, ClickableSpan clickableSpan) {
                    Log.d(TAG, "onMessageClickListener.onMessageLinkClick");
                    Log.d(TAG, "text: " + messageView.getText().toString());

                    if (ChatUI.getInstance().getOnMessageClickListener() != null) {
                        ChatUI.getInstance().getOnMessageClickListener()
                                .onMessageLinkClick(messageView, clickableSpan);
                    } else {
                        Log.d(TAG, "Chat.Configuration.getMessageClickListener() == null");
                    }
                }
            };


    private void initInputPanel() {
        Log.d(TAG, "initInputPanel");

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // bugfix ssue #4
                if (editText.getText().toString() == null ||
                        editText.getText().toString().isEmpty() ||
                        // source : https://stackoverflow.com/questions/28040993/check-if-string-is-only-line-breaks
                        // This regular expression will match all the strings that
                        // contain one or more characters from the set of \n and \r.
                        editText.getText().toString().matches("[\\n\\r]+")) {
                    // not valid input - hides the send button
                    sendButton.setVisibility(View.GONE);
                    attachButton.setVisibility(View.VISIBLE);
                } else {
                    // valid input - shows the send button
                    sendButton.setVisibility(View.VISIBLE);
                    attachButton.setVisibility(View.GONE);
                }
            }
        });

        emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
        attachButton.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
        sendButton.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                emojiPopup.toggle();
            }
        });
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "MessageListActivity.onAttachClicked");

                if (ChatUI.getInstance().getOnAttachClickListener() != null) {
                    ChatUI.getInstance().getOnAttachClickListener().onAttachClicked(null);
                }

                showAttachBottomSheet();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "onSendClicked");

                String text = editText.getText().toString();

                if (!StringUtils.isValid(text)) {
//                    Toast.makeText(MessageListActivity.this,
//                            getString(R.string.cannot_send_empty_message),
//                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ChatManager.getInstance()
                        .sendTextMessage(recipient.getId(), recipient.getFullName(), text, null,
                                new SendMessageListener() {
                                    @Override
                                    public void onBeforeMessageSent(Message message, ChatRuntimeException chatException) {
                                        if (chatException == null) {
                                            // if the message exists update it, else add it
                                            Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.id: " + message.getId());
                                            Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.recipient: " + message.getRecipient());

                                            messageListAdapter.updateMessage(message);
                                            scrollToBottom();
                                        } else {

                                            Toast.makeText(MessageListActivity.this,
                                                    "Failed to send message",
                                                    Toast.LENGTH_SHORT).show();

                                            Log.e(TAG, "sendTextMessage.onBeforeMessageSent: ", chatException);
                                        }
                                    }

                                    @Override
                                    public void onMessageSentComplete(Message message, ChatRuntimeException chatException) {
                                        if (chatException == null) {

                                            Log.d(TAG, "message sent: " + message.toString());
                                        } else {
                                            Toast.makeText(MessageListActivity.this,
                                                    "Failed to send message",
                                                    Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "error sending message : ", chatException);
                                        }
                                    }
                                });


                // clear the edittext
                editText.setText("");
            }
        });
        setUpEmojiPopup();

//        toggleTelegramPanelVisibility();
    }

    //TODO chiama firebase per i gruppi una soltanto volta all'interno dell'activity
//    private void toggleTelegramPanelVisibility() {
//        if (conversation != null && conversation.isGroupChannel()) {
//            // group conversation
//            GroupUtils.subscribeOnGroupsChanges(ChatManager.getInstance().getAppId(), conversation.getConvers_with(),
//                    new GroupUtils.OnGroupsChangeListener() {
//                        @Override
//                        public void onGroupChanged(Group group, String groupId) {
//                            // the logged user is a member of the group
//                            if (group != null && group.getMembers() != null) {
//                                if (group.getMembers().containsKey(
//                                        ChatManager.getInstance().getLoggedUser().getId())) {
//                                    mEmojiBar.setVisibility(View.VISIBLE);
//                                    // hides a placeholder message layout
//                                    mNoMessageLayout.setVisibility(View.GONE);
//                                } else {
//                                    // TODO implement this
//                                    // mMessageDAO.detachObserveMessageTree(onDetachObserveMessageTree);
//                                }
//                            } else {
//                                Log.e(TAG, "toggleTelegramPanelVisibility" +
//                                        ".subscribeOnGroupsChanges.onGroupChanged: group is null.");
//                            }
//                        }
//
//                        @Override
//                        public void onGroupCancelled(String errorMessage) {
//                            Log.e(TAG, errorMessage);
//                        }
//                    });
//        } else {
//            // one to one conversation
//            mEmojiBar.setVisibility(View.VISIBLE);
//        }
//    }

//    // callback called when the message listener is removed
//    private OnDetachObserveMessageTree onDetachObserveMessageTree
//            = new OnDetachObserveMessageTree() {
//        @Override
//        public void onDetachedObserveMessageTree() {
//            mEmojiBar.setVisibility(View.GONE); // dismiss the input edittext
//
//            // shows a placeholder message layout
//            mNoMessageLayout.setVisibility(View.VISIBLE);
//        }
//    };


    @Override
    public void onConversationMessageReceived(Message message, ChatRuntimeException e) {
        Log.d(TAG, "onConversationMessageReceived");

        if (e == null) {
            messageListAdapter.updateMessage(message);
            scrollToBottom();
        } else {
            Log.w(TAG, "Error onConversationMessageReceived ", e);
        }
    }

    @Override
    public void onConversationMessageChanged(Message message, ChatRuntimeException e) {
        Log.d(TAG, "onConversationMessageChanged");

        if (e == null) {
            messageListAdapter.updateMessage(message);
            scrollToBottom();

        } else {
            Log.w(TAG, "Error onConversationMessageReceived ", e);
        }
    }

    private void scrollToBottom() {
        // scroll to last position
        if (messageListAdapter.getItemCount() > 0) {
            int position = messageListAdapter.getItemCount() - 1;
            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    private void showAttachBottomSheet() {
        Log.d(TAG, "MessageListActivity.onAttachClicked");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BottomSheetAttach dialog = BottomSheetAttach.newInstance();
        dialog.show(ft, BottomSheetAttach.class.getName());
    }
//
//
////    private void showFilePickerDialog() {
////        Log.d(TAG, "showFilePickerDialog");
////
////        // retrieve properties
////        DialogProperties properties = getDialogProperties();
////
////        // dialog
////        FilePickerDialog dialog = new FilePickerDialog(MessageListActivity.this, properties);
////        dialog.setTitle("Select a File");
////        dialog.setDialogSelectionListener(new DialogSelectionListener() {
////            @Override
////            public void onSelectedFilePaths(String[] files) {
////                //files is the array of the paths of files selected by the Application User.
////            }
////        });
////        dialog.show();
////    }
////
////    private DialogProperties getDialogProperties() {
////        Log.d(TAG, "getDialogProperties");
////
////        // properties
////        DialogProperties properties = new DialogProperties();
////        properties.selection_mode = DialogConfigs.SINGLE_MODE;
////        properties.selection_type = DialogConfigs.FILE_SELECT;
//////        properties.root = new File(DialogConfigs.DEFAULT_DIR);
////        properties.root = Environment.getExternalStorageDirectory();
////        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
////        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
////        properties.extensions = null;
////        return properties;
////    }


    @TargetApi(19)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {

        // comes from admin panel activity
        //TODO da ristrutturare con il GroupHandler
        if (requestCode == ChatUI._REQUEST_CODE_GROUP_ADMIN_PANEL_ACTIVITY) {

//            if (resultCode == RESULT_OK) {
//                toggleTelegramPanelVisibility(); // update the input panel ui
//            }

//            // bugfix Issue #33
//            if (conversation != null)
//                initToolbar(conversation);

            // bugfix Issue #15
        } else if (requestCode == _INTENT_ACTION_GET_PICTURE) {
            if (data != null && data.getData() != null && resultCode == RESULT_OK) {

//                if (conversation == null)
//                    return;

                Uri uri = data.getData();

                // convert the stream to a file
                File fileToUpload = new File(StorageHandler.getFilePathFromUri(this, uri));
                showConfirmUploadDialog(fileToUpload);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // bugfix Issue #64
    private void showConfirmUploadDialog(
            final File file) {
        Log.d(TAG, "uploadFile");

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.activity_message_list_confirm_dialog_upload_title_label))
                .setMessage(getString(R.string.activity_message_list_confirm_dialog_upload_message_label))
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // upload the file
                        uploadFile(file);
                    }
                })
                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss(); // close the alert dialog
                    }
                }).show();
    }

    // bugfix Issue #15
    private void uploadFile(File file) {
        Log.d(TAG, "uploadFile");

        // bugfix Issue #45
        final ProgressDialog progressDialog = new ProgressDialog(MessageListActivity.this);
        progressDialog.setMessage(getString(R.string.activity_message_list_progress_dialog_upload));
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageHandler.uploadFile(this, file, new OnUploadedCallback() {
            @Override
            public void onUploadSuccess(Uri downloadUrl, String type) {
                Log.d(TAG, "uploadFile.onUploadSuccess - downloadUrl: " + downloadUrl);

                progressDialog.dismiss(); // bugfix Issue #45

//                if (StringUtils.isValid((conversation.getGroup_id()))) {
//                    mMessageDAO.sendGroupMessage(downloadUrl.toString(), type,
//                            conversation);
//                } else {
                // update firebase references and send notification

                ChatManager.getInstance().sendImageMessage(recipient.getId(), recipient.getFullName(), downloadUrl.toString(), null, null);

//                    ChatManager.getInstance().sendMessage(downloadUrl.toString(), type,
//                            conversation, extras);
//                }
            }

            @Override
            public void onProgress(double progress) {
                Log.d(TAG, "uploadFile.onProgress - progress: " + progress);

                // bugfix Issue #45
                progressDialog.setProgress((int) progress);

                // TODO: 06/09/17 agganciare progress direttamente alla cella della recyclerview
            }

            @Override
            public void onUploadFailed(Exception e) {
                Log.e(TAG, "uploadFile.onUploadFailed: " + e.getMessage());

                progressDialog.dismiss(); // bugfix Issue #45

                Toast.makeText(MessageListActivity.this,
                        getString(R.string.activity_message_list_progress_dialog_upload_failed),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // bugfix Issue #4
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }

//        else {
//            if (isFromBackgroundNotification || isFromForegroundNotification) {
//                goToParentActivity();
//            } else {
//                finish();
//            }
//        }
    }

//    // bugfix Issue #4
//    public void goToParentActivity() {
//        Log.d(TAG, "goToParentActivity");
//        Intent upIntent = getNotificationParentActivityIntent();
//        Log.d(TAG, "upIntent: " + upIntent.toString());
//
//        // This activity is NOT part of this app's task, so create a new task
//        // when navigating up, with a synthesized back stack.
//        TaskStackBuilder.create(this)
//                // Add all of this activity's parents to the back stack
//                .addNextIntentWithParentStack(upIntent)
//                // Navigate up to the closest parent
//                .startActivities();
//        finish();
//    }
//
//    // bugfix Issue #4
//    private Intent getNotificationParentActivityIntent() {
//        Intent intent = null;
//        try {
//            // targetClass MUST NOT BE NULL
//            // targetClass MUST NOT BE NULL
//            Class<?> targetClass = Class.forName(getString(R.string.target_notification_parent_activity));
//            intent = new Intent(this, targetClass);
//        } catch (ClassNotFoundException e) {
//            String errorMessage = "cannot retrieve notification target acticity class. " + e.getMessage();
//            Log.e(TAG, errorMessage);
//        }
//
//        return intent;
//    }
//
//    private String getRecipientIdFromPushNotification(Intent pushData) {
//        String recipientId = pushData.getStringExtra("recipient");
////        isGroupConversation = false;
////
////        // retrieve the group_id
////        try {
////            String groupId = pushData.getStringExtra("group_id");
////            if (StringUtils.isValid(groupId)) {
////                conversationId = groupId;
////                isGroupConversation = true;
////            } else {
////                Log.w(TAG, "group_id is empty or null. ");
////            }
////        } catch (Exception e) {
////            Log.w(TAG, "cannot retrieve group_id. it may not exist" + e.getMessage());
////        }
//
//        return recipientId;
//    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
                    @Override
                    public void onEmojiBackspaceClick(final View v) {
                        Log.d(TAG, "Clicked on Backspace");
                    }
                })
                .setOnEmojiClickListener(new OnEmojiClickListener() {
                    @Override
                    public void onEmojiClick(@NonNull final EmojiImageView imageView, @NonNull final Emoji emoji) {
                        Log.d(TAG, "Clicked on emoji");
                    }
                })
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        emojiButton.setImageResource(R.drawable.ic_keyboard_24dp);
                    }
                })
                .setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
                    @Override
                    public void onKeyboardOpen(@Px final int keyBoardHeight) {
                        Log.d(TAG, "Opened soft keyboard");
                    }
                })
//                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
//                    @Override
//                    public void onEmojiPopupDismiss() {
//                        emojiButton.setImageResource(R.drawable.emoji_ios_category_people);
//                    }
//                })

                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        emojiButton.setImageResource(R.drawable.emoji_google_category_people);
                    }
                })
                .setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
                    @Override
                    public void onKeyboardClose() {
                        Log.d(TAG, "Closed soft keyboard");
                    }
                })
                .build(editText);
    }

    @Override
    public void isUserOnline(boolean isConnected) {
        Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.isUserOnline: " +
                "isConnected == " + isConnected);

        if (isConnected) {
            conversWithOnline = true;
            mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_online));
        } else {
            conversWithOnline = false;

            if (conversWithLastOnline != 0) {
                mSubTitleTextView.setText(TimeUtils.getFormattedTimestamp(conversWithLastOnline));
                Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.isUserOnline: " +
                        "conversWithLastOnline == " + conversWithLastOnline);
            } else {
                mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_offline));
            }
        }
    }

    @Override
    public void userLastOnline(long lastOnline) {
        Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.userLastOnline: " +
                "lastOnline == " + lastOnline);

        conversWithLastOnline = lastOnline;

        if (!conversWithOnline)
            mSubTitleTextView.setText(TimeUtils.getFormattedTimestamp(lastOnline));
    }

    @Override
    public void onPresenceError(Exception e) {
        Log.e(DEBUG_USER_PRESENCE, "MessageListActivity.onMyPresenceError: " + e.toString());

        mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_offline));
    }
}