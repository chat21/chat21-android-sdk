package org.chat21.android.ui.chat_groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import org.chat21.android.R;
import org.chat21.android.connectivity.AbstractNetworkReceiver;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.chat_groups.listeners.ChatGroupsListener;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.chat_groups.adapters.GroupMembersListAdapter;
import org.chat21.android.ui.chat_groups.fragments.BottomSheetGroupAdminPanelMember;
import org.chat21.android.ui.chat_groups.listeners.OnGroupMemberClickListener;
import org.chat21.android.ui.decorations.ItemDecoration;
import org.chat21.android.utils.TimeUtils;

import static org.chat21.android.ui.ChatUI.BUNDLE_CHAT_GROUP;
import static org.chat21.android.ui.ChatUI.BUNDLE_GROUP_ID;

/**
 * Created by stefanodp91 on 29/06/17.
 */
public class GroupAdminPanelActivity extends AppCompatActivity implements
        OnGroupMemberClickListener, ChatGroupsListener {
    private static final String TAG = GroupAdminPanelActivity.class.getName();

    private RecyclerView mMemberList;
    private GroupMembersListAdapter mGroupMembersListAdapter;
    private ImageView mGroupImage;
    private LinearLayout mBoxAddMember;
    private LinearLayout mBoxMembers;
    private LinearLayout mBoxUnavailableMembers;

    private GroupsSyncronizer groupsSyncronizer;

    private String groupId;
    private ChatGroup chatGroup;
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
        groupsSyncronizer.addGroupsListener(this);
        groupsSyncronizer.connect();

        chatGroup = groupsSyncronizer.getById(groupId);

        groupAdmins = getGroupAdmins();

        setToolbar();
        setCreatedBy();
        setCreatedOn();
        setGroupId();
        initRecyclerViewMembers();
        toggleAddMemberButton();
    }

    @Override
    protected void onDestroy() {

        groupsSyncronizer.removeGroupsListener(this);

        super.onDestroy();
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

        for (IChatUser member : chatGroup.getMembersList()) {
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
        String creator = loggedUser.getId()
                .equals(chatGroup.getOwner()) ? loggedUser.getFullName() : "";
        for (IChatUser member : chatGroup.getMembersList()) {
            if (member.getId().equals(chatGroup.getOwner())) {
                creator = member.getFullName();
                break;
            }
        }

//        String createdBy = getString(R.string.activity_group_admin_panel_formatted_created_by_label, creator);
        createdByView.setText(creator);
    }

    private void setCreatedOn() {
        TextView createdOnView = (TextView) findViewById(R.id.created_on);

        // retrieve the creation date
        String timestamp = TimeUtils.getFormattedTimestamp(this, chatGroup.getCreatedOnLong());

        // format the user creator and creating date string
//        String createOn = getString(R.string.activity_group_admin_panel_formatted_created_on_label, timestamp);

        // show the text
        createdOnView.setText(timestamp);
    }

    private void setGroupId() {
        TextView groupIdView = findViewById(R.id.group_id);
//        String groupId = getString(R.string.activity_group_admin_panel_formatted_group_id_label, chatGroup.getGroupId());
        groupIdView.setText(chatGroup.getGroupId());
    }

    private void initRecyclerViewMembers() {
        Log.d(TAG, "initRecyclerViewMembers");
        mMemberList.addItemDecoration(new ItemDecoration(this,
                DividerItemDecoration.VERTICAL,
                getResources().getDrawable(R.drawable.decorator_activity_group_admin_panel_members_list)));
        mMemberList.setLayoutManager(new LinearLayoutManager(this));
        updateGroupMemberListAdapter(chatGroup.getMembersList());
    }

    private void toggleGroupMembersVisibility() {
        Log.d(TAG, "initCardViewMembers");

        if (chatGroup.getMembersList() != null && chatGroup.getMembersList().size() > 0) {
            mBoxUnavailableMembers.setVisibility(View.GONE);
            mBoxMembers.setVisibility(View.VISIBLE);

        } else {
            Log.e(TAG, "GroupAdminPanelActivity.toggleCardViewMembers: " +
                    "groupMembers is not valid");
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

        toggleGroupMembersVisibility();
    }

    private void toggleAddMemberButton() {
        Log.d(TAG, "toggleAddMemberButton");

        // check if the current user is an admin and a member of the group
        if (groupAdmins.contains(loggedUser) && chatGroup.getMembersList().contains(loggedUser)) {
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

//        Intent intent = new Intent(this, AddMembersToGroupActivity.class);
        Intent intent = new Intent(this, AddMemberToChatGroupActivity.class);
        intent.putExtra(BUNDLE_CHAT_GROUP, chatGroup);
        startActivity(intent);
    }

    // handles the click on a member
    @Override
    public void onGroupMemberClicked(IChatUser member, int position) {
        Log.i(TAG, "onGroupMemberClicked");

        showOnMemberClickedBottomSheet(member, chatGroup);
    }

    private void showOnMemberClickedBottomSheet(IChatUser groupMember, ChatGroup chatGroup) {
        Log.d(TAG, "showOnMemberClickedBottomSheet");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BottomSheetGroupAdminPanelMember dialog = BottomSheetGroupAdminPanelMember
                .newInstance(groupMember, chatGroup);
        dialog.show(ft, BottomSheetGroupAdminPanelMember.TAG);
    }

    @Override
    public void onGroupAdded(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {

            if (chatGroup.getGroupId().equals(groupId)) {
                Log.d(TAG, "GroupAdminPanelActivity.onGroupAdded.chatGroup:" +
                        " chatGroup == " + chatGroup.toString());

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
                Log.d(TAG, "GroupAdminPanelActivity.onGroupChanged.chatGroup: " +
                        "chatGroup == " + chatGroup.toString());

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