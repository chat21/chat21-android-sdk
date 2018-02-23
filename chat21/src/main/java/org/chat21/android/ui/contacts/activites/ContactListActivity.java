package org.chat21.android.ui.contacts.activites;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import org.chat21.android.R;
import org.chat21.android.connectivity.AbstractNetworkReceiver;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.contacts.listeners.ContactListener;
import org.chat21.android.core.contacts.synchronizers.ContactsSynchronizer;
import org.chat21.android.core.exception.ChatRuntimeException;
import org.chat21.android.core.messages.models.Message;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.chat_groups.activities.AddMembersToGroupActivity;
import org.chat21.android.ui.contacts.adapters.ContactListAdapter;
import org.chat21.android.ui.contacts.listeners.OnContactClickListener;
import org.chat21.android.ui.decorations.ItemDecoration;
import org.chat21.android.ui.messages.activities.MessageListActivity;
import org.chat21.android.utils.image.CropCircleTransformation;

import static org.chat21.android.ui.ChatUI.REQUEST_CODE_CREATE_GROUP;
import static org.chat21.android.utils.DebugConstants.DEBUG_CONTACTS_SYNC;

/**
 * Created by stefano on 25/08/2015.
 */
public class ContactListActivity extends AppCompatActivity implements OnContactClickListener,
        ContactListener {
    private static final String TAG = ContactListActivity.class.getSimpleName();

//    public static final String TAG_CONTACTS_SEARCH = "TAG_CONTACTS_SEARCH";

    private RecyclerView recyclerView;
    private List<IChatUser> contactList;
    private ContactListAdapter mAdapter;
    private SearchView searchView;
    private RelativeLayout mEmptyLayout;
    private ImageView mGroupIcon;
    private LinearLayout mBoxCreateGroup;

    private ContactsSynchronizer contactsSynchronizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        this.contactsSynchronizer = ChatManager.getInstance().getContactsSynchronizer();

        // contacts
        contactList = contactsSynchronizer.getContacts();
        excludeLoggedUser(contactList);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // empty layout
        mEmptyLayout = (RelativeLayout) findViewById(R.id.layout_no_contacts);
        TextView mSubTitle = (TextView) mEmptyLayout.findViewById(R.id.error_subtitle);
        mSubTitle.setVisibility(View.GONE);

        // contacts list adapter
        mAdapter = new ContactListAdapter(contactList);
        mAdapter.setOnContactClickListener(this);

        // recyclerview
        recyclerView = findViewById(R.id.user_list);
        recyclerView.addItemDecoration(new ItemDecoration(this,
                getResources().getDrawable(R.drawable.decorator_activity_contact_list)));
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        toggleEmptyLayout(contactList); // show or hide the empty layout

        // create group box
        mBoxCreateGroup = (LinearLayout) findViewById(R.id.box_create_group);
        mGroupIcon = (ImageView) findViewById(R.id.group_icon);
        initBoxCreateGroup();

        this.contactsSynchronizer.upsertContactsListener(this);
        Log.d(DEBUG_CONTACTS_SYNC,
                "ContactListActivity.onCreate: contactsSynchronizer attached");
//        this.contactsSynchronizer.connect();
    }

    /**
     * It excludes the logged user from the list of visible users
     * @param contactList the list from which to exclude the logged in user
     */
    private void excludeLoggedUser(List<IChatUser> contactList) {
       int loggedUserIndex = contactList.indexOf(ChatManager.getInstance().getLoggedUser());
       if(loggedUserIndex != -1) {
           contactList.remove(loggedUserIndex);
       }
    }

    @Override
    protected void onDestroy() {

        contactsSynchronizer.removeContactsListener(this);
        Log.d(DEBUG_CONTACTS_SYNC,
                "ContactListActivity.onDestroy: contactsSynchronizer detached");

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_contacts_list, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
//                Log.d(TAG, "ContactListActivity.OnQueryTextListener.onQueryTextChange:" +
//                        " query == " + query);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
//            searchView.setIconified(true);
            searchView.onActionViewCollapsed();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onContactClicked(IChatUser contact, int position) {
        Log.d(TAG, "onRecyclerItemClicked");
        Log.d(TAG, "contact: " + contact.toString() + ", position: " + position);

        if (ChatUI.getInstance().getOnContactClickListener() != null) {
            ChatUI.getInstance().getOnContactClickListener().onContactClicked(contact, position);
        }

        // start the conversation activity
        startMessageListActivity(contact);
    }

    private void startMessageListActivity(IChatUser contact) {
        Log.d(TAG, "startMessageListActivity");

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, contact);
        intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.DIRECT_CHANNEL_TYPE);

        startActivity(intent);

        // finish the contact list activity when it start a new conversation
        finish();
    }

    private void toggleEmptyLayout(List<IChatUser> contacts) {
        if (contacts != null && contacts.size() > 0) {
            mEmptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            mEmptyLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onContactReceived(IChatUser contact, ChatRuntimeException e) {
        if (e == null) {
            toggleEmptyLayout(contactList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onContactChanged(IChatUser contact, ChatRuntimeException e) {
        if (e == null) {
            toggleEmptyLayout(contactList);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onContactRemoved(IChatUser contact, ChatRuntimeException e) {
        if (e == null) {
            toggleEmptyLayout(contactList);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initBoxCreateGroup() {
        Log.d(TAG, "initBoxCreateGroup");

        if (ChatUI.getInstance().areGroupsEnabled()) {
            Glide.with(getApplicationContext())
                    .load("")
                    .placeholder(R.drawable.ic_group_avatar)
                    .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                    .into(mGroupIcon);

            // box click
            mBoxCreateGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AbstractNetworkReceiver.isConnected(getApplicationContext())) {

                        if (ChatUI.getInstance().getOnCreateGroupClickListener() != null) {
                            ChatUI.getInstance().getOnCreateGroupClickListener()
                                    .onCreateGroupClicked();
                        }

                        startCreateGroupActivity();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.activity_contact_list_error_cannot_create_group_offline_label),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mBoxCreateGroup.setVisibility(View.VISIBLE);
        } else {
            mBoxCreateGroup.setVisibility(View.GONE);
        }
    }

    private void startCreateGroupActivity() {
        Log.d(TAG, "startCreateGroupActivity");

        Intent intent = new Intent(this, AddMembersToGroupActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CREATE_GROUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }
}