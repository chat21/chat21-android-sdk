package chat21.android.ui.messages.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
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
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.groups.models.Group;
import chat21.android.core.messages.handlers.ConversationMessagesHandler;
import chat21.android.core.messages.listeners.ConversationMessagesListener;
import chat21.android.core.messages.listeners.SendMessageListener;
import chat21.android.core.messages.models.Message;
import chat21.android.core.presence.listeners.OnPresenceListener;
import chat21.android.dao.message.OnDetachObserveMessageTree;
import chat21.android.groups.utils.GroupUtils;
import chat21.android.storage.OnUploadedCallback;
import chat21.android.storage.StorageHandler;
import chat21.android.ui.ChatUI;
import chat21.android.ui.groups.activities.GroupAdminPanelActivity;
import chat21.android.ui.messages.adapters.MessageListAdapter;
import chat21.android.ui.messages.fragments.BottomSheetAttach;
import chat21.android.ui.messages.listeners.OnMessageClickListener;
import chat21.android.ui.messages.listeners.OnProfilePictureClickListener;
import chat21.android.utils.ChatUtils;
import chat21.android.utils.StringUtils;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.image.CropCircleTransformation;

import static chat21.android.utils.DebugConstants.DEBUG_USER_PRESENCE;

/**
 * Created by stefano on 31/08/2015.
 */
public class MessageListActivity extends AppCompatActivity implements
        ConversationMessagesListener {

    private static final String TAG = MessageListActivity.class.getName();
    private static final String TAG_NOTIFICATION = "TAG_NOTIFICATION";

    public static final int _INTENT_ACTION_GET_PICTURE = 853;

    private String recipientId;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MessageListAdapter messageListAdapter;
    private Toolbar toolbar;
    private ImageView toolbarRecipientPicture;

    private TextView mTitleTextView;
    private TextView mSubTitleTextView;
    private RelativeLayout mNoMessageLayout;

    private boolean isGroupConversation;

    private boolean conversWithOnline = false;
    private long conversWithLastOnline = 0;

    // check if this activity is called from a background notification
    private boolean isFromBackgroundNotification = false;
    // check if this activity is called from a foreground notification
    private boolean isFromForegroundNotification = false;

    private Conversation conversation;

    private EmojiPopup emojiPopup;
    private EmojiEditText editText;
    private ViewGroup rootView;
    private ImageView emojiButton;
    private ImageView attachButton;
    private ImageView sendButton;
    private LinearLayout mEmojiBar;

    private ConversationMessagesHandler conversationMessagesHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_list);

        registerViews();

        conversation = (Conversation) getIntent().getExtras().get(ChatUI.INTENT_BUNDLE_CONVERSATION);

        // retrieve the conversationId
        recipientId = getRecipientId();

        conversationMessagesHandler = ChatManager.getInstance().getConversationMessagesHandler(recipientId);
        conversationMessagesHandler.upsertConversationMessagesListener(this);
        conversationMessagesHandler.connect();

        initRecyclerView();

        // panel which contains the edittext, the emoji button and the attach button
        initInputPanel();

        initToolbar();

        // create a conversation object
//        if (isFromBackgroundNotification) {
//            onConversationRetrievedSuccess(ConversationUtils.createConversationFromBackgroundPush(getIntent()));
//        } else {
//            ConversationUtils.getConversationFromId(ChatManager.getInstance().getTenant(),
//                    ChatManager.getInstance().getLoggedUser().getId(),
//                    recipientId, this);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "  MessageListActivity.onDestroy");

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

    @Override
    protected void onStop() {
        Log.d(TAG, "  MessageListActivity.onStop");

        // dismiss the emoji panel
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        // detach the conversation messages listener
        super.onStop();
    }


    private String getRecipientId() {
        Log.d(TAG, "getRecipientId");

        String getRecipientId;

        if (getIntent().getSerializableExtra(ChatUI.INTENT_BUNDLE_RECIPIENT_ID) != null) {
            // retrieve conversationId
            isFromBackgroundNotification = false;
            isFromForegroundNotification = false;
            recipientId = getIntent().getStringExtra(ChatUI.INTENT_BUNDLE_RECIPIENT_ID);
            // check if the activity has been called from foreground notification
            try {
                isFromForegroundNotification = getIntent().getExtras().getBoolean(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION);

            } catch (Exception e) {
                Log.e(TAG, "MessageListActivity.getConversationId: cannot retrieve 'is from notification extra' " + e.getMessage());
                isFromForegroundNotification = false;
            }
        } else {
            //from background notification
            isFromBackgroundNotification = true;
            recipientId = getIntent().getStringExtra("recipient");

        }

        Log.d(TAG_NOTIFICATION, "MessageListActivity.recipientId: recipientId: " + recipientId);

        return recipientId;
    }

