package org.chat21.android.ui.conversations.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import chat21.android.R;
import org.chat21.android.ui.conversations.fragments.ConversationListFragment;

/**
 * Created by stefano on 15/10/2016.
 */
public class ConversationListActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        registerViews();
        initViews();
    }

    private void registerViews() {
        mToolbar = findViewById(R.id.toolbar);
    }

    private void initViews() {
        initToolbar();
        initContainer();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initContainer() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ConversationListFragment())
                .commit();
    }
}