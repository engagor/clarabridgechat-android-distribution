package com.clarabridge.core;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.clarabridge.core.utils.JavaUtils;
import com.clarabridge.core.utils.ProcessName;
import com.clarabridge.core.utils.StringUtils;

/**
 * ClarabridgeChat Entry Point
 */
public final class ClarabridgeChat {

    private static final String TAG = "ClarabridgeChat";
    private static final int ERROR_STATUS = 400;

    static final Object INITIALIZATION_LOCK = new Object();
    static ClarabridgeChatInternal clarabridgeChatInternal;

    private static SortedMap<Integer, ConversationDelegate> conversationDelegates = new TreeMap<>();
    private static ConversationViewDelegate conversationViewDelegate;
    private static MessageModifierDelegate messageModifierDelegate;

    private ClarabridgeChat() {
    }

    /**
     * Initializes ClarabridgeChat without settings. Use this only if you intend to call
     * {@link #init(Application, Settings, ClarabridgeChatCallback)} outside of an {@link Application} class
     * <p>
     * This may only be called from {@link Application#onCreate()}.
     *
     * @param application The {@link Application}
     */
    public static void init(@NonNull final Application application) {
        initInternal(application, new Settings(null), new StubbedCallback<InitializationStatus>());
    }

    /**
     * Initialize the ClarabridgeChat SDK with the provided settings.
     * <p>
     * This may only be called from {@link Application#onCreate()}, unless {@link #init(Application)} was called there.
     * <p>
     * Use {@link #getSettings() ClarabridgeChat.getSettings()} to retrieve and modify the given settings object.
     *
     * @param application The {@link Application}
     * @param settings    The {@link Settings} to use
     * @param callback    An optional {@link ClarabridgeChatCallback} instance to be notified of the result.
     *                    Call {@link ClarabridgeChatCallback.Response#getData()} to check the {@link InitializationStatus}
     * @see Settings
     * @see ClarabridgeChatCallback
     * @see ClarabridgeChatCallback.Response
     * @see InitializationStatus
     */
    public static void init(@NonNull final Application application,
                            @NonNull final Settings settings,
                            @Nullable ClarabridgeChatCallback<InitializationStatus> callback) {
        callback = ensureNonNull(callback);
        if (StringUtils.isEmpty(settings.getIntegrationId())) {
            ClarabridgeChatCallback.Response<InitializationStatus> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<InitializationStatus>(ERROR_STATUS)
                            .withError("Provided settings did not have an integration id, aborting init sequence!")
                            .build();
            callback.run(callbackResponse);
            return;
        }

        initInternal(application, settings, callback);
    }

    private static void initInternal(@NonNull Application application,
                                     @NonNull Settings settings,
                                     @NonNull ClarabridgeChatCallback<InitializationStatus> callback) {
        synchronized (INITIALIZATION_LOCK) {
            String currentProcessName = null;

            try {
                currentProcessName = ProcessName.getCurrentProcessName();
            } catch (IOException e) {
                Log.e(TAG, "Error getting current process name");
            }

            if (StringUtils.isEqual(currentProcessName, application.getApplicationInfo().processName)) {
                int runningActivities = 0;

                if (clarabridgeChatInternal != null) {
                    runningActivities = clarabridgeChatInternal.getRunningActivities();
                }

                destroy(false);

                clarabridgeChatInternal = new ClarabridgeChatInternal(application, settings, runningActivities);
                clarabridgeChatInternal.init(callback);
            }
        }
    }

