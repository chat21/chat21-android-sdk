package chat21.android.ui.groups.activities;

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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.contacts.synchronizers.ContactsSynchronizer;
import chat21.android.core.groups.models.ChatGroup;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.groups.adapters.GroupMembersListAdapter;
import chat21.android.ui.groups.fragments.BottomSheetGroupAdminPanelMemberFragment;
import chat21.android.ui.groups.listeners.OnGroupMemberClickListener;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.image.CropCircleTransformation;

import static chat21.android.ui.ChatUI.BUNDLE_GROUP_ID;

/**
 * Created by stefanodp91 on 29/06/17.
 */
public class GroupAdminPanelActivity extends AppCompatActivity implements OnGroupMemberClickListener {
    private static final String TAG = GroupAdminPanelActivity.class.getName();

    private RecyclerView mMemberList;
    private LinearLayoutManager mMemberLayoutManager;
    private GroupMembersListAdapter mGroupMembersListAdapter;
    private ImageView mGroupImage;
    private LinearLayout mBoxAddMember;
    private LinearLayout mBoxMembers;
    private LinearLayout mBoxUnavailableMembers;

    private MenuItem mAddMemberMenuItem;
    private ContactsSynchronizer contactsSynchronizer;

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

        // retrieve the groupId (from MessageListActivity)
        groupId = getIntent().getStringExtra(BUNDLE_GROUP_ID);

        // retrieve the chatGroup by its groupId from memory
        this.contactsSynchronizer = ChatManager.getInstance().getContactsSynchronizer();
        chatGroup = ChatManager.getInstance().getGroupsSyncronizer().getById(groupId);

        loggedUser = ChatManager.getInstance().getLoggedUser();

        if (chatGroup != null) {
            groupMembers = convertMembersMapToList(chatGroup.getMembers());
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

    private List<IChatUser> convertMembersMapToList(Map<String, Integer> memberMaps) {
        List<IChatUser> members = new ArrayList<>();

        // iterate contacts
        for (IChatUser contact : contactsSynchronizer.getContacts()) {
            // iterate keys
            for (String member : memberMaps.keySet()) {
                if (member.equals(contact.getId())) {
                    members.add(contact);
                }
            }
        }

        return members;
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

//        if (mGroup != null) {
//            // the user is the admin of the chatGroup
//            // and the user is a member of the chatGroup
//            if (GroupUtils.isAnAdmin(mGroup, ChatManager.getInstance().getLoggedUser().getId())) {
//                showAddMember();
//            } else {
//                hideAddMember();
//            }
//        } else {
//            GroupUtils.subscribeOnGroupsChanges(ChatManager.getInstance().getAppId(), chatGroup.getGroupId(),
//                    new GroupUtils.OnGroupsChangeListener() {
//                        @Override
//                        public void onGroupChanged(ChatGroup chatGroup, String groupId) {
//                            mGroup = chatGroup;
//
//                            // the user is the admin of the chatGroup
//                            // and the user is a member of the chatGroup
//                            if (GroupUtils.isAnAdmin(mGroup, ChatManager.getInstance().getLoggedUser().getId())) {
//                                showAddMember();
//                            } else {
//                                hideAddMember();
//                            }
//                        }
//
//                        @Override
//                        public void onGroupCancelled(String errorMessage) {
//                            Log.e(TAG, errorMessage);
//                        }
//                    });
//        }
    }


//    private void showAddMember() {
//        Log.d(TAG, "showAddMember");
//
//        // shows the add member box
//        mBoxAddMember.setVisibility(View.VISIBLE);
//
//        // hides the add member menu item
//        if (mAddMemberMenuItem != null)
//            mAddMemberMenuItem.setVisible(true);
//
//        // set the click listener
//        mBoxAddMember.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (AbstractNetworkReceiver.isConnected(getApplicationContext())) {
//                    startAddMemberActivity();
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            getString(R.string.menu_activity_group_admin_panel_activity_cannot_add_member_offline),
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//    private void hideAddMember() {
//        Log.d(TAG, "hideAddMember");
//
//        // hides the add member box
//        mBoxAddMember.setVisibility(View.GONE);
//
//        // hides the add member menu item
//        if (mAddMemberMenuItem != null)
//            mAddMemberMenuItem.setVisible(false);
//
//        // unset the click listener
//        mBoxAddMember.setOnClickListener(null);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.menu_activity_group_admin_panel, menu);

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

//        final Intent intent = new Intent(this, AddMembersActivity.class);
//
//        if (mGroup != null) {
//            intent.putExtra(ChatUI.BUNDLE_GROUP, mGroup);
//            intent.putExtra(ChatUI.BUNDLE_PARENT_ACTIVITY,
//                    GroupAdminPanelActivity.class.getName());
//            intent.putExtra(BUNDLE_GROUP_ID, recipient.getId());
//            startActivity(intent);
//        } else {
//            GroupUtils.subscribeOnGroupsChanges(ChatManager.getInstance().getAppId(), recipient.getId(),
//                    new GroupUtils.OnGroupsChangeListener() {
//                        @Override
//                        public void onGroupChanged(ChatGroup chatGroup, String groupId) {
//                            mGroup = chatGroup;
//                            intent.putExtra(ChatUI.BUNDLE_GROUP, chatGroup);
//                            intent.putExtra(ChatUI.BUNDLE_PARENT_ACTIVITY,
//                                    GroupAdminPanelActivity.class.getName());
//                            intent.putExtra(BUNDLE_GROUP_ID, groupId);
//                            startActivity(intent);
//                        }
//
//                        @Override
//                        public void onGroupCancelled(String errorMessage) {
//                            Log.e(TAG, "onGroupCancelled. " + errorMessage);
//                        }
//                    });
//        }
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
    public void onBackPressed() {
        setResult(RESULT_OK); // force update interface of the calling activity
        super.onBackPressed();
    }
}