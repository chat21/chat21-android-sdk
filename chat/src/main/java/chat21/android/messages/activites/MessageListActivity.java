package chat21.android.messages.activites;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chat21.android.R;
import chat21.android.conversations.listeners.OnConversationRetrievedCallback;
import chat21.android.conversations.models.Conversation;
import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.core.ChatManager;
import chat21.android.dao.message.MessageDAO;
import chat21.android.dao.message.MessageDAOImpl;
import chat21.android.dao.message.OnDetachObserveMessageTree;
import chat21.android.groups.activities.GroupAdminPanelActivity;
import chat21.android.groups.models.Group;
import chat21.android.groups.utils.GroupUtils;
import chat21.android.messages.adapters.MessageListAdapter;
import chat21.android.messages.fargments.BottomSheetAttach;
import chat21.android.messages.listeners.OnMessageClickListener;
import chat21.android.messages.listeners.OnMessageTreeUpdateListener;
import chat21.android.messages.models.Message;
import chat21.android.presence.OnPresenceChangesListener;
import chat21.android.presence.PresenceHandler;
import chat21.android.storage.OnUploadedCallback;
import chat21.android.storage.StorageHandler;
import chat21.android.utils.ChatUtils;
import chat21.android.utils.StringUtils;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.glide.CropCircleTransformation;
import chat21.android.utils.listeners.OnProfilePictureClickListener;

/**
 * Created by stefano on 31/08/2015.
 */
