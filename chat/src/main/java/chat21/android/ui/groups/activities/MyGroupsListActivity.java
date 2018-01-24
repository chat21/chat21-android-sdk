package chat21.android.ui.groups.activities;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.groups.models.ChatGroup;
import chat21.android.core.users.models.ChatUser;
import chat21.android.core.users.models.IChatUser;
import chat21.android.core.groups.GroupsDAO;
import chat21.android.core.groups.OnGroupsRetrievedCallback;
import chat21.android.ui.ChatUI;
import chat21.android.ui.groups.adapters.MyGroupsListAdapter;
import chat21.android.ui.groups.listeners.OnGroupClickListener;
import chat21.android.ui.messages.activities.MessageListActivity;
import chat21.android.utils.StringUtils;

import static chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public class MyGroupsListActivity extends AppCompatActivity implements OnGroupsRetrievedCallback,
        OnGroupClickListener {

    private GroupsDAO mGroupsDAO;
    private Toolbar mToolbar;
    private RecyclerView mMyGroupsListRecyclerView;
    private MyGroupsListAdapter mMyGroupsListRecyclerAdapter;
    private RelativeLayout mEmptyLayout;

    private List<ChatGroup> mChatGroupList = new ArrayList<>();

    private ProgressBar mProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_GROUPS, "MyGroupsListActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups_list);

        registerViews();

        initViews();

        mGroupsDAO = new GroupsDAO();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mProgress != null)
            mProgress.setVisibility(View.VISIBLE);

        retrieveGroupsForUser(ChatManager.getInstance().getLoggedUser().getId());
    }

    private void registerViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mMyGroupsListRecyclerView = (RecyclerView) findViewById(R.id.list);
        mEmptyLayout = (RelativeLayout) findViewById(R.id.layout_no_groups);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void initViews() {
        initToolbar();

        if (mProgress != null)
            mProgress.setVisibility(View.VISIBLE);

        // grid facebook like
//        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
//        mMyGroupsListRecyclerView.setLayoutManager(mLayoutManager);

        mMyGroupsListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateAdapter(mChatGroupList);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updateAdapter(List<ChatGroup> chatGroups) {
        if (mMyGroupsListRecyclerAdapter == null) {
            mMyGroupsListRecyclerAdapter = new MyGroupsListAdapter(this, chatGroups);
            mMyGroupsListRecyclerAdapter.setOnGroupClickListener(this);
            mMyGroupsListRecyclerView.setAdapter(mMyGroupsListRecyclerAdapter);
        } else {
            mMyGroupsListRecyclerAdapter.setList(chatGroups);
            mMyGroupsListRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void retrieveGroupsForUser(String userId) {
        Log.d(DEBUG_GROUPS, "MyGroupsListActivity.retrieveGroupsForUser: userId == " + userId);

        if (mGroupsDAO == null)
            mGroupsDAO = new GroupsDAO();
        mGroupsDAO.getGroupsForUser(userId, this);
    }


    @Override
    public void onGroupsRetrievedSuccess(List<ChatGroup> chatGroups) {
        if (chatGroups != null && chatGroups.size() > 0) {
            Log.d(DEBUG_GROUPS, "MyGroupsListActivity.onGroupsRetrievedSuccess: " +
                    "chatGroups == " + chatGroups.toString());

            if (mProgress != null)
                mProgress.setVisibility(View.GONE);

            mMyGroupsListRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);

            updateAdapter(chatGroups);
        } else {
            Log.e(DEBUG_GROUPS, "MyGroupsListActivity.onGroupsRetrievedSuccess: " +
                    "groupsIds is empty or null");
            onGroupsRetrievedError(new Exception("groupsIds is empty or null"));
        }
    }

    @Override
    public void onGroupsRetrievedError(Exception e) {
        Log.e(DEBUG_GROUPS, "MyGroupsListActivity.onGroupsRetrievedError" + e.getMessage());

        if (mProgress != null)
            mProgress.setVisibility(View.GONE);

        mMyGroupsListRecyclerView.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void onGroupClicked(final ChatGroup chatGroup, int position) {
        Log.d(DEBUG_GROUPS, "MyGroupsListActivity.onGroupClicked: " +
                "chatGroup == " + chatGroup.toString() + ", position == " + position);

        DatabaseReference nodeConversation;

        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            nodeConversation = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("apps/" + ChatManager.getInstance().getAppId()
                            + "/users/" + ChatManager.getInstance().getLoggedUser().getId()
                            + "/conversations/" + chatGroup.getGroupId());
        } else {
            nodeConversation = FirebaseDatabase.getInstance().getReference()
                    .child("apps/" + ChatManager.getInstance().getAppId()
                            + "/users/" + ChatManager.getInstance().getLoggedUser().getId()
                            + "/conversations/" + chatGroup.getGroupId());
        }

        Log.d(DEBUG_GROUPS, "MyGroupsListActivity.onGroupsRetrievedSuccess" +
                ".addValueEventListener.onDataChange: " +
                "nodeConversation == " + nodeConversation.toString());


        nodeConversation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_GROUPS, "MyGroupsListActivity.onGroupsRetrievedSuccess" +
                        ".addValueEventListener.onDataChange: " +
                        "dataSnapshot == " + dataSnapshot.toString());

                IChatUser groupRecipient = new ChatUser(chatGroup.getGroupId(), chatGroup.getName());

                // start the message list activity
                Intent intent = new Intent(MyGroupsListActivity.this, MessageListActivity.class);
                intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupRecipient);
                intent.putExtra(ChatUI.BUNDLE_IS_FROM_NOTIFICATION, false);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(DEBUG_GROUPS, "MyGroupsListActivity.onGroupClicked" +
                        ".addValueEventListener.onDataChange: " + databaseError.getMessage());

                onGroupsRetrievedError(new Exception(databaseError.getMessage()));
            }
        });
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