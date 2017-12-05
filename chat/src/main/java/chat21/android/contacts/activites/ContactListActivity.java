package chat21.android.contacts.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
import chat21.android.adapters.AbstractRecyclerAdapter;
import chat21.android.connectivity.AbstractNetworkReceiver;
import chat21.android.contacts.adapters.ContactListAdapter;
import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.core.ChatManager;
import chat21.android.groups.activities.CreateGroupActivity;
import chat21.android.messages.activites.MessageListActivity;
import chat21.android.user.models.IChatUser;
import chat21.android.utils.ChatUtils;
import chat21.android.utils.glide.CropCircleTransformation;


/**
 * Created by stefano on 25/08/2015.
 */
public class ContactListActivity extends AppCompatActivity
        implements AbstractRecyclerAdapter.OnRecyclerItemClickListener<IChatUser> {
    private static final String TAG = ContactListActivity.class.getName();

    private ContactListAdapter contactListAdapter;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private LinearLayout mBoxCreateGroup; // bugfix Issue #17
    private ImageView mGroupIcon;
    private RelativeLayout mEmptyLayout;

    private interface OnGroupSettingEnabledCallback {
        void onGroupSettingEnabledCallback(View boxGroup);
    }

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

        enableGroups(new OnGroupSettingEnabledCallback() {
            @Override
            public void onGroupSettingEnabledCallback(View boxGroup) {
                Glide.with(getApplicationContext())
                        .load("")
                        .placeholder(R.drawable.ic_group_avatar)
                        .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                        .into(mGroupIcon);

                // box click
                boxGroup.setOnClickListener(new View.OnClickListener() {
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
            }
        });
    }

    private void initEmptyLayout() {
        TextView mSubTitle = (TextView) mEmptyLayout.findViewById(R.id.error_subtitle);
        mSubTitle.setVisibility(View.GONE);
    }

    // bugfix Issue #17
    private void enableGroups(OnGroupSettingEnabledCallback callback) {
        Log.d(TAG, "enableGroups");


        if (ChatUtils.areGroupsEnabled(this)) {
            Log.d(TAG, "groups enabled");

            mBoxCreateGroup.setVisibility(View.VISIBLE);

            callback.onGroupSettingEnabledCallback(mBoxCreateGroup);
        } else {
            Log.d(TAG, "groups not enabled");

            mBoxCreateGroup.setVisibility(View.GONE);
        }
    }

    private void updateAdapter(List<IChatUser> contacts) {
        Log.d(TAG, "updateAdapter");

        toggleEmptyLayout(contacts);

        if (contactListAdapter == null) {
            contactListAdapter = new ContactListAdapter(this, contacts);
            contactListAdapter.setOnRecyclerItemClickListener(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        inflateGroupCreationToolbarMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_contact_list_create_group) {
            startCreateGroupActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    // check if the creation menu settings is enabled
    // if is enabled inflate the creation menu
    private void inflateGroupCreationToolbarMenu(Menu menu) {
        Log.d(TAG, "inflateGroupCreationToolbarMenu");

        if (ChatUtils.areGroupsEnabled(this)) {
            Log.d(TAG, "groups enabled");
            getMenuInflater().inflate(R.menu.menu_activity_contact_list, menu);
        }
    }

    private void startCreateGroupActivity() {
        Log.d(TAG, "startCreateGroupActivity");

        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivityForResult(intent, ChatManager._REQUEST_CODE_CREATE_GROUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ChatManager._REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public void onRecyclerItemClicked(IChatUser contact, int position) {
        Log.d(TAG, "onRecyclerItemClicked");
        Log.d(TAG, "contact: " + contact.toString() + ", position: " + position);

        if (ChatManager.getInstance().getOnContactClickListener() != null) {
            ChatManager.getInstance().getOnContactClickListener().onContactClick(contact);
        }

        String loggedUserId = ChatManager.getInstance().getLoggedUser().getId();
        String contactId = contact.getId();
        String conversationId = ConversationUtils.getConversationId(loggedUserId, contactId);

        // start the conversation activity
        startMessageListActivity(conversationId);
    }

    private void startMessageListActivity(String conversationId) {
        Log.d(TAG, "startMessageListActivity");

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra(ChatManager._INTENT_BUNDLE_CONVERSATION_ID, conversationId);
        intent.putExtra(ChatManager.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);

        // put this flag to start activity without an activity (using context instead of activity)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

        // finish the contact list activity when it start a new conversation
        finish();
    }
}