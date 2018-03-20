//package org.chat21.android.ui.messages.activities;
//
//import android.annotation.TargetApi;
//import android.app.ProgressDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.PorterDuff;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.annotation.DrawableRes;
//import android.support.annotation.NonNull;
//import android.support.annotation.Px;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.Toolbar;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.text.style.ClickableSpan;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.animation.GlideAnimation;
//import com.bumptech.glide.request.target.SimpleTarget;
//import com.vanniktech.emoji.EmojiEditText;
//import com.vanniktech.emoji.EmojiImageView;
//import com.vanniktech.emoji.EmojiPopup;
//import com.vanniktech.emoji.emoji.Emoji;
//import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
//import com.vanniktech.emoji.listeners.OnEmojiClickListener;
//import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
//import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
//import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
//import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
//
//import org.chat21.android.R;
//import org.chat21.android.core.ChatManager;
//import org.chat21.android.core.chat_groups.listeners.ChatGroupsListener;
//import org.chat21.android.core.chat_groups.models.ChatGroup;
//import org.chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
//import org.chat21.android.core.exception.ChatRuntimeException;
//import org.chat21.android.core.messages.handlers.ConversationMessagesHandler;
//import org.chat21.android.core.messages.listeners.ConversationMessagesListener;
//import org.chat21.android.core.messages.listeners.SendMessageListener;
//import org.chat21.android.core.messages.models.Message;
//import org.chat21.android.core.presence.PresenceHandler;
//import org.chat21.android.core.presence.listeners.PresenceListener;
//import org.chat21.android.core.users.models.IChatUser;
//import org.chat21.android.storage.OnUploadedCallback;
//import org.chat21.android.storage.StorageHandler;
//import org.chat21.android.ui.ChatUI;
//import org.chat21.android.ui.chat_groups.activities.GroupAdminPanelActivity;
//import org.chat21.android.ui.messages.adapters.MessageListAdapter;
//import org.chat21.android.ui.messages.fragments.BottomSheetAttach;
//import org.chat21.android.ui.messages.listeners.OnMessageClickListener;
//import org.chat21.android.ui.users.activities.PublicProfileActivity;
//import org.chat21.android.utils.StringUtils;
//import org.chat21.android.utils.TimeUtils;
//import org.chat21.android.utils.image.CropCircleTransformation;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.chat21.android.ui.ChatUI.BUNDLE_CHANNEL_TYPE;
//import static org.chat21.android.utils.DebugConstants.DEBUG_NOTIFICATION;
//import static org.chat21.android.utils.DebugConstants.DEBUG_USER_PRESENCE;
//
///**
// * Created by stefano on 31/08/2015.
// */
//public class bk_MessageListActivity extends AppCompatActivity
//        implements ConversationMessagesListener, PresenceListener, ChatGroupsListener {
//    private static final String TAG = bk_MessageListActivity.class.getName();
//
//    public static final int _INTENT_ACTION_GET_PICTURE = 853;
//
//    private PresenceHandler presenceHandler = null;
//    private ConversationMessagesHandler conversationMessagesHandler;
//    private boolean conversWithOnline = false;
//    private long conversWithLastOnline = -1;
//
//    private GroupsSyncronizer groupsSyncronizer = null;
//
//    private RecyclerView recyclerView;
//    private LinearLayoutManager mLinearLayoutManager;
//    private MessageListAdapter messageListAdapter;
//    private Toolbar toolbar;
//
//    private ImageView mPictureView;
//    private TextView mTitleTextView;
//    private TextView mSubTitleTextView;
//    private RelativeLayout mNoMessageLayout;
//
//    private EmojiPopup emojiPopup;
//    private EmojiEditText editText;
//    private ViewGroup rootView;
//    private ImageView emojiButton;
//    private ImageView attachButton;
//    private ImageView sendButton;
//    private LinearLayout mEmojiBar;
//
//    /**
//     * {@code recipient} is the real contact whom is talking with.
//     * it contains all the info to start a conversation.
//     */
//    private IChatUser recipient;
//    /**
//     * {@code chatGroup} is a support item witch contains all addictional info
//     * about group such as the members list which cannot be included inside the {@code recipient}
//     */
//    private ChatGroup chatGroup;
//    private String channelType; // detect if is a group or a direct conversation
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_message_list);
//
//        registerViews();
//
//        // retrieve recipient
//        recipient = (IChatUser) getIntent().getSerializableExtra(ChatUI.BUNDLE_RECIPIENT);
//
//        // retrieve channel type
//        channelType = (String) getIntent().getExtras().get(BUNDLE_CHANNEL_TYPE);
//        // default case
//        if (!StringUtils.isValid(channelType)) {
//            channelType = Message.DIRECT_CHANNEL_TYPE;
//        }
//
//        if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
//            // retrieve group
//            chatGroup = ChatManager.getInstance().getGroupsSyncronizer().getById(recipient.getId());
//        } else {
//            // retrive contact
//            String recipientId;
//            if (recipient != null) {
//                recipientId = recipient.getId();
//            } else {
//                if (StringUtils.isValid(getIntent().getStringExtra("sender"))) {
//                    recipientId = getIntent().getStringExtra("sender");
//                    Log.d(DEBUG_NOTIFICATION, "MessageListActivity.onCreate:" +
//                            " recipientId == " + recipientId);
//                } else {
//                    throw new ChatRuntimeException("Recipient can not be retrieved! " +
//                            "Did you pass it correctly?");
//                }
//            }
//
//            // retrieve the updated recipient
//            recipient = ChatManager.getInstance().getContactsSynchronizer().findById(recipientId);
//        }
//
//        // ######### begin conversation messages handler
//        conversationMessagesHandler = ChatManager.getInstance()
//                .getConversationMessagesHandler(recipient);
//        conversationMessagesHandler.upsertConversationMessagesListener(this);
//        Log.d(TAG, "MessageListActivity.onCreate: conversationMessagesHandler attached");
//        conversationMessagesHandler.connect();
//        Log.d(TAG, "MessageListActivity.onCreate: conversationMessagesHandler connected");
//        // ######### end conversation messages handler
//
//        initRecyclerView();
//
//        //////// toolbar
//        if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {
//            if (recipient != null) {
//                initDirectToolbar(recipient);
//            }
//        } else if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
//            if (chatGroup != null) {
//                initGroupToolbar(chatGroup);
//            }
//        }
//
//        // minimal settings
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        //////// end toolbar
//
//        /////// presence manager
//        if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {
//            if (recipient != null) {
//                presenceHandler = ChatManager.getInstance().getPresenceHandler(recipient.getId());
//                presenceHandler.upsertPresenceListener(this);
//                presenceHandler.connect();
//            }
//        } else {
//            if (recipient != null) {
//                groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();
//                groupsSyncronizer.upsertGroupsListener(this);
//                groupsSyncronizer.connect();
//            }
//        }
//
//
//        // panel which contains the edittext, the emoji button and the attach button
//        initInputPanel();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // set the active conversation
//        ChatManager.getInstance().getConversationsHandler()
//                .setCurrentOpenConversationId(recipient.getId());
//        Log.d(TAG, "MessageListActivity.onResume: " +
//                "currentOpenConversationId == " + recipient.getId());
//
//        // set the current conversation as read
//        ChatManager.getInstance()
//                .getConversationsHandler()
//                .setConversationRead(recipient.getId());
//    }
//
//    @Override
//    protected void onPause() {
//        // unset the active conversation
//        ChatManager.getInstance().getConversationsHandler()
//                .setCurrentOpenConversationId(null);
//        Log.d(TAG, "MessageListActivity.onResume: currentOpenConversationId detached");
//
//        super.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        Log.d(TAG, "  MessageListActivity.onStop");
//
//        // dismiss the emoji panel
//        if (emojiPopup != null) {
//            emojiPopup.dismiss();
//        }
//
//        super.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "  MessageListActivity.onDestroy");
//
//        if (presenceHandler != null) {
//            presenceHandler.removePresenceListener(this);
//            Log.d(TAG, "MessageListActivity.onDestroy:" +
//                    " presenceHandler detached");
//        }
//
//        if(groupsSyncronizer != null) {
//            groupsSyncronizer.removeGroupsListener(this);
//            Log.d(TAG, "MessageListActivity.onDestroy:" +
//                    " groupsSyncronizer detached");
//        }
//
//        // unset the active conversation
//        ChatManager.getInstance().getConversationsHandler()
//                .setCurrentOpenConversationId(null);
//        Log.d(TAG, "MessageListActivity.onResume: currentOpenConversationId detached");
//
//        // detach the conversation messages listener
//        conversationMessagesHandler.removeConversationMessagesListener(this);
//    }
//
//    private void registerViews() {
//        Log.d(TAG, "registerViews");
//
//        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        mPictureView = (ImageView) findViewById(R.id.toolbar_picture);
//        mTitleTextView = (TextView) findViewById(R.id.toolbar_title);
//        mSubTitleTextView = (TextView) findViewById(R.id.toolbar_subtitle);
//
//        mNoMessageLayout = (RelativeLayout) findViewById(R.id.no_messages_layout);
//
//        editText = (EmojiEditText) findViewById(R.id.main_activity_chat_bottom_message_edittext);
//        rootView = (ViewGroup) findViewById(R.id.main_activity_root_view);
//        emojiButton = (ImageView) findViewById(R.id.main_activity_emoji);
//        attachButton = (ImageView) findViewById(R.id.main_activity_attach);
//        sendButton = (ImageView) findViewById(R.id.main_activity_send);
//        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
//        mEmojiBar = (LinearLayout) findViewById(R.id.main_activity_emoji_bar);
//    }
//
//    private void initDirectToolbar(final IChatUser recipient) {
//        // toolbar picture
//        setPicture(recipient.getProfilePictureUrl(), R.drawable.ic_person_avatar);
//
//        // toolbar recipient display name
//        mTitleTextView.setText(recipient.getFullName());
//
//        toolbar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(bk_MessageListActivity.this,
//                        PublicProfileActivity.class);
//
//                intent.putExtra(ChatUI.BUNDLE_RECIPIENT, recipient);
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void initGroupToolbar(final ChatGroup chatGroup) {
//        // toolbar picture
//        setPicture(chatGroup.getIconURL(), R.drawable.ic_group_avatar);
//
//        // group name
//        mTitleTextView.setText(chatGroup.getName());
//
//        // toolbar group members
//        String groupMembers;
//        if (chatGroup != null && chatGroup.getMembersList() != null &&
//                chatGroup.getMembersList().size() > 0) {
//            groupMembers = chatGroup.printMembersListWithSeparator(", ");
//        } else {
//            // if there are no members show the logged user as "you"
//            groupMembers = getString(R.string.activity_message_list_group_info_you_label);
//        }
//        mSubTitleTextView.setText(groupMembers);
//
//        toolbar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(bk_MessageListActivity.this,
//                        GroupAdminPanelActivity.class);
//                intent.putExtra(ChatUI.BUNDLE_GROUP_ID, chatGroup.getGroupId());
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void setPicture(String pictureUrl, @DrawableRes int placeholder) {
//        Glide.with(getApplicationContext())
//                .load(StringUtils.isValid(pictureUrl) ? pictureUrl : "")
//                .placeholder(placeholder)
//                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
//                .into(mPictureView);
//    }
//
//    private void initRecyclerView() {
//        Log.d(TAG, "initRecyclerView");
//
//        mLinearLayoutManager = new LinearLayoutManager(this);
//        mLinearLayoutManager.setStackFromEnd(true);  // put adding from bottom
//        recyclerView.setLayoutManager(mLinearLayoutManager);
//        initRecyclerViewAdapter(recyclerView);
//    }
//
//    private void initRecyclerViewAdapter(RecyclerView recyclerView) {
//        Log.d(TAG, "initRecyclerViewAdapter");
//
//        Log.d(TAG, "conversationMessagesHandler.getMessages(): " +
//                "size() is " + conversationMessagesHandler.getMessages().size());
//
//        messageListAdapter = new MessageListAdapter(this,
//                conversationMessagesHandler.getMessages());
//        messageListAdapter.setMessageClickListener(this.onMessageClickListener);
//        recyclerView.setAdapter(messageListAdapter);
//
//        // scroll to last position
//        if (messageListAdapter.getItemCount() > 0) {
//            int position = messageListAdapter.getItemCount() - 1;
//            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
//        }
//    }
//
//    /**
//     * Listener called when a message is clicked.
//     */
//    public OnMessageClickListener onMessageClickListener = new OnMessageClickListener() {
//        @Override
//        public void onMessageLinkClick(TextView messageView, ClickableSpan clickableSpan) {
//            Log.d(TAG, "onMessageClickListener.onMessageLinkClick");
//            Log.d(TAG, "text: " + messageView.getText().toString());
//
//            if (ChatUI.getInstance().getOnMessageClickListener() != null) {
//                ChatUI.getInstance().getOnMessageClickListener()
//                        .onMessageLinkClick(messageView, clickableSpan);
//            } else {
//                Log.d(TAG, "Chat.Configuration.getMessageClickListener() == null");
//            }
//        }
//    };
//
//    private void initInputPanel() {
//        Log.d(TAG, "initInputPanel");
//
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // bugfix ssue #4
//                if (editText.getText().toString() == null ||
//                        editText.getText().toString().isEmpty() ||
//                        // source : https://stackoverflow.com/questions/28040993/check-if-string-is-only-line-breaks
//                        // This regular expression will match all the strings that
//                        // contain one or more characters from the set of \n and \r.
//                        editText.getText().toString().matches("[\\n\\r]+")) {
//                    // not valid input - hides the send button
//                    sendButton.setVisibility(View.GONE);
//                    attachButton.setVisibility(View.VISIBLE);
//                } else {
//                    // valid input - shows the send button
//                    sendButton.setVisibility(View.VISIBLE);
//                    attachButton.setVisibility(View.GONE);
//                }
//            }
//        });
//
//        emojiButton.setColorFilter(ContextCompat
//                .getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
//        attachButton.setColorFilter(ContextCompat
//                .getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
//        sendButton.setColorFilter(ContextCompat
//                .getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
//
//        emojiButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                emojiPopup.toggle();
//            }
//        });
//        attachButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                Log.d(TAG, "MessageListActivity.onAttachClicked");
//
//                if (ChatUI.getInstance().getOnAttachClickListener() != null) {
//                    ChatUI.getInstance().getOnAttachClickListener().onAttachClicked(null);
//                }
//
//                showAttachBottomSheet();
//            }
//        });
//
//        sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                Log.d(TAG, "onSendClicked");
//
//                String text = editText.getText().toString();
//
//                if (!StringUtils.isValid(text)) {
////                    Toast.makeText(MessageListActivity.this,
////                            getString(R.string.cannot_send_empty_message),
////                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                ChatManager.getInstance().sendTextMessage(recipient.getId(), recipient.getFullName(),
//                        text, channelType, null, new SendMessageListener() {
//                            @Override
//                            public void onBeforeMessageSent(Message message, ChatRuntimeException chatException) {
//                                if (chatException == null) {
//                                    // if the message exists update it, else add it
//                                    Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.id: " + message.getId());
//                                    Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.recipient: " + message.getRecipient());
//
//                                    messageListAdapter.updateMessage(message);
//                                    scrollToBottom();
//                                } else {
//
//                                    Toast.makeText(bk_MessageListActivity.this,
//                                            "Failed to send message",
//                                            Toast.LENGTH_SHORT).show();
//
//                                    Log.e(TAG, "sendTextMessage.onBeforeMessageSent: ", chatException);
//                                }
//                            }
//
//                            @Override
//                            public void onMessageSentComplete(Message message, ChatRuntimeException chatException) {
//                                if (chatException == null) {
//
//                                    Log.d(TAG, "message sent: " + message.toString());
//                                } else {
//                                    Toast.makeText(bk_MessageListActivity.this,
//                                            "Failed to send message",
//                                            Toast.LENGTH_SHORT).show();
//                                    Log.e(TAG, "error sending message : ", chatException);
//                                }
//                            }
//                        });
//
//                // clear the edittext
//                editText.setText("");
//            }
//        });
//        setUpEmojiPopup();
//
//        if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
//            if (chatGroup != null && chatGroup.getMembersList().contains(ChatManager.getInstance().getLoggedUser())) {
//                mEmojiBar.setVisibility(View.VISIBLE);
//            } else {
//                mEmojiBar.setVisibility(View.GONE);
//            }
//        }
//    }
//
//    @Override
//    public void onConversationMessageReceived(Message message, ChatRuntimeException e) {
//        Log.d(TAG, "onConversationMessageReceived");
//
//        if (e == null) {
//            messageListAdapter.updateMessage(message);
//            scrollToBottom();
//        } else {
//            Log.w(TAG, "Error onConversationMessageReceived ", e);
//        }
//    }
//
//    @Override
//    public void onConversationMessageChanged(Message message, ChatRuntimeException e) {
//        Log.d(TAG, "onConversationMessageChanged");
//
//        if (e == null) {
//            messageListAdapter.updateMessage(message);
//            scrollToBottom();
//
//        } else {
//            Log.w(TAG, "Error onConversationMessageReceived ", e);
//        }
//    }
//
//    private void scrollToBottom() {
//        // scroll to last position
//        if (messageListAdapter.getItemCount() > 0) {
//            int position = messageListAdapter.getItemCount() - 1;
//            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
//        }
//    }
//
//    private void showAttachBottomSheet() {
//        Log.d(TAG, "MessageListActivity.onAttachClicked");
//
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        BottomSheetAttach dialog = BottomSheetAttach.newInstance(recipient, channelType);
//        dialog.show(ft, BottomSheetAttach.class.getName());
//    }
////
////
//////    private void showFilePickerDialog() {
//////        Log.d(TAG, "showFilePickerDialog");
//////
//////        // retrieve properties
//////        DialogProperties properties = getDialogProperties();
//////
//////        // dialog
//////        FilePickerDialog dialog = new FilePickerDialog(MessageListActivity.this, properties);
//////        dialog.setTitle("Select a File");
//////        dialog.setDialogSelectionListener(new DialogSelectionListener() {
//////            @Override
//////            public void onSelectedFilePaths(String[] files) {
//////                //files is the array of the paths of files selected by the Application User.
//////            }
//////        });
//////        dialog.show();
//////    }
//////
//////    private DialogProperties getDialogProperties() {
//////        Log.d(TAG, "getDialogProperties");
//////
//////        // properties
//////        DialogProperties properties = new DialogProperties();
//////        properties.selection_mode = DialogConfigs.SINGLE_MODE;
//////        properties.selection_type = DialogConfigs.FILE_SELECT;
////////        properties.root = new File(DialogConfigs.DEFAULT_DIR);
//////        properties.root = Environment.getExternalStorageDirectory();
//////        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
//////        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
//////        properties.extensions = null;
//////        return properties;
//////    }
//
//
//    @TargetApi(19)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent
//            data) {
//
//        if (requestCode == _INTENT_ACTION_GET_PICTURE) {
//            if (data != null && data.getData() != null && resultCode == RESULT_OK) {
//
//                Uri uri = data.getData();
//
//                // convert the stream to a file
//                File fileToUpload = new File(StorageHandler.getFilePathFromUri(this, uri));
//                showConfirmUploadDialog(fileToUpload);
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
//
//    // bugfix Issue #64
//    private void showConfirmUploadDialog(
//            final File file) {
//        Log.d(TAG, "uploadFile");
//
//        new AlertDialog.Builder(this)
//                .setTitle(getString(R.string.activity_message_list_confirm_dialog_upload_title_label))
//                .setMessage(getString(R.string.activity_message_list_confirm_dialog_upload_message_label))
//                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // upload the file
//                        uploadFile(file);
//                    }
//                })
//                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss(); // close the alert dialog
//                    }
//                }).show();
//    }
//
//    // bugfix Issue #15
//    private void uploadFile(File file) {
//        Log.d(TAG, "uploadFile");
//
//        // bugfix Issue #45
//        final ProgressDialog progressDialog = new ProgressDialog(bk_MessageListActivity.this);
//        progressDialog.setMessage(getString(R.string.activity_message_list_progress_dialog_upload));
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        StorageHandler.uploadFile(this, file, new OnUploadedCallback() {
//            @Override
//            public void onUploadSuccess(final String uid, final Uri downloadUrl, final String type) {
//                Log.d(TAG, "uploadFile.onUploadSuccess - downloadUrl: " + downloadUrl);
//
//                progressDialog.dismiss(); // bugfix Issue #45
//
//                Glide.with(getApplicationContext())
//                        .load(downloadUrl)
//                        .asBitmap()
//                        .into(new SimpleTarget<Bitmap>() {
//                            @Override
//                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
//                                int width = bitmap.getWidth();
//                                int height = bitmap.getHeight();
//
//                                Log.d(TAG, " MessageListActivity.uploadFile:" +
//                                        " width == " + width + " - height == " + height);
//
//                                Map<String, Object> metadata = new HashMap<>();
//                                metadata.put("width", width);
//                                metadata.put("height", height);
//                                metadata.put("src", downloadUrl.toString());
////                                metadata.put("uid", uid);
//                                metadata.put("description", "");
//
//                                Log.d(TAG, " MessageListActivity.uploadFile:" +
//                                        " metadata == " + metadata);
//
//                                // get the localized type
//                                String lastMessageText = "";
//                                if (type.toLowerCase().equals(StorageHandler.Type.Image.toString().toLowerCase())) {
//                                    lastMessageText = getString(R.string.activity_message_list_type_image_label);
//                                } else if (type.equals(StorageHandler.Type.File)) {
//                                    lastMessageText = getString(R.string.activity_message_list_type_file_label);
//                                }
//
//                                // TODO: 13/02/18 add image message to the adapter  (like text message)
//                                ChatManager.getInstance().sendImageMessage(recipient.getId(),
//                                        recipient.getFullName(), lastMessageText + ": " + downloadUrl.toString(), channelType,
//                                        metadata, null);
//                            }
//                        });
//            }
//
//            @Override
//            public void onProgress(double progress) {
//                Log.d(TAG, "uploadFile.onProgress - progress: " + progress);
//
//                // bugfix Issue #45
//                progressDialog.setProgress((int) progress);
//
//                // TODO: 06/09/17 progress within viewholder
//            }
//
//            @Override
//            public void onUploadFailed(Exception e) {
//                Log.e(TAG, "uploadFile.onUploadFailed: " + e.getMessage());
//
//                progressDialog.dismiss(); // bugfix Issue #45
//
//                Toast.makeText(bk_MessageListActivity.this,
//                        getString(R.string.activity_message_list_progress_dialog_upload_failed),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    // bugfix Issue #4
//    @Override
//    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed");
//
//        if (emojiPopup != null && emojiPopup.isShowing()) {
//            emojiPopup.dismiss();
//        } else {
//            super.onBackPressed();
//        }
//
////        else {
////            if (isFromBackgroundNotification || isFromForegroundNotification) {
////                goToParentActivity();
////            } else {
////                finish();
////            }
////        }
//    }
//
////    // bugfix Issue #4
////    public void goToParentActivity() {
////        Log.d(TAG, "goToParentActivity");
////        Intent upIntent = getNotificationParentActivityIntent();
////        Log.d(TAG, "upIntent: " + upIntent.toString());
////
////        // This activity is NOT part of this app's task, so create a new task
////        // when navigating up, with a synthesized back stack.
////        TaskStackBuilder.create(this)
////                // Add all of this activity's parents to the back stack
////                .addNextIntentWithParentStack(upIntent)
////                // Navigate up to the closest parent
////                .startActivities();
////        finish();
////    }
////
////    // bugfix Issue #4
////    private Intent getNotificationParentActivityIntent() {
////        Intent intent = null;
////        try {
////            // targetClass MUST NOT BE NULL
////            // targetClass MUST NOT BE NULL
////            Class<?> targetClass = Class.forName(getString(R.string.target_notification_parent_activity));
////            intent = new Intent(this, targetClass);
////        } catch (ClassNotFoundException e) {
////            String errorMessage = "cannot retrieve notification target acticity class. " + e.getMessage();
////            Log.e(TAG, errorMessage);
////        }
////
////        return intent;
////    }
//
//    private void setUpEmojiPopup() {
//        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
//                .setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
//                    @Override
//                    public void onEmojiBackspaceClick(final View v) {
//                        Log.d(TAG, "Clicked on Backspace");
//                    }
//                })
//                .setOnEmojiClickListener(new OnEmojiClickListener() {
//                    @Override
//                    public void onEmojiClick(@NonNull final EmojiImageView imageView, @NonNull final Emoji emoji) {
//                        Log.d(TAG, "Clicked on emoji");
//                    }
//                })
//                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
//                    @Override
//                    public void onEmojiPopupShown() {
//                        emojiButton.setImageResource(R.drawable.ic_keyboard_24dp);
//                    }
//                })
//                .setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
//                    @Override
//                    public void onKeyboardOpen(@Px final int keyBoardHeight) {
//                        Log.d(TAG, "Opened soft keyboard");
//                    }
//                })
////                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
////                    @Override
////                    public void onEmojiPopupDismiss() {
////                        emojiButton.setImageResource(R.drawable.emoji_ios_category_people);
////                    }
////                })
//
//                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
//                    @Override
//                    public void onEmojiPopupDismiss() {
//                        emojiButton.setImageResource(R.drawable.emoji_ios_category_people);
//                    }
//                })
//                .setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
//                    @Override
//                    public void onKeyboardClose() {
//                        Log.d(TAG, "Closed soft keyboard");
//                    }
//                })
//                .build(editText);
//    }
//
//    @Override
//    public void isUserOnline(boolean isConnected) {
//        Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.isUserOnline: " +
//                "isConnected == " + isConnected);
//
//        if (isConnected) {
//            conversWithOnline = true;
//            mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_online));
//        } else {
//            conversWithOnline = false;
//
//            if (conversWithLastOnline != PresenceHandler.LAST_ONLINE_UNDEFINED) {
//                mSubTitleTextView.setText(TimeUtils.getFormattedTimestamp(this, conversWithLastOnline));
//                Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.isUserOnline: " +
//                        "conversWithLastOnline == " + conversWithLastOnline);
//            } else {
//                mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_offline));
//            }
//        }
//    }
//
//    @Override
//    public void userLastOnline(long lastOnline) {
//        Log.d(DEBUG_USER_PRESENCE, "MessageListActivity.userLastOnline: " +
//                "lastOnline == " + lastOnline);
//
//        conversWithLastOnline = lastOnline;
//
//        if (!conversWithOnline) {
//            mSubTitleTextView.setText(TimeUtils.getFormattedTimestamp(this, lastOnline));
//        }
//
//        if (!conversWithOnline && lastOnline == PresenceHandler.LAST_ONLINE_UNDEFINED) {
//            mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_offline));
//        }
//    }
//
//    @Override
//    public void onPresenceError(Exception e) {
//        Log.e(DEBUG_USER_PRESENCE, "MessageListActivity.onMyPresenceError: " + e.toString());
//
//        mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_offline));
//    }
//
//    @Override
//    public void onGroupAdded(ChatGroup chatGroup, ChatRuntimeException e) {
//        if (e == null) {
//            this.chatGroup = chatGroup;
//            initGroupToolbar(chatGroup);
//            initInputPanel();
//        } else {
//            Log.e(TAG, "MessageListActivity.onGroupAdded: " + e.toString());
//        }
//    }
//
//    @Override
//    public void onGroupChanged(ChatGroup chatGroup, ChatRuntimeException e) {
//        if (e == null) {
//            this.chatGroup = chatGroup;
//            initGroupToolbar(chatGroup);
//            initInputPanel();
//        } else {
//            Log.e(TAG, "MessageListActivity.onGroupChanged: " + e.toString());
//        }
//    }
//
//    @Override
//    public void onGroupRemoved(ChatRuntimeException e) {
//        Log.e(TAG, "MessageListActivity.onGroupRemoved: " + e.toString());
//    }
//}