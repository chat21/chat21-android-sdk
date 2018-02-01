package chat21.android.ui.chat_groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.List;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.chat_groups.models.ChatGroup;
import chat21.android.core.messages.models.Message;
import chat21.android.core.users.models.ChatUser;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.ChatUI;
import chat21.android.ui.chat_groups.adapters.MyGroupsListAdapter;
import chat21.android.ui.chat_groups.listeners.OnGroupClickListener;
import chat21.android.ui.messages.activities.MessageListActivity;

import static chat21.android.ui.ChatUI.BUNDLE_CHANNEL_TYPE;
import static chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public class MyGroupsListActivity extends AppCompatActivity implements OnGroupClickListener {

    private RecyclerView mMyGroupsListRecyclerView;
    private MyGroupsListAdapter mMyGroupsListRecyclerAdapter;
    private RelativeLayout layoutNoGroups;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups_list);

        //////// toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //////// end toolbar

        layoutNoGroups = findViewById(R.id.layout_no_groups);

        //////// recycler view
        mMyGroupsListRecyclerView = (RecyclerView) findViewById(R.id.list);
        mMyGroupsListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateAdapter(ChatManager.getInstance().getGroupsSyncronizer().getChatGroups());
        //////// end recycler view
    }

    private void updateAdapter(List<ChatGroup> chatGroups) {

        if (chatGroups.size() > 0) {
            mMyGroupsListRecyclerView.setVisibility(View.VISIBLE);
            layoutNoGroups.setVisibility(View.GONE);
        } else {
            mMyGroupsListRecyclerView.setVisibility(View.GONE);
            layoutNoGroups.setVisibility(View.VISIBLE);
        }

        if (mMyGroupsListRecyclerAdapter == null) {
            mMyGroupsListRecyclerAdapter = new MyGroupsListAdapter(this, chatGroups);
            mMyGroupsListRecyclerAdapter.setOnGroupClickListener(this);
            mMyGroupsListRecyclerView.setAdapter(mMyGroupsListRecyclerAdapter);
        } else {
            mMyGroupsListRecyclerAdapter.setList(chatGroups);
            mMyGroupsListRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGroupClicked(final ChatGroup chatGroup, int position) {
        Log.d(DEBUG_GROUPS, "MyGroupsListActivity.onGroupClicked: " +
                "chatGroup == " + chatGroup.toString() + ", position == " + position);

        IChatUser groupRecipient = new ChatUser(chatGroup.getGroupId(), chatGroup.getName());

        // start the message list activity
        Intent intent = new Intent(MyGroupsListActivity.this, MessageListActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupRecipient);
        intent.putExtra(BUNDLE_CHANNEL_TYPE, Message.GROUP_CHANNEL_TYPE);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}