package chat21.android.ui.groups.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.dao.groups.GroupsDAO;
import chat21.android.dao.groups.GroupsDAOImpl;
import chat21.android.dao.groups.OnGroupsRetrievedCallback;
import chat21.android.ui.groups.adapters.MyGroupsListAdapter;
import chat21.android.ui.groups.listeners.OnGroupClickListener;
import chat21.android.core.groups.models.Group;
import chat21.android.ui.ChatUI;
import chat21.android.utils.StringUtils;
import chat21.android.utils.image.CropCircleTransformation;

import static chat21.android.utils.DebugConstants.DEBUG_NODE_GROUPS;

/**
 * Created by stefanodp91 on 26/09/17.
 */

public class ChooseGroupActivity extends AppCompatActivity implements OnGroupsRetrievedCallback,
        OnGroupClickListener {

    private GroupsDAO mGroupsDAO;

    private Toolbar mToolbar;
    private RecyclerView mMyGroupsListRecyclerView;
    private MyGroupsListAdapter mMyGroupsListRecyclerAdapter;
    private RelativeLayout mEmptyLayout;
    private ProgressBar mProgress;
    private LinearLayout mBoxCreateGroup;
    private ImageView mGroupIcon;

    private Conversation mConversation;
    private List<Group> mGroupList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_NODE_GROUPS, "ChooseGroupActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);

        registerViews();

        initViews();

        mGroupsDAO = new GroupsDAOImpl(this);
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
        mBoxCreateGroup = (LinearLayout) findViewById(R.id.box_create_group);
        mGroupIcon = (ImageView) findViewById(R.id.group_icon);
    }

    private void initViews() {
        initToolbar();

        if (mProgress != null)
            mProgress.setVisibility(View.VISIBLE);

        // grid facebook like
//        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
//        mMyGroupsListRecyclerView.setLayoutManager(mLayoutManager);

        mMyGroupsListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateAdapter(mGroupList);

        initBoxCreateGroup();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updateAdapter(List<Group> groups) {
        if (mMyGroupsListRecyclerAdapter == null) {
            mMyGroupsListRecyclerAdapter = new MyGroupsListAdapter(this, groups);
            mMyGroupsListRecyclerAdapter.setOnGroupClickListener(this);
            mMyGroupsListRecyclerView.setAdapter(mMyGroupsListRecyclerAdapter);
        } else {
            mMyGroupsListRecyclerAdapter.setList(groups);
            mMyGroupsListRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void initBoxCreateGroup() {
        Log.d(DEBUG_NODE_GROUPS, "initBoxCreateGroup");

        Glide.with(getApplicationContext())
                .load("")
                .placeholder(R.drawable.ic_group_avatar)
                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .into(mGroupIcon);

        // box click
        mBoxCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateGroupActivity();
            }
        });
    }

    private void startCreateGroupActivity() {
        Log.d(DEBUG_NODE_GROUPS, "startCreateGroupActivity");

        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivityForResult(intent, ChatUI._REQUEST_CODE_CREATE_GROUP);
    }

    private void retrieveGroupsForUser(String userId) {
        Log.d(DEBUG_NODE_GROUPS, "ChooseGroupActivity.startCreateGroupActivity: userId == " + userId);

        if (mGroupsDAO == null)
            mGroupsDAO = new GroupsDAOImpl(this);
        mGroupsDAO.getGroupsForUser(userId, this);
    }


    @Override
    public void onGroupsRetrievedSuccess(List<Group> groups) {
        if (groups != null && groups.size() > 0) {
            Log.d(DEBUG_NODE_GROUPS, "ChooseGroupActivity.onGroupsRetrievedSuccess: " +
                    "groups == " + groups.toString());

            if (mProgress != null)
                mProgress.setVisibility(View.GONE);

            mMyGroupsListRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);

            updateAdapter(groups);
        } else {
            Log.e(DEBUG_NODE_GROUPS, "ChooseGroupActivity.onGroupsRetrievedSuccess: " +
                    "groupsIds is empty or null");
            onGroupsRetrievedError(new Exception("groupsIds is empty or null"));
        }
    }

    @Override
    public void onGroupsRetrievedError(Exception e) {
        Log.e(DEBUG_NODE_GROUPS, "ChooseGroupActivity.onGroupsRetrievedError" + e.getMessage());

        if (mProgress != null)
            mProgress.setVisibility(View.GONE);

        mMyGroupsListRecyclerView.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void onGroupClicked(final Group group, int position) {
        Log.d(DEBUG_NODE_GROUPS, "ChooseGroupActivity.onGroupClicked: " +
                "group == " + group.toString() + ", position == " + position);

        showChoiceGroupConfirmDialog(group);
    }

    private void showChoiceGroupConfirmDialog(final Group group) {
        String groupDisplayName = StringUtils.isValid(group.getName()) ? group.getName() : group.getGroupId();

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.activity_choose_group_choice_group_confirm_dialog_title))
                .setMessage(getString(R.string.activity_choose_group_choice_group_confirm_dialog_message, groupDisplayName))
                .setPositiveButton(getString(R.string.activity_choose_group_choice_group_confirm_dialog_positive_button_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                onShowChoiceGroupDialogPositiveClick(group);
                            }
                        }).setNegativeButton(getString(R.string.activity_choose_group_choice_group_confirm_dialog_negative_button_label),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private void onShowChoiceGroupDialogPositiveClick(Group group) {
        Intent intent = getIntent();
        intent.putExtra(ChatUI._INTENT_BUNDLE_GROUP, group);
        setResult(RESULT_OK, intent);
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