package chat21.android.ui.groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.groups.models.ChatGroup;
import chat21.android.ui.ChatUI;
import chat21.android.utils.StringUtils;

/**
 * Created by stefanodp91 on 16/01/17.
 */
public class CreateGroupActivity extends AppCompatActivity {
    private static final String TAG = CreateGroupActivity.class.getName();

    private Toolbar mToolbar;
    private TextView mMessage;
    private RelativeLayout mGroupIconBox;
    private TextView mGroupIconLabel;
    private EditText mGroupName;
    private MenuItem mNextMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        registerViews();
        initViews();
        initListeners();
    }

    private void registerViews() {
        Log.d(TAG, "registerViews");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mMessage = (TextView) findViewById(R.id.message);
        mGroupIconBox = (RelativeLayout) findViewById(R.id.group_icon_box);
        mGroupIconLabel = (TextView) findViewById(R.id.group_icon_label);
        mGroupName = (EditText) findViewById(R.id.group_name);
    }

    private void initViews() {
        Log.d(TAG, "initViews");

        initToolbar();

        initMessage();

        initBoxGroupIcon();
    }

    private void initListeners() {
        Log.d(TAG, "initListeners");

        mGroupName.addTextChangedListener(onGroupNameTextChangeListener);
    }

    private void initToolbar() {
        Log.d(TAG, "initToolbar");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initMessage() {
//        if (getString(R.string.enable_group_icon).compareToIgnoreCase("false") == 0) {
//            // icona disabilitata
//            mMessage.setText(getString(R.string.create_group_activity_add_group_message_label));
//        } else {
//            // icona abilitata
        mMessage.setText(getString(R.string.create_group_activity_add_group_message_with_image_label));
//        }
    }

    private void initBoxGroupIcon() {
        Log.d(TAG, "initBoxGroupIcon");

        // TODO: 16/01/17
        // se la foto è visibile visualizzala e nascondi la label "aggiungi foto"
        // se la foto non è visible (o non è settata) allora visualizza la label "aggiungi foto"

//        mGroupIcon.setOnClickListener(onPhotoClickListener);
        mGroupIconLabel.setOnClickListener(onGroupIconClickListener);
    }

    // listener called when the group icon is clicked
    private View.OnClickListener onGroupIconClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onGroupIconClickListener.onClick");

            Toast.makeText(CreateGroupActivity.this, "coming soon",
                    Toast.LENGTH_SHORT).show();
        }
    };

    private TextWatcher onGroupNameTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            // show / hide the btn next
            if (mNextMenuItem != null) {
                if (isValidGroupName() && mNextMenuItem != null) {
                    mNextMenuItem.setVisible(true);
                } else {
                    mNextMenuItem.setVisible(false);
                }
            }
        }
    };

    // check if the group name is valid
    // if it is valid show the "next" button
    private boolean isValidGroupName() {
        Log.d(TAG, "isValidGroupName");

        String groupName = mGroupName.getText().toString();
        return StringUtils.isValid(groupName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.menu_activity_create_group, menu);

        mNextMenuItem = menu.findItem(R.id.action_next);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_next) {
            onNextOptionsItemClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // listener called the next button is clicked
    private void onNextOptionsItemClicked() {
        Log.d(TAG, "onNextOptionsItemClicked");

        ChatGroup chatGroup = createGroup();
        if (chatGroup != null) {
            Log.d(TAG, chatGroup.toString());
            startAddMembersActivity(chatGroup);
        } else {
            String errorMessage = "onNextOptionsItemClicked: chatGroup object is null";
            Log.e(TAG, errorMessage);
        }
    }

    private ChatGroup createGroup() {
        Log.d(TAG, "createGroup");

        ChatGroup chatGroup = null;
        if (isValidGroupName()) {
            String groupName = mGroupName.getText().toString();
            chatGroup = new ChatGroup(groupName, ChatManager.getInstance().getLoggedUser().getId());
            chatGroup.getCreatedOn();
        }
        return chatGroup;
    }

    private void startAddMembersActivity(ChatGroup chatGroup) {
        Log.d(TAG, "startAddMembersActivity");

//        try {
        // targetClass MUST NOT BE NULL
//            Class<?> targetClass = Class.forName(getString(R.string.target_add_members_activity_class));
//            Intent intent = new Intent(this, targetClass);
        Intent intent = new Intent(this, AddMembersActivity.class);

        intent.putExtra(ChatUI.BUNDLE_GROUP, chatGroup);
        intent.putExtra(ChatUI.BUNDLE_PARENT_ACTIVITY,
                CreateGroupActivity.class.getName());
        startActivityForResult(intent, ChatUI._REQUEST_CODE_CREATE_GROUP);
//        } catch (ClassNotFoundException e) {
//            String errorMessage = "cannot retrieve the add chatGroup activity target class. \n" + e.getMessage();
//            Log.e(TAG, errorMessage);
//            FirebaseCrash.report(new Exception(errorMessage));
//        }

//        Intent intent = new Intent(this, AddMembersActivity.class);
//        intent.putExtra(Chat.BUNDLE_GROUP, chatGroup);
//        startActivityForResult(intent, Chat.REQUEST_CREATE_GROUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatUI._REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}