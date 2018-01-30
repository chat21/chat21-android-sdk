package chat21.android.ui.chat_groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import chat21.android.R;
import chat21.android.connectivity.AbstractNetworkReceiver;
import chat21.android.core.ChatManager;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.core.chat_groups.listeners.ChatGroupsListener;
import chat21.android.core.chat_groups.models.ChatGroup;
import chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.chat_groups.adapters.GroupMembersListAdapter;
import chat21.android.ui.chat_groups.fragments.BottomSheetGroupAdminPanelMemberFragment;
import chat21.android.ui.chat_groups.listeners.OnGroupMemberClickListener;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.image.CropCircleTransformation;

import static chat21.android.ui.ChatUI.BUNDLE_CHAT_GROUP;
import static chat21.android.ui.ChatUI.BUNDLE_GROUP_ID;

/**
 * Created by stefanodp91 on 29/06/17.
 */
public class GroupAdminPanelActivity extends AppCompatActivity implements OnGroupMemberClickListener, ChatGroupsListener {
    private static final String TAG = GroupAdminPanelActivity.class.getName();

    private RecyclerView mMemberList;
    private LinearLayoutManager mMemberLayoutManager;
    private GroupMembersListAdapter mGroupMembersListAdapter;
    private ImageView mGroupImage;
    private LinearLayout mBoxAddMember;
    private LinearLayout mBoxMembers;
    private LinearLayout mBoxUnavailableMembers;

    private GroupsSyncronizer groupsSyncronizer;

    private String groupId;
    private ChatGroup chatGroup;
    private List<IChatUser> groupMembers;
    private List<IChatUser> groupAdmins;

