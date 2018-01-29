package chat21.android.ui.chat_groups.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.Map;

import chat21.android.R;
import chat21.android.core.ChatManager;
import chat21.android.core.chat_groups.listeners.ChatGroupCreatedListener;
import chat21.android.core.chat_groups.models.ChatGroup;
import chat21.android.core.chat_groups.syncronizers.GroupsSyncronizer;
import chat21.android.core.exception.ChatRuntimeException;
import chat21.android.ui.chat_groups.WizardNewGroup;
import chat21.android.utils.StringUtils;

import static chat21.android.utils.DebugConstants.DEBUG_GROUPS;

/**
 * Created by stefanodp91 on 26/01/18.
 */

public class NewGroupActivity extends AppCompatActivity {

    private GroupsSyncronizer groupsSyncronizer;

    private EditText groupNameView;
    private MenuItem actionNextMenuItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupNameView = findViewById(R.id.group_name);
        groupNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (actionNextMenuItem != null) {
                    if (isValidGroupName() && actionNextMenuItem != null) {
                        WizardNewGroup.getInstance().getTempChatGroup().setName(groupNameView.getText().toString());
                        actionNextMenuItem.setVisible(true);
                    } else {
                        actionNextMenuItem.setVisible(false);
                    }
                }
            }
        });
    }

    // check if the group name is valid
    // if yes show the "next" button, hide it otherwise
    private boolean isValidGroupName() {

        String groupName = groupNameView.getText().toString();
        return StringUtils.isValid(groupName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_new_group, menu);

        actionNextMenuItem = menu.findItem(R.id.action_next);
        return true;
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

    private void onActionNextClicked() {

        String chatGroupName = WizardNewGroup.getInstance().getTempChatGroup().getName();
        Map<String, Integer> chatGroupMembers = WizardNewGroup.getInstance().getTempChatGroup().getMembers();

        groupsSyncronizer.createChatGroup(chatGroupName, chatGroupMembers, new ChatGroupCreatedListener() {
            @Override
            public void onChatGroupCreated(ChatGroup chatGroup, ChatRuntimeException chatException) {
                Log.d(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked.onChatGroupCreated");

                if (chatException == null) {
                    Log.d(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                            ".onChatGroupCreated: chatGroup == " + chatGroup.toString());
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e(DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                            ".onChatGroupCreated: " + chatException.getLocalizedMessage());
                    // TODO: 29/01/18
                    setResult(RESULT_CANCELED);
                    return;
                }
            }
        });
    }
}
