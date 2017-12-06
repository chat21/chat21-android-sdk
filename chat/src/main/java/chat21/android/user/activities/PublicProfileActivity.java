package chat21.android.user.activities;

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
import chat21.android.core.ChatManager;
import chat21.android.presence.OnPresenceChangesListener;
import chat21.android.presence.PresenceHandler;
import chat21.android.utils.StringUtils;
import chat21.android.utils.TimeUtils;
import chat21.android.utils.glide.CropCircleTransformation;

/**
 * Created by stefanodp91 on 04/08/17.
 * <p>
 * bugfix Issue #30
 */
public class PublicProfileActivity extends AppCompatActivity implements
        OnPresenceChangesListener {
    private static final String TAG = PublicProfileActivity.class.getName();

    private TextView mPresenceTextView;
    private boolean isOnline = false;
    private long lastOnline = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticvity_public_profile);

        initToolbar();

        mPresenceTextView = (TextView) findViewById(R.id.presence);

        // init user display name
        initDisplayName(getUserDisplayName());

        // init user profile picture
        initProfilePicture();

        // subscribe for convers with user presence changes
        // bugfix Issue #16
        PresenceHandler.observeUserPresenceChanges(this, getUserId(), this);
    }

    private void initToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private String getUserId() {
        Log.d(TAG, "getUserId");

        String userId = getIntent().getExtras().getString(ChatManager.INTENT_BUNDLE_CONTACT_ID);
        Log.d(TAG, "userId: " + userId);
        return userId;
    }

    private String getUserDisplayName() {
        Log.d(TAG, "getUserDisplayName");

        String displayName = "";
        if (getIntent().getExtras() != null) {
            if (StringUtils.isValid(getIntent().getExtras().getString(ChatManager.INTENT_BUNDLE_CONTACT_DISPLAY_NAME))) {
                displayName = StringUtils.isValid(getIntent().getExtras().getString(ChatManager.INTENT_BUNDLE_CONTACT_DISPLAY_NAME)) ?
                        getIntent().getExtras().getString(ChatManager.INTENT_BUNDLE_CONTACT_DISPLAY_NAME) :
                        getUserId();
            }
        }
        return displayName;
    }

    private void initDisplayName(String displayName) {
        Log.d(TAG, "initDisplayName");

        if (StringUtils.isValid(displayName)) {
            getSupportActionBar().setTitle(displayName);
        } else {
            getSupportActionBar().setTitle(getUserId());
        }
    }

    private void initProfilePicture() {
        Log.d(TAG, "initProfilePicture");

        ImageView profilePictureToolbar = (ImageView) findViewById(R.id.image);

        Glide.with(getApplicationContext())
                .load("")
                .placeholder(R.drawable.ic_person_avatar)
                .bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .into(profilePictureToolbar);
    }

    @Override
    public void onPresenceChange(boolean imConnected) {
        Log.d(TAG, "onPresenceChange - imConnected: " + imConnected);

        if (imConnected) {
            isOnline = true;
            mPresenceTextView.setText(getString(R.string.activity_public_profile_presence_online));
        } else {
            isOnline = false;

            if (lastOnline != 0) {
                mPresenceTextView.setText(TimeUtils.getFormattedTimestamp(lastOnline));
            } else {
                mPresenceTextView.setText("");
            }
        }
    }

    @Override
    public void onLastOnlineChange(long lastOnline) {
        Log.d(TAG, "onLastOnlineChange - lastOnline: " + lastOnline);

        this.lastOnline = lastOnline;

        if (!isOnline)
            mPresenceTextView.setText(TimeUtils.getFormattedTimestamp(lastOnline));
    }

    @Override
    public void onPresenceChangeError(Exception e) {
        Log.e(TAG, "onPresenceChangeError " + e.getMessage());

        mPresenceTextView.setText(R.string.activity_public_profile_presence_not_available);
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