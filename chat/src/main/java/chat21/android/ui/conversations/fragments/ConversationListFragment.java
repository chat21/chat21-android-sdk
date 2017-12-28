package chat21.android.ui.conversations.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.conversations.ConversationsHandler;
import chat21.android.core.conversations.listeners.ConversationsListener;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.ui.ChatUI;
import chat21.android.ui.conversations.adapters.ConversationsListAdapter;
import chat21.android.ui.conversations.listeners.OnContactListClickListener;
import chat21.android.ui.conversations.listeners.OnConversationClickListener;
import chat21.android.ui.conversations.listeners.OnConversationLongClickListener;
import chat21.android.ui.conversations.listeners.OnSupportContactListClickListener;
import chat21.android.ui.groups.activities.MyGroupsListActivity;
import chat21.android.ui.messages.activities.MessageListActivity;
import chat21.android.utils.ChatUtils;

import static chat21.android.ui.ChatUI.INTENT_BUNDLE_RECIPIENT_ID;
import static chat21.android.ui.ChatUI.INTENT_BUNDLE_CONVERSATION;

/**
 * Created by stefano on 15/10/2016.
 */
public class ConversationListFragment extends Fragment implements
        ConversationsListener,
        OnConversationClickListener,
        OnConversationLongClickListener {

    private static final String TAG = ConversationListFragment.class.getName();

    private ConversationsHandler conversationsHandler;

    // conversation list recyclerview
    private RecyclerView recyclerViewConversations;
    private LinearLayoutManager rvConversationsLayoutManager;
    private ConversationsListAdapter conversationsListAdapter;

    // no conversations layout
    private RelativeLayout noConversationsLayout;

    private FloatingActionButton addNewConversation;

    private TextView currentUserGroups;


//    // current user presence listener
//    private OnPresenceListener onMyPresenceListener = new OnPresenceListener() {
//        @Override
//        public void onChanged(boolean imConnected) {
//            Log.d(DEBUG_MY_PRESENCE, "ConversationListFragment.onMyPresenceChange" +
//                    ".onChanged: imConnected == " + imConnected);
//        }
//
//        @Override
//        public void onLastOnlineChanged(long lastOnline) {
//            Log.d(DEBUG_MY_PRESENCE, "ConversationListFragment.onMyPresenceChange" +
//                    ".onLastOnlineChanged: lastOnline == " + lastOnline);
//        }
//
//        @Override
//        public void onError(Exception e) {
//            Log.e(DEBUG_MY_PRESENCE, "ConversationListFragment.onMyPresenceChange" +
//                    ".onError: " + e.getMessage());
//        }
//    };

    public static Fragment newInstance() {
        Fragment mFragment = new ConversationListFragment();
        return mFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // attach listener for conversations change
//        conversationsHandler = ChatManager.getInstance().addConversationsListener(this);
//        Log.d(TAG, "ConversationListFragment.onCreate.conversationsHandler.getConversationsNode(): "
//                + conversationsHandler.getConversationsNode().toString());

        conversationsHandler = ChatManager.getInstance().getConversationsHandler();
        conversationsHandler.connect();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "ConversationListFragment.onCreateView");
        View view = inflater.inflate(R.layout.fragment_conversation_list, container, false);

        conversationsHandler.upsertConversationsListener(this);
        Log.d(TAG, "  ConversationListFragment.onCreateView: conversationMessagesHandler attached");

        // init RecyclerView
        recyclerViewConversations = view.findViewById(R.id.conversations_list);
        rvConversationsLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversations.setLayoutManager(rvConversationsLayoutManager);

        // init RecyclerView adapter
        conversationsListAdapter = new ConversationsListAdapter(getActivity(), conversationsHandler.getConversations());
        conversationsListAdapter.setOnConversationClickListener(this);
        conversationsListAdapter.setOnConversationLongClickListener(this);
        recyclerViewConversations.setAdapter(conversationsListAdapter);

        // no conversations layout
        noConversationsLayout = view.findViewById(R.id.layout_no_conversations);
        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());

        // add new conversations button
        addNewConversation = (FloatingActionButton) view.findViewById(R.id.button_new_conversation);
        setAddNewConversationClickBehaviour();

        currentUserGroups = view.findViewById(R.id.groups);
        showCurrentUserGroups();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "ConversationListFragment.onViewCreated");

