package com.clarabridge.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

import com.clarabridge.core.ActionState;
import com.clarabridge.core.CardSummary;
import com.clarabridge.core.Config;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.ConversationDelegate;
import com.clarabridge.core.ConversationEvent;
import com.clarabridge.core.ConversationViewDelegate;
import com.clarabridge.core.InitializationStatus;
import com.clarabridge.core.LoginResult;
import com.clarabridge.core.LogoutResult;
import com.clarabridge.core.Message;
import com.clarabridge.core.MessageAction;
import com.clarabridge.core.MessageUploadStatus;
import com.clarabridge.core.PaymentStatus;
import com.clarabridge.core.ClarabridgeChat;
import com.clarabridge.core.ClarabridgeChatCallback;
import com.clarabridge.core.ClarabridgeChatConnectionStatus;
import com.clarabridge.ui.fragment.ConversationFragment;
import com.clarabridge.ui.fragment.ShaderFragment;
import com.clarabridge.ui.fragment.ShaderFragment.ShaderFragmentListener;
import com.clarabridge.ui.fragment.StripeFragment;
import com.clarabridge.ui.fragment.StripeFragment.StripeFragmentListener;
import com.clarabridge.ui.fragment.WebviewFragment;

public class ConversationActivity extends AppCompatActivity implements ConversationDelegate,
        StripeFragmentListener, ShaderFragmentListener,
        WebviewFragment.WebviewFragmentListener {
    private static final String CONVERSATION_FRAGMENT = "ConversationFragment";
    private static final String STRIPE_FRAGMENT = "StripeFragment";
    private static final String SHADER_FRAGMENT = "ShaderFragment";
    private static final int CONVERSATION_ACTIVITY_DELEGATE = 10;

    private static ConversationActivity runningActivity;

    private FragmentManager manager = getSupportFragmentManager();
    private Handler handler = new Handler();

    private boolean stripeShouldBePopped;
    private boolean webviewShown;
    private StripeFragment stripeFragment;
    private ConversationFragment conversationFragment;
    private ShaderFragment shaderFragment;
    private Conversation conversation;

    /**
     * Returns a new builder for configuring and displaying {@link ConversationActivity}.
     *
     * @return a new {@link ConversationActivityBuilder}
     */
    public static ConversationActivityBuilder builder() {
        return new ConversationActivityBuilder();
    }

    /**
     * Closes the conversation view.
     * <p>
     * Ignored if the conversation is not running.
     */
    public static void close() {
        if (runningActivity != null) {
            runningActivity.finish();
            runningActivity = null;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runningActivity = this;

        conversationFragment = (ConversationFragment) manager.findFragmentByTag(CONVERSATION_FRAGMENT);
        stripeFragment = (StripeFragment) manager.findFragmentByTag(STRIPE_FRAGMENT);
        shaderFragment = (ShaderFragment) manager.findFragmentByTag(SHADER_FRAGMENT);

        final ConversationViewDelegate viewDelegate = ClarabridgeChat.getConversationViewDelegate();
        if (viewDelegate != null) {
            viewDelegate.onStartActivityCalled(getIntent());
        }
        setup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this == runningActivity) {
            runningActivity = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean handled = false;

        if (item.getItemId() == android.R.id.home) {
            finish();
            handled = true;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        WebviewFragment webviewFragment = (WebviewFragment) manager.findFragmentByTag(WebviewFragment.FRAGMENT_NAME);

        if (webviewFragment != null) {
            if (webviewFragment.goBack()) {
                return;
            }
        }

        super.onBackPressed();

        if (stripeFragmentShown()) {
            stripeFragment = null;
            onStripeFragmentPopped();
        }

        if (webviewShown) {
            onWebviewHidden();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ClarabridgeChat.addConversationUiDelegate(CONVERSATION_ACTIVITY_DELEGATE, this);

        if (stripeShouldBePopped) {
            popStripeAfterSeconds(3);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        ClarabridgeChat.addConversationUiDelegate(CONVERSATION_ACTIVITY_DELEGATE, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        showConversationFragment();
    }

    @Override
    public void onStop() {
        super.onStop();

        conversation = ClarabridgeChat.getConversation();

        if (conversation != null) {
            conversation.clarabridgeChatHidden();
        }
    }

    private void showConversationFragment() {
        if (conversationFragment == null) {
            final FragmentTransaction fragmentTx = manager.beginTransaction();

            conversationFragment = new ConversationFragment();

            final Intent intent = getIntent();
            final String startingText = intent.getStringExtra(ConversationActivityBuilder.INTENT_STARTING_TEXT);
            final String conversationId = intent.getStringExtra(ConversationActivityBuilder.INTENT_CONVERSATION_ID);

            if (startingText != null) {
                conversationFragment.setStartingText(startingText);
            }

            fragmentTx.add(R.id.clarabridgechat_activity_fragment_container, conversationFragment, CONVERSATION_FRAGMENT);

            if (conversationId != null) {
                ClarabridgeChat.loadConversation(conversationId, new ClarabridgeChatCallback<Conversation>() {
                    @Override
                    public void run(@NonNull Response<Conversation> response) {
                        Conversation loadedConversation = response.getData();
                        if (loadedConversation != null) {
                            conversation = loadedConversation;
                            fragmentTx.commit();
                            addShaderFragment();
                        }
                    }
                });
            } else {
                conversation = ClarabridgeChat.getConversation();
                if (conversation != null) {
                    conversation.clarabridgeChatShown();
                    fragmentTx.commit();
                    addShaderFragment();
                }
            }
        }
    }

    private void showStripeFragment(final MessageAction action) {
        if (stripeFragment == null) {
            final FragmentTransaction fragmentTx = manager.beginTransaction();
            final Bundle argumentBundle = new Bundle();

            stripeFragment = new StripeFragment();

            argumentBundle.putSerializable("action", action);

            stripeFragment.setArguments(argumentBundle);

            fragmentTx.add(R.id.clarabridgechat_activity_fragment_container, stripeFragment, STRIPE_FRAGMENT);
            fragmentTx.addToBackStack(null);
            fragmentTx.commit();
        }
    }

    private void addShaderFragment() {
        if (shaderFragment == null) {
            final FragmentTransaction fragmentTx = manager.beginTransaction();

            shaderFragment = new ShaderFragment();

            fragmentTx.add(R.id.clarabridgechat_activity_fragment_container, shaderFragment, SHADER_FRAGMENT);
            fragmentTx.commit();
        }
    }

    private void setup() {
        final ActionBar actionBar = getSupportActionBar();

        setContentView(R.layout.clarabridgechat_activity_conversation);

        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

    }

    private boolean stripeFragmentShown() {
        return manager.findFragmentByTag(STRIPE_FRAGMENT) != null;
    }

    private void popStripeAfterSeconds(int seconds) {
        runAfter(new Runnable() {
            @Override
            public void run() {
                popStripeFragment();
            }
        }, 1000 * seconds);
    }

    private void onStripeFragmentPopped() {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.show();
        }

        removeShade();
        addActionBarSpacingAndRestoreScrollPosition();

        runAfter(new Runnable() {
            @Override
            public void run() {
                hideKeyboard();
            }
        }, getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null && imm != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void runAfter(final Runnable runnable, final int milliseconds) {
        handler.postDelayed(runnable, milliseconds);
    }

    private void preserveScrollPositionAndRemoveActionBarSpacing() {
        if (conversationFragment != null && conversationFragment.getView() != null) {
            conversationFragment.preserveScroll();
            conversationFragment.getView().findViewById(R.id.ClarabridgeChat_actionBarSpace).setVisibility(View.GONE);
        }
    }

    private void addActionBarSpacingAndRestoreScrollPosition() {
        if (conversationFragment != null && conversationFragment.getView() != null) {
            final View actionBarSpace = conversationFragment.getView().findViewById(R.id.ClarabridgeChat_actionBarSpace);

            actionBarSpace.setVisibility(View.VISIBLE);
            conversationFragment.restoreScroll();
        }
    }

    private void addShade() {
        if (shaderFragment != null) {
            shaderFragment.show();
        }
    }

    private void removeShade() {
        if (shaderFragment != null) {
            shaderFragment.hide();
        }
    }

    private void popStripeFragment() {
        if (stripeFragment != null && stripeFragment.isResumed()) {
            stripeFragment = null;
            stripeShouldBePopped = false;
            manager.popBackStack();
            onStripeFragmentPopped();
        } else {
            stripeShouldBePopped = true;
        }
    }

    private void stripeComplete() {
        popStripeAfterSeconds(2);
    }

    @Override
    public void onMessagesReceived(@NonNull final Conversation conversation,
                                   @NonNull final List<Message> messages) {
        if (conversationFragment != null && conversationFragment.isResumed()) {
            conversationFragment.onMessagesReceived(conversation, messages);
        }
    }

    @Override
    public void onMessagesReset(@NonNull final Conversation conversation,
                                @NonNull final List<Message> messages) {
        if (conversationFragment != null && conversationFragment.isResumed()) {
            conversationFragment.onMessagesReset(conversation, messages);
        }
    }

    @Override
    public void onUnreadCountChanged(@NonNull final Conversation conversation, final int unreadCount) {
        // Do nothing
    }

    @Override
    public void onMessageSent(@NonNull final Message message, @NonNull final MessageUploadStatus status) {
        if (conversationFragment != null && conversationFragment.isResumed()) {
            conversationFragment.onMessageSent(message, status);
        }
    }

    @Override
    public void onConversationEventReceived(@NonNull ConversationEvent conversationEvent) {
        if (conversationFragment != null && conversationFragment.isResumed()) {
            conversationFragment.onConversationEventReceived(conversationEvent);
        }
    }

    @Override
    public void onInitializationStatusChanged(@NonNull final InitializationStatus status) {
        if (conversationFragment != null && conversationFragment.isResumed()) {
            conversationFragment.onInitializationStatusChanged(status);
        }
    }

    @Override
    public void onLoginComplete(@NonNull LoginResult result) {
        // Do nothing
    }

    @Override
    public void onLogoutComplete(@NonNull LogoutResult result) {
        // Do nothing
    }

    @Override
    public void onPaymentProcessed(@NonNull final MessageAction messageAction,
                                   @NonNull final PaymentStatus status) {
        if (stripeFragment != null) {
            stripeFragment.onPaymentProcessed(status);
        }
        if (conversationFragment != null) {
            conversationFragment.onPaymentProcessed();
        }
    }

    @Override
    public boolean shouldTriggerAction(@NonNull final MessageAction messageAction) {
        final Config clarabridgeChatConfig = ClarabridgeChat.getConfig();
        final String actionType = messageAction.getType();
        final String actionState = messageAction.getState();
        boolean stripeEnabled = false;

        if (clarabridgeChatConfig != null) {
            stripeEnabled = clarabridgeChatConfig.isStripeEnabled();
        }

        if (actionType != null && actionType.equals("buy")) {
            if (actionState == null || !actionState.equals(ActionState.PAID.getValue())) {
                if (!stripeEnabled) {
                    conversationFragment.triggerAction(messageAction);
                } else {
                    showStripeFragment(messageAction);
                }
            }
        } else if (conversationFragment != null) {
            conversationFragment.triggerAction(messageAction);
        }

        return true;
    }

    @Override
    public void onCardSummaryLoaded(@NonNull final CardSummary cardSummary) {
        if (stripeFragment != null) {
            stripeFragment.onCardSummaryLoaded(cardSummary);
        }
    }

    @Override
    public void onClarabridgeChatConnectionStatusChanged(@NonNull final ClarabridgeChatConnectionStatus status) {
        if (conversationFragment != null) {
            conversationFragment.onClarabridgeChatConnectionStatusChanged(status);
        }
    }

    @Override
    public void onClarabridgeChatShown() {
        // Intentionally empty
    }

    @Override
    public void onClarabridgeChatHidden() {
        // Intentionally empty
    }

    @Override
    public void onConversationsListUpdated(@NonNull List<Conversation> conversationsList) {
        // Intentionally empty
    }

    @Override
    public void onStripeFragmentShown() {
        final ActionBar actionBar = getSupportActionBar();

        hideKeyboard();
        preserveScrollPositionAndRemoveActionBarSpacing();
        addShade();

        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onStripeFragmentClose() {
        popStripeFragment();
    }

    @Override
    public void onPurchaseComplete() {
        stripeComplete();
    }

    @Override
    public void onShadedAreaClick() {
        popStripeFragment();
    }

    @Override
    public void onWebviewShown() {
        final ActionBar actionBar = getSupportActionBar();

        webviewShown = true;
        preserveScrollPositionAndRemoveActionBarSpacing();

        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onWebviewHidden() {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.show();
        }

        webviewShown = false;
        addActionBarSpacingAndRestoreScrollPosition();
    }
}