//    @Override
//    public void onConversationRetrievedSuccess(Conversation conversation) {
//        this.conversation = conversation;
//
//        if (StringUtils.isValid(conversation.getGroup_id())) {
//            isGroupConversation = true;
//        }
//
//        // if it is a direct conversation observe the convers_with user presence
//        if (!isGroupConversation) {
//            // subscribe for convers_with user presence changes
//            // bugfix Issue #16
//            PresenceManger.observeUserPresenceChanges(ChatManager.getInstance().getTenant(),
//                    conversation.getConvers_with(),
//                    onConversWithPresenceListener);
//        }
//
//        if (!areViewsInit) {
//            initViews(conversation);
//            areViewsInit = true;
//        }
//
//        if (!isNodeObserved) {
//            observeMessages(conversation.getConversationId());
//            isNodeObserved = true;
//        }
//    }
//
//    @Override
//    public void onNewConversationCreated(String conversationId) {
//
//        conversation = ConversationUtils.createNewConversation(conversationId);
//
//        // subscribe for convers_with user presence changes
//        PresenceManger.observeUserPresenceChanges(ChatManager.getInstance().getTenant(),
//                conversation.getConvers_with(),
//                onConversWithPresenceListener);
//
//        if (!areViewsInit) {
//            initViews(conversation);
//            areViewsInit = true;
//        }
//
//        if (!isNodeObserved) {
//            observeMessages(conversationId);
//            isNodeObserved = true;
//        }
//    }
//
//    @Override
//    public void onConversationRetrievedError(Exception e) {
//        Log.e(TAG, e.toString());
//
//        mEmojiBar.setVisibility(View.GONE); // dismiss the input edittext
//
//        // shows a placeholder message layout
//        mNoMessageLayout.setVisibility(View.VISIBLE);
//    }


    private void registerViews() {
        Log.d(TAG, "registerViews");


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarRecipientPicture = (ImageView) findViewById(R.id.profile_picture);
        mTitleTextView = (TextView) findViewById(R.id.toolbar_title);
        mSubTitleTextView = (TextView) findViewById(R.id.toolbar_subtitle);

        mNoMessageLayout = (RelativeLayout) findViewById(R.id.no_messages_layout);
        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);

        editText = (EmojiEditText) findViewById(R.id.main_activity_chat_bottom_message_edittext);
        rootView = (ViewGroup) findViewById(R.id.main_activity_root_view);
        emojiButton = (ImageView) findViewById(R.id.main_activity_emoji);
        attachButton = (ImageView) findViewById(R.id.main_activity_attach);
        sendButton = (ImageView) findViewById(R.id.main_activity_send);
        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
        mEmojiBar = (LinearLayout) findViewById(R.id.main_activity_emoji_bar);
    }


    private void initToolbar() {
        Log.d(TAG, "initToolbar");

        // setup the toolbar with conversations data
        if (conversation != null) {

            // bugfix Issue #29
            if (conversation.getChannelType() == Conversation.GROUP_CHANNEL_TYPE) {
                // its a group conversation
                initGroupToolbar(conversation);
            } else if (conversation.getChannelType() == Conversation.DIRECT_CHANNEL_TYPE) {
                // its a one to one conversation
                initOneToOneToolbar(conversation);
            } else {
                Toast.makeText(this, "channel type is undefined", Toast.LENGTH_SHORT).show();
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initOneToOneToolbar(Conversation conversation) {
        Log.d(TAG, "initOneToOneToolbar");

        // set username
        String deNormalizedUsername = ChatUtils.deNormalizeUsername(conversation.getConvers_with());

        String displayName = StringUtils.isValid(conversation.getConvers_with_fullname()) ?
                conversation.getConvers_with_fullname() : deNormalizedUsername;
        mTitleTextView.setText(displayName);

        // set profile picture
        // TODO: 27/12/17 retrieve the recipient picture url
        Glide.with(getApplicationContext())
                .load("")
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .into(toolbarRecipientPicture);

        OnProfilePictureClickListener onProfilePictureClickListener =
                new OnProfilePictureClickListener(this, conversation.getConvers_with());
        onProfilePictureClickListener.setContactDisplayName(displayName);

        toolbar.setOnClickListener(onProfilePictureClickListener);
    }

    private void initGroupToolbar(Conversation conversation) {
        Log.d(TAG, "initGroupToolbar");

        // group name
        mTitleTextView.setText(conversation.getRecipientFullName());

        displayGroupMembersInSubtitle();

        // group picture
        // TODO: 27/12/17 retrieve the recipient picture url
        Glide.with(getApplicationContext())
                .load("")
                .placeholder(R.drawable.ic_group_avatar)
                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .into(toolbarRecipientPicture);

        // click on the toolbar to show the group information
        if (StringUtils.isValid(conversation.getRecipient())) {
            toolbar.setOnClickListener(onToolbarClickListener);
        }
    }

    // bugfix Issue #31
    private void displayGroupMembersInSubtitle() {
        mSubTitleTextView.setText(getString(R.string.activity_message_list_group_info_label));

        GroupUtils.subscribeOnGroupsChanges(ChatManager.getInstance().getTenant(), recipientId,
                new GroupUtils.OnGroupsChangeListener() {
                    @Override
                    public void onGroupChanged(Group group, String groupId) {

                        String members;
                        if (group != null && group.getMembers() != null) {
                            members = GroupUtils.getGroupMembersAsList(group.getMembers());
                        } else {
                            Log.e(TAG, "displayGroupMembersInSubtitle" +
                                    ".subscribeOnGroupsChanges.onGroupChanged: group is null.");
                            members = getString(R.string.activity_message_list_group_info_you_label);
                        }

                        mSubTitleTextView.setText(members);
                    }

                    @Override
                    public void onGroupCancelled(String errorMessage) {
                        Log.e(TAG, errorMessage);
                    }
                });
    }

    private View.OnClickListener onToolbarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onToolbarClickListener.onClick");

            starGroupDetailsActivity();
        }
    };

    private void starGroupDetailsActivity() {
        Log.d(TAG, "starGroupDetailsActivity");

        if (conversation == null)
            return;

        Intent intent = new Intent(this, GroupAdminPanelActivity.class);
        intent.putExtra(GroupAdminPanelActivity.EXTRAS_GROUP_NAME, conversation.getRecipientFullName());
        intent.putExtra(GroupAdminPanelActivity.EXTRAS_GROUP_ID, conversation.getRecipient());
        startActivityForResult(intent, ChatUI._REQUEST_CODE_GROUP_ADMIN_PANEL_ACTIVITY);
    }


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

                if (conversation == null)
                    return;

                showAttachBottomSheet(conversation);
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

//                if (conversation == null)
//                    return;
//                String recipient_id, String text, Map customAttributes, SendMessageListener sendMessageListener){
                ChatManager.getInstance()
                        .sendTextMessage(recipientId, text, null,
                                new SendMessageListener() {
                                    @Override
                                    public void onBeforeMessageSent(Message message, ChatRuntimeException chatException) {
                                        if (chatException == null) {
                                            // if the message exists update it, else add it
                                            Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.id: " + message.getId());
                                            Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.sender: " + message.getSender());

                                            messageListAdapter.updateMessage(message);
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

        toggleTelegramPanelVisibility();
    }

    private void toggleTelegramPanelVisibility() {
        if (isGroupConversation) {
            // group conversation
            GroupUtils.subscribeOnGroupsChanges(ChatManager.getInstance().getTenant(), recipientId,
                    new GroupUtils.OnGroupsChangeListener() {
                        @Override
                        public void onGroupChanged(Group group, String groupId) {
                            // the logged user is a member of the group
                            if (group != null && group.getMembers() != null) {
                                if (group.getMembers().containsKey(
                                        ChatManager.getInstance().getLoggedUser().getId())) {
                                    mEmojiBar.setVisibility(View.VISIBLE);
                                    // hides a placeholder message layout
                                    mNoMessageLayout.setVisibility(View.GONE);
                                } else {
                                    // TODO implement this
                                    // mMessageDAO.detachObserveMessageTree(onDetachObserveMessageTree);
                                }
                            } else {
                                Log.e(TAG, "toggleTelegramPanelVisibility" +
                                        ".subscribeOnGroupsChanges.onGroupChanged: group is null.");
                            }
                        }

                        @Override
                        public void onGroupCancelled(String errorMessage) {
                            Log.e(TAG, errorMessage);
                        }
                    });
        } else {
            // one to one conversation
            mEmojiBar.setVisibility(View.VISIBLE);
        }
    }

    // callback called when the message listener is removed
    private OnDetachObserveMessageTree onDetachObserveMessageTree
            = new OnDetachObserveMessageTree() {
        @Override
        public void onDetachedObserveMessageTree() {
            mEmojiBar.setVisibility(View.GONE); // dismiss the input edittext

            // shows a placeholder message layout
            mNoMessageLayout.setVisibility(View.VISIBLE);

//            isNodeObserved = false;
        }
    };


    @Override
    public void onConversationMessageReceived(Message message, ChatRuntimeException e) {
        Log.d(TAG, "onConversationMessageReceived");

//        try {
//            updateMessageStatus(message, node, dataSnapshot);
//        } catch (Exception e) {
//            Log.e(TAG, "cannot update conversation status. " + e.getMessage());
//        }

        if (e == null) {
            messageListAdapter.updateMessage(message);
            scrollToBottom();
        } else {
            Log.w(TAG, "Error onConversationMessageReceived ", e);
        }
    }

    @Override
    public void onConversationMessageChanged(Message message, ChatRuntimeException e) {

        Log.d(TAG, "onTreeChildChanged");

//        if (StringUtils.isValid(message.getRecipientGroupId())) {
//            // it is a group conversation
//            node.child(dataSnapshot.getKey()).child("status").setValue(Message.STATUS_READ);
//        } else {
//            // it is a one to one conversations
//            // udpate status read
//
//            if (message.getRecipient().compareTo(ChatManager.getInstance().getLoggedUser().getId()) == 0) {
//                node.child(dataSnapshot.getKey()).child("status").setValue(Message.STATUS_READ);
//            } else {
//                Log.d(TAG, "recipient is not equal to loggedUser");
//            }
//        }

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


    //    @Override
//    public void onAttachClicked() {
//        Log.d(TAG, "MessageListActivity.onAttachClicked");
//
//        if (conversation == null)
//            return;
//
//        showAttachBottomSheet(conversation);
//    }
//
    private void showAttachBottomSheet(Conversation conversation) {
        Log.d(TAG, "MessageListActivity.onAttachClicked");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BottomSheetAttach dialog = BottomSheetAttach.newInstance(conversation);
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

        if (requestCode == ChatUI._REQUEST_CODE_GROUP_ADMIN_PANEL_ACTIVITY) {

            if (resultCode == RESULT_OK) {
                toggleTelegramPanelVisibility(); // update the input panel ui
            }

            // TODO: 27/12/17 update only toolbar members, not re-init the toolbar
//            // bugfix Issue #33
//            if (conversation != null)
//                initToolbar(conversation);

            // bugfix Issue #15
        } else if (requestCode == _INTENT_ACTION_GET_PICTURE) {
            if (data != null && data.getData() != null && resultCode == RESULT_OK) {

                if (conversation == null)
                    return;

                Uri uri = data.getData();

                // convert the stream to a file
                File fileToUpload = new File(StorageHandler.getFilePathFromUri(this, uri));
                showConfirmUploadDialog(conversation, fileToUpload);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // bugfix Issue #64
    private void showConfirmUploadDialog(final Conversation conversation,
                                         final File file) {
        Log.d(TAG, "uploadFile");

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.activity_message_list_confirm_dialog_upload_title_label))
                .setMessage(getString(R.string.activity_message_list_confirm_dialog_upload_message_label))
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // upload the file
                        uploadFile(conversation, file);
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
    private void uploadFile(final Conversation conversation, File file) {
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

                ChatManager.getInstance().sendTextMessage(recipientId, downloadUrl.toString(), null, null);

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
//            mMessageDAO.detachObserveMessageTree(onDetachObserveMessageTree);

            if (isFromBackgroundNotification || isFromForegroundNotification) {
                goToParentActivity();
            } else {
                finish();
            }
        }
    }

    // bugfix Issue #4
    public void goToParentActivity() {
        Log.d(TAG, "goToParentActivity");
        Intent upIntent = getNotificationParentActivityIntent();
        Log.d(TAG, "upIntent: " + upIntent.toString());

        // This activity is NOT part of this app's task, so create a new task
        // when navigating up, with a synthesized back stack.
        TaskStackBuilder.create(this)
                // Add all of this activity's parents to the back stack
                .addNextIntentWithParentStack(upIntent)
                // Navigate up to the closest parent
                .startActivities();
        finish();
    }

    // bugfix Issue #4
    private Intent getNotificationParentActivityIntent() {
        Intent intent = null;
        try {
            // targetClass MUST NOT BE NULL
            // targetClass MUST NOT BE NULL
            Class<?> targetClass = Class.forName(getString(R.string.target_notification_parent_activity));
            intent = new Intent(this, targetClass);
        } catch (ClassNotFoundException e) {
            String errorMessage = "cannot retrieve notification target acticity class. " + e.getMessage();
            Log.e(TAG, errorMessage);
        }

        return intent;
    }

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
                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        emojiButton.setImageResource(R.drawable.emoji_ios_category_people);
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

    private OnPresenceListener onConversWithPresenceListener = new OnPresenceListener() {
        @Override
        public void onChanged(boolean imConnected) {
            Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.onConversWithPresenceListener" +
                    ".onChanged: imConnected ==  " + imConnected);

            if (imConnected) {
                conversWithOnline = true;
                mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_online));
            } else {
                conversWithOnline = false;

                if (conversWithLastOnline != 0) {
                    mSubTitleTextView.setText(TimeUtils.getFormattedTimestamp(conversWithLastOnline));
                    Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.onConversWithPresenceListener " +
                            ".onChanged: conversWithLastOnline == " + conversWithLastOnline);
                } else {
                    mSubTitleTextView.setText("");
                }
            }
        }

        @Override
        public void onLastOnlineChanged(long lastOnline) {
            Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.onConversWithPresenceListener" +
                    ".onLastOnlineChanged: lastOnline ==  " + lastOnline);

            conversWithLastOnline = lastOnline;

            if (!conversWithOnline)
                mSubTitleTextView.setText(TimeUtils.getFormattedTimestamp(lastOnline));
        }

        @Override
        public void onError(Exception e) {
            Log.e(DEBUG_USER_PRESENCE, "MessageListActivity.onConversWithPresenceListener" +
                    ".onError == " + e.getMessage());
        }
    };

}