//        // subscribe for current user presence changes
//        PresenceManger.observeUserPresenceChanges(ChatManager.getInstance().getTenant(),
//                ChatManager.getInstance().getLoggedUser().getId(), onMyPresenceListener);
    }

    @Override
    public void onDestroy() {

        conversationsHandler.removeConversationsListener(this);
        Log.d(TAG, "  ConversationListFragment.onDestroy: conversationMessagesHandler detached");

        super.onDestroy();
    }


    // check if the support account is enabled or not and assign the listener
    private void setAddNewConversationClickBehaviour() {
        Log.d(TAG, "ConversationListFragment.setAddNewConversationClickBehaviour");

        if (!ChatUtils.isChatSupportAccountEnabled(getContext())) {
            // enable contact list button action
            addNewConversation.setOnClickListener(new OnContactListClickListener(getContext()));
        } else {
            // enable support account button action
            addNewConversation.setOnClickListener(new OnSupportContactListClickListener(getContext()));
        }
    }

    // toggle the no conversation layout visibilty.
    // if there are items show the list of item, otherwise show a placeholder layout
    private void toggleNoConversationLayoutVisibility(int itemCount) {
        if (itemCount > 0) {
            // show the item list
            recyclerViewConversations.setVisibility(View.VISIBLE);
            noConversationsLayout.setVisibility(View.GONE);
        } else {
            // show the placeholder layout
            recyclerViewConversations.setVisibility(View.GONE);
            noConversationsLayout.setVisibility(View.VISIBLE);
        }
    }

    // show current user groups
    private void showCurrentUserGroups() {
        if (ChatUtils.areGroupsEnabled(getActivity())) {
            // groups enabled
            currentUserGroups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), MyGroupsListActivity.class);
                    startActivity(intent);
                }
            });

            currentUserGroups.setVisibility(View.VISIBLE);
        } else {
            // groups not enabled

            currentUserGroups.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConversationAdded(Conversation conversation, ChatRuntimeException e) {
        // added a new conversation

        Log.d(TAG, "ConversationListFragment.onConversationAdded");

//        if (e == null) {
//            conversationsListAdapter.insertTop(conversation);
//        } else {
//            Log.w(TAG, "ConversationListFragment.onConversationAdded: Error onConversationAdded ", e);
//        }

//        if (e == null) {
//            conversationsListAdapter.update(conversation);
//        } else {
//            Log.w(TAG, "ConversationListFragment.onConversationChanged: Error onConversationMessageReceived ", e);
//        }

        conversationsListAdapter.notifyDataSetChanged();

        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
    }

    @Override
    public void onConversationChanged(Conversation conversation, ChatRuntimeException e) {
        // existing conversation updated

        Log.d(TAG, "ConversationListFragment.onConversationChanged");

//        if (e == null) {
//            conversationsListAdapter.update(conversation);
//        } else {
//            Log.w(TAG, "ConversationListFragment.onConversationChanged: Error onConversationMessageReceived ", e);
//        }

        conversationsListAdapter.notifyDataSetChanged();

        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
    }

    @Override
    public void onConversationRemoved(ChatRuntimeException e) {
        conversationsListAdapter.notifyDataSetChanged();
        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
    }

    @Override
    public void onConversationClicked(Conversation conversation, int position) {
        // click on conversation

//        try {
        // set the conversation as read
        conversationsHandler.setConversationRead(conversation.getConversationId());

        // start the message list activity of the corresponding conversation
        startMessageActivity(conversation);
//        } catch (Exception e) {
//            Log.e(TAG, "cannot start messageActivity. " + e.getMessage());
//
//            Toast.makeText(getActivity(),
//                    getString(R.string.fragment_conversation_list_cannot_open_conversation_label),
//                    Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onConversationLongClicked(Conversation conversation, int position) {
        // long click on conversation

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        BottomSheetConversationsListFragmentLongPress dialog =
                BottomSheetConversationsListFragmentLongPress.newInstance(conversation);
        dialog.show(ft, BottomSheetConversationsListFragmentLongPress.class.getName());
    }

    private void startMessageActivity(Conversation conversation) {
        Log.d(TAG, "ConversationListFragment.startMessageActivity");

        Intent intent = new Intent(getActivity(), MessageListActivity.class);
        intent.putExtra(INTENT_BUNDLE_CONVERSATION, conversation);
        intent.putExtra(INTENT_BUNDLE_RECIPIENT_ID, conversation.getConversationId());
//        intent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
        getActivity().startActivity(intent);
    }
}