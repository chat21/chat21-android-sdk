package chat21.android.ui.chat_groups.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.chat_groups.models.ChatGroup;
import chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import chat21.android.core.contacts.synchronizers.ContactsSynchronizer;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.contacts.listeners.OnContactClickListener;
import chat21.android.ui.chat_groups.WizardNewGroup;
import chat21.android.ui.chat_groups.adapters.SelectedContactListAdapter;
import chat21.android.ui.chat_groups.listeners.OnRemoveClickListener;
import chat21.android.utils.image.CropCircleTransformation;

import static chat21.android.ui.ChatUI.BUNDLE_CHAT_GROUP;
import static chat21.android.ui.ChatUI.REQUEST_CODE_CREATE_GROUP;

/**
 * Created by stefanodp91 on 26/01/18.
 */

public class AddMembersToGroupActivity extends AppCompatActivity implements OnContactClickListener, OnRemoveClickListener {

    private ContactsSynchronizer contactsSynchronizer;
    private GroupsSyncronizer groupsSyncronizer;

    private List<IChatUser> dictionaryWords; // List of all dictionary words
    private List<IChatUser> filteredList;
    private List<IChatUser> selectedContactsList;

    // contacts views
    private RecyclerView contactsListView;  // contacts
    private ContactsListAdapter contactsListAdapter;
    private CardView contactsListCard;

    // selected contacts views
    private RecyclerView selectedContactsListView; // selected contacts to add to the group
    private SelectedContactListAdapter selectedContactsListAdapter;
    private CardView selectedContactsListCard;

    private RelativeLayout emptyLayout;
    private SearchView searchView;
    private MenuItem actionNextMenuItem;

    private ChatGroup chatGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);

        // setup providers
        this.contactsSynchronizer = ChatManager.getInstance().getContactsSynchronizer();
        this.groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();

        // setup data
        dictionaryWords = getContactsListWithoutCurrentUser();
        filteredList = new ArrayList<>();
        filteredList.addAll(dictionaryWords);
        selectedContactsList = new ArrayList<IChatUser>();

        // retrieve the chatGroup if exists
        if (getIntent().hasExtra(BUNDLE_CHAT_GROUP)) {
            chatGroup = (ChatGroup) getIntent().getSerializableExtra(BUNDLE_CHAT_GROUP);
        }

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactsListCard = findViewById(R.id.cardview_contacts_list);
        selectedContactsListCard = findViewById(R.id.cardview_selected_contacts);
        emptyLayout = (RelativeLayout) findViewById(R.id.layout_no_contacts);
        contactsListView = (RecyclerView) findViewById(R.id.item_list);
        selectedContactsListView = (RecyclerView) findViewById(R.id.selected_list);

        // contact list views
        setupContactList();

        // selected contact list views
        setupSelectedContactList();
    }

    private void setupContactList() {
        contactsListView.setLayoutManager(new LinearLayoutManager(this));
//        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
//        itemDecorator.setDrawable(getResources().getDrawable(R.drawable.decorator_add_filtered_member));
//        mContactList.addItemDecoration(itemDecorator);
        contactsListView.setItemAnimator(new DefaultItemAnimator());
        updateContactListAdapter(filteredList);
    }

    private void setupSelectedContactList() {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        selectedContactsListView.setLayoutManager(layoutManager);
        selectedContactsListView.setItemAnimator(new DefaultItemAnimator());
        updateSelectedContactListAdapter(selectedContactsList, 0);
    }

    private void updateContactListAdapter(List<IChatUser> contactList) {

        toggleEmptyLayout(contactList);

        if (contactsListAdapter == null) {
            contactsListAdapter = new ContactsListAdapter(this, contactList);
            contactsListAdapter.setHasStableIds(true);
            contactsListAdapter.setOnContactClickListener(this);
            contactsListView.setAdapter(contactsListAdapter);
        } else {
            contactsListAdapter.setList(contactList);
            contactsListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * @param selectedList
     * @param position     position to move the view when an item is added/removed
     */
    private void updateSelectedContactListAdapter(List<IChatUser> selectedList, int position) {

        if (selectedContactsListAdapter == null) {
            selectedContactsListAdapter = new SelectedContactListAdapter(this, selectedList);
            selectedContactsListAdapter.setHasStableIds(true);
            selectedContactsListAdapter.setOnRemoveClickListener(this);
            selectedContactsListView.setAdapter(selectedContactsListAdapter);
        } else {
            selectedContactsListAdapter.setList(selectedList);
            selectedContactsListAdapter.notifyDataSetChanged();
        }

        if (selectedContactsListAdapter.getItemCount() > 0) {
            selectedContactsListCard.setVisibility(View.VISIBLE);
            if (actionNextMenuItem != null) actionNextMenuItem.setVisible(true); // show next action
        } else {
            selectedContactsListCard.setVisibility(View.GONE);
            if (actionNextMenuItem != null)
                actionNextMenuItem.setVisible(false); // hide next action
        }

        selectedContactsListView.smoothScrollToPosition(position);
    }

    private void toggleEmptyLayout(List<IChatUser> contacts) {
        if (contacts != null && contacts.size() > 0) {
            // contacts available
            emptyLayout.setVisibility(View.GONE);
            contactsListCard.setVisibility(View.VISIBLE);
        } else {
            // no contacts
            emptyLayout.setVisibility(View.VISIBLE);
            contactsListCard.setVisibility(View.GONE);
        }
    }

    private void toggleEmptyLayout(int size) {
        if (size > 0) {
            // contacts available
            emptyLayout.setVisibility(View.GONE);
            selectedContactsListCard.setVisibility(View.VISIBLE);
            contactsListCard.setVisibility(View.VISIBLE);
        } else {
            // no contacts
            emptyLayout.setVisibility(View.VISIBLE);
            selectedContactsListCard.setVisibility(View.GONE);
            contactsListCard.setVisibility(View.GONE);
        }
    }

    // exclude the current user from the searchable contact list
    private List<IChatUser> getContactsListWithoutCurrentUser() {

        List<IChatUser> list = new ArrayList<>();

        IChatUser currentUser = ChatManager.getInstance().getLoggedUser();

        if (this.contactsSynchronizer.getContacts() != null)
            for (IChatUser chatUser : this.contactsSynchronizer.getContacts()) {
                if (!chatUser.getId().equals(currentUser.getId())) {
                    list.add(chatUser);
                }
            }

        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_activity_add_members, menu);

        initSearchMenuItem(menu);

        actionNextMenuItem = menu.findItem(R.id.action_next);

        return true;
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
                    contactsListAdapter.getFilter().filter("");
                } else {
                    contactsListAdapter.getFilter().filter(newText.toString());
                }
                return true;
            }
        });
    }

    private void onActionNextClicked() {

        // convert the members list to a sanified format
        Map<String, Integer> membersMap = convertListToMap(selectedContactsList);

        if (chatGroup != null) {
            groupsSyncronizer.addMembersToChatGroup(chatGroup.getGroupId(), membersMap);
            finish(); // back to previous activity
        } else {
            WizardNewGroup.getInstance().getTempChatGroup().addMembers(membersMap);
            Intent intent = new Intent(this, NewGroupActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CREATE_GROUP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_next) {
            onActionNextClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRemoveClickListener(int position) {

        IChatUser contact = selectedContactsList.get(position);
        // remove the contact only if it exists
        if (isContactAlreadyAdded(contact, selectedContactsList)) {
            // remove the item at position from the contacts list and update the adapter
            selectedContactsList.remove(position);

            // hide the already added
            contactsListAdapter.hideContactAlreadyAdded(contactsListView, contact);

            updateSelectedContactListAdapter(selectedContactsList, position);
        } else {
            Snackbar.make(findViewById(R.id.coordinator),
                    getString(R.string.add_members_activity_contact_not_added_label),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onContactClicked(IChatUser contact, int position) {
        //close the searchview
        searchView.onActionViewCollapsed();

        // add a contact only if it not exists
        addMemberToGroup(contact, selectedContactsList, position);
    }

    private void addMemberToGroup(IChatUser user, List<IChatUser> contactList, int position) {

        // add a contact only if it not exists
        if (!isContactAlreadyAdded(user, contactList)) {
            // add the contact to the contact list and update the adapter
            contactList.add(user);

            updateSelectedContactListAdapter(contactList, position);

            // shows already added
            contactsListAdapter.showContactAlreadyAdded(contactsListView, position);
        } else {

            // shows already added
            contactsListAdapter.showContactAlreadyAdded(contactsListView, position);
        }
    }

    // check if a contact is already added to a list
    private boolean isContactAlreadyAdded(IChatUser toCheck, List<IChatUser> mlist) {

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

    // convert the list of contact to a map of members
    private Map<String, Integer> convertListToMap(List<IChatUser> contacts) {

        Map<String, Integer> members = new HashMap<>();
        for (IChatUser contact : contacts) {
            // the value "1" is a default value with no usage
            members.put(contact.getId(), 1);
        }

        // add the current user to members list in background
        members.put(ChatManager.getInstance().getLoggedUser().getId(), 1);

        return members;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_CREATE_GROUP) {
            if(resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    // create a custom RecycleViewAdapter class
    public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.ViewHolder> implements
            Filterable {
        private List<IChatUser> mValues;
        private ContactsListAdapter.CustomFilter mFilter;
        private OnContactClickListener onContactClickListener;

        public ContactsListAdapter(Context context, List<IChatUser> items) {
            mValues = items;
            mFilter = new ContactsListAdapter.CustomFilter(ContactsListAdapter.this);
        }

        public OnContactClickListener getOnContactClickListener() {
            return onContactClickListener;
        }

        public void setOnContactClickListener(OnContactClickListener onContactClickListener) {
            this.onContactClickListener = onContactClickListener;
        }

        @Override
        public ContactsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_add_member, parent, false);
            return new ContactsListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ContactsListAdapter.ViewHolder holder, final int position) {
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
            ContactsListAdapter.ViewHolder holder =
                    (ContactsListAdapter.ViewHolder) recyclerView.findViewHolderForLayoutPosition(position);

            // If the view has been recycled, it is null
            if (holder != null) {
                // hide the username label
                holder.mUsername.setVisibility(View.GONE);

                // show the already added label
                holder.mAlreadyAddedLabel.setVisibility(View.VISIBLE);
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

                // shows empty layout
                if (results.count == 0) {
                    toggleEmptyLayout(results.count);
                }

                this.mAdapter.notifyDataSetChanged();
            }
        }
    }
}
