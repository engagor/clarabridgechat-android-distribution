package com.clarabridge.features.conversationlist;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

/**
 * This builder is used to configure the {@link ConversationListActivityBuilder}
 */
public class ConversationListActivityBuilder {

    static final String INTENT_CREATE_CONVERSATION = "INTENT_CREATE_CONVERSATION";

    private int flags;
    private boolean createConversation = true;

    /**
     * Creates a new instance of {@link ConversationListActivityBuilder} for configuring the activity
     */
    ConversationListActivityBuilder() {
        // Intentionally empty
    }

    /**
     * Specifies the intent flags for the conversation list activity
     *
     * @param flags the {@link Intent} flags
     * @return the {@link ConversationListActivityBuilder}
     */
    public ConversationListActivityBuilder withFlags(int flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Specifies the create conversation boolean used for showing or hiding the create conversation button in
     * the Conversation List Screen.
     *
     * @param createConversation the create conversation boolean used for showing or hiding the create conversation
     *                           button
     * @return the {@link ConversationListActivityBuilder}
     */
    public ConversationListActivityBuilder showCreateConversationButton(boolean createConversation) {
        this.createConversation = createConversation;
        return this;
    }

    /**
     * Starts the {@link ConversationListActivity} with the specified configuration
     *
     * @param context the {@link Context} from which {@link Context#startActivity(Intent)} will be invoked
     */
    public void show(final Context context) {
        context.startActivity(intent(context));
    }

    /**
     * Creates an {@link Intent} for the {@link ConversationListActivity} with the specified configuration
     *
     * @param context the {@link Context} from which {@link Context#startActivity(Intent)} will be invoked
     * @return the constructed {@link Intent}
     */
    public Intent intent(@NonNull final Context context) {
        final Intent intent = new Intent(context, ConversationListActivity.class);

        intent.putExtra(INTENT_CREATE_CONVERSATION, createConversation);

        if (flags != 0) {
            intent.setFlags(flags);
        }

        return intent;
    }
}
