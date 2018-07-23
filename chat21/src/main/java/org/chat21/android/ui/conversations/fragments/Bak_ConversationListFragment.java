package org.chat21.android.ui.conversations.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.conversations.ConversationsHandler;
import org.chat21.android.core.conversations.listeners.ConversationsListener;
import org.chat21.android.core.conversations.models.Conversation;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.presence.MyPresenceHandler;
import org.chat21.android.core.presence.listeners.MyPresenceListener;
import org.chat21.android.core.users.models.ChatUser;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.chat_groups.activities.ChatGroupsListActivity;
import org.chat21.android.ui.conversations.adapters.ConversationsListAdapter;
import org.chat21.android.ui.conversations.listeners.OnConversationClickListener;
import org.chat21.android.ui.conversations.listeners.OnConversationLongClickListener;
import org.chat21.android.ui.decorations.ItemDecoration;
import org.chat21.android.ui.messages.activities.MessageListActivity;

import static org.chat21.android.utils.DebugConstants.DEBUG_MY_PRESENCE;

/**
 * Created by stefano on 15/10/2016.
 */
public class Bak_ConversationListFragment extends Fragment implements
        ConversationsListener,
        OnConversationClickListener,
        OnConversationLongClickListener,
        MyPresenceListener {

    private static final String TAG = Bak_ConversationListFragment.class.getName();

    private ConversationsHandler conversationsHandler;
    private MyPresenceHandler myPresenceHandler;

    // conversation list recyclerview
    private RecyclerView recyclerViewConversations;
    private LinearLayoutManager rvConversationsLayoutManager;
    private ConversationsListAdapter conversationsListAdapter;

    // no conversations layout
    private RelativeLayout noConversationsLayout;

    private FloatingActionButton addNewConversation;

    private TextView currentUserGroups;

//    private SwipeHelper swipeHelper;
//    private List<UnderlayButton> swipeableMenu;

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
//        public void onMyPresenceError(Exception e) {
//            Log.e(DEBUG_MY_PRESENCE, "ConversationListFragment.onMyPresenceChange" +
//                    ".onMyPresenceError: " + e.getMessage());
//        }
//    };

    public static Fragment newInstance() {
        Fragment mFragment = new Bak_ConversationListFragment();
        return mFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        swipeableMenu = new ArrayList<>();

        conversationsHandler = ChatManager.getInstance().getConversationsHandler();
        myPresenceHandler = ChatManager.getInstance().getMyPresenceHandler();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "ConversationListFragment.onCreateView");
        View view = inflater.inflate(R.layout.fragment_conversation_list, container, false);

        // init RecyclerView
        recyclerViewConversations = view.findViewById(R.id.conversations_list);
//        recyclerViewConversations.addItemDecoration(new ItemDecoration(getActivity(),
//                getResources().getDrawable(R.drawable.decorator_fragment_conversation_list)));
        recyclerViewConversations.addItemDecoration(new ItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL,
                getResources().getDrawable(R.drawable.decorator_fragment_conversation_list)));
        rvConversationsLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversations.setLayoutManager(rvConversationsLayoutManager);

//        initSwipeableMenu();
//        attachSwipe(recyclerViewConversations);

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

        conversationsHandler.upsertConversationsListener(this);
        Log.d(TAG, "ConversationListFragment.onCreateView: conversationMessagesHandler attached");
        conversationsHandler.connect();

        myPresenceHandler.upsertPresenceListener(this);
        Log.d(DEBUG_MY_PRESENCE, "ConversationListFragment.onCreateView: myPresenceHandler attached");
        myPresenceHandler.connect();

