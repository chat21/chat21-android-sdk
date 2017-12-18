package chat21.android.ui.groups.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.groups.models.Group;
import chat21.android.core.users.models.IChatUser;
import chat21.android.groups.utils.GroupUtils;
import chat21.android.ui.ChatUI;
import chat21.android.ui.contacts.listeners.OnContactClickListener;
import chat21.android.ui.groups.adapters.SelectedContactListAdapter;
import chat21.android.ui.groups.listeners.OnRemoveClickListener;
import chat21.android.utils.StringUtils;
import chat21.android.utils.image.CropCircleTransformation;

/**
 * Created by stefanodp91 on 16/01/17.
 * <p>
 * bugfix Issue #14
 */
public class AddMembersActivity extends AppCompatActivity implements
        OnContactClickListener,
        OnRemoveClickListener,
        GroupUtils.OnGroupCreatedListener,
        GroupUtils.OnGroupUpdatedListener {
    private static final String TAG = AddMembersActivity.class.getName();

    // List of all dictionary words
    private List<IChatUser> dictionaryWords;
    private List<IChatUser> filteredList;
    private List<IChatUser> mSelectedList;

    private Toolbar mToolbar;

    // tutti i contatti della rubrica
    private RecyclerView mContactList;
    private ContactsListAdapter mContactListAdapter;

    // contatti selezionati da aggiungere al gruppo
    private RecyclerView mSelectedContacts;
    private SelectedContactListAdapter mSelectedContactListAdapter;
    private TextView mBoxSelectedContactsLabel;

    private RelativeLayout mEmptyLayout;
    private TextView mContactsListLabel;

    private MenuItem mCreateMenuItem;
    private MenuItem mAddMemberMenuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);

        initData();
        registerViews();
        initViews();
    }

    private void initData() {
        dictionaryWords = getContactsListWithoutCurrentUser();
        filteredList = new ArrayList<IChatUser>();
        filteredList.addAll(dictionaryWords);
        initSelectedList();
    }

    private void initSelectedList() {
        mSelectedList = new ArrayList<IChatUser>();
    }

    private void registerViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mContactList = (RecyclerView) findViewById(R.id.item_list);
        mSelectedContacts = (RecyclerView) findViewById(R.id.selected_list);
        mBoxSelectedContactsLabel = (TextView) findViewById(R.id.selected_contacts_label);
        mContactsListLabel = (TextView) findViewById(R.id.contact_list_label);
        mEmptyLayout = (RelativeLayout) findViewById(R.id.layout_no_contacts);
    }

    private void initViews() {
        initToolbar();
        initContactListRecyclerView();
        initSelectedContactListRecyclerView();
        updateContactListAdapter(filteredList);
        initEmptyLayout();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initContactListRecyclerView() {
        mContactList.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(getResources().getDrawable(R.drawable.decorator_add_filtered_member));
        mContactList.addItemDecoration(itemDecorator);
        mContactList.setItemAnimator(new DefaultItemAnimator());
    }

//    private void initAlreadyAddedContacts(final List<IChatUser> contacts) {
//        ValueEventListener groupMembersCallback = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "initAlreadyAddedContacts.groupMembersCallback.onDataChange:" +
//                        "dataSnapshot == " + dataSnapshot.toString());
//
//                Map<String, Object> members = GroupUtils.decodeGroupMembersSnapshop(dataSnapshot);
//
//                for (Map.Entry<String, Object> entry : members.entrySet()) {
//                    for (int i = 0; i < contacts.size(); i++) {
//                        IChatUser contact = contacts.get(i);
//                        if (entry.getKey().equals(contact.getId())) {
//                            mContactListAdapter.showContactAlreadyAdded(mContactList, i);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d(TAG, "initAlreadyAddedContacts.groupMembersCallback.onCancelled:" +
//                        "databaseError == " + databaseError.toString());
//            }
//        };
//
//        mNodeDAO.getGroupMembersNode(getGroupId())
//                .addValueEventListener(groupMembersCallback);
//    }

    private void initSelectedContactListRecyclerView() {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mSelectedContacts.setLayoutManager(layoutManager);
        mSelectedContacts.setItemAnimator(new DefaultItemAnimator());
        updateSelectedContactListAdapter(mSelectedList, 0);
    }

    private void initEmptyLayout() {
        TextView mSubTitle = (TextView) mEmptyLayout.findViewById(R.id.error_subtitle);
        mSubTitle.setVisibility(View.GONE);
    }

    private void updateContactListAdapter(List<IChatUser> contactList) {

        toggleEmptyLayout(contactList);

        if (mContactListAdapter == null) {
            mContactListAdapter = new ContactsListAdapter(this, contactList);
            mContactListAdapter.setHasStableIds(true);
            mContactListAdapter.setOnContactClickListener(this);
            mContactList.setAdapter(mContactListAdapter);
        } else {
            mContactListAdapter.setList(contactList);
            mContactListAdapter.notifyDataSetChanged();
        }

//        initAlreadyAddedContacts(contactList);
    }

    /*
     * @param selectedList
     * @param position     position in cui spostare la visuale quando si aggiunge/rimuove un elemento
     */
    private void updateSelectedContactListAdapter(List<IChatUser> selectedList, int position) {

        if (mSelectedContactListAdapter == null) {
            mSelectedContactListAdapter = new SelectedContactListAdapter(this, selectedList);
            mSelectedContactListAdapter.setHasStableIds(true);
            mSelectedContactListAdapter.setOnRemoveClickListener(this);
            mSelectedContacts.setAdapter(mSelectedContactListAdapter);
        } else {
            mSelectedContactListAdapter.setList(selectedList);
            mSelectedContactListAdapter.notifyDataSetChanged();
        }

        if (mSelectedContactListAdapter.getItemCount() > 0) {
            mBoxSelectedContactsLabel.setVisibility(View.VISIBLE);
        } else {
            mBoxSelectedContactsLabel.setVisibility(View.GONE);
        }

        mSelectedContacts.smoothScrollToPosition(position);

        if (StringUtils.isValid(getParentActivity()) &&
                getParentActivity().equals(CreateGroupActivity.class.getName())) {
            toggleCreateBtn();
        } else if (StringUtils.isValid(getParentActivity()) &&
                getParentActivity().equals(GroupAdminPanelActivity.class.getName())) {
            toggleAddMemberBtn();
        }
    }

    private void toggleEmptyLayout(List<IChatUser> contacts) {
        if (contacts != null && contacts.size() > 0) {
            // contacts available
            mEmptyLayout.setVisibility(View.GONE);
            mSelectedContacts.setVisibility(View.VISIBLE);
            mContactsListLabel.setVisibility(View.VISIBLE);
        } else {
            // no contacts
            mEmptyLayout.setVisibility(View.VISIBLE);
            mSelectedContacts.setVisibility(View.GONE);
            mContactsListLabel.setVisibility(View.GONE);
        }
    }


    @Override
    public void onContactClicked(IChatUser item, int position) {

        //close the searchview
        searchView.onActionViewCollapsed();

        // add a contact only if it not exists
        addUserToMembersGroup(item, mSelectedList, position);
    }

    private void addUserToMembersGroup(IChatUser user, List<IChatUser> contactList, int position) {
        Log.d(TAG, "addUserToMembersGroup");

        // add a contact only if it not exists
        if (!isContactAlreadyAdded(user, contactList)) {
            // add the contact to the contact list and update the adapter
            contactList.add(user);

            updateSelectedContactListAdapter(contactList, position);

            // shows already added
            mContactListAdapter.showContactAlreadyAdded(mContactList, position);
        } else {

            // shows already added
            mContactListAdapter.showContactAlreadyAdded(mContactList, position);
        }
    }

    // check if a contact is already added to a list
    private boolean isContactAlreadyAdded(IChatUser toCheck, List<IChatUser> mlist) {
        Log.d(TAG, "isContactAlreadyAdded");

        boolean exists = false;
        for (IChatUser contact : mlist) {
            String contactId = contact.getId();

            if (contactId.equals(toCheck.getId())) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    // exclude the current user from the searchable contact list
    private List<IChatUser> getContactsListWithoutCurrentUser() {
        Log.d(TAG, "getContactsListWithoutCurrentUser");

        List<IChatUser> list = new ArrayList<>();

        IChatUser currentUser = ChatManager.getInstance()
                .getLoggedUser();

        if (ChatManager.getInstance().getContacts() != null)
            for (IChatUser chatUser : ChatManager.getInstance().getContacts()) {
                if (!chatUser.getId().equals(currentUser.getId())) {
                    list.add(chatUser);
                }
            }

        return list;
    }

    @Override
    public void onRemoveClickListener(int position) {
        Log.d(TAG, "onRemoveClickListener");

        try {
            IChatUser contact = mSelectedList.get(position);
            // remove the contact only if it exists
            if (isContactAlreadyAdded(contact, mSelectedList)) {
                // remove the item at position from the contacts list and update the adapter
                mSelectedList.remove(position);

                // hide the already added
                mContactListAdapter.hideContactAlreadyAdded(mContactList, contact);

                updateSelectedContactListAdapter(mSelectedList, position);
            } else {
                Snackbar.make(findViewById(R.id.coordinator),
                        getString(R.string.add_members_activity_contact_not_added_label),
                        Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Snackbar.make(findViewById(R.id.coordinator),
                    getString(R.string.add_members_activity_contact_cannot_remove_contact_label),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    // create a custom RecycleViewAdapter class
    public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.ViewHolder> implements
            Filterable {
        private List<IChatUser> mValues;
        private CustomFilter mFilter;
        private OnContactClickListener onContactClickListener;

        public ContactsListAdapter(Context context, List<IChatUser> items) {
            mValues = items;
            mFilter = new CustomFilter(ContactsListAdapter.this);
        }

        public OnContactClickListener getOnContactClickListener() {
            return onContactClickListener;
        }

        public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
            this.onContactClickListener = onContactClickListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_add_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);

            Glide.with(holder.itemView.getContext())
                    .load(mValues.get(position).getProfilePictureUrl())
                    .placeholder(R.drawable.ic_person_avatar)
                    .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))
                    .into(holder.mImageView);

            holder.mFullname.setText(mValues.get(position).getFullName());
            holder.mUsername.setText(mValues.get(position).getId());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getOnContactClickListener().onContactClicked(mValues.get(position), position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        public void setList(List<IChatUser> list) {
            mValues = list;
        }

        private void showContactAlreadyAdded(RecyclerView recyclerView, int position) {
//            View viewAtPosition = recyclerView.getLayoutManager().getChildAt(position);
            ContactsListAdapter.ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForLayoutPosition(position);

            // If the view has been recycled, it is null
            if (holder != null) {
                // hide the username label
                holder.mUsername.setVisibility(View.GONE);

                // show the already added label
                holder.mAlreadyAddedLabel.setVisibility(View.VISIBLE);

//                // show the already added icon
//                holder.mAlreadyAddedIcon.setVisibility(View.VISIBLE);
            }
        }

        private void hideContactAlreadyAdded(RecyclerView recyclerView, IChatUser contact) {

            // get the contact position
            int position = 0;
            for (int i = 0; i < mValues.size(); i++) {
                if (mValues.get(i).getId().equals(contact.getId())) {
                    position = i;
                    break;
                }
            }

            // retrieve the contact view by position
            View viewAtPosition = recyclerView.getLayoutManager().getChildAt(position);

            // If the view has been recycled, it is null
            if (viewAtPosition != null) {
                // show the username label
                TextView mUsername = viewAtPosition.findViewById(R.id.username);
                mUsername.setVisibility(View.VISIBLE);

                // hide the already added label
                TextView mAlreadyAddedLabel = viewAtPosition.findViewById(R.id.already_added_label);
                mAlreadyAddedLabel.setVisibility(View.GONE);

//                // hide the already added icon
//                LinearLayout mAlreadyAddedIcon = viewAtPosition.findViewById(R.id.already_added_icon);
//                mAlreadyAddedIcon.setVisibility(View.GONE);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final ImageView mImageView;
            public final TextView mFullname;
            public final TextView mUsername;
            public final TextView mAlreadyAddedLabel;
            public final LinearLayout mAlreadyAddedIcon;
            public IChatUser mItem;

            public ViewHolder(View view) {
                super(view);
                mImageView = (ImageView) view.findViewById(R.id.profile_picture);
                mFullname = (TextView) view.findViewById(R.id.fullname);
                mUsername = (TextView) view.findViewById(R.id.username);
                mAlreadyAddedLabel = (TextView) view.findViewById(R.id.already_added_label);
                mAlreadyAddedIcon = (LinearLayout) view.findViewById(R.id.already_added_icon);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mFullname.getText() + "'";
            }
        }

        public class CustomFilter extends Filter {
            private ContactsListAdapter mAdapter;

            private CustomFilter(ContactsListAdapter mAdapter) {
                super();
                this.mAdapter = mAdapter;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                filteredList.clear();
                final FilterResults results = new FilterResults();
                if (constraint.length() == 0) {
                    filteredList.addAll(dictionaryWords);
                } else {
                    final String filterPattern = constraint.toString().toLowerCase().trim();
                    for (final IChatUser mWords : dictionaryWords) {
                        // query di tipo like su fullname e id
                        if (mWords.getFullName().toLowerCase().contains(filterPattern) ||
                                mWords.getId().toLowerCase().contains(filterPattern)) {
                            filteredList.add(mWords);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                this.mAdapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.menu_activity_add_members, menu);

        initCreateMenuItem(menu);

        initAddMemberMenuItem(menu);

        initSearchMenuItem(menu);

        return true;
    }

    private void initCreateMenuItem(Menu menu) {
        mCreateMenuItem = menu.findItem(R.id.action_create); // create group button
        mCreateMenuItem.setVisible(false); // create menu item must not to be visible
    }

    private void initAddMemberMenuItem(Menu menu) {
        mAddMemberMenuItem = menu.findItem(R.id.action_add_member); // add member button
        mAddMemberMenuItem.setVisible(false); // add member menu item must not to be visible
    }

    private void initSearchMenuItem(Menu menu) {
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    mContactListAdapter.getFilter().filter("");
                } else {
                    mContactListAdapter.getFilter().filter(newText.toString());
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_create) {
            onCreateOptionsItemClicked();
            return true;
        } else if (item.getItemId() == R.id.action_add_member) {
            onAddMemberOptionsItemClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // show / hide the btn next
    private void toggleCreateBtn() {
        Log.d(TAG, "toggleCreateBtn");

        if (mCreateMenuItem != null) {
            if (mSelectedList.size() > 0) {
                mCreateMenuItem.setVisible(true);
            } else {
                mCreateMenuItem.setVisible(false);
            }
        } else {
            Log.d(TAG, "mCreateMenuItem is null");
        }
    }

    // show / hide the btn add member
    private void toggleAddMemberBtn() {
        Log.d(TAG, "toggleAddMemberBtn");

        if (mAddMemberMenuItem != null) {
            if (mSelectedList.size() > 0) {
                mAddMemberMenuItem.setVisible(true);
            } else {
                mAddMemberMenuItem.setVisible(false);
            }
        } else {
            Log.d(TAG, "mAddMemberMenuItem is null");
        }
    }

    private void onCreateOptionsItemClicked() {
        Log.d(TAG, "onCreateOptionsItemClicked");

        Group group = addMembersToGroup();
        Log.d(TAG, group.toString());

        uploadGroup(ChatManager.getInstance().getTenant(), getGroupId(), group);
    }

    private void onAddMemberOptionsItemClicked() {
        Log.d(TAG, "onAddMemberOptionsItemClicked");

        Group group = addMembersToGroup();
        Log.d(TAG, group.toString());

        uploadGroup(ChatManager.getInstance().getTenant(), getGroupId(), group);
    }

    // add the list of member to the created group
    private Group addMembersToGroup() {
        Log.d(TAG, "addMembersToGroup");

        Group group = getGroup();
        Map<String, Integer> members = convertListToMap(mSelectedList);

        // if the current group has members update them
        if (group != null && group.getMembers() != null && group.getMembers().size() > 0) {
            members.putAll(group.getMembers());
        }

        group.addMembers(members);
        return group;
    }

    // convert the list of contact to a map of members
    private Map<String, Integer> convertListToMap(List<IChatUser> contacts) {
        Log.d(TAG, "convertListToMap");

        Map<String, Integer> members = new HashMap<>();
        for (IChatUser contact : contacts) {
            // the value "1" is a default value with no usage
            members.put(contact.getId(), 1);
        }

        // add the current user to members list in background
        members.put(ChatManager.getInstance().getLoggedUser().getId(), 1);

        return members;
    }

    private Group getGroup() {
        Log.d(TAG, "getGroup");

        return (Group) getIntent()
                .getExtras()
                .getSerializable(ChatUI._INTENT_BUNDLE_GROUP);
    }

    private String getGroupId() {
        return getIntent()
                .getExtras()
                .getString(ChatUI._INTENT_EXTRAS_GROUP_ID);
    }

    private String getParentActivity() {
        return getIntent()
                .getExtras()
                .getString(ChatUI._INTENT_EXTRAS_PARENT_ACTIVITY);
    }

    private void uploadGroup(String appId, String groupId, final Group group) {
        Log.d(TAG, "uploadGroup");

        if (group.getMembers().size() > 0) {
            if (!StringUtils.isValid(groupId)) {

                DatabaseReference nodeGroups = FirebaseDatabase.getInstance().getReference()
                        .child("apps/" + ChatManager.getInstance().getTenant() + "/groups");

                nodeGroups.push().setValue(group, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        Log.d(TAG, "uploadGroup.onComplete");

                        if (databaseError != null) {
                            String errorMessage = "uploadGroup.onComplete: " +
                                    "group not uploaded. " + databaseError.getMessage();
                            onGroupCreatedError(errorMessage);
                        } else {
                            Log.d(TAG, "group uploaded with success");

                            // example of database reference
                            // https://chat-95351.firebaseio.com/groups/-KncezORPzKzmAiIppSF
                            String groupId = databaseReference.getKey();

                            onGroupCreatedSuccess(groupId, group);
                        }
                    }
                });
            } else {
                FirebaseDatabase.getInstance().getReference()
                        .child("apps/" + appId + "/groups/" + groupId + "/members")
                        .setValue(group.getMembers(),
                                new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError,
                                                           DatabaseReference databaseReference) {
                                        Log.d(TAG, "uploadGroup.onComplete");

                                        if (databaseError != null) {
                                            String errorMessage = "uploadGroup.onComplete: " +
                                                    "group not uploaded. " + databaseError.getMessage();
                                            onGroupUpdatedError(errorMessage);
                                        } else {
                                            Log.d(TAG, "group uploaded with success");

                                            DatabaseReference membersKey = databaseReference.getParent();
                                            String groupId = membersKey.getKey();

                                            onGroupUpdatedSuccess(groupId, group);
                                        }
                                    }
                                });
            }
        } else {
            String errorMessage = "uploadGroup: group not uploaded. " +
                    "cannot upload group. group size is less or equals 0";
            onGroupCreatedError(errorMessage);
        }
    }

    @Override
    public void onGroupCreatedSuccess(String groupId, Group group) {
        Log.d(TAG, "onGroupCreatedSuccess");

        //createConversationsOnFirebaseAsync(ChatManager.getInstance().getTenant(), groupId, group);

        startNextActivity(groupId);
    }

    @Override
    public void onGroupCreatedError(String errorMessage) {
        Log.d(TAG, "onGroupCreatedError");

        Log.e(TAG, errorMessage);
        FirebaseCrash.report(new Exception(errorMessage));
    }

    @Override
    public void onGroupUpdatedSuccess(String groupId, Group group) {
        Log.d(TAG, "onGroupCreatedSuccess");

        //updateConversationsOnFirebaseAsync(ChatManager.getInstance().getTenant(), groupId, group);

        startNextActivity(groupId);
    }

    @Override
    public void onGroupUpdatedError(String errorMessage) {
        Log.d(TAG, "onGroupUpdatedError");

        Log.e(TAG, errorMessage);
        FirebaseCrash.report(new Exception(errorMessage));
    }

    // create a conversation for each member of the group with a custom message.
    // the creator of the group has a custom message
//    private void createConversationsOnFirebaseAsync(String appId, String groupId, Group group) {
//        Log.d(TAG, "createConversationsOnFirebaseAsync");
//
//        IChatUser loggedUser = ChatManager.getInstance().getLoggedUser();
//
//        // iterate the list of members
//        for (Map.Entry<String, Integer> entry : group.getMembers().entrySet()) {
//            Conversation conversation = new Conversation();
//
//            String memberId = entry.getKey();
//
//            // check if the memberid is the current user or not and create the conversation
//            if (memberId.equals(loggedUser.getId())) {
//                // logged user
//                conversation.setLast_message_text(
//                        getString(R.string.menu_add_members_created_the_group, group.getName()));
//            } else {
//                // other members
//                // bugfix Issue #49
//                String groupOwner = group.getOwner();
//                conversation.setLast_message_text(
//                        getString(R.string.menu_add_members_added_to_the_group, groupOwner));
//            }
//
//            // update the common conversation info
////            conversation.setGroup_id(groupId); // group id
////            conversation.setGroup_name(group.getName()); // group name
//            conversation.setIs_new(true);
//            conversation.setSender(loggedUser.getId());
//            conversation.setSender_fullname(loggedUser.getFullName());
//            conversation.setStatus(Conversation.CONVERSATION_STATUS_LAST_MESSAGE);
//            conversation.setConversationId(groupId);
//
//            // upload the conversation
//            ConversationUtils.uploadConversationOnFirebase(appId, groupId, memberId, conversation);
//        }
//    }
//
//    private void updateConversationsOnFirebaseAsync(String appId, String groupId, Group group) {
//        Log.d(TAG, "updateConversationsOnFirebaseAsync");
//
//        IChatUser loggedUser = ChatManager.getInstance().getLoggedUser();
//
//        // iterate the list of members
//        for (Map.Entry<String, Integer> entry : group.getMembers().entrySet()) {
//            Conversation conversation = new Conversation();
//
//            String memberId = entry.getKey();
//
//            // check if the memberid is the current user or not and create the conversation
//            if (!memberId.equals(loggedUser.getId())) {
//
//                // bugfix Issue #49
//                String groupOwner = group.getOwner();
//                conversation.setLast_message_text(
//                        getString(R.string.menu_add_members_added_to_the_group, groupOwner));
//
//                // update the common conversation info
//                conversation.setGroup_id(groupId); // group id
//                conversation.setGroup_name(group.getName()); // group name
//                conversation.setIs_new(true);
//                conversation.setSender(loggedUser.getId());
//                conversation.setSender_fullname(loggedUser.getFullName());
//                conversation.setStatus(Conversation.CONVERSATION_STATUS_LAST_MESSAGE);
//                conversation.setConversationId(groupId);
//
//                // upload the conversation
//                ConversationUtils.uploadConversationOnFirebase(appId, groupId, memberId, conversation);
//            }
//        }
//    }

    private void startNextActivity(String groupId) {
        Log.d(TAG, "startNextActivity");

        Intent callingIntent = getIntent();
        if (callingIntent != null) {
            callingIntent.putExtra(ChatUI._INTENT_EXTRAS_GROUP_ID, groupId);
            setResult(RESULT_OK, callingIntent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}