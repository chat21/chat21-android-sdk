package org.chat21.android.ui.contacts.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.chat21.android.R;
import org.chat21.android.connectivity.AbstractNetworkReceiver;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.chat_groups.activities.AddMemberToChatGroupActivity;
import org.chat21.android.ui.contacts.fragments.ContactsListFragment;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;
import org.chat21.android.ui.messages.activities.MessageListActivity;
import org.chat21.android.utils.image.CropCircleTransformation;

import static org.chat21.android.ui.ChatUI.REQUEST_CODE_CREATE_GROUP;

/**
 * Created by stefano on 25/08/2015.
 */
public class ContactListActivity extends AppCompatActivity implements OnContactClickListener {
    private static final String TAG = ContactListActivity.class.getSimpleName();

    private ImageView mGroupIcon;
    private LinearLayout mBoxCreateGroup;
    private ContactsListFragment contactsListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        contactsListFragment = new ContactsListFragment();
        contactsListFragment.setOnContactClickListener(this);

        // #### BEGIN TOOLBAR ####
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // #### END  TOOLBAR ####

        // #### BEGIN CONTAINER ####
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, contactsListFragment)
                .commit();
        // #### BEGIN CONTAINER ####

        // #### BEGIN BOX CREATE GROUP ####
        mBoxCreateGroup = (LinearLayout) findViewById(R.id.box_create_group);
        mGroupIcon = (ImageView) findViewById(R.id.group_icon);
        initBoxCreateGroup();
        // #### BEGIN BOX CREATE GROUP ####
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "ContactListActivity.onOptionsItemSelected");

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        contactsListFragment.onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onContactClicked(IChatUser contact, int position) {
        Log.d(TAG, "ContactListActivity.onRecyclerItemClicked:" +
                " contact == " + contact.toString() + ", position ==  " + position);

        if (ChatUI.getInstance().getOnContactClickListener() != null) {
            ChatUI.getInstance().getOnContactClickListener().onContactClicked(contact, position);
        }

        // start the conversation activity
        startMessageListActivity(contact);
    }

    private void startMessageListActivity(IChatUser contact) {
        Log.d(TAG, "ContactListActivity.startMessageListActivity");

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, contact);
        intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.DIRECT_CHANNEL_TYPE);

        startActivity(intent);

        // finish the contact list activity when it start a new conversation
        finish();
    }

    private void initBoxCreateGroup() {
        Log.d(TAG, "ContactListActivity.initBoxCreateGroup");

        if (ChatUI.getInstance().areGroupsEnabled()) {
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

                        if (ChatUI.getInstance().getOnCreateGroupClickListener() != null) {
                            ChatUI.getInstance().getOnCreateGroupClickListener()
                                    .onCreateGroupClicked();
                        }

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

    private void startCreateGroupActivity() {
        Log.d(TAG, "ContactListActivity.startCreateGroupActivity");

//        Intent intent = new Intent(this, AddMembersToGroupActivity.class);
        Intent intent = new Intent(this, AddMemberToChatGroupActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CREATE_GROUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }
}