public class MessageListActivity extends AppCompatActivity implements
        OnMessageTreeUpdateListener,
        OnPresenceChangesListener,
        OnConversationRetrievedCallback {
    private static final String TAG = MessageListActivity.class.getName();
    private static final String TAG_NOTIFICATION = "TAG_NOTIFICATION";
    private static final String MY_PRESENCE_HANDLER = "DEBUG_MY_PRESENCE";

    public static final int _INTENT_ACTION_GET_PICTURE = 853;

    private String conversationId;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MessageListAdapter messageListAdapter;
    private Map<String, Object> extras;
    private Toolbar toolbar;

    private TextView mTitleTextView;
    private TextView mSubTitleTextView;
    private RelativeLayout mNoMessageLayout;

    private List<Message> messageList = new ArrayList<>();

    private boolean isGroupConversation;

    private boolean conversWithOnline = false;
    private long conversWithLastOnline = 0;

    // check if this activity is called from a background notification
    private boolean isFromBackgroundNotification = false;
    // check if this activity is called from a foreground notification
    private boolean isFromForegroundNotification = false;

    private MessageDAO mMessageDAO;

    private Conversation conversation;

    private boolean isNodeObserved = false;
    private boolean areViewsInit = false;

    private EmojiPopup emojiPopup;
    private EmojiEditText editText;
    private ViewGroup rootView;
    private ImageView emojiButton;
    private ImageView attachButton;
    private ImageView sendButton;
    private LinearLayout mEmojiBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FIXME: 24/11/17
//        // check if Configuration.Context is null.
//        // if yes set the current ApplicationContext
//        if (Chat.Configuration.getContext() == null) {
//            Chat.Configuration.setContext(getApplicationContext());
//
//            EmojiManager.install(new IosEmojiProvider());
//        }

        setContentView(R.layout.activity_message_list);

        registerViews();

        mMessageDAO = new MessageDAOImpl(this);

        // retrieve custom extras, if they exist
        extras = getExtras();

        // retrieve the conversationId
        conversationId = getConversationId();

        // create a conversation object
        if (isFromBackgroundNotification) {
            onConversationRetrievedSuccess(ConversationUtils.createConversationFromBackgroundPush(getIntent()));
        } else {
            ConversationUtils.getConversationFromId(this, conversationId, this);
        }
    }

    private Map<String, Object> getExtras() {
        Log.d(TAG, "getExtras");

        Map<String, Object> extras = (Map<String, Object>) getIntent()
                .getExtras()
                .getSerializable(ChatManager.INTENT_BUNDLE_EXTRAS);

        return extras;
    }

    private String getConversationId() {
        Log.d(TAG, "getConversationId");

        String conversationId;

        if (getIntent().getSerializableExtra(ChatManager._INTENT_BUNDLE_CONVERSATION_ID) != null) {
            // retrieve conversationId
            isFromBackgroundNotification = false;
            isFromForegroundNotification = false;
            conversationId = getIntent().getStringExtra(ChatManager._INTENT_BUNDLE_CONVERSATION_ID);
            // check if the activity has been called from foreground notification
            try {
                isFromForegroundNotification = getIntent().getExtras().getBoolean(ChatManager.INTENT_BUNDLE_IS_FROM_NOTIFICATION);

            } catch (Exception e) {
                Log.e(TAG, "MessageListActivity.getConversationId: cannot retrieve 'is from notification extra' " + e.getMessage());
                isFromForegroundNotification = false;
            }
        } else {
            //from background notification
            isFromBackgroundNotification = true;
            conversationId = getConversationIdFromPushNotification(getIntent());
        }

        Log.i(TAG_NOTIFICATION, "MessageListActivity.getConversationId: conversationId: " + conversationId);

        return conversationId;
    }

    @Override
    public void onConversationRetrievedSuccess(Conversation conversation) {
        this.conversation = conversation;

        if (StringUtils.isValid(conversation.getGroup_id())) {
            isGroupConversation = true;
        }

        // if it is a direct conversation observe the convers_with user presence
        if (!isGroupConversation) {
            // subscribe for convers_with user presence changes
            // bugfix Issue #16
            PresenceHandler.observeUserPresenceChanges(this, conversation.getConvers_with(), this);
        }

        if (!areViewsInit) {
            initViews(conversation);
            areViewsInit = true;
        }

        if (!isNodeObserved) {
            observeNode(conversation.getConversationId());
            isNodeObserved = true;
        }
    }

    @Override
    public void onNewConversationCreated(String conversationId) {

        conversation = ConversationUtils.createNewConversation(conversationId);

        // subscribe for convers_with user presence changes
        PresenceHandler.observeUserPresenceChanges(this, conversation.getConvers_with(), this);

        if (!areViewsInit) {
            initViews(conversation);
            areViewsInit = true;
        }

        if (!isNodeObserved) {
            observeNode(conversationId);
            isNodeObserved = true;
        }
    }

    @Override
    public void onConversationRetrievedError(Exception e) {
        Log.e(TAG, e.toString());

        mEmojiBar.setVisibility(View.GONE); // dismiss the input edittext

        // shows a placeholder message layout
        mNoMessageLayout.setVisibility(View.VISIBLE);
    }

    private void observeNode(String conversationId) {
        Log.d(TAG, "observeNode");

        mMessageDAO.observeMessageTree(conversationId, this);
    }

    private void registerViews() {
        Log.d(TAG, "registerViews");

        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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

    private void initViews(Conversation conversation) {
        Log.d(TAG, "initViews");

        initToolbar(conversation);
        initRecyclerView();

        // panel which contains the edittext, the emoji button and the attach button
        initInputPanel();
    }

    private void initToolbar(Conversation conversation) {
        Log.d(TAG, "initToolbar");

        // bugfix Issue #29
        if (StringUtils.isValid(conversation.getGroup_id()) ||
                StringUtils.isValid(conversation.getGroup_name())) {
            // its a group conversation
            initGroupToolbar(conversation);
        } else {
            // its a one to one conversation
            initOneToOneToolbar(conversation);
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
        setProfilePicture();

        OnProfilePictureClickListener onProfilePictureClickListener =
                new OnProfilePictureClickListener(this, conversation.getConvers_with());
        onProfilePictureClickListener.setContactDisplayName(displayName);

        toolbar.setOnClickListener(onProfilePictureClickListener);
    }

    private void initGroupToolbar(Conversation conversation) {
        Log.d(TAG, "initGroupToolbar");

        // group name
        mTitleTextView.setText(conversation.getGroup_name());

        displayGroupMembersInSubtitle();

        // group picture
        setGroupPicture();

        // click on the toolbar to show the group information
        if (StringUtils.isValid(conversation.getGroup_id())) {
            toolbar.setOnClickListener(onToolbarClickListener);
        }
    }

    // bugfix Issue #31
    private void displayGroupMembersInSubtitle() {
        mSubTitleTextView.setText(getString(R.string.activity_message_list_group_info_label));

        GroupUtils.subscribeOnGroupsChanges(this, conversationId,
                new GroupUtils.OnGroupsChangeListener() {
                    @Override
                    public void onGroupChanged(Group group, String groupId) {

                        String members;
                        if (group != null && group.getMembers() != null) {
                            members = GroupUtils.getGroupMembersAsList(MessageListActivity.this, group.getMembers());
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
        intent.putExtra(GroupAdminPanelActivity.EXTRAS_GROUP_NAME, conversation.getGroup_name());
        intent.putExtra(GroupAdminPanelActivity.EXTRAS_GROUP_ID, conversation.getGroup_id());
        startActivityForResult(intent, ChatManager._REQUEST_CODE_GROUP_ADMIN_PANEL_ACTIVITY);
    }

    private void setProfilePicture() {
        Log.d(TAG, "setProfilePicture");

        ImageView profilePictureToolbar = (ImageView) findViewById(R.id.profile_picture);

//        RequestOptions options = new RequestOptions()
//                .centerCrop()
//                .placeholder(getResources().getDrawable(R.drawable.ic_person_avatar))
//                .circleCropTransform()
//                .skipMemoryCache(false)
//                .diskCacheStrategy(DiskCacheStrategy.ALL);
//
//        if (this.getApplicationContext() != null) {
//            Glide.with(getApplicationContext())
//                    .load("")
//                    .apply(options)
//                    .into(profilePictureToolbar);
//        }

        Glide
                .with(this)
                .load("")
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(profilePictureToolbar);
    }

    private void setGroupPicture() {
        Log.d(TAG, "setProfilePicture");

        ImageView profilePictureToolbar = (ImageView) findViewById(R.id.profile_picture);

        Glide
                .with(this)
                .load("")
                .placeholder(R.drawable.ic_group_avatar)
                .bitmapTransform(new CropCircleTransformation(this))
                .into(profilePictureToolbar);
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

        messageListAdapter = new MessageListAdapter(this, messageList);
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

                    if (ChatManager.getInstance().getOnMessageClickListener() != null) {
                        ChatManager.getInstance().getOnMessageClickListener()
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

                if (conversation == null)
                    return;

                if (StringUtils.isValid((conversation.getGroup_id()))) {

                    mMessageDAO.sendGroupMessage(text, Message.TYPE_TEXT,
                            conversation);
                } else {
                    // update firebase references and send notification
                    mMessageDAO.sendMessage(text, Message.TYPE_TEXT,
                            conversation, extras);
                }

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
            GroupUtils.subscribeOnGroupsChanges(this, conversationId,
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
                                    mMessageDAO.detachObserveMessageTree(onDetachObserveMessageTree);
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

            isNodeObserved = false;
        }
    };

    @Override
    public void onTreeChildAdded(DatabaseReference node,
                                 DataSnapshot dataSnapshot, Message message) {
        Log.d(TAG, "onTreeChildAdded");

        try {
            updateStatus(message, node, dataSnapshot);
        } catch (Exception e) {
            Log.e(TAG, "cannot update conversation status. " + e.getMessage());
        }

        messageListAdapter.insertBottom(message);

        // scroll to last position
        if (messageListAdapter.getItemCount() > 0) {
            int position = messageListAdapter.getItemCount() - 1;
            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    private void updateStatus(Message message, DatabaseReference node,
                              DataSnapshot dataSnapshot) throws Exception {
        if (StringUtils.isValid(message.getRecipientGroupId())) {
            // it is a group conversation
            node.child(dataSnapshot.getKey()).child("status").setValue(Message.STATUS_READ);
        } else {
            // it is a one to one conversations
            // udpate status read
            if (message.getRecipient().compareTo(ChatManager.getInstance().getLoggedUser().getId()) == 0) {

                node.child(dataSnapshot.getKey()).child("status").setValue(Message.STATUS_READ);
            } else {
                Log.d(TAG, "recipient is not equal to loggedUser");
            }
        }
    }

    @Override
    public void onTreeChildChanged(DatabaseReference node, DataSnapshot
            dataSnapshot, Message message) {
        Log.d(TAG, "onTreeChildChanged");

        if (StringUtils.isValid(message.getRecipientGroupId())) {
            // it is a group conversation
            node.child(dataSnapshot.getKey()).child("status").setValue(Message.STATUS_READ);
        } else {
            // it is a one to one conversations
            // udpate status read

            if (message.getRecipient().compareTo(ChatManager.getInstance().getLoggedUser().getId()) == 0) {
                node.child(dataSnapshot.getKey()).child("status").setValue(Message.STATUS_READ);
            } else {
                Log.d(TAG, "recipient is not equal to loggedUser");
            }
        }

        messageListAdapter.updateMessage(message);

        // scroll to last position
        if (messageListAdapter.getItemCount() > 0) {
            int position = messageListAdapter.getItemCount() - 1;
            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }


    @Override
    public void onTreeChildRemoved() {
        Log.d(TAG, "onTreeChildRemoved");

        // TODO: 19/10/17
    }

    @Override
    public void onTreeChildMoved() {
        Log.d(TAG, "onTreeChildMoved");

        // TODO: 19/10/17
    }

    @Override
    public void onTreeCancelled() {
        Log.d(TAG, "onTreeCancelled");

        // TODO: 19/10/17
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

        if (requestCode == ChatManager._REQUEST_CODE_GROUP_ADMIN_PANEL_ACTIVITY) {

            if (resultCode == RESULT_OK) {
                toggleTelegramPanelVisibility(); // update the input panel ui
            }

            // bugfix Issue #33
            if (conversation != null)
                initToolbar(conversation);

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

                if (StringUtils.isValid((conversation.getGroup_id()))) {
                    mMessageDAO.sendGroupMessage(downloadUrl.toString(), type,
                            conversation);
                } else {
                    // update firebase references and send notification
                    mMessageDAO.sendMessage(downloadUrl.toString(), type,
                            conversation, extras);
                }
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
    public void onPresenceChange(boolean imConnected) {
        Log.d(MY_PRESENCE_HANDLER, "MessageListActivity.onPresenceChange - imConnected: " + imConnected);

        if (imConnected) {
            conversWithOnline = true;
            mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_online));
        } else {
            conversWithOnline = false;

            if (conversWithLastOnline != 0) {
                mSubTitleTextView.setText(TimeUtils.getFormattedTimestamp(conversWithLastOnline));
                Log.i(MY_PRESENCE_HANDLER, "MessageListActivity.onPresenceChange " +
                        "- conversWithLastOnline: " + conversWithLastOnline);
            } else {
                mSubTitleTextView.setText("");
            }
        }
    }

    @Override
    public void onLastOnlineChange(long lastOnline) {
        Log.d(TAG, "onLastOnlineChange - lastOnline: " + lastOnline);
        Log.i(MY_PRESENCE_HANDLER, "MessageListActivity.onLastOnlineChange - lastOnline: " + lastOnline);

        conversWithLastOnline = lastOnline;

        if (!conversWithOnline)
            mSubTitleTextView.setText(TimeUtils.getFormattedTimestamp(lastOnline));
    }

    @Override
    public void onPresenceChangeError(Exception e) {
        Log.e(TAG, "onPresenceChangeError " + e.getMessage());
        Log.i(MY_PRESENCE_HANDLER, "MessageListActivity.onLastOnlineChange: " + e.getMessage());
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
            Class<?> targetClass = Class.forName(getString(R.string.target_notification_parent_activity));
            intent = new Intent(this, targetClass);
        } catch (ClassNotFoundException e) {
            String errorMessage = "cannot retrieve notification target acticity class. " + e.getMessage();
            Log.e(TAG, errorMessage);
        }

        return intent;
    }

    @Override
    protected void onStop() {
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        super.onStop();
    }

    private String getConversationIdFromPushNotification(Intent pushData) {
        String conversationId = pushData.getStringExtra("conversationId");
        isGroupConversation = false;

        // retrieve the group_id
        try {
            String groupId = pushData.getStringExtra("group_id");
            if (StringUtils.isValid(groupId)) {
                conversationId = groupId;
                isGroupConversation = true;
            } else {
                Log.w(TAG, "group_id is empty or null. ");
            }
        } catch (Exception e) {
            Log.w(TAG, "cannot retrieve group_id. it may not exist" + e.getMessage());
        }

        return conversationId;
    }

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
}