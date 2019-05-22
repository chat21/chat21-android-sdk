package org.chat21.android.ui.chat_groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.chat21.android.R;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.chat_groups.fragments.ChatGroupsListFragment;
import org.chat21.android.ui.chat_groups.listeners.OnGroupClickListener;
import org.chat21.android.ui.messages.activities.MessageListActivity;

import static org.chat21.android.ui.ChatUI.BUNDLE_CHANNEL_TYPE;

/**
 * Created by stefano on 25/08/2015.
 */
public class ChatGroupsListActivity extends AppCompatActivity implements OnGroupClickListener {
    private static final String TAG = ChatGroupsListActivity.class.getSimpleName();

    private ChatGroupsListFragment contactsListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_groups_list);

        contactsListFragment = new ChatGroupsListFragment();
        contactsListFragment.setOnChatGroupClickListener(this);

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "ContactListActivity.onOptionsItemSelected");

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGroupClicked(ChatGroup chatGroup, int position) {
        IChatUser groupRecipient = new ChatUser(chatGroup.getGroupId(), chatGroup.getName());

        // start the message list activity
        Intent intent = new Intent(ChatGroupsListActivity.this, MessageListActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupRecipient);
        intent.putExtra(BUNDLE_CHANNEL_TYPE, Message.GROUP_CHANNEL_TYPE);
        startActivity(intent);
        finish();
    }
}