package chat21.android.ui.users.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import chat21.android.R;
import chat21.android.core.users.models.IChatUser;
import chat21.android.ui.ChatUI;
import chat21.android.utils.image.CropCircleTransformation;

/**
 * Created by stefanodp91 on 04/08/17.
 * <p>
 * bugfix Issue #30
 */
public class PublicProfileActivity extends AppCompatActivity {
    private static final String TAG = PublicProfileActivity.class.getName();

    private IChatUser contact;
//    private TextView mPresenceTextView;
//    private boolean isOnline = false;
//    private long mlastOnline = 0;

//    private OnPresenceListener onPresenceListener = new OnPresenceListener() {
//        @Override
//        public void onChanged(boolean imConnected) {
//            Log.d(DEBUG_USER_PRESENCE, "PublicProfileActivity.onPresenceListener.onChanged:" +
//                    " imConnected == " + imConnected);
//
//            if (imConnected) {
//                isOnline = true;
//
//                if (mPresenceTextView != null)
//                    mPresenceTextView.setText(getString(R.string.activity_public_profile_presence_online));
//            } else {
//                isOnline = false;
//
//                if (mlastOnline != 0) {
//                    if (mPresenceTextView != null)
//                        mPresenceTextView.setText(TimeUtils.getFormattedTimestamp(mlastOnline));
//                } else {
//                    if (mPresenceTextView != null)
//                        mPresenceTextView.setText("");
//                }
//            }
//        }
//
//        @Override
//        public void onLastOnlineChanged(long lastOnline) {
//            Log.d(DEBUG_USER_PRESENCE, "PublicProfileActivity.onPresenceListener.onLastOnlineChanged:" +
//                    " lastOnline == " + lastOnline);
//
//            mlastOnline = lastOnline;
//
//            if (!isOnline)
//                mPresenceTextView.setText(TimeUtils.getFormattedTimestamp(lastOnline));
//        }
//
//        @Override
//        public void onError(Exception e) {
//            Log.e(DEBUG_USER_PRESENCE, "PublicProfileActivity.onPresenceListener.onError: " + e.getMessage());
//
//            if (mPresenceTextView != null)
//                mPresenceTextView.setText(R.string.activity_public_profile_presence_not_available);
//        }
//    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticvity_public_profile);

        contact = (IChatUser) getIntent().getSerializableExtra(ChatUI.INTENT_BUNDLE_RECIPIENT);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // fullname as title
        TextView mToolbarTitle = findViewById(R.id.toolbar_title);
        mToolbarTitle.setText(contact.getFullName());

        // connection status (online/offline) as subtitle
        TextView mToolbarSubTitle = findViewById(R.id.toolbar_subtitle);
        mToolbarSubTitle.setText("connection status will be here");

        // set user email
        TextView mEmail = findViewById(R.id.email);
        mEmail.setText(contact.getEmail());

        // set user id
        TextView mUID = findViewById(R.id.userid);
        mUID.setText(contact.getId());

        // init user profile picture
        initProfilePicture();

//        // subscribe for convers with user presence changes
//        // bugfix Issue #16
//        mPresenceTextView = (TextView) findViewById(R.id.presence);
//        PresenceManger.observeUserPresenceChanges(ChatManager.getInstance().getAppId(), contact.getId(), onPresenceListener);
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
}