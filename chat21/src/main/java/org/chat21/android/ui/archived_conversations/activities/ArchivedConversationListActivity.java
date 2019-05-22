package org.chat21.android.ui.archived_conversations.activities;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.chat21.android.R;
import org.chat21.android.ui.archived_conversations.fragments.ArchivedConversationListFragment;

/**
 * Created by stefano on 02/08/2018.
 */
public class ArchivedConversationListActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_archived_conversation_list);

        // #### BEGIN TOOLBAR ####
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // #### END  TOOLBAR ####

        // #### BEGIN CONTAINER ####
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ArchivedConversationListFragment())
                .commit();
        // #### BEGIN CONTAINER ####
    }
}