    /**
     * Logs in a new ClarabridgeChat user.
     * <p>
     * You can either use this method to transition from logged out state to logged in, refresh the
     * token of the existing logged in user or to switch the currently logged in user to a different one.
     * You may not call login while the conversation screen is shown unless it is being called for
     * the user already logged in. Doing so will result in a no-op.
     * {@link #init(Application, Settings, ClarabridgeChatCallback)} must have been called prior to calling login.
     *
     * @param externalId The distinct id of the user to login. Must not be null.
     * @param jwt        jwt used to prove the origin of the login request. Must not be null.
     * @param callback   An optional {@link ClarabridgeChatCallback} instance to be notified of the result.
     *                   Call {@link ClarabridgeChatCallback.Response#getData()} to check the {@link LoginResult}
     * @see ClarabridgeChatCallback
     * @see ClarabridgeChatCallback.Response
     * @see LoginResult
     */
    public static void login(@NonNull final String externalId,
                             @NonNull final String jwt,
                             @Nullable ClarabridgeChatCallback<LoginResult> callback) {

        if (!isInitialized()) {
            return;
        }

        callback = ensureNonNull(callback);

        String errorMessage = null;
        if (StringUtils.isEmpty(externalId)) {
            errorMessage = "Login called with null or empty externalId. Call logout instead!";

        } else if (StringUtils.isEmpty(jwt)) {
            errorMessage = "Login called with null or empty jwt. Ignoring!";

        } else if (getConversation() != null
                && getConversation().isClarabridgeChatShown()
                && !externalId.equals(clarabridgeChatInternal.getExternalId())) {
            errorMessage = "Login called while on the conversation screen. Ignoring!";
        }

        if (errorMessage != null) {
            ClarabridgeChatCallback.Response<LoginResult> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<LoginResult>(ERROR_STATUS)
                            .withError(errorMessage)
                            .build();
            callback.run(callbackResponse);
            return;
        }

        clarabridgeChatInternal.login(externalId, jwt, callback);
    }

    /**
     * Logs out the current user.
     * <p>
     * Calling this method while in anonymous state has no effect. You may not call logout while the
     * conversation screen is shown. Doing so will result in a no-op.
     *
     * @param callback An optional {@link ClarabridgeChatCallback} instance to be notified of the result.
     *                 Call {@link ClarabridgeChatCallback.Response#getData()} to check the {@link LogoutResult}
     * @see ClarabridgeChatCallback
     * @see ClarabridgeChatCallback.Response
     * @see LogoutResult
     */
    public static void logout(@Nullable ClarabridgeChatCallback<LogoutResult> callback) {
        if (!isInitialized()) {
            return;
        }

        callback = ensureNonNull(callback);
        if (getConversation() != null && getConversation().isClarabridgeChatShown()) {
            Log.e(TAG, "Logout called while on the conversation screen. Ignoring!");
        } else {
            clarabridgeChatInternal.logout(callback);
        }
    }

    /**
     * @see #createConversation(String, String, String, List, Map, ClarabridgeChatCallback)
     * <p>
     * This method has been Deprecated. Use
     * {@link #createConversation(String, String, String, List, Map, ClarabridgeChatCallback)}
     * instead
     */
    @Deprecated
    public static void startConversation(@Nullable ClarabridgeChatCallback<Void> callback) {
        createConversation(null, null, null, Collections.<Message>emptyList(), null, callback);
    }

    /**
     * Create a conversation for the current user.
     * <p>
     * Creates a user and conversation on the server, allowing the business to reach out proactively
     * to the user via the public API.
     * <p>
     * Creating a conversation via this method will count as an active user conversation (AUC) whether
     * messages are exchanged or not, which may incur cost based on your plan. It is strongly
     * recommended to only call this method in the case where a message is likely to be sent.
     * <p>
     * A List containing one {@link Message} can be passed in as well to add messages when the conversation is
     * created.
     * Only messages of type text are supported. Any other message type will cause an error.
     * <p>
     * This method is called automatically when starting a conversation via the
     * {@link Conversation#sendMessage(Message)} or {@link Conversation#uploadImage(Message, ClarabridgeChatCallback)}
     * methods, or when a user sends a message via the conversation activity.
     * <p>
     *
     * @param name        A user-friendly name to label the conversation in the list (max length 100).
     * @param description A string describing the purpose of the conversation (max length 100).
     * @param messages    A {@link List} of text {@link Message} to be sent when the conversation is created.
     *                    Only messages of type text are supported. Any other message type will cause an error.
     *                    The List can only contain one message.
     * @param iconUrl     An iconUrl to display in the conversation list. It should be a valid icon URL.
     * @param callback    An optional {@link ClarabridgeChatCallback} instance to be notified of the result.
     * @param metadata    The conversation metadata (JSON Object)
     * @see ClarabridgeChatCallback
     * @see ClarabridgeChatCallback.Response
     */
    public static void createConversation(@Nullable String name,
                                          @Nullable String description,
                                          @Nullable String iconUrl,
                                          @Nullable List<Message> messages,
                                          @Nullable Map<String, Object> metadata,
                                          @Nullable ClarabridgeChatCallback<Void> callback) {
        if (!isInitialized()) {
            return;
        }
        callback = ensureNonNull(callback);
        if (messages == null) {
            messages = Collections.emptyList();
        }
        clarabridgeChatInternal.createConversation("conversation:start", name, description, iconUrl, messages, metadata,
                callback);
    }

    /**
     * Loads a conversation by its ID and sets it as the active conversation for the current session.
     * <p>
     * When called, subscribes the current device for push notifications on the passed conversationId,
     * and sets the SDK to send and receive messages for that conversation going forward.
     * Does not unsubscribe for notification on previously loaded conversations.
     * <p>
     * The provided callback contain the conversation that was loaded if successful.
     * <p>
     * If the conversation is already set to the passed id, this call will just return the currently
     * loaded conversation.
     *
     * @param conversationId The conversationId to set.
     * @param callback       An optional {@link ClarabridgeChatCallback} instance to be notified of the result.
     * @see ClarabridgeChatCallback
     * @see ClarabridgeChatCallback.Response
     */
    public static void loadConversation(@NonNull String conversationId,
                                        @Nullable ClarabridgeChatCallback<Conversation> callback) {
        if (!isInitialized()) {
            return;
        }

        callback = ensureNonNull(callback);
        if (StringUtils.isEmpty(conversationId)) {
            ClarabridgeChatCallback.Response<Conversation> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<Conversation>(ERROR_STATUS)
                            .withError("Load conversation called with null or empty conversationId. Ignoring!")
                            .build();
            callback.run(callbackResponse);
        } else {
            clarabridgeChatInternal.loadConversation(conversationId, callback);
        }
    }

    /**
     * @param conversationId the conversationId
     * @param name           A user-friendly name to label the conversation (max length 100 characters)
     * @param description    A string describing the purpose of the conversation (max length 100 characters)
     * @param iconUrl        An iconUrl to display in the conversation list
     * @param metadata       The conversation metadata (JSON Object)
     * @param callback       An optional {@link ClarabridgeChatCallback} instance to be notified of the result.
     * @abstract Updates the specified conversation for the current user.
     * <p>
     * +initWithSettings:completionHandler: must have been called prior to calling this method.
     * <p>
     * If the name, description, iconUrl, and metadata parameters are all nil, this is a no-op.
     * @see ClarabridgeChatCallback
     * @see ClarabridgeChatCallback.Response
     */
    public static void updateConversationById(@NonNull String conversationId,
                                              @Nullable String name,
                                              @Nullable String description,
                                              @Nullable String iconUrl,
                                              @Nullable Map<String, Object> metadata,
                                              @Nullable ClarabridgeChatCallback<Conversation> callback) {
        if (!isInitialized()) {
            return;
        }

        callback = ensureNonNull(callback);
        if (StringUtils.isEmpty(conversationId)) {
            ClarabridgeChatCallback.Response<Conversation> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<Conversation>(ERROR_STATUS)
                            .withError("Update conversation called with null or empty conversationId. Ignoring!")
                            .build();
            callback.run(callbackResponse);
        } else if (JavaUtils.allNull(name, description, iconUrl, metadata)) {
            ClarabridgeChatCallback.Response<Conversation> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<Conversation>(ERROR_STATUS)
                            .withError("Update conversation called with null values for name, description, iconUrl " +
                                    "and metadata. Ignoring!")
                            .build();
            callback.run(callbackResponse);
        } else {
            clarabridgeChatInternal.updateConversationById(conversationId, name, description, iconUrl, metadata, callback);
        }
    }

    /**
     * Terminate the ClarabridgeChat facilities.
     * <p>
     * ClarabridgeChat is automatically terminated when the application exits.
     * This method does not, usually, have to be called manually.
     */
    public static void destroy() {
        destroy(true);
    }

    private static void destroy(boolean unbindService) {
        synchronized (INITIALIZATION_LOCK) {
            if (!isInitialized()) {
                return;
            }

            clarabridgeChatInternal.destroy(unbindService);
            clarabridgeChatInternal = null;
        }
    }

    /**
     * Accessor method for the SDK settings.
     * <p>
     * Use this object to update settings at run time.
     * Note: Some settings may only be configured at init time. See the {@link Settings} class
     * reference for more information.
     *
     * @return Settings object passed in {@link #init(Application, Settings, ClarabridgeChatCallback)
     * init(Application, Settings, ClarabridgeChatCallback)}, or null if ClarabridgeChat hasn't been initialized yet.
     * @see Settings
     */
    @Nullable
    public static Settings getSettings() {
        return isInitialized() ? clarabridgeChatInternal.getSettings() : null;
    }

    /**
     * Accessor method for the current conversation.
     *
     * @return Current conversation, or null if ClarabridgeChat.init() hasn't been called yet.
     * @see Conversation
     */
    @Nullable
    public static Conversation getConversation() {
        return isInitialized() ? clarabridgeChatInternal.getConversation() : null;
    }

    /**
     * Get a {@link Conversation} by its ID. This is an asynchronous call and requires a
     * {@link ClarabridgeChatCallback} to retrieve the result.
     *
     * @param conversationId the ID of the conversation to be retrieved
     * @param callback       An instance of {@link ClarabridgeChatCallback} to be notified of the result.
     * @see Conversation
     */
    public static void getConversationById(@NonNull String conversationId,
                                           @Nullable ClarabridgeChatCallback<Conversation> callback) {
        if (!isInitialized()) {
            return;
        }

        callback = ensureNonNull(callback);

        if (StringUtils.isEmpty(conversationId)) {
            ClarabridgeChatCallback.Response<Conversation> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<Conversation>(ERROR_STATUS)
                            .withError("Get Conversation by ID called with null or empty conversation ID. Ignoring!")
                            .build();
            callback.run(callbackResponse);
        } else {
            clarabridgeChatInternal.getConversationById(conversationId, callback);
        }
    }

    /**
     * Get the list of the 10 most recently active {@link Conversation}s for the current user, sorted
     * from most recently updated to last.
     * <p>
     * Note that these conversations will only contain the most recent message for that conversation.
     * Use {@link #getConversationById(String, ClarabridgeChatCallback)} to retrieve the full details of a conversation,
     * or {@link #loadConversation(String, ClarabridgeChatCallback)} to make it active.
     *
     * @param callback An instance of {@link ClarabridgeChatCallback} to be notified of the result.
     * @see #getConversationById(String, ClarabridgeChatCallback)
     * @see #loadConversation(String, ClarabridgeChatCallback)
     * @see #getConversation()
     */
    public static void getConversationsList(@Nullable ClarabridgeChatCallback<List<Conversation>> callback) {
        if (!isInitialized()) {
            return;
        }

        callback = ensureNonNull(callback);

        clarabridgeChatInternal.getConversationsList(callback);
    }

    /**
     * Get the list of the 10 more recently active {@link Conversation}s for the current user, sorted
     * from most recently updated to last.
     * <p>
     * Note that these conversations will only contain the most recent message for that conversation.
     * Use {@link #getConversationById(String, ClarabridgeChatCallback)} to retrieve the full details of a conversation,
     * or {@link #loadConversation(String, ClarabridgeChatCallback)} to make it active.
     *
     * @param callback An instance of {@link ClarabridgeChatCallback} to be notified of the result.
     * @see #getConversationById(String, ClarabridgeChatCallback)
     * @see #loadConversation(String, ClarabridgeChatCallback)
     * @see #getConversation()
     */
    public static void getMoreConversationsList(@Nullable ClarabridgeChatCallback<List<Conversation>> callback) {
        if (!isInitialized()) {
            return;
        }

        callback = ensureNonNull(callback);

        clarabridgeChatInternal.getMoreConversationsList(callback);
    }

    /**
     * Accessor method for knowing if there are more conversations to be fetched or not.
     *
     * @return true if there is more conversations to be fetched or false if not.
     */
    public static boolean hasMoreConversations() {
        return isInitialized() && clarabridgeChatInternal.hasMoreConversations();
    }

    /**
     * Accessor method for the currently connected app configuration.
     *
     * @return Current app configuration, or null if ClarabridgeChat.init() hasn't been called yet.
     * @see Config
     */
    @Nullable
    public static Config getConfig() {
        return isInitialized() ? clarabridgeChatInternal.getConfig() : null;
    }

    /**
     * Accessor method for the current Initialization status.
     *
     * @return Initialization status, or null if {@link ClarabridgeChat#init(Application, Settings, ClarabridgeChatCallback)}
     * hasn't been called yet.
     * @see InitializationStatus
     */
    @Nullable
    public static InitializationStatus getInitializationStatus() {
        return isInitialized() ? clarabridgeChatInternal.getInitializationStatus() : null;
    }

    /**
     * Accessor method for the current {@link ClarabridgeChatConnectionStatus}, or null if
     * {@link ClarabridgeChat#init(Application, Settings, ClarabridgeChatCallback)} hasn't been called yet.
     *
     * @see ClarabridgeChatConnectionStatus
     */
    @Nullable
    public static ClarabridgeChatConnectionStatus getClarabridgeChatConnectionStatus() {
        return isInitialized() ? clarabridgeChatInternal.getClarabridgeChatConnectionStatus() : null;
    }

    /**
     * Accessor for last known {@link LoginResult} or <code>null</code> if the user is anonymous
     * or {@link ClarabridgeChat#init(Application, Settings, ClarabridgeChatCallback)} hasn't been called
     *
     * @see LoginResult
     */
    @Nullable
    public static LoginResult getLastLoginResult() {
        return isInitialized() ? clarabridgeChatInternal.getLastLoginResult() : null;
    }

    /**
     * Accessor method to get the Firebase Cloud Messaging project id that was
     * linked to the application via the Web portal.
     *
     * @return The Firebase Cloud Messaging project Id
     */
    @Nullable
    public static String getFirebaseCloudMessagingProjectId() {
        return isInitialized() ? clarabridgeChatInternal.getFirebaseCloudMessagingProjectId() : null;
    }

    /**
     * Method used to register the Firebase Cloud Messaging token to ClarabridgeChat
     *
     * @param token The Firebase Cloud Messaging token
     */
    public static void setFirebaseCloudMessagingToken(@Nullable final String token) {
        setFirebaseCloudMessagingToken(token, null);
    }

    /**
     * Method used to register the Firebase Cloud Messaging token to ClarabridgeChat
     *
     * @param token    The Firebase Cloud Messaging token
     * @param callback The callback to be called when token is received by ClarabridgeChat
     */
    public static void setFirebaseCloudMessagingToken(@Nullable final String token,
                                                      @Nullable ClarabridgeChatCallback<LoginResult> callback) {
        if (!isInitialized()) {
            return;
        }
        callback = ensureNonNull(callback);
        clarabridgeChatInternal.setFirebaseCloudMessagingToken(token, callback);
    }

    @Nullable
    static ClarabridgeChatInternal getInstance() {
        return isInitialized() ? clarabridgeChatInternal : null;
    }

    /**
     * Return whether or not the ClarabridgeChat SDK has been initialized.
     *
     * @return true if the ClarabridgeChat SDK has been initialized, false otherwise
     */
    @VisibleForTesting
    static boolean isInitialized() {
        if (clarabridgeChatInternal == null) {
            Log.e(TAG, "ClarabridgeChat has been called without being initialized first!");
            return false;
        }
        return true;
    }

    /**
     * Sets the {@link ConversationDelegate}
     * <p>
     * This delegate will be notified before the delegate set using
     * {@link #addConversationUiDelegate(int, ConversationDelegate)}
     * <p>
     * Any delegate functions with the form boolean return type will prevent notification of conversationUiDelegate
     * <p>
     * See {@link #addConversationUiDelegate(int, ConversationDelegate)}
     *
     * @param conversationDelegate an instance of {@link ConversationDelegate}
     */
    public static void setConversationDelegate(@Nullable ConversationDelegate conversationDelegate) {
        if (conversationDelegate != null) {
            ClarabridgeChat.conversationDelegates.put(ConversationDelegate.INTEGRATOR_DELEGATE, conversationDelegate);
        } else {
            ClarabridgeChat.conversationDelegates.remove(ConversationDelegate.INTEGRATOR_DELEGATE);
        }
    }

    /**
     * Gets the delegate set via {@link #setConversationDelegate(ConversationDelegate)}.
     *
     * @return an instance of {@link ConversationDelegate}
     */
    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static ConversationDelegate getConversationDelegate() {
        return ClarabridgeChat.conversationDelegates.get(ConversationDelegate.INTEGRATOR_DELEGATE);
    }

    /**
     * Add a {@link ConversationDelegate} used by the clarabridgeChat-ui package
     * <p>
     * The primary use of this property is to notify the ClarabridgeChat UI of changes in the Conversation
     * <p>
     * Note that this delegate will be notified after the delegate set using
     * {@link #setConversationDelegate(ConversationDelegate)}
     * <p>
     * See {@link #setConversationDelegate(ConversationDelegate)}
     *
     * @param key                    the key to use, must be >= 1
     * @param conversationUiDelegate an instance of {@link ConversationDelegate}
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static void addConversationUiDelegate(int key, @Nullable ConversationDelegate conversationUiDelegate) {
        if (key < 1) {
            return;
        }
        if (conversationUiDelegate != null) {
            ClarabridgeChat.conversationDelegates.put(key, conversationUiDelegate);
        } else {
            ClarabridgeChat.conversationDelegates.remove(key);
        }
    }

    /**
     * Gets a delegate set via {@link #addConversationUiDelegate(int key, ConversationDelegate)} using the key.
     *
     * @param key the key to use
     * @return an instance of {@link ConversationDelegate}, null if not delegate for key
     */
    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static ConversationDelegate getConversationUiDelegate(int key) {
        return ClarabridgeChat.conversationDelegates.get(key);
    }

    /**
     * Gets the collection of {@link ConversationDelegate} that have been set on the SDK as a {@link SortedMap}
     * where the integrator defined delegate will appear before the delegate used to notify the ClarabridgeChat UI
     *
     * @return a {@link SortedMap} of {@link ConversationDelegate}
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static Collection<ConversationDelegate> getConversationDelegates() {
        return ClarabridgeChat.conversationDelegates.values();
    }

    /**
     * Sets the {@link ConversationViewDelegate}
     * <p>
     * The delegate allows you to be notified of events related to the conversation view
     *
     * @param conversationViewDelegate an instance of {@link ConversationViewDelegate}
     */
    public static void setConversationViewDelegate(@Nullable ConversationViewDelegate conversationViewDelegate) {
        ClarabridgeChat.conversationViewDelegate = conversationViewDelegate;
    }

    /**
     * Gets the delegate set via {@link #setConversationViewDelegate(ConversationViewDelegate)}.
     *
     * @return an instance of {@link ConversationViewDelegate}
     */
    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static ConversationViewDelegate getConversationViewDelegate() {
        return ClarabridgeChat.conversationViewDelegate;
    }

    /**
     * Sets the {@link MessageModifierDelegate}
     * <p>
     * The delegate allows you to change the way messages are displayed or sent to ClarabridgeChat.
     *
     * @param messageModifierDelegate The messageModifierDelegate
     */
    public static void setMessageModifierDelegate(@Nullable MessageModifierDelegate messageModifierDelegate) {
        ClarabridgeChat.messageModifierDelegate = messageModifierDelegate;
    }

    /**
     * Gets the delegate set via {@link ClarabridgeChat#setMessageModifierDelegate(MessageModifierDelegate)}.
     *
     * @return an instance of {@link MessageModifierDelegate}
     */
    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static MessageModifierDelegate getMessageModifierDelegate() {
        return ClarabridgeChat.messageModifierDelegate;
    }

    /**
     * Ensure the that we have a non-null callback
     *
     * @param callback the provided callback
     * @param <T>      the type of the callback
     * @return the callback if it wasn't null, or an instance of {@link StubbedCallback}
     */
    @NonNull
    static <T> ClarabridgeChatCallback<T> ensureNonNull(@Nullable ClarabridgeChatCallback<T> callback) {
        return callback != null
                ? callback
                : new StubbedCallback<T>();
    }

}

