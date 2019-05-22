package org.chat21.android.ui.chat_groups.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.chat_groups.listeners.ChatGroupsListener;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.ui.chat_groups.adapters.ChatGroupsListAdapter;
import org.chat21.android.ui.chat_groups.listeners.OnGroupClickListener;
import org.chat21.android.ui.decorations.ItemDecoration;

import java.util.List;


/**
 * Created by stefanodp91 on 05/03/18.
 */

public class ChatGroupsListFragment extends Fragment implements ChatGroupsListener {
    private static final String TAG = ChatGroupsListFragment.class.getName();

    private GroupsSyncronizer chatGroupsSynchronizer;
    private OnGroupClickListener onChatGroupClickListener;

    // contacts list recyclerview
    private RecyclerView recyclerViewChatGroups;
    private LinearLayoutManager lmRvChatGroups;
    private ChatGroupsListAdapter chatGroupsListAdapter;

    // no contacts layout
    private RelativeLayout noChatGroupsLayout;

    public static Fragment newInstance() {
        Fragment mFragment = new ChatGroupsListFragment();
        return mFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        chatGroupsSynchronizer = ChatManager.getInstance().getGroupsSyncronizer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_groups_list, container, false);

        // init RecyclerView
        recyclerViewChatGroups = view.findViewById(R.id.chat_groups_list);
        recyclerViewChatGroups.addItemDecoration(new ItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL,
                getResources().getDrawable(R.drawable.decorator_activity_my_groups_list)));
        lmRvChatGroups = new LinearLayoutManager(getActivity());
        recyclerViewChatGroups.setLayoutManager(lmRvChatGroups);
        updateChatGroupsListAdapter(chatGroupsSynchronizer.getChatGroups());

        // no contacts layout
        noChatGroupsLayout = view.findViewById(R.id.layout_no_groups);
        toggleNoContactsLayoutVisibility(chatGroupsListAdapter.getItemCount());

        chatGroupsSynchronizer.addGroupsListener(this);
        chatGroupsSynchronizer.connect();

        return view;
    }

    public void updateChatGroupsListAdapter(List<ChatGroup> list) {
        if (chatGroupsListAdapter == null) {
            // init RecyclerView adapter
            chatGroupsListAdapter = new ChatGroupsListAdapter(getActivity(), list);
            if (getOnChatGroupClickListener() != null)
                chatGroupsListAdapter.setOnGroupClickListener(getOnChatGroupClickListener());
            recyclerViewChatGroups.setAdapter(chatGroupsListAdapter);
        } else {
            chatGroupsListAdapter.setList(list);
            chatGroupsListAdapter.notifyDataSetChanged();
        }
    }

    // toggle the no contacts layout visibilty.
    // if there are items show the list of item, otherwise show a placeholder layout
    private void toggleNoContactsLayoutVisibility(int itemCount) {
        if (itemCount > 0) {
            // show the item list
            recyclerViewChatGroups.setVisibility(View.VISIBLE);
            noChatGroupsLayout.setVisibility(View.GONE);
        } else {
            // show the placeholder layout
            recyclerViewChatGroups.setVisibility(View.GONE);
            noChatGroupsLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setOnChatGroupClickListener(OnGroupClickListener onChatGroupClickListener) {
        this.onChatGroupClickListener = onChatGroupClickListener;
    }

    public OnGroupClickListener getOnChatGroupClickListener() {
        return onChatGroupClickListener;
    }

    @Override
    public void onGroupAdded(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {
            chatGroupsListAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "ChatGroupsListFragment.onGroupAdded: e == " + e.toString());
        }
    }

    @Override
    public void onGroupChanged(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {
            chatGroupsListAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "ChatGroupsListFragment.onGroupChanged: e == " + e.toString());
        }

    }

    @Override
    public void onGroupRemoved(ChatRuntimeException e) {
        if (e == null) {
            chatGroupsListAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "ChatGroupsListFragment.onGroupRemoved: e == " + e.toString());
        }

    }
}