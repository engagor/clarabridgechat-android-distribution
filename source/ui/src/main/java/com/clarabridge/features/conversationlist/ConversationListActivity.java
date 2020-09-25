package com.clarabridge.features.conversationlist;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import com.clarabridge.core.Conversation;
import com.clarabridge.ui.R;

/**
 * Hold the {@link ConversationListFragment} that responsible for displaying a list of {@link Conversation}
 */
public class ConversationListActivity extends AppCompatActivity {

    private static final String CONVERSATION_LIST_FRAGMENT = "ConversationListFragment";

    /**
     * Returns a new builder for configuring and displaying {@link ConversationListActivity}.
     *
     * @return a new {@link ConversationListActivityBuilder}
     */
    public static ConversationListActivityBuilder builder() {
        return new ConversationListActivityBuilder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.clarabridgechat_activity_conversation_list);
        setup();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setup() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        showConversationListFragment();
    }

    private void showConversationListFragment() {
        final FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(CONVERSATION_LIST_FRAGMENT) == null) {
            final FragmentTransaction fragmentTx = manager.beginTransaction();


            boolean createConversation =
                    getIntent().getBooleanExtra(ConversationListActivityBuilder.INTENT_CREATE_CONVERSATION, false);
            final ConversationListFragment conversationListFragment =
                    ConversationListFragment.create(createConversation);

            fragmentTx.replace(
                    R.id.clarabridgechat_activity_conversation_list_fragment_container,
                    conversationListFragment,
                    CONVERSATION_LIST_FRAGMENT
            );
            fragmentTx.commit();
        }
    }

}