    private IChatUser loggedUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_admin_panel);
        registerViews();

        loggedUser = ChatManager.getInstance().getLoggedUser();

        // retrieve the groupId (from MessageListActivity)
        groupId = getIntent().getStringExtra(BUNDLE_GROUP_ID);

        this.groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();

        chatGroup = groupsSyncronizer.getById(groupId);
        groupsSyncronizer.addGroupsListener(this);

        if (chatGroup != null) {
            groupsSyncronizer.connect();

            groupMembers = chatGroup.getMembersList();
            groupAdmins = getGroupAdmins();

            setToolbar();
            setCreatedBy();
            setCreatedOn();
            initRecyclerViewMembers();
            toggleAddMemberButtons();
        }
    }

    private void registerViews() {
        Log.d(TAG, "registerViews");

        mMemberList = (RecyclerView) findViewById(R.id.members);
        mGroupImage = (ImageView) findViewById(R.id.image);
        mBoxAddMember = (LinearLayout) findViewById(R.id.box_add_member);
        mBoxMembers = (LinearLayout) findViewById(R.id.box_members);
        mBoxUnavailableMembers = (LinearLayout) findViewById(R.id.box_unavailable_members);
    }

    private List<IChatUser> getGroupAdmins() {
        List<IChatUser> admins = new ArrayList<>();

        String owner = chatGroup.getOwner(); // it always exists

        for (IChatUser member : groupMembers) {
            if (member.getId().equals(owner)) {
                admins.add(member);
                break;
            }
        }

        return admins;
    }

    private void setToolbar() {
        Log.d(TAG, "GroupAdminPanelActivity.setToolbar");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // chatGroup name
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(chatGroup.getName());

//        TextView toolbarSubTitle = findViewById(R.id.toolbar_subtitle);
//        toolbarSubTitle.setText("");

        // chatGroup picture
        Glide.with(getApplicationContext())
                .load(chatGroup.getIconURL())
                .placeholder(R.drawable.ic_group_avatar)
                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .into(mGroupImage);

        // minimal settings
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setCreatedBy() {
        Log.d(TAG, "GroupAdminPanelActivity.setCreatedByOn");

        TextView createdByView = (TextView) findViewById(R.id.created_by);

        // if the creator of the chatGroup is the logged user set it
        // otherwise retrieve the chatGroup creator from the chatGroup member list
        String creator = loggedUser.getId().equals(chatGroup.getOwner()) ? loggedUser.getFullName() : "";
        for (IChatUser member : groupMembers) {
            if (member.getId().equals(chatGroup.getOwner())) {
                creator = member.getFullName();
                break;
            }
        }

        String createdBy = getString(R.string.activity_group_admin_panel_formatted_created_by_label, creator);
        createdByView.setText(createdBy);
    }

    private void setCreatedOn() {

        TextView createdOnView = (TextView) findViewById(R.id.created_on);

        // retrieve the creation date
        String timestamp = TimeUtils.getFormattedTimestamp(chatGroup.getCreatedOnLong());

        // format the user creator and creating date string
        String createOn = getString(R.string.activity_group_admin_panel_formatted_created_on_label, timestamp);

        // show the text
        createdOnView.setText(createOn);
    }

    private void initRecyclerViewMembers() {
        Log.d(TAG, "initRecyclerViewMembers");

        mMemberLayoutManager = new LinearLayoutManager(this);
        mMemberList.setLayoutManager(mMemberLayoutManager);

        toggleGroupMembersVisibility();
    }

    private void toggleGroupMembersVisibility() {
        Log.d(TAG, "initCardViewMembers");

        if (groupMembers != null && groupMembers.size() > 0) {
            mBoxUnavailableMembers.setVisibility(View.GONE);
            mBoxMembers.setVisibility(View.VISIBLE);

            // displays the member list
            updateGroupMemberListAdapter(groupMembers);
        } else {
            Log.e(TAG, "GroupAdminPanelActivity.toggleCardViewMembers: groupMembers is not valid");
            mBoxMembers.setVisibility(View.GONE);
            mBoxUnavailableMembers.setVisibility(View.VISIBLE);
        }
    }

    private void updateGroupMemberListAdapter(List<IChatUser> members) {
        Log.d(TAG, "updateGroupMemberListAdapter");

        if (mGroupMembersListAdapter == null) {
            mGroupMembersListAdapter = new GroupMembersListAdapter(this, members);
            mGroupMembersListAdapter.setOnGroupMemberClickListener(this);
            mMemberList.setAdapter(mGroupMembersListAdapter);
        } else {
            mGroupMembersListAdapter.setList(members);
            mGroupMembersListAdapter.notifyDataSetChanged();
        }

        for (IChatUser admin : groupAdmins) {
            mGroupMembersListAdapter.addAdmin(admin);
        }
    }

    private void toggleAddMemberButtons() {
        Log.d(TAG, "toggleAddMemberButtons");

        // check if the current user is an admin and a member of the group
        if (groupAdmins.contains(loggedUser) && groupMembers.contains(loggedUser)) {
            showAddMember();
        } else {
            hideAddMember();
        }
    }

    private void showAddMember() {
        Log.d(TAG, "GroupAdminPanelActivity.showAddMember");

        // shows the add member box
        mBoxAddMember.setVisibility(View.VISIBLE);

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
        Log.d(TAG, "GroupAdminPanelActivity.hideAddMember");

        // hides the add member box
        mBoxAddMember.setVisibility(View.GONE);

        // unset the click listener
        mBoxAddMember.setOnClickListener(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void startAddMemberActivity() {
        Log.d(TAG, "startAddMemberActivity");

//        Intent intent = new Intent(this, AddMembersActivity.class);
        Intent intent = new Intent(this, AddMembersToGroupActivity.class);
        intent.putExtra(BUNDLE_CHAT_GROUP, chatGroup);
        startActivity(intent);
    }

    // handles the click on a member
    @Override
    public void onGroupMemberClicked(IChatUser member, int position) {
        Log.i(TAG, "onGroupMemberClicked");

        showMemberBottomSheetFragment(member, chatGroup);
    }

    private void showMemberBottomSheetFragment(IChatUser groupMember, ChatGroup chatGroup) {
        Log.d(TAG, "showMemberBottomSheetFragment");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BottomSheetGroupAdminPanelMemberFragment dialog =
                BottomSheetGroupAdminPanelMemberFragment.newInstance(groupMember, chatGroup);
        dialog.show(ft, BottomSheetGroupAdminPanelMemberFragment.TAG);
    }

    @Override
    public void onGroupAdded(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {

            if (chatGroup.getGroupId().equals(groupId)) {
                Log.d(TAG, "GroupAdminPanelActivity.onGroupAdded.chatGroup: " + chatGroup.toString());

                this.chatGroup = chatGroup;

                updateGroupMemberListAdapter(chatGroup.getMembersList()); // update members
            }
        } else {
            Log.e(TAG, "GroupAdminPanelActivity.onGroupAdded: " + e.toString());
        }
    }

    @Override
    public void onGroupChanged(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {

            this.chatGroup = chatGroup;

            if (chatGroup.getGroupId().equals(groupId)) {
                Log.d(TAG, "GroupAdminPanelActivity.onGroupChanged.chatGroup: " + chatGroup.toString());

                updateGroupMemberListAdapter(chatGroup.getMembersList()); // update members
            }
        } else {
            Log.e(TAG, "GroupAdminPanelActivity.onGroupChanged: " + e.toString());
        }
    }

    @Override
    public void onGroupRemoved(ChatRuntimeException e) {
        if (e == null) {
            Log.d(TAG, "GroupAdminPanelActivity.onGroupRemoved");
        } else {
            Log.e(TAG, "GroupAdminPanelActivity.onGroupRemoved: " + e.toString());
        }
    }
}