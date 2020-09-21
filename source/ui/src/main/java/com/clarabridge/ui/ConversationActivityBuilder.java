package com.clarabridge.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.clarabridge.core.utils.StringUtils;

/**
 * This builder is used to configure the {@link ConversationActivity}
 */
public class ConversationActivityBuilder {

    static final String INTENT_STARTING_TEXT = "INTENT_STARTING_TEXT";

    private int flags;
    private String startingText;

    /**
     * Creates a new instance of {@link ConversationActivityBuilder} for configuring the activity
     */
    ConversationActivityBuilder() {
        // Intentionally empty
    }

    /**
     * Specifies the intent flags for the conversation activity
     *
     * @param flags the {@link Intent} flags
     * @return the {@link ConversationActivityBuilder}
     */
    public ConversationActivityBuilder withFlags(int flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Specifies the starting text used to pre-fill the conversation activity text box
     *
     * @param startingText the starting text to display
     * @return the {@link ConversationActivityBuilder}
     */
    public ConversationActivityBuilder withStartingText(String startingText) {
        this.startingText = startingText;
        return this;
    }

    /**
     * Starts the {@link ConversationActivity} with the specified configuration
     *
     * @param context the {@link Context} from which {@link Context#startActivity(Intent)} will be invoked
     */
    public void show(final Context context) {
        context.startActivity(intent(context));
    }

    /**
     * Creates an {@link Intent} for the {@link ConversationActivity} with the specified configuration
     *
     * @param context the {@link Context} from which {@link Context#startActivity(Intent)} will be invoked
     * @return the constructed {@link Intent}
     */
    public Intent intent(@NonNull final Context context) {
        final Intent intent = new Intent(context, ConversationActivity.class);

        if (!StringUtils.isEmpty(startingText)) {
            intent.putExtra(INTENT_STARTING_TEXT, startingText);
        }

        if (flags != 0) {
            intent.setFlags(flags);
        }

        return intent;
    }
}
