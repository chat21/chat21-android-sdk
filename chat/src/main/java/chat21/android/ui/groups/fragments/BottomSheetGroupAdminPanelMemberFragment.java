package chat21.android.ui.groups.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.groups.GroupUtils;
import chat21.android.core.groups.models.ChatGroup;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.ChatUI;
import chat21.android.ui.users.activities.PublicProfileActivity;
import chat21.android.utils.StringUtils;

/**
 * Created by frontiere21 on 25/11/16.
 */
public class BottomSheetGroupAdminPanelMemberFragment extends BottomSheetDialogFragment implements
        View.OnClickListener {
    public static final String TAG = BottomSheetGroupAdminPanelMemberFragment.class.getName();

    private static final String PRIVATE_BUNDLE_GROUP_MEMBER = "PRIVATE_BUNDLE_GROUP_MEMBER";
    private static final String PRIVATE_BUNDLE_GROUP = "PRIVATE_BUNDLE_GROUP";

    private IChatUser groupMember;
    private ChatGroup chatGroup;
    private String loggedUserId;

    private TextView mUsername;
    private Button mBtnRemoveMember;
    private Button mBtnSeeProfile;
    private Button mBtnSendMessage;
    private Button mBtnCancel;

    public static BottomSheetGroupAdminPanelMemberFragment newInstance(IChatUser groupMember, ChatGroup chatGroup) {
        Log.i(TAG, "newInstance");

        BottomSheetGroupAdminPanelMemberFragment f =
                new BottomSheetGroupAdminPanelMemberFragment();
        Bundle args = new Bundle();
        args.putSerializable(PRIVATE_BUNDLE_GROUP_MEMBER, groupMember);
        args.putSerializable(PRIVATE_BUNDLE_GROUP, chatGroup);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieves the username from newInstance params
        groupMember = (IChatUser) getArguments().getSerializable(PRIVATE_BUNDLE_GROUP_MEMBER);

        // retrieves the groupId from newInstance params
        chatGroup = (ChatGroup) getArguments().getSerializable(PRIVATE_BUNDLE_GROUP);

        // retrieves the logged userId from chant configuration
        loggedUserId = ChatManager.getInstance().getLoggedUser().getId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater
                .inflate(R.layout.fragment_bottom_sheet_group_admin_panel_member,
                        container, false);

        registerViews(rootView);
        initViews();
        initListeners();

        return rootView;
    }


    private void registerViews(View rootView) {
        Log.i(TAG, "registerViews");

        mUsername = (TextView) rootView.findViewById(R.id.username);
        mBtnRemoveMember = (Button) rootView.findViewById(R.id.btn_remove_member);
        mBtnSeeProfile = (Button) rootView.findViewById(R.id.btn_see_profile);
        mBtnSendMessage = (Button) rootView.findViewById(R.id.btn_send_message);
        mBtnCancel = (Button) rootView.findViewById(R.id.btn_cancel);
    }

    private void initViews() {
        Log.i(TAG, "initViews");

        mUsername.setText(groupMember.getFullName());

        initRemoveMemberButton();

        // bugfix Issue #43
        initSendMessageButton();
    }

    private void initListeners() {
        Log.d(TAG, "initListeners");

        mBtnRemoveMember.setOnClickListener(this);
        mBtnSeeProfile.setOnClickListener(this);
        mBtnSendMessage.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }

    private void initRemoveMemberButton() {
        Log.d(TAG, "initRemoveMemberButton");

        GroupUtils.subscribeOnGroupsChanges(ChatManager.getInstance().getAppId(), chatGroup.getGroupId(),
                new GroupUtils.OnGroupsChangeListener() {
                    @Override
                    public void onGroupChanged(ChatGroup chatGroup, String groupId) {

                        // the logged user is the admin of the chatGroup
                        // and the logged user is a member of the chatGroup
                        if (GroupUtils.isAnAdmin(chatGroup, loggedUserId)) {
                            // the clicked user is an admin
                            if (groupMember.getId().equals(chatGroup.getOwner())) {
                                // cannot delete and admin
                                mBtnRemoveMember.setVisibility(View.GONE);
                            } else {
                                mBtnRemoveMember.setVisibility(View.VISIBLE);
                            }
                        } else {
                            mBtnRemoveMember.setVisibility(View.GONE);
                        }

                        // allows the logged user to leave the chatGroup
                        if (groupMember.getId().equals(loggedUserId)) {
                            mBtnRemoveMember.setText(getString(
                                    R.string.fragment_bottom_sheet_group_admin_panel_member_leave_group_btn_label));
                            mBtnRemoveMember.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onGroupCancelled(String errorMessage) {
                        Log.e(TAG, errorMessage);
                    }
                });
    }

    // bugfix Issue #43
    private void initSendMessageButton() {
        Log.d(TAG, "initSendMessageButton");

        GroupUtils.subscribeOnGroupsChanges(ChatManager.getInstance().getAppId(), chatGroup.getGroupId(),
                new GroupUtils.OnGroupsChangeListener() {
                    @Override
                    public void onGroupChanged(ChatGroup chatGroup, String groupId) {

                        // hide the send message to itself
                        if (groupMember.getId().equals(loggedUserId)) {
                            mBtnSendMessage.setVisibility(View.GONE);
                        } else {
                            mBtnSendMessage.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onGroupCancelled(String errorMessage) {
                        Log.e(TAG, errorMessage);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == mBtnRemoveMember.getId()) {
            onRemoveClickListener();
        } else if (view.getId() == mBtnSeeProfile.getId()) {
            onSeeProfileClickListener();
        } else if (view.getId() == mBtnSendMessage.getId()) {
//            onSendMessageClickListener();
        } else if (view.getId() == mBtnCancel.getId()) {
            onCancelClickListener();
        } else {
            Log.w(TAG, "not handled click");
        }
    }

    private void onRemoveClickListener() {
        Log.d(TAG, "onRemoveClickListener");

        showRemoveMemberAlertDialog();
    }

    private void onSeeProfileClickListener() {
        Log.d(TAG, "onSeeprofileClickListener");

        Intent intent = new Intent(getActivity().getApplicationContext(), PublicProfileActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupMember);
        startActivity(intent);
    }

//    private void onSendMessageClickListener() {
//        Log.d(TAG, "onSendMessageClickListener");
//
//        String conversationId =  username;
////        String conversationId = ConversationUtils.getConversationId(ChatManager.getInstance()
////                .getLoggedUser()
////                .getId(), username);
//
//        startMessageActivity(conversationId);
//
//        // dismiss the bottomsheet
//        getDialog().dismiss();
//    }

//    private void startMessageActivity(String conversationId) {
//        Log.d(TAG, "startMessageActivity");
//
//        Intent intent = new Intent(getActivity(), MessageListActivity.class);
//        intent.putExtra(INTENT_BUNDLE_RECIPIENT_ID, conversationId);
//        intent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
//        getActivity().startActivity(intent);
//    }

    private void onCancelClickListener() {
        Log.d(TAG, "onCancelClickListener");

        // dismiss the bottomsheet
        getDialog().dismiss();
    }

    private void showRemoveMemberAlertDialog() {
        Log.d(TAG, "showRemoveMemberAlertDialog");

        String message, positiveClickMessage;

        // allows the logged user to leave the chatGroup
        if (groupMember.getId().equals(loggedUserId)) {
            message = getString(R.string.fragment_bottom_sheet_group_admin_panel_member_leave_group_alert_message);
            positiveClickMessage = getString(R.string.fragment_bottom_sheet_group_admin_panel_member_leave_group_alert_positive_click);
        } else {
            message = getString(R.string.fragment_bottom_sheet_group_admin_panel_member_remove_member_alert_message, groupMember.getFullName());
            positiveClickMessage = getString(R.string.fragment_bottom_sheet_group_admin_panel_member_remove_member_alert_positive_click);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.fragment_bottom_sheet_group_admin_panel_member_remove_member_alert_title))
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(positiveClickMessage, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeMemberFromGroup(ChatManager.getInstance().getAppId(), chatGroup.getGroupId(), groupMember.getId());

                        // dismiss the dialog
                        dialog.dismiss();

                        // dismiss the bottomsheet
                        getDialog().dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.fragment_bottom_sheet_group_admin_panel_member_remove_member_alert_negative_click), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss the dialog
                        dialogInterface.dismiss();

                        // dismiss the bottomsheet
                        getDialog().dismiss();
                    }
                }).show();
    }

    private void removeMemberFromGroup(String appId, final String groupId, final String userId) {
        Log.d(TAG, "removeMemberFromGroup");

        DatabaseReference nodeMembers;

        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            nodeMembers = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("apps/" + appId + "/groups/" + groupId + "/members/" + userId);
        } else {
            nodeMembers = FirebaseDatabase.getInstance().getReference()
                    .child("apps/" + appId + "/groups/" + groupId + "/members/" + userId);
        }

        nodeMembers.removeValue();
    }
}