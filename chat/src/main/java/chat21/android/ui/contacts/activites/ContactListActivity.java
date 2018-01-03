package chat21.android.ui.contacts.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import chat21.android.R;
import chat21.android.connectivity.AbstractNetworkReceiver;
import chat21.android.core.ChatManager;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.messages.models.Message;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.ChatUI;
import chat21.android.ui.contacts.adapters.ContactListAdapter;
import chat21.android.ui.contacts.listeners.OnContactClickListener;
import chat21.android.ui.groups.activities.CreateGroupActivity;
import chat21.android.ui.messages.activities.MessageListActivity;
import chat21.android.utils.ChatUtils;
import chat21.android.utils.image.CropCircleTransformation;


/**
 * Created by stefano on 25/08/2015.
 */
public class ContactListActivity extends AppCompatActivity
        implements OnContactClickListener {
    private static final String TAG = ContactListActivity.class.getName();

    private ContactListAdapter contactListAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private LinearLayout mBoxCreateGroup; // bugfix Issue #17
    private ImageView mGroupIcon;
    private RelativeLayout mEmptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        registerViews();
        initViews();
    }

    private void registerViews() {
        Log.d(TAG, "registerViews");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.user_list);
        mBoxCreateGroup = (LinearLayout) findViewById(R.id.box_create_group);// bugfix Issue #17
        mGroupIcon = (ImageView) findViewById(R.id.group_icon);
        mEmptyLayout = (RelativeLayout) findViewById(R.id.layout_no_contacts);
    }

    private void initViews() {
        Log.d(TAG, "initViews");

        initToolbar();
        initContactRecyclerView();
        initBoxCreateGroup();
        initEmptyLayout();
    }

    private void initToolbar() {
        Log.d(TAG, "initToolbar");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initContactRecyclerView() {
        Log.d(TAG, "initContactRecyclerView");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(getResources().getDrawable(R.drawable.decorator_contact_list));
        recyclerView.addItemDecoration(itemDecorator);
        updateAdapter(ChatManager.getInstance().getContacts());
    }

    private void initBoxCreateGroup() {
        Log.d(TAG, "initBoxCreateGroup");

        if (ChatUtils.areGroupsEnabled(this)) {
            Glide.with(getApplicationContext())
                    .load("")
                    .placeholder(R.drawable.ic_group_avatar)
                    .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                    .into(mGroupIcon);

            // box click
            mBoxCreateGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AbstractNetworkReceiver.isConnected(getApplicationContext())) {
                        startCreateGroupActivity();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.activity_contact_list_error_cannot_create_group_offline_label),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mBoxCreateGroup.setVisibility(View.VISIBLE);
        } else {
            mBoxCreateGroup.setVisibility(View.GONE);
        }
    }

    private void initEmptyLayout() {
        TextView mSubTitle = (TextView) mEmptyLayout.findViewById(R.id.error_subtitle);
        mSubTitle.setVisibility(View.GONE);
    }

    private void updateAdapter(List<IChatUser> contacts) {
        Log.d(TAG, "updateAdapter");

        toggleEmptyLayout(contacts);

        if (contactListAdapter == null) {
            contactListAdapter = new ContactListAdapter(this, contacts);
            contactListAdapter.setOnContactClickListener(this);
            recyclerView.setAdapter(contactListAdapter);
        } else {
            contactListAdapter.notifyDataSetChanged();
        }
    }

    private void toggleEmptyLayout(List<IChatUser> contacts) {
        if (contacts != null && contacts.size() > 0) {
            mEmptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            mEmptyLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startCreateGroupActivity() {
        Log.d(TAG, "startCreateGroupActivity");

        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivityForResult(intent, ChatUI._REQUEST_CODE_CREATE_GROUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ChatUI._REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public void onContactClicked(IChatUser contact, int position) {
        Log.d(TAG, "onRecyclerItemClicked");
        Log.d(TAG, "contact: " + contact.toString() + ", position: " + position);

        if (ChatUI.getInstance().getOnContactClickListener() != null) {
            ChatUI.getInstance().getOnContactClickListener().onContactClicked(contact, position);
        }

        // TODO: 27/12/17 check
        Conversation conversation = new Conversation();
        conversation.setSender(ChatManager.getInstance().getLoggedUser().getId());
        conversation.setSender_fullname(ChatManager.getInstance().getLoggedUser().getFullName());
        conversation.setConvers_with(contact.getId());
        conversation.setConvers_with_fullname(contact.getFullName());
        conversation.setRecipient(contact.getId());
        conversation.setRecipientFullName(contact.getFullName());
        conversation.setChannelType(Message.DIRECT_CHANNEL_TYPE);

        String conversationId = contact.getId();

        // start the conversation activity
        startMessageListActivity(conversationId, conversation, contact.getFullName());
    }

    private void startMessageListActivity(String conversationId, Conversation conversation, String contactFullName) {
        Log.d(TAG, "startMessageListActivity");

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra(ChatUI.INTENT_BUNDLE_RECIPIENT_ID, conversationId);
        intent.putExtra(ChatUI.INTENT_BUNDLE_CONVERSATION, conversation);
//        intent.putExtra(ChatUI.INTENT_BUNDLE_CONTACT_FULL_NAME, contactFullName);
        intent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);

        // put this flag to start activity without an activity (using context instead of activity)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        // finish the contact list activity when it start a new conversation
        finish();
    }
}