//        // subscribe for current user presence changes
//        PresenceManger.observeUserPresenceChanges(ChatManager.getInstance().getTenant(),
//                ChatManager.getInstance().getLoggedUser().getId(), onMyPresenceListener);
    }

    @Override
    public void onDestroy() {

        conversationsHandler.removeConversationsListener(this);
        Log.d(TAG, "ConversationListFragment.onDestroy: conversationMessagesHandler detached");

        myPresenceHandler.removePresenceListener(this);
        Log.d(DEBUG_MY_PRESENCE, "ConversationListFragment.onDestroy: myPresenceHandler detached");

        super.onDestroy();
    }

    // check if the support account is enabled or not and assign the listener
    private void setAddNewConversationClickBehaviour() {
        Log.d(TAG, "ConversationListFragment.setAddNewConversationClickBehaviour");

        addNewConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ChatUI.getInstance().getOnNewConversationClickListener() != null) {
                    ChatUI.getInstance().getOnNewConversationClickListener().onNewConversationClicked();
                }
            }
        });
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
        if (ChatUI.getInstance().areGroupsEnabled()) {
            // groups enabled
            currentUserGroups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ChatGroupsListActivity.class);
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

        conversationsListAdapter.notifyDataSetChanged();

        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
    }

    @Override
    public void onConversationChanged(Conversation conversation, ChatRuntimeException e) {
        // existing conversation updated

        Log.d(TAG, "ConversationListFragment.onConversationChanged");

        conversationsListAdapter.notifyDataSetChanged();

        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
    }

    @Override
    public void onConversationRemoved(ChatRuntimeException e) {
        Log.d(TAG, "ConversationListFragment.onConversationRemoved");
        if (e == null) {
            conversationsListAdapter.notifyDataSetChanged();
            toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
        } else {
            Log.d(TAG, "ConversationListFragment.onConversationRemoved: " + e.toString());
        }
    }

    @Override
    public void onConversationClicked(Conversation conversation, int position) {
        // click on conversation

        // set the conversation as read
        conversationsHandler.setConversationRead(conversation.getConversationId());

        // start the message list activity of the corresponding conversation
        startMessageActivity(conversation);
    }

    @Override
    public void onConversationLongClicked(Conversation conversation, int position) {
        // long click on conversation

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        BottomSheetConversationsListFragmentLongPress dialog =
                BottomSheetConversationsListFragmentLongPress.newInstance(conversation);
        dialog.setConversationsHandler(conversationsHandler);
        dialog.show(ft, BottomSheetConversationsListFragmentLongPress.class.getName());
    }

    private void startMessageActivity(Conversation conversation) {
        Log.d(TAG, "ConversationListFragment.startMessageActivity: conversation == " + conversation.toString());

        Intent intent = new Intent(getActivity(), MessageListActivity.class);
        IChatUser recipient = new ChatUser(conversation.getConvers_with(), conversation.getConvers_with_fullname());
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, recipient);
        intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, conversation.getChannelType());
        getActivity().startActivity(intent);
    }

    @Override
    public void isLoggedUserOnline(boolean isConnected, String deviceId) {
        // TODO: 09/01/18
        Log.d(DEBUG_MY_PRESENCE, "ConversationListFragment.isUserOnline: " +
                "isConnected == " + isConnected + ", deviceId == " + deviceId);
    }

    @Override
    public void onMyPresenceError(Exception e) {
        // TODO: 09/01/18
        Log.e(DEBUG_MY_PRESENCE, "ConversationListFragment.onMyPresenceError: " + e.toString());
    }

//    private void attachSwipe(RecyclerView recyclerView) {
//
//        // attach only if the menu has button
//        if (swipeableMenu != null && swipeableMenu.size() > 0) {
//            swipeHelper = new SwipeHelper(getActivity(), recyclerView) {
//                @Override
//                public float getButtonWidth() {
//
//                    // 2/3 of the screen size
//                    double width = Resources.getSystem().getDisplayMetrics().widthPixels * 0.6;
//
//                    // number of items
//                    int numberOfItems = swipeableMenu.size();
//
//                    // calculate the item width based on the map size
//                    return (float) (width / numberOfItems);
//                }
//
//                @Override
//                public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
//                    // add all the custom actions to the menu
//                    for (UnderlayButton button : swipeableMenu) {
//                        underlayButtons.add(button);
//                    }
//                }
//            };
//
//            swipeHelper.attachSwipe();
//        }
//    }
//    private void initSwipeableMenu() {
//        swipeableMenu.add(new UnderlayButton(
//                getActivity(),
//                "Chiudi",
//                0,
//                Color.parseColor("#4970A3"),
//                new UnderlayButton.UnderlayButtonClickListener() {
//                    @Override
//                    public void onClick(int pos) {
//                        // TODO
//
//                        if (swipeHelper != null)
//                            swipeHelper.recoverSwipedItem();
//                        else
//                            Log.d(TAG, "LOL");
//                    }
//                }
//        ));
//
//        swipeableMenu.add(new UnderlayButton(
//                getActivity(),
//                "Non letto",
//                0,
//                Color.parseColor("#2200FF"),
//                new UnderlayButton.UnderlayButtonClickListener() {
//                    @Override
//                    public void onClick(int pos) {
//                        // TODO
//
//                        if (swipeHelper != null)
//                            swipeHelper.recoverSwipedItem();
//                        else
//                            Log.d(TAG, "LOL");
//                    }
//                }
//        ));
//    }
}