package org.chat21.android.ui.chat_groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.chat_groups.models.ChatGroup;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.chat_groups.WizardNewGroup;
import org.chat21.android.ui.chat_groups.adapters.SelectedContactListAdapter;
import org.chat21.android.ui.chat_groups.listeners.OnRemoveClickListener;
import org.chat21.android.ui.contacts.fragments.ContactsListFragment;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.chat21.android.ui.ChatUI.BUNDLE_CHAT_GROUP;
import static org.chat21.android.ui.ChatUI.REQUEST_CODE_CREATE_GROUP;

/**
 * Created by stefanodp91 on 07/03/18.
 */

public class AddMemberToChatGroupActivity extends AppCompatActivity implements OnContactClickListener, OnRemoveClickListener {

    private static final String TAG = AddMemberToChatGroupActivity.class.getName();

    private ContactsListFragment contactsListFragment;

    private List<IChatUser> selectedList;
    private CardView cvSelectedContacts;
    private RecyclerView rvSelectedList;
    private SelectedContactListAdapter selectedContactsListAdapter;

    private MenuItem actionNextMenuItem;

    private ChatGroup chatGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_to_chat_group);

        selectedList = new ArrayList<>();
        cvSelectedContacts = findViewById(R.id.cardview_selected_contacts);
        rvSelectedList = findViewById(R.id.selected_list);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false);
        rvSelectedList.setLayoutManager(layoutManager);
        rvSelectedList.setItemAnimator(new DefaultItemAnimator());
        updateSelectedContactListAdapter(selectedList, 0);

        contactsListFragment = new ContactsListFragment();
        contactsListFragment.setOnContactClickListener(this);

        // #### BEGIN TOOLBAR ####
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // #### END  TOOLBAR ####

        // #### BEGIN CONTAINER ####
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, contactsListFragment)
                .commit();
        // #### BEGIN CONTAINER ####
    }

    @Override
    protected void onStart() {
        long startTime = System.currentTimeMillis();

        // retrieve the chatGroup if exists
        if (getIntent().hasExtra(BUNDLE_CHAT_GROUP)) {
            chatGroup = (ChatGroup) getIntent().getSerializableExtra(BUNDLE_CHAT_GROUP);
        }

        super.onStart();
    }

    private void updateSelectedContactListAdapter(List<IChatUser> list, int position) {

        if (selectedContactsListAdapter == null) {
            selectedContactsListAdapter = new SelectedContactListAdapter(this, list);
            selectedContactsListAdapter.setOnRemoveClickListener(this);
            rvSelectedList.setAdapter(selectedContactsListAdapter);
        } else {
            selectedContactsListAdapter.setList(list);
            selectedContactsListAdapter.notifyDataSetChanged();
        }

        if (selectedContactsListAdapter.getItemCount() > 0) {
            cvSelectedContacts.setVisibility(View.VISIBLE);
            if (actionNextMenuItem != null) {
                actionNextMenuItem.setVisible(true);
            }// show next action
        } else {
            cvSelectedContacts.setVisibility(View.GONE);
            if (actionNextMenuItem != null) {
                actionNextMenuItem.setVisible(false); // hide next action
            }
        }

        rvSelectedList.smoothScrollToPosition(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_add_members, menu);

        actionNextMenuItem = menu.findItem(R.id.action_next);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "ChooseContactActivity.onOptionsItemSelected");

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_next) {
            onActionNextClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onActionNextClicked() {
        // convert the members list to a sanified format
        Map<String, Integer> membersMap = convertListToMap(selectedList);

        if (chatGroup != null) {
            ChatManager.getInstance().getGroupsSyncronizer()
                    .addMembersToChatGroup(chatGroup.getGroupId(), membersMap);
            finish(); // back to previous activity
        } else {
            WizardNewGroup.getInstance().getTempChatGroup().addMembers(membersMap);
            Intent intent = new Intent(this, NewGroupActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CREATE_GROUP);
        }
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
    public void onContactClicked(IChatUser contact, int position) {
        // add a contact only if it not exists
        addMemberToGroup(contact, selectedList, position);
    }

    private void addMemberToGroup(IChatUser contact, List<IChatUser> contactList, int position) {
        // add a contact only if it not exists
        if (!isContactAlreadyAdded(contact, contactList)) {
            // add the contact to the contact list and update the adapter
            contactList.add(contact);
        }

//        contactsListAdapter.addToAlreadyAddedList(contact, position);

        updateSelectedContactListAdapter(contactList, position);
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

    @Override
    public void onRemoveClickListener(int position) {

        IChatUser contact = selectedList.get(position);
        // remove the contact only if it exists
        if (isContactAlreadyAdded(contact, selectedList)) {
            // remove the item at position from the contacts list and update the adapter
            selectedList.remove(position);

//            contactsListAdapter.removeFromAlreadyAddedList(contact);

            updateSelectedContactListAdapter(selectedList, position);
        } else {
            Snackbar.make(findViewById(R.id.coordinator),
                    getString(R.string.add_members_activity_contact_not_added_label),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        contactsListFragment.onBackPressed();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
