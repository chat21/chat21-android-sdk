package chat21.android.groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import  chat21.android.R;
import  chat21.android.connectivity.AbstractNetworkReceiver;
import chat21.android.core.ChatManager;
import  chat21.android.groups.adapters.GroupMembersListAdapter;
import  chat21.android.groups.fragments.BottomSheetGroupAdminPanelMemberFragment;
import  chat21.android.groups.models.Group;
import  chat21.android.groups.utils.GroupUtils;
import  chat21.android.user.models.ChatUser;
import  chat21.android.user.models.IChatUser;
import  chat21.android.utils.TimeUtils;

/**
 * Created by stefanodp91 on 29/06/17.
 */
public class GroupAdminPanelActivity extends AppCompatActivity implements
        GroupMembersListAdapter.OnRecyclerItemClickListener<IChatUser>,
        GroupUtils.OnNodeMembersChangeListener,
        GroupUtils.OnGroupsChangeListener {
    private static final String TAG = GroupAdminPanelActivity.class.getName();

    public static final String EXTRAS_GROUP_NAME = "EXTRAS_GROUP_NAME";
    public static final String EXTRAS_GROUP_ID = "EXTRAS_GROUP_ID";

    private Toolbar mToolbar;
    private RecyclerView mMemberList;
    private LinearLayoutManager mMemberLayoutManager;
    private GroupMembersListAdapter mGroupMembersListAdapter;
    private ImageView mGroupImage;
    private LinearLayout mBoxAddMember;
    private LinearLayout mBoxMembers;
    private LinearLayout mBoxUnavailableMembers;

    private Group mGroup;

    private MenuItem mAddMemberMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_admin_panel);

        registerViews();
        initViews();
    }

    @Override
    protected void onResume() {
        // observes for group changes
        GroupUtils.subscribeOnGroupsChanges(this, getGroupId(), this);

        // observes for members change
        GroupUtils.subscribeOnNodeMembersChanges(this, getGroupId(), this);

        super.onResume();
    }

    private void registerViews() {
        Log.d(TAG, "registerViews");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mMemberList = (RecyclerView) findViewById(R.id.members);
        mGroupImage = (ImageView) findViewById(R.id.image);
        mBoxAddMember = (LinearLayout) findViewById(R.id.box_add_member);
        mBoxMembers = (LinearLayout) findViewById(R.id.box_members);
        mBoxUnavailableMembers = (LinearLayout) findViewById(R.id.box_unavailable_members);
    }

    private void initViews() {
        Log.d(TAG, "initViews");

        intiToolbar();

        initRecyclerViewMembers();

        toggleAddMemberButtons();
    }

    private void intiToolbar() {
        Log.d(TAG, "intiToolbar");

        mToolbar.setTitle(getGroupName());

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRecyclerViewMembers() {
        Log.d(TAG, "initRecyclerViewMembers");

        mMemberLayoutManager = new LinearLayoutManager(this);
        mMemberList.setLayoutManager(mMemberLayoutManager);
    }


    private void toggleAddMemberButtons() {
        Log.d(TAG, "toggleAddMemberButtons");

        if (mGroup != null) {
            // the user is the admin of the group
            // and the user is a member of the group
            if (GroupUtils.isAnAdmin(mGroup, ChatManager.getInstance().getLoggedUser().getId())) {
                showAddMember();
            } else {
                hideAddMember();
            }
        } else {
            GroupUtils.subscribeOnGroupsChanges(this, getGroupId(),
                    new GroupUtils.OnGroupsChangeListener() {
                        @Override
                        public void onGroupChanged(Group group, String groupId) {
                            mGroup = group;

                            // the user is the admin of the group
                            // and the user is a member of the group
                            if (GroupUtils.isAnAdmin(mGroup, ChatManager.getInstance().getLoggedUser().getId())) {
                                showAddMember();
                            } else {
                                hideAddMember();
                            }
                        }

                        @Override
                        public void onGroupCancelled(String errorMessage) {
                            Log.e(TAG, errorMessage);
                        }
                    });
        }
    }


    private void showAddMember() {
        Log.d(TAG, "showAddMember");

        // shows the add member box
        mBoxAddMember.setVisibility(View.VISIBLE);

        // hides the add member menu item
        if (mAddMemberMenuItem != null)
            mAddMemberMenuItem.setVisible(true);

        // set the click listener
        mBoxAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (AbstractNetworkReceiver.isConnected(getApplicationContext())) {
                    startAddMemberActivity();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.menu_activity_group_admin_panel_activity_cannot_add_member_offline),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void hideAddMember() {
        Log.d(TAG, "hideAddMember");

        // hides the add member box
        mBoxAddMember.setVisibility(View.GONE);

        // hides the add member menu item
        if (mAddMemberMenuItem != null)
            mAddMemberMenuItem.setVisible(false);

        // unset the click listener
        mBoxAddMember.setOnClickListener(null);
    }

    private void initCreatedByOn(Group group) {
        Log.d(TAG, "initCreatedByOn");

        TextView mCreatedByOn = (TextView) findViewById(R.id.created_by_on);
        String createdBy = group.getOwner();

        try {
            for (IChatUser mUser : ChatManager.getInstance().getContacts()) {
                if (group != null && group.getMembers() != null) {
                    if (group.getOwner().equals(mUser.getId())) {
                        createdBy = mUser.getFullName();
                    } else if (group.getOwner().equals(ChatManager.getInstance().getLoggedUser().getId())) {
                        createdBy = getString(R.string.activity_group_admin_panel_you_label);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "cannot retrive created by/on. " + e.getMessage());
        }

        try {
            String timestamp = TimeUtils.getFormattedTimestamp(group.getCreatedOnLong());
            mCreatedByOn.setText(
                    getString(R.string.activity_group_admin_panel_formatted_created_by_on_label,
                            createdBy, timestamp));
        } catch (Exception e) {
            Log.e(TAG, "initCreatedByOn: cannot set the timestamp.");
            mCreatedByOn.setText(getString(R.string.activity_group_admin_panel_not_available_formatted_created_by_on_label));
        }
    }


    private void initCardViewMembers(Group group) {
        Log.d(TAG, "initCardViewMembers");

        List<IChatUser> members = new ArrayList<IChatUser>();

        if (group != null && group.getMembers() != null) {
            mBoxUnavailableMembers.setVisibility(View.GONE);
            mBoxMembers.setVisibility(View.VISIBLE);

            // bugfix Issue #18
            for (Map.Entry<String, Integer> member : group.getMembers().entrySet()) {
                IChatUser mUser = new ChatUser();
                mUser.setId(member.getKey());
                mUser.setFullName(member.getKey());
                members.add(mUser);
            }

            // displays the member list
            updateGroupMemberList(members);

            if (mGroupMembersListAdapter != null) {
                mGroupMembersListAdapter.setGroup(group);
            }
        } else {
            Log.e(TAG, "initCardViewMembers: group is null");
            mBoxMembers.setVisibility(View.GONE);
            mBoxUnavailableMembers.setVisibility(View.VISIBLE);
        }
    }

    private void initGroupImage(String iconURL) {
        Log.d(TAG, "initGroupImage");

        try {
            Glide.with(this)
                    .load(iconURL)
                    .placeholder(R.drawable.ic_group_banner_gray)
                    .centerCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mGroupImage);
        } catch (Exception e) {
            Log.e(TAG, "cannot load group image with Glide. Group image will be used as image drawable.");
            mGroupImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_group_banner_gray));
        }
    }

    private void updateGroupMemberList(List<IChatUser> members) {
        Log.d(TAG, "updateGroupMemberList");

        if (mGroupMembersListAdapter == null) {
            mGroupMembersListAdapter = new GroupMembersListAdapter(this, members);
            mGroupMembersListAdapter.setOnRecyclerItemClickListener(this);
            mMemberList.setAdapter(mGroupMembersListAdapter);
        } else {
            mGroupMembersListAdapter.setList(members);
            mGroupMembersListAdapter.notifyDataSetChanged();
        }
    }

    private String getGroupName() {
        Log.d(TAG, "getGroupName");

        return getIntent().getExtras().getString(EXTRAS_GROUP_NAME);
    }

    private String getGroupId() {
        Log.d(TAG, "getGroupId");

        return getIntent().getExtras().getString(EXTRAS_GROUP_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        getMenuInflater()
                .inflate(R.menu.menu_activity_group_admin_panel, menu);

        mAddMemberMenuItem = menu.findItem(R.id.action_add_member);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_add_member) {
            onAddMemberOptionsItemClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void onAddMemberOptionsItemClicked() {
        Log.d(TAG, "onAddMemberOptionsItemClicked");

        startAddMemberActivity();
    }

    private void startAddMemberActivity() {
        Log.d(TAG, "startAddMemberActivity");

        try {
            // targetClass MUST NOT BE NULL
            Class<?> targetClass = Class
                    .forName(getString(R.string.target_add_members_activity_class));
            final Intent intent = new Intent(this, targetClass);

            if (mGroup != null) {
                intent.putExtra(ChatManager._INTENT_BUNDLE_GROUP, mGroup);
                intent.putExtra(ChatManager._INTENT_EXTRAS_PARENT_ACTIVITY,
                        GroupAdminPanelActivity.class.getName());
                intent.putExtra(ChatManager._INTENT_EXTRAS_GROUP_ID, getGroupId());
//                startActivityForResult(intent, Chat.INTENT_ADD_MEMBERS_ACTIVITY);
                startActivity(intent);
            } else {
                GroupUtils.subscribeOnGroupsChanges(this, getGroupId(),
                        new GroupUtils.OnGroupsChangeListener() {
                            @Override
                            public void onGroupChanged(Group group, String groupId) {
                                mGroup = group;
                                intent.putExtra(ChatManager._INTENT_BUNDLE_GROUP, group);
                                intent.putExtra(ChatManager._INTENT_EXTRAS_PARENT_ACTIVITY,
                                        GroupAdminPanelActivity.class.getName());
                                intent.putExtra(ChatManager._INTENT_EXTRAS_GROUP_ID, groupId);
//                                startActivityForResult(intent, Chat.INTENT_ADD_MEMBERS_ACTIVITY);
                                startActivity(intent);
                            }

                            @Override
                            public void onGroupCancelled(String errorMessage) {
                                Log.e(TAG, "onGroupCancelled. " + errorMessage);
                            }
                        });
            }

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "cannot retrieve the user list activity target class. " +
                    "Message: " + e.getMessage());
        }
    }

    // handles the click on a member
    @Override
    public void onRecyclerItemClicked(IChatUser item, int position) {
        Log.i(TAG, "onRecyclerItemClicked");

        showMemberBottomSheetFragment(item.getId(), getGroupId());
    }

    private void showMemberBottomSheetFragment(String username, String groupId) {
        Log.d(TAG, "showMemberBottomSheetFragment");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BottomSheetGroupAdminPanelMemberFragment dialog =
                BottomSheetGroupAdminPanelMemberFragment.newInstance(username, groupId);
        dialog.show(ft, BottomSheetGroupAdminPanelMemberFragment.TAG);
    }

    // GroupUtils.OnNodeMembersChangeListener
    @Override
    public void onMembersAdded(List<IChatUser> members) {
        Log.d(TAG, "onMemberAddedListener");

        // displays the member list
        updateGroupMemberList(members);
    }

    @Override
    public void onNodeMembersChanged(List<IChatUser> members) {
        Log.d(TAG, "onMemberChangedListener");

        // displays the member list
        updateGroupMemberList(members);
    }

    @Override
    public void onNodeMembersRemoved() {
        Log.d(TAG, "onMembersRemoved");
    }

    @Override
    public void onNodeMembersMoved() {
        Log.d(TAG, "onMembersMoved");
    }

    @Override
    public void onNodeMembersCancelled(String errorMessage) {
        Log.e(TAG, "onMembersCancelled. " + errorMessage);
    }
    // end GroupUtils.OnNodeMembersChangeListener

    // GroupUtils.OnGroupsChangeListener
    @Override
    public void onGroupChanged(Group group, String groupId) {
        Log.d(TAG, "onGroupChanged");

        initCreatedByOn(group);

        initCardViewMembers(group);

        initGroupImage(group.getIconURL());
    }

    @Override
    public void onGroupCancelled(String errorMessage) {
        Log.e(TAG, "onGroupCancelled. " + errorMessage);
    }
    // end GroupUtils.OnGroupsChangeListener


    @Override
    public void onBackPressed() {
        setResult(RESULT_OK); // force update interface of the calling activity
        super.onBackPressed();
    }
}