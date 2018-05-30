package org.chat21.android.ui.conversations.fragments;

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

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.conversations.ConversationsHandler;
import org.chat21.android.core.conversations.listeners.ConversationsListener;
import org.chat21.android.core.conversations.models.Conversation;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.utils.StringUtils;

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

    private ConversationsHandler conversationsHandler;

    public static BottomSheetConversationsListFragmentLongPress
    newInstance(Conversation conversation) {
        BottomSheetConversationsListFragmentLongPress f =
                new BottomSheetConversationsListFragmentLongPress();
        Bundle args = new Bundle();
        args.putSerializable(_BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION, conversation);
        f.setArguments(args);
        return f;
    }

    public ConversationsHandler getConversationsHandler() {
        return this.conversationsHandler;
    }

    public void setConversationsHandler(ConversationsHandler conversationsHandler) {
        this.conversationsHandler = conversationsHandler;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationsHandler = getConversationsHandler();

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

        mDeleteConversationView = view.findViewById(R.id.btn_delete_conversation);
        mDeleteConversationView.setOnClickListener(this);

        return view;
    }

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
                .setTitle(getString(R.string.bottom_sheet_conversation_list_confirm_delete_conversation_alert_title))
                .setMessage(getString(R.string.bottom_sheet_conversation_list_confirm_delete_conversation_alert_message))
                .setPositiveButton(getString(R.string.bottom_sheet_conversation_list_confirm_delete_conversation_alert_positive_button_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                                        ".showRemoveMemberAlertDialog.setPositiveButton");

                                perfomDeleteConversation();
                            }
                        })
                .setNegativeButton(getString(R.string.bottom_sheet_conversation_list_confirm_delete_conversation_alert_positive_button_negative),
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

        String conversationId = mConversation.getConversationId();

        if(getConversationsHandler() != null) {
            getConversationsHandler().deleteConversation(conversationId, conversationsListener);
        }
    }

    private ConversationsListener conversationsListener = new ConversationsListener() {

        @Override
        public void onConversationAdded(Conversation conversation, ChatRuntimeException e) {
            // do nothing
        }

        @Override
        public void onConversationChanged(Conversation conversation, ChatRuntimeException e) {
            // do nothing
        }

        @Override
        public void onConversationRemoved(ChatRuntimeException e) {
            if(e == null) {
                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                        ".conversationsListener.onConversationRemoved: no errors");

                // dismiss the bottomsheet
                getDialog().dismiss();
            } else {
                // there are error
                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                        ".conversationsListener.onConversationRemoved: " + e.toString());

                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
            }
        }
    };
}