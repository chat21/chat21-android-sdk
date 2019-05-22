package org.chat21.android.ui.users.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import org.chat21.android.R;
import org.chat21.android.core.ChatManager;
import org.chat21.android.core.contacts.synchronizers.ContactsSynchronizer;
import org.chat21.android.core.presence.PresenceHandler;
import org.chat21.android.core.presence.listeners.PresenceListener;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.utils.StringUtils;
import org.chat21.android.utils.TimeUtils;

import static org.chat21.android.utils.DebugConstants.DEBUG_USER_PRESENCE;

/**
 * Created by stefanodp91 on 04/08/17.
 * <p>
 * bugfix Issue #30
 */
public class PublicProfileActivity extends AppCompatActivity implements PresenceListener {
    private static final String TAG = PublicProfileActivity.class.getName();

    private IChatUser contact;
    private PresenceHandler presenceHandler;
    private boolean conversWithOnline = false;
    private long conversWithLastOnline = -1;

    private TextView mToolbarSubTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticvity_public_profile);

        contact = (IChatUser) getIntent().getSerializableExtra(ChatUI.BUNDLE_RECIPIENT);

        presenceHandler = ChatManager.getInstance().getPresenceHandler(contact.getId());

        // BEGIN contactsSynchronizer
        ContactsSynchronizer contactsSynchronizer = ChatManager.getInstance().getContactsSynchronizer();
        if (contactsSynchronizer != null) {
            IChatUser matchedContact = contactsSynchronizer.findById(contact.getId());

            if(matchedContact != null) {
                contact = matchedContact;
            }
        }
        // END contactsSynchronizer

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // fullname as title
        TextView mToolbarTitle = findViewById(R.id.toolbar_title);
        mToolbarTitle.setText(StringUtils.isValid(contact.getFullName()) ?
                contact.getFullName() : contact.getId());

        // connection status (online/offline) as subtitle
        mToolbarSubTitle = findViewById(R.id.toolbar_subtitle);
        mToolbarSubTitle.setText("");

        // set user email
        TextView mEmail = findViewById(R.id.email);
        mEmail.setText(contact.getEmail());

        // set user id
        TextView mUID = findViewById(R.id.userid);
        mUID.setText(contact.getId());

        // init user profile picture
        initProfilePicture();

        presenceHandler.upsertPresenceListener(this);
        presenceHandler.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "  PublicProfileActivity.onDestroy");

        presenceHandler.removePresenceListener(this);
        Log.d(DEBUG_USER_PRESENCE, "PublicProfileActivity.onDestroy: presenceHandler detached");
    }

    private void initProfilePicture() {
        Log.d(TAG, "initProfilePicture");

        ImageView profilePictureToolbar = (ImageView) findViewById(R.id.image);

        Glide.with(getApplicationContext())
                .load(contact.getProfilePictureUrl())
                .placeholder(R.drawable.ic_person_avatar)
//                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .into(profilePictureToolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void isUserOnline(boolean isConnected) {
        Log.d(DEBUG_USER_PRESENCE, "PublicProfileActivity.isUserOnline: " +
                "isConnected == " + isConnected);

        if (isConnected) {
            conversWithOnline = true;
            mToolbarSubTitle.setText(getString(R.string.activity_public_profile_presence_online));
        } else {
            conversWithOnline = false;

            if (conversWithLastOnline != PresenceHandler.LAST_ONLINE_UNDEFINED) {
                mToolbarSubTitle.setText(TimeUtils
                        .getFormattedTimestamp(this, conversWithLastOnline));
                Log.d(DEBUG_USER_PRESENCE, "PublicProfileActivity.isUserOnline: " +
                        "conversWithLastOnline == " + conversWithLastOnline);
            } else {
                mToolbarSubTitle.setText(getString(R.string.activity_public_profile_presence_offline));
            }
        }
    }

    @Override
    public void userLastOnline(long lastOnline) {
        Log.d(DEBUG_USER_PRESENCE, "PublicProfileActivity.userLastOnline: " +
                "lastOnline == " + lastOnline);

        conversWithLastOnline = lastOnline;

        if (!conversWithOnline) {
            mToolbarSubTitle.setText(TimeUtils.getFormattedTimestamp(this, lastOnline));
        }

        if (!conversWithOnline && lastOnline == PresenceHandler.LAST_ONLINE_UNDEFINED) {
            mToolbarSubTitle.setText(getString(R.string.activity_public_profile_presence_offline));
        }
    }

    @Override
    public void onPresenceError(Exception e) {
        Log.e(DEBUG_USER_PRESENCE, "PublicProfileActivity.onMyPresenceError: " + e.toString());

        mToolbarSubTitle.setText(getString(R.string.activity_public_profile_presence_offline));
    }
}