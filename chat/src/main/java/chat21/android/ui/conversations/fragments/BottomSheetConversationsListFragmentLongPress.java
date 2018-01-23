package chat21.android.ui.conversations.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.users.models.IChatUser;
import chat21.android.utils.StringUtils;

/**
 * Created by stefanodp91 on 28/09/17.
 */
public class BottomSheetConversationsListFragmentLongPress extends BottomSheetDialogFragment implements View.OnClickListener /**, ConversationsListener */
{

    private static final String DEBUG_TAG = BottomSheetConversationsListFragmentLongPress.class.getName();

    private static final String _BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION =
            "_BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION";

    private Conversation mConversation;
    private IChatUser mLoggedUser;

    private Button mDeleteConversationView;

//    private ConversationsHandler conversationsHandler;

    public static BottomSheetConversationsListFragmentLongPress
    newInstance(Conversation conversation) {
        BottomSheetConversationsListFragmentLongPress f =
                new BottomSheetConversationsListFragmentLongPress();
        Bundle args = new Bundle();
        args.putSerializable(_BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION, conversation);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        conversationsHandler = ChatManager.getInstance().getConversationsHandler();
//        conversationsHandler.connect();
//        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onCreate: conversationsHandler connected");

        mConversation = (Conversation) getArguments()
                .getSerializable(_BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION);
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onCreate: " +
                "mConversation == " + mConversation.toString());

        mLoggedUser = ChatManager.getInstance().getLoggedUser();
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onCreate:" +
                " mLoggedUser == " + mLoggedUser.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_conversation_list_long_press, container, false);

//        conversationsHandler.addGroupsListener(this);
//        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onCreateView: conversationsHandler attached");

        mDeleteConversationView = view.findViewById(R.id.btn_delete_conversation);
        mDeleteConversationView.setOnClickListener(this);

        return view;
    }

//    @Override
//    public void onDestroy() {
//
//        conversationsHandler.removeGroupsListener(this);
//        Log.d(DEBUG_TAG, "  BottomSheetConversationsListFragmentLongPress.onDestroy: conversationMessagesHandler detached");
//
//        super.onDestroy();
//    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_delete_conversation) {
            mLoggedUser = ChatManager.getInstance().getLoggedUser();
            Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onClick: btn_delete_conversation ");

            showRemoveMemberAlertDialog();
        }
    }

    private void showRemoveMemberAlertDialog() {
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.showRemoveMemberAlertDialog");

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.fragment_bottom_sheet_conversation_list_confirm_delete_conversation_alert_title))
                .setMessage(getString(R.string.fragment_bottom_sheet_conversation_list_confirm_delete_conversation_alert_message))
                .setPositiveButton(getString(R.string.fragment_bottom_sheet_conversation_list_confirm_delete_conversation_alert_positive_button_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                                        ".showRemoveMemberAlertDialog.setPositiveButton");

                                perfomDeleteConversation();
                            }
                        })
                .setNegativeButton(getString(R.string.fragment_bottom_sheet_conversation_list_confirm_delete_conversation_alert_positive_button_negative),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                                        ".showRemoveMemberAlertDialog.setNegativeButton");

                                // dismiss the dialog
                                dialogInterface.dismiss();

                                // dismiss the bottomsheet
                                getDialog().dismiss();
                            }
                        }).show();
    }

    private void perfomDeleteConversation() {
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.perfomDeleteConversation");

//        conversationsHandler.deleteConversation(mConversation.getConversationId(),
//                BottomSheetConversationsListFragmentLongPress.this);

        String conversationId = mConversation.getConversationId();

//TODO move to ChatManager conversation API
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                ".perfomDeleteConversation: conversationId == " + conversationId);

        DatabaseReference nodeConversation;
        if (StringUtils.isValid(ChatManager.Configuration.firebaseUrl)) {
            nodeConversation = FirebaseDatabase.getInstance().getReferenceFromUrl(ChatManager.Configuration.firebaseUrl)
                    .child("apps/" + ChatManager.getInstance().getAppId() + "/users/" + mLoggedUser.getId() + "/conversations/" + conversationId);
        } else {
            nodeConversation = FirebaseDatabase.getInstance().getReference()
                    .child("apps/" + ChatManager.getInstance().getAppId() + "/users/" + mLoggedUser.getId() + "/conversations/" + conversationId);
        }

        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                ".perfomDeleteConversation: nodeConversation == " + nodeConversation.toString());

        nodeConversation.removeValue(onConversationRemoved);
    }

//    @Override
//    public void onConversationAdded(Conversation conversation, ChatRuntimeException e) {
//        // TODO: 20/12/17
//    }
//
//    @Override
//    public void onConversationChanged(Conversation conversation, ChatRuntimeException e) {
//        // TODO: 20/12/17
//    }
//
//    @Override
//    public void onConversationRemoved(ChatRuntimeException e) {
//        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onConversationRemoved");
//
//        if (e == null) {
//            // dismiss the bottomsheet
//            getDialog().dismiss();
//        } else {
//            Log.e(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onConversationRemoved cannot delete conversation.", e);
//        }
//    }

    private DatabaseReference.CompletionListener onConversationRemoved
            = new DatabaseReference.CompletionListener() {
        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            if (databaseError == null) {
                // no errors
                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                        ".onConversationRemoved: no errors");

                // dismiss the bottomsheet
                getDialog().dismiss();
            } else {
                // there are error
                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                        ".onConversationRemoved: " + databaseError.toString());

                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
            }
        }
    };
}