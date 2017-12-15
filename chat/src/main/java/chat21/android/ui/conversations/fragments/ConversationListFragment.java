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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import chat21.android.R;
import chat21.android.conversations.utils.ConversationUtils;
import chat21.android.core.ChatManager;
import chat21.android.core.conversations.listeners.OnConversationTreeChangeListener;
import chat21.android.core.conversations.models.Conversation;
import chat21.android.core.presence.PresenceManger;
import chat21.android.core.presence.listeners.OnMyPresenceChangesListener;
import chat21.android.messages.activites.MessageListActivity;
import chat21.android.ui.ChatUI;
import chat21.android.ui.conversations.adapters.ConversationListAdapter;
import chat21.android.ui.conversations.listeners.OnConversationClickListener;
import chat21.android.ui.conversations.listeners.OnConversationLongClickListener;
import chat21.android.ui.groups.activities.MyGroupsListActivity;
import chat21.android.utils.ChatUtils;
import chat21.android.utils.listeners.OnContactListClickListener;
import chat21.android.utils.listeners.OnSupportContactListClickListener;

import static chat21.android.utils.DebugConstants.DEBUG_MY_PRESENCE;

/**
 * Created by stefano on 15/10/2016.
 */
public class ConversationListFragment extends Fragment implements
        OnConversationTreeChangeListener,
        OnConversationClickListener,
        OnConversationLongClickListener,
        OnMyPresenceChangesListener {
    public static final String TAG = ConversationListFragment.class.getName();

    private RelativeLayout noConversationLayout;
    private RecyclerView recyclerView;
    private ConversationListAdapter conversationListAdapter;
    private FloatingActionButton addNewConversation;

    private LinearLayoutManager mLayoutManager;

    private TextView mGroupsView;

    private ProgressBar mProgressBar;

    public static Fragment newInstance() {
        Fragment mFragment = new ConversationListFragment();
        return mFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_conversation_list, container, false);

        registerViews(rootView);

        initViews(rootView);

        observeConversations(ChatManager.getInstance().getTenant());

        // subscribe for user presence changes
        PresenceManger.observeMyPresenceChanges(this);

        return rootView;
    }

    private void registerViews(View rootView) {
        Log.d(TAG, "registerViews");

        recyclerView = (RecyclerView) rootView.findViewById(R.id.chat_list);

        noConversationLayout = (RelativeLayout) rootView
                .findViewById(R.id.layout_no_conversations);

        addNewConversation = (FloatingActionButton) rootView
                .findViewById(R.id.button_new_conversation);

        mGroupsView = rootView.findViewById(R.id.groups);

        mProgressBar = rootView.findViewById(R.id.progress_bar);
    }

    private void initViews(View rootView) {
        Log.d(TAG, "registerViews");

        mProgressBar.setVisibility(View.VISIBLE);

        setRecyclerView();
        showEmptyLayout();
        setAddNewConversationBtn(rootView);

        mGroupsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyGroupsListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setRecyclerView() {
        Log.d(TAG, "setRecyclerView");
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private void showEmptyLayout() {
        Log.d(TAG, "showEmptyLayout");

        ImageView vNoOffersAvailableImage =
                (ImageView) noConversationLayout.findViewById(R.id.error_image);
        int noOffersAvailableImageColor = getResources().getColor(R.color.error_view_image_color);
        vNoOffersAvailableImage.setColorFilter(noOffersAvailableImageColor);
    }

    private void setAddNewConversationBtn(View rootView) {
        Log.d(TAG, "setAddNewConversationBtn");

        // check if the support account is enabled or not and assign the listener
        if (!ChatUtils.isChatSupportAccountEnabled(getContext())) {
            addNewConversation =
                    (FloatingActionButton) rootView.findViewById(R.id.button_new_conversation);
            addNewConversation.setVisibility(View.VISIBLE);
            addNewConversation.setOnClickListener(new OnContactListClickListener(getContext()));
        } else {
            addNewConversation =
                    (FloatingActionButton) rootView.findViewById(R.id.button_new_conversation);
            addNewConversation.setVisibility(View.VISIBLE);
            addNewConversation.setOnClickListener(
                    new OnSupportContactListClickListener(getContext()));
        }
    }

    private void observeConversations(String appId) {
        Log.d(TAG, "observeConversations");

        DatabaseReference nodeConversations = FirebaseDatabase.getInstance().getReference()
                .child("apps/" + appId + "/users/" + ChatManager.getInstance().getLoggedUser().getId() + "/conversations");

        ConversationUtils.observeMessageTree(appId, ChatManager.getInstance().getLoggedUser().getId(),
                nodeConversations, this);
    }

    private void updateConversationListAdapter(DatabaseReference node) {
        Log.d(TAG, "updateConversationListAdapter");

        if (conversationListAdapter == null) {
            conversationListAdapter = new ConversationListAdapter(node);
            conversationListAdapter.setOnConversationClickListener(this);
            conversationListAdapter.setOnConversationLongClickListener(this);
            recyclerView.setAdapter(conversationListAdapter);
        } else {
            conversationListAdapter.notifyDataSetChanged();
        }

        // scroll to first position
        // bugfix Issue #19
        if (conversationListAdapter.getItemCount() > 0) {
            int position = conversationListAdapter.getItemCount() - 1;
            mLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    @Override
    public void onTreeDataChanged(DatabaseReference node, DataSnapshot dataSnapshot,
                                  int childrenCount) {
        Log.d(TAG, "onTreeDataChanged");

        Log.d(TAG, "childrenCount: " + childrenCount);
        // if at least one conversation extists show the list, else show a placeholder layout
        if (childrenCount > 0) {
            noConversationLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            updateConversationListAdapter(node);
        } else {
            Log.d(TAG, "dataSnapshot hasn't Children()");
            noConversationLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onTreeChildAdded(DatabaseReference node, DataSnapshot dataSnapshot,
                                 Conversation conversation) {
        Log.d(TAG, "onTreeChildAdded");

        updateConversationListAdapter(node);
    }

    @Override
    public void onTreeChildChanged(DatabaseReference node, DataSnapshot dataSnapshot,
                                   Conversation conversation) {
        Log.d(TAG, "onTreeChildChanged");

        updateConversationListAdapter(node);
    }

    @Override
    public void onTreeChildRemoved() {
        Log.d(TAG, "onTreeChildRemoved");
    }

    @Override
    public void onTreeChildMoved() {
        Log.d(TAG, "onTreeChildMoved");
    }

    @Override
    public void onTreeCancelled() {
        Log.d(TAG, "onTreeCancelled");
    }

    @Override
    public void onConversationClicked(Conversation conversation, int position) {

        String conversationId = conversation.getConversationId();

        try {
            ConversationUtils.setConversationRead(ChatManager.getInstance().getTenant(),
                    ChatManager.getInstance().getLoggedUser().getId(),
                    conversationId);
            startMessageActivity(conversation.getConversationId());
        } catch (Exception e) {
            Log.e(TAG, "cannot start messageActivity. " + e.getMessage());

            Toast.makeText(getActivity(),
                    getString(R.string.fragment_conversation_list_cannot_open_conversation_label),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConversationLongClicked(Conversation item, int position) {

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        BottomSheetConversationsListFragmentLongPress dialog =
                BottomSheetConversationsListFragmentLongPress.newInstance(item);
        dialog.show(ft, BottomSheetConversationsListFragmentLongPress.class.getName());
    }

    private void startMessageActivity(String conversationId) {
        Log.d(TAG, "startMessageActivity");

        Intent intent = new Intent(getActivity(), MessageListActivity.class);
        intent.putExtra(ChatUI._INTENT_BUNDLE_CONVERSATION_ID, conversationId);
        intent.putExtra(ChatUI.INTENT_BUNDLE_IS_FROM_NOTIFICATION, false);
        getActivity().startActivity(intent);
    }

    @Override
    public void onMyPresenceChange(boolean imConnected) {
        Log.d(TAG, "onPresenceChange - imConnected: " + imConnected);
        Log.i(DEBUG_MY_PRESENCE, "ConversationListFragment.onMyPresenceChange - imConnected: " + imConnected);

        // TODO: 19/10/17
    }

    @Override
    public void onMyLastOnlineChange(long lastOnline) {
        Log.i(DEBUG_MY_PRESENCE, "ConversationListFragment.onMyLastOnlineChange - lastOnline: " + lastOnline);
        // TODO: 04/08/17
    }

    @Override
    public void onMyPresenceChangeError(Exception e) {
        Log.i(DEBUG_MY_PRESENCE, "ConversationListFragment.onMyPresenceChangeError: " + e.getMessage());

        Log.e(TAG, "onPresenceChangeError " + e.getMessage());

        // TODO: 19/10/17
    }
}