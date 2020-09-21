package com.clarabridge.core.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.clarabridge.core.AuthenticationCallback;
import com.clarabridge.core.BuildConfig;
import com.clarabridge.core.ConversationEventType;
import com.clarabridge.core.CreditCard;
import com.clarabridge.core.InitializationStatus;
import com.clarabridge.core.Logger;
import com.clarabridge.core.LoginResult;
import com.clarabridge.core.LogoutResult;
import com.clarabridge.core.Message;
import com.clarabridge.core.Notifier;
import com.clarabridge.core.PaymentStatus;
import com.clarabridge.core.Settings;
import com.clarabridge.core.ClarabridgeChat;
import com.clarabridge.core.ClarabridgeChatCallback;
import com.clarabridge.core.ClarabridgeChatConnectionStatus;
import com.clarabridge.core.StubbedCallback;
import com.clarabridge.core.di.ClarabridgeChatComponent;
import com.clarabridge.core.facade.PersistenceFacade;
import com.clarabridge.core.model.AppUserDto;
import com.clarabridge.core.model.CardSummaryDto;
import com.clarabridge.core.model.ConfigDto;
import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.model.ConversationEventDto;
import com.clarabridge.core.model.ConversationResponseDto;
import com.clarabridge.core.model.ConversationsListResponseDto;
import com.clarabridge.core.model.FileUploadDto;
import com.clarabridge.core.model.GetConfigDto;
import com.clarabridge.core.model.IntegrationDto;
import com.clarabridge.core.model.MessageActionDto;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.model.PostMessageDto;
import com.clarabridge.core.model.RetryConfigurationDto;
import com.clarabridge.core.model.SdkUserDto;
import com.clarabridge.core.model.StripeCustomerDto;
import com.clarabridge.core.model.StripeTokenDto;
import com.clarabridge.core.model.UpgradeAppUserDto;
import com.clarabridge.core.model.UpgradeDto;
import com.clarabridge.core.model.UserSettingsDto;
import com.clarabridge.core.monitor.ConversationEventRole;
import com.clarabridge.core.monitor.ConversationMonitor;
import com.clarabridge.core.monitor.ConversationMonitorBuilder;
import com.clarabridge.core.network.ClarabridgeChatApiClient;
import com.clarabridge.core.network.ClarabridgeChatApiClientCallback;
import com.clarabridge.core.utils.StringUtils;

public class ClarabridgeChatService extends Service implements ConversationMonitor.Delegate {
    private static final String TAG = "ClarabridgeChatService";
    private static final long MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024;
    private static final long TYPING_EVENT_TIMEOUT_MILLIS = 10000L;
    private static final long STOP_TYPING_BUFFER_MILLIS = 1000L;

    //region Callbacks
    private final ClarabridgeChatCallback<LoginResult> loginOnInitCallback = new ClarabridgeChatCallback<LoginResult>() {
        @Override
        public void run(@NonNull Response<LoginResult> response) {
            if (response.getError() == null) {
                onInitializationStatusChanged(InitializationStatus.SUCCESS);
            } else {
                onInitFailure(
                        response.getStatus(),
                        loginOnInitTask,
                        "Error logging in. Make sure userId and JWT are valid"
                );
            }
        }
    };

    @NonNull
    private ClarabridgeChatCallback<InitializationStatus> initCallback = new StubbedCallback<>();
    //endregion

    //region Runnables
    @VisibleForTesting
    final Runnable initTask = new Runnable() {
        @Override
        public void run() {
            init();
        }
    };

    private final Runnable loginOnInitTask = new Runnable() {
        @Override
        public void run() {
            login(appUserRemote.getUserId(), persistenceFacade.getJwt(), loginOnInitCallback, false);
        }
    };

    private final Runnable fetchAppUserTask = new Runnable() {
        @Override
        public void run() {
            fetchUser();
        }
    };

    private final Runnable upgradeTask = new Runnable() {
        @Override
        public void run() {
            upgradeUser();
        }
    };

    private final Runnable consumeAuthCodeTask = new Runnable() {
        @Override
        public void run() {
            consumeAuthCode();
        }
    };

    private final Runnable appUserSyncTask = new Runnable() {
        @Override
        public void run() {
            syncAppUser(null);
        }
    };

    private final Runnable startConversationMonitorTask = new Runnable() {
        @Override
        public void run() {
            synchronized (conversationMonitorLock) {
                buildAndStartConversationMonitor();
            }
        }
    };

    private final Runnable stopTypingTask = new Runnable() {
        @Override
        public void run() {
            stopTyping();
        }
    };
    //endregion

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateConnectionStatus();
        }
    };

    private final LinkedList<Runnable> onClarabridgeChatInitSuccessList = new LinkedList<>();
    private final LinkedList<Runnable> onClarabridgeChatInitCompleteList = new LinkedList<>();
    private final LinkedList<Runnable> onClarabridgeChatReadyList = new LinkedList<>();
    private final LinkedList<MessageDto> messageQueue = new LinkedList<>();
    private final Map<String, MessageDto> pendingUploads = new HashMap<>();
    private final Map<String, ProcessingFileUpload> processingUploads = new HashMap<>();
    private final Map<String, Object> messageIdLocks = new HashMap<>();
    private final Map<String, ClarabridgeChatCallback.Response<Message>> rejectedUploads = new HashMap<>();
    private final Object conversationMonitorLock = new Object();

    private boolean initSyncScheduled = false;
    private boolean processingMessageQueue = false;
    private boolean running = false;
    private boolean retryInitOnConnect = false;
    private boolean fetchCustomerGuard = false;
    private long initSyncLastUploaded = 0;
    private int retryCount = 0;

    //region Observers
    private ServiceObserver serviceObserver;
    private ConversationObserver conversationObserver;
    private ClarabridgeChatObserver clarabridgeChatObserver = new StubbedClarabridgeChatObserver();
    //endregion

    private ServiceSettings serviceSettings;
    private ClarabridgeChatApiClient clarabridgeChatApiClient;
    private PersistenceFacade persistenceFacade;
    private ConversationManager conversationManager;
    private Settings settings;
    private ConfigDto config;
    private AppUserDto appUserLocal;
    private AppUserDto appUserRemote;
    private UserSettingsDto userSettings;
    private RetryConfigurationDto retryConfiguration;
    private String sessionToken;
    private CardSummaryDto cardSummary;
    private ConversationDto conversation = new ConversationDto();
    @Nullable
    private Handler handler;
    private ConversationMonitor conversationMonitor;
    private InitializationStatus initializationStatus = InitializationStatus.UNKNOWN;
    private ConnectionStatus connectionStatus = ConnectionStatus.UNKNOWN;
    private LoginResult lastLoginResult;
    private Long lastStartTypingEvent;
    private Long lastUploadedStartTypingEvent;

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return new ClarabridgeChatServiceBinder(this);
    }

    @Override
    public void onDestroy() {
        removeCallbacks(initTask);
        removeCallbacks(appUserSyncTask);
        removeCallbacks(startConversationMonitorTask);
        removeCallbacks(stopTypingTask);
        handler = null;

        disconnectConversationMonitor();

        unregisterReceiver(connectivityReceiver);
        super.onDestroy();
    }

    private void removeCallbacks(Runnable runnable) {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @VisibleForTesting
    boolean shouldStartConversationMonitor() {
        boolean userExists = getAppUserId() != null;

        boolean isRealtimeEnabled = userSettings != null
                && userSettings.getRealtime() != null
                && userSettings.getRealtime().isEnabled();

        return userExists && isRealtimeEnabled;
    }

    @VisibleForTesting
    void buildAndStartConversationMonitor() {
        synchronized (conversationMonitorLock) {
            boolean shouldBuildNewMonitor = conversationMonitor == null
                    || !conversationMonitor.getAppId().equals(persistenceFacade.getAppId());

            if (shouldBuildNewMonitor) {
                if (conversationMonitor != null) {
                    conversationMonitor.close();
                }

                conversationMonitor = new ConversationMonitorBuilder()
                        .setAppId(persistenceFacade.getAppId())
                        .setAppUserId(getAppUserId())
                        .setClientId(serviceSettings.getClientId())
                        .setDelegate(this)
                        .setJwt(persistenceFacade.getJwt())
                        .setSessionToken(sessionToken)
                        .setHost(userSettings.getRealtime().getBaseUrl())
                        .setMaxConnectionAttempts(userSettings.getRealtime().getMaxConnectionAttempts())
                        .setRetryInterval(userSettings.getRealtime().getRetryInterval() * 1000)
                        .build();
            }

            if (isRunning() && conversationMonitor != null) {
                conversationMonitor.resume();
            }

            userSettings.getRealtime().setEnabled(true);
        }
    }

    private void disconnectConversationMonitor() {
        if (conversationMonitor != null) {
            conversationMonitor.close();
        }

        conversationMonitor = null;
    }

    @Override
    public void onMessageReceived(String conversationId, final MessageDto message) {
        final boolean isFromCurrentUser = StringUtils.isEqual(getAppUserId(), message.getAuthorId());
        final boolean isCurrentConversation = StringUtils.isEqual(getConversationId(), conversationId);
        final boolean isConversationVisible = isCurrentConversation
                && ClarabridgeChat.getConversation() != null
                && ClarabridgeChat.getConversation().isClarabridgeChatShown();
        final boolean isConversationUpToDate = conversationManager.isSavedConversationUpToDate(conversationId);

        message.setIsFromCurrentUser(isFromCurrentUser);

        conversationManager.addMessageToConversationList(
                conversationId,
                message,
                getAppUserId(),
                isConversationVisible);

        clarabridgeChatObserver.onConversationsListUpdated(persistenceFacade.getConversationsList());

        if (!isCurrentConversation) {
            if (isConversationUpToDate) {
                conversationManager.addMessageToConversation(
                        conversationId,
                        message,
                        getAppUserId(),
                        false);
            }

            if (!isFromCurrentUser) {
                triggerNotification(conversationId, message);
            }

            return;
        }

        if (!messageExists(message)) {
            conversationManager.addMessageToConversation(
                    conversationId,
                    message,
                    getAppUserId(),
                    isConversationVisible);

            if (!isFromCurrentUser) {
                triggerNotification(conversationId, message);
            }

            onConversationUpdated(persistenceFacade.getConversationById(conversationId));
        }
    }

    /***
     * Checks if the given {@link MessageDto} already exists in the current {@link #conversation}.
     * If the message is not from the current user then {@link List#contains(Object)} is sufficient,
     * otherwise {@link MessageDto#getText()} will be used to compare. The later can occur if a faye
     * event is received before the network response when posting a new message.
     *
     * @param message the {@link MessageDto} to look for
     * @return true if {@link #conversation} contains the message, false otherwise
     */
    private boolean messageExists(MessageDto message) {
        boolean messageExists;

        synchronized (conversation.getMessages()) {
            final List<MessageDto> messages = conversation.getMessages();

            messageExists = messages.contains(message);

            if (!messageExists && message.isFromCurrentUser()) {
                for (int i = messages.size() - 1; i >= 0 && !messageExists; i--) {
                    final MessageDto m = messages.get(i);
                    if (m.getId() == null) {
                        messageExists = StringUtils.isEqual(m.getText(), message.getText());
                    }
                }
            }
        }

        return messageExists;
    }

    @Override
    public void onMessageRejected(String messageId, int status, String errorCode) {
        synchronized (getMessageIdLock(messageId)) {
            ProcessingFileUpload processingUpload = processingUploads.remove(messageId);

            ClarabridgeChatCallback.Response.Builder<Message> responseBuilder =
                    new ClarabridgeChatCallback.Response.Builder<Message>(status)
                            .withError(errorCode);

            if (processingUpload != null) {
                responseBuilder.withData(processingUpload.getMessage());
                onFileUploadComplete(responseBuilder.build(), null, processingUpload.getCallback());
            } else {
                rejectedUploads.put(messageId, responseBuilder.build());
            }
        }
    }

    /**
     * When uploading images or files to the server the SDK will wait until the Faye message is
     * received to consider the upload finished.
     * <p>
     * If the REST API responds first, the message is added to {@link #processingUploads} and waits
     * for the Faye event, then this method will finish processing the upload.
     * <p>
     * If the Faye message arrives first, the message is pre-processed by this method and added to
     * {@link #pendingUploads} so that it can be handled by the REST API response callback.
     * <p>
     * When the Faye message is received, we update the stored conversation with it so that the new
     * {@link MessageDto#getMediaUrl()} is saved local storage.
     *
     * @param message the {@link MessageDto} with the file that was uploaded
     */
    @Override
    public void onUploadComplete(final MessageDto message) {
        message.setIsFromCurrentUser(StringUtils.isEqual(message.getAuthorId(), getAppUserId()));
        synchronized (conversation.getMessages()) {
            for (MessageDto entity : conversation.getMessages()) {
                if (entity.equals(message)) {
                    entity.update(message);
                    break;
                }
            }
            persistenceFacade.saveConversationById(conversation.getId(), conversation);
        }

        synchronized (getMessageIdLock(message.getId())) {
            ProcessingFileUpload processingUpload = processingUploads.remove(message.getId());

            if (processingUpload != null) {
                ClarabridgeChatCallback.Response<Message> callbackResponse =
                        new ClarabridgeChatCallback.Response.Builder<Message>(200)
                                .withData(processingUpload.getMessage())
                                .build();
                onFileUploadComplete(callbackResponse, message, processingUpload.getCallback());
            } else {
                pendingUploads.put(message.getId(), message);
            }
        }
    }

    @Override
    public void onConversationActivityReceived(final ConversationEventDto event) {
        //noinspection ConstantConditions
        if (event == null || event.getType() == null) {
            return;
        }

        switch (event.getType()) {
            case CONVERSATION_READ:
                updateLastRead(event);
                break;
            case CONVERSATION_REMOVED:
                refreshConversationList();
                persistenceFacade.saveConversationById(event.getConversationId(), null);
                // That's everything for now. If that conversation was currently loaded or
                // displayed in the UI it will remain there until another conversation is loaded
                // and any calls will result in an error
                break;
            case CONVERSATION_ADDED:
            case PARTICIPANT_ADDED:
            case PARTICIPANT_REMOVED:
                refreshConversationList();
                if (StringUtils.isEqual(event.getConversationId(), getConversationId())) {
                    refreshConversation(null);
                }

                break;
            case TYPING_START:
            case TYPING_STOP:
            default:
                break;
        }

        schedule(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatObserver.onConversationEventReceived(event);
            }
        });
    }

    /**
     * Update the timestamps of stored conversations using the {@link ConversationManager} to
     * match the timestamps of the received {@link ConversationEventDto}.
     *
     * @param event the received {@link ConversationEventDto} of type
     *              {@link ConversationEventType#CONVERSATION_READ}
     */
    @VisibleForTesting
    void updateLastRead(@NonNull ConversationEventDto event) {
        if (event.getType() != ConversationEventType.CONVERSATION_READ) {
            return;
        }

        ConversationEventRole role = ConversationEventRole.findByValue(event.getRole());
        if (role == null) {
            return;
        }

        switch (role) {
            case APP_MAKER:
                conversationManager.setAppMakerLastRead(
                        event.getConversationId(),
                        event.getLastRead());
                break;
            case APP_USER:
                conversationManager.setParticipantLastRead(
                        event.getConversationId(),
                        event.getLastRead(),
                        event.getAppUserId());
                break;
        }

        clarabridgeChatObserver.onConversationsListUpdated(persistenceFacade.getConversationsList());

        boolean isCurrentConvo = StringUtils.isNotNullAndEqual(getConversationId(), event.getConversationId());
        ConversationDto updatedConversation = persistenceFacade.getConversationById(event.getConversationId());

        if (isCurrentConvo && updatedConversation != null) {
            conversation.update(updatedConversation);
        }
    }

    @Override
    public void onMonitorConnected() {
        onClarabridgeChatConnectionStatusChanged(ClarabridgeChatConnectionStatus.CONNECTED);
    }

    @Override
    public void onMonitorDisconnected() {
        onClarabridgeChatConnectionStatusChanged(ClarabridgeChatConnectionStatus.DISCONNECTED);
    }

    /**
     * Initialize the members of this service making it ready to be used. This should be the first
     * method to be called in this service, as soon as the connection that was used to bind it receives
     * the {@link android.content.ServiceConnection#onServiceConnected(ComponentName, IBinder)} callback.
     *
     * @param settings        the {@link Settings} used to initialise the SDK
     * @param clarabridgeChatComponent an instance of {@link ClarabridgeChatComponent} that can be used to retrieve dependencies
     */
    public void initializeService(
            final Settings settings,
            final ClarabridgeChatComponent clarabridgeChatComponent) {

        this.settings = settings;

        if (StringUtils.isEmpty(settings.getIntegrationId())) {
            return;
        }

        handler = clarabridgeChatComponent.handler();
        persistenceFacade = clarabridgeChatComponent.persistenceFacade();
        persistenceFacade.setIntegrationId(settings.getIntegrationId());

        conversationManager = clarabridgeChatComponent.conversationManager();

        serviceSettings = clarabridgeChatComponent.serviceSettings();

        if (settings.getServiceBaseUrl() != null) {
            serviceSettings.setBaseUrl(settings.getServiceBaseUrl());
        } else if (settings.getRegion() != null) {
            serviceSettings.setRegion(settings.getRegion());
        }

        retryConfiguration = persistenceFacade.getRetryConfiguration();

        clarabridgeChatApiClient = clarabridgeChatComponent.clarabridgeChatApiClient();
        clarabridgeChatApiClient.setAuthenticationCallback(new AuthenticationCallback() {
            @Override
            public void updateToken(@NonNull String jwt) {
                if (!StringUtils.isEqual(jwt, persistenceFacade.getJwt())) {
                    Log.i(TAG, "Updated JWT");
                    persistenceFacade.saveJwt(jwt);
                }
            }
        });
    }

    public boolean isRunning() {
        return running;
    }

    public InitializationStatus getInitializationStatus() {
        return initializationStatus;
    }

    public void start() {
        running = true;
    }

    public void pause() {
        running = false;

        if (conversationMonitor != null) {
            conversationMonitor.pause();
        }
    }

    public void resume() {
        running = true;

        if (serviceObserver != null) {
            serviceObserver.onServiceResumed();
        }

        if (conversationMonitor != null) {
            conversationMonitor.resume();
        }
    }

    public void setServiceObserver(final ServiceObserver serviceObserver) {
        this.serviceObserver = serviceObserver;
    }

    public void setConversationObserver(final ConversationObserver conversationObserver) {
        this.conversationObserver = conversationObserver;
    }

    public void setClarabridgeChatObserver(final ClarabridgeChatObserver clarabridgeChatObserver) {
        this.clarabridgeChatObserver = clarabridgeChatObserver != null ? clarabridgeChatObserver : new StubbedClarabridgeChatObserver();
    }

    // delay calls to be run only when ClarabridgeChat has been initialized successfully
    private synchronized void onClarabridgeChatInitSuccess(final Runnable clarabridgeChatApiCall) {
        if (getInitializationStatus().equals(InitializationStatus.SUCCESS)) {
            clarabridgeChatApiCall.run();
        } else {
            onClarabridgeChatInitSuccessList.push(clarabridgeChatApiCall);
        }
    }

    // delay calls to be run only when ClarabridgeChat has been initialized (failed or not)
    public synchronized void onClarabridgeChatInitComplete(final Runnable clarabridgeChatApiCall) {
        if (!getInitializationStatus().equals(InitializationStatus.UNKNOWN)) {
            clarabridgeChatApiCall.run();
        } else {
            onClarabridgeChatInitCompleteList.push(clarabridgeChatApiCall);
        }
    }

    /**
     * Method to enqueue network calls that will be delayed until the {@link #initializationStatus}
     * is {@link InitializationStatus#SUCCESS}.
     *
     * @param clarabridgeChatApiCall a {@link Runnable} with the network call to be delayed
     */
    private synchronized void onClarabridgeChatReady(final Runnable clarabridgeChatApiCall) {
        if (getInitializationStatus().equals(InitializationStatus.SUCCESS)) {
            if (conversationMonitor != null && conversationMonitor.isConnected()) {
                clarabridgeChatApiCall.run();
                return;
            }

            boolean userExists = getAppUserId() != null;

            boolean isRealtimeEnabled = userSettings != null
                    && userSettings.getRealtime() != null
                    && userSettings.getRealtime().isEnabled();

            if (userExists && !isRealtimeEnabled) {
                buildAndStartConversationMonitor();
            }
        }

        onClarabridgeChatReadyList.push(clarabridgeChatApiCall);
    }

    public void onFcmMessageReceived() {
        if (getInitializationStatus().equals(InitializationStatus.SUCCESS)) {
            if (!isConversationStarted()) {
                refreshConversation(null);
            } else if (userSettings != null && userSettings.getRealtime() != null) {
                userSettings.getRealtime().setEnabled(true);

                if (shouldStartConversationMonitor()) {
                    buildAndStartConversationMonitor();
                }
            }
        }
    }

    @VisibleForTesting
    void registerForPushNotifications() {
        if (settings.isFirebaseCloudMessagingAutoRegistrationEnabled()) {
            final String fcmProjectId = getFirebaseCloudMessagingProjectId();

            if (fcmProjectId != null) {
                getFirebaseMessagingToken(new FirebaseTokenCallback() {
                    @Override
                    public void onTokenReceived(String token) {
                        setFirebaseCloudMessagingToken(token, new StubbedCallback<LoginResult>());
                    }
                });
            }
        }
    }

    public void init() {
        removeCallbacks(initTask);

        if (StringUtils.isEmpty(settings.getIntegrationId())) {
            return;
        }

        clarabridgeChatApiClient.getConfig(new ClarabridgeChatApiClientCallback<GetConfigDto>() {
            @Override
            public void onResult(boolean isSuccessful, int statusCode, @Nullable GetConfigDto responseBody) {
                if (isSuccessful && responseBody != null) {
                    retryCount = 0;

                    final ConfigDto config = responseBody.getConfig();

                    onInitialization(config);

                    if (isAppValid()) {
                        String appId = config.getApp().getId();

                        persistenceFacade.setAppId(appId);

                        appUserLocal = persistenceFacade.getAppUserLocal();
                        appUserRemote = persistenceFacade.getAppUserRemote();
                        sessionToken = persistenceFacade.getSessionToken();
                        userSettings = persistenceFacade.getUserSettings();

                        lookForStripeIntegration(config);

                        clarabridgeChatApiClient.setAppId(appId);
                        clarabridgeChatApiClient.setBaseUrl(config.getBaseUrl().getAndroid());

                        scheduleAppUserSync();

                        registerForPushNotifications();

                        if (shouldConsumeAuthCode()) {
                            consumeAuthCode();
                        } else if (shouldUpgradeUser()) {
                            upgradeUser();
                        } else {
                            completeInitialization();
                        }
                    } else {
                        onInitFailure(401, null, null);
                    }
                } else {
                    onInitFailure(statusCode, initTask, null);
                }
            }
        });
    }

    private void onInitFailure(int responseStatus, Runnable taskToRetry, String authErrorMessageOverride) {
        if (retryConfiguration == null) {
            retryConfiguration = new RetryConfigurationDto();
        }

        final boolean shouldRetry = retryCount < retryConfiguration.getMaxRetries()
                && (responseStatus == 429 || (responseStatus >= 500 && responseStatus < 600));

        if (responseStatus == 401) {
            String authErrorMessage = StringUtils.isEmpty(authErrorMessageOverride)
                    ? "Permissions for this app may have been revoked." : authErrorMessageOverride;
            notifyFailure(responseStatus, authErrorMessage, InitializationStatus.INVALID_ID);
        } else if (responseStatus == 404) {
            String authErrorMessage = StringUtils.isEmpty(authErrorMessageOverride)
                    ? "ClarabridgeChat integration id was invalid." : authErrorMessageOverride;
            notifyFailure(responseStatus, authErrorMessage, InitializationStatus.INVALID_ID);
        } else if (connectionStatus != ConnectionStatus.CONNECTED) {
            // If the user is offline, delay next init until reconnected (and reset initializationStatus)
            retryInitOnConnect = true;
            initializationStatus = InitializationStatus.UNKNOWN;
        } else if (shouldRetry) {
            boolean shouldUseAggressiveInterval = ClarabridgeChat.getConversation() != null
                    && ClarabridgeChat.getConversation().isClarabridgeChatShown();
            int baseDelayInterval = shouldUseAggressiveInterval
                    ? retryConfiguration.getAggressiveInterval()
                    : retryConfiguration.getRegularInterval();
            int delayInterval = (int) (baseDelayInterval * Math.pow(
                    retryConfiguration.getBackoffMultiplier(),
                    retryCount
            ));

            int jitteredDelayInterval = ((2 * delayInterval) / 3) + new Random().nextInt(delayInterval / 3);
            String retryErrorMessage = "An unexpected error occurred during initialization. Retrying in "
                    + jitteredDelayInterval + " seconds...";
            notifyFailure(responseStatus, retryErrorMessage, InitializationStatus.ERROR);
            schedule(taskToRetry, jitteredDelayInterval * 1000);

            retryCount++;
        } else {
            notifyFailure(
                    responseStatus,
                    "An unexpected error occurred during initialization.",
                    InitializationStatus.ERROR
            );
        }
    }

    private void notifyFailure(final int responseStatus,
                               final String message,
                               final InitializationStatus failedStatus) {
        onInitializationStatusChanged(failedStatus);
        Log.e(TAG, message);
        schedule(new Runnable() {
            @Override
            public void run() {
                ClarabridgeChatCallback.Response<InitializationStatus> callbackResponse =
                        new ClarabridgeChatCallback.Response.Builder<InitializationStatus>(responseStatus)
                                .withError(message)
                                .withData(failedStatus)
                                .build();
                initCallback.run(callbackResponse);
            }
        });
    }

    private boolean isAppValid() {
        if (config == null || config.getApp() == null || config.getBaseUrl() == null) {
            return false;
        }

        boolean hasAppId = !StringUtils.isEmpty(config.getApp().getId());
        boolean isAppActive = StringUtils.isEqual(config.getApp().getStatus(), "active");
        boolean isValidUrl;

        try {
            URL url = new URL(config.getBaseUrl().getAndroid());
            isValidUrl = BuildConfig.DEBUG || !"localhost".equals(url.getHost());
        } catch (MalformedURLException e) {
            isValidUrl = false;
        }

        return hasAppId && isAppActive && isValidUrl;
    }

    private boolean shouldConsumeAuthCode() {
        return !StringUtils.isEmpty(settings.getAuthCode());
    }

    private void consumeAuthCode() {
        clarabridgeChatApiClient.consumeAuthCode(
                settings.getAuthCode(),
                new ClarabridgeChatApiClientCallback<UpgradeDto>() {
                    @Override
                    public void onResult(boolean isSuccessful, int statusCode, @Nullable UpgradeDto upgradeResponse) {
                        if (isSuccessful && upgradeResponse != null) {
                            appUserRemote = null;
                            persistenceFacade.saveJwt(null);
                            conversation.update(new ConversationDto());
                            upgradeUser(upgradeResponse.getAppUser());
                            onUpgradeComplete();
                        } else if (isSuccessful) {
                            // This shouldn't be reached
                            notifyFailure(statusCode, "Invalid user upgrade response", InitializationStatus.ERROR);
                        } else if (isRetryableStatusCode(statusCode)
                                && retryCount < retryConfiguration.getMaxRetries()) {
                            onInitFailure(statusCode, consumeAuthCodeTask, null);
                        } else {
                            retryCount = 0;
                            notifyFailure(statusCode, "Invalid auth code", InitializationStatus.ERROR);
                        }
                    }
                });
    }

    /**
     * Determine if a network request can be retried
     *
     * @param statusCode the http status of the response
     * @return true if it can be retried, false otherwise
     */
    private boolean isRetryableStatusCode(int statusCode) {
        return statusCode >= 500 || statusCode == 429 || statusCode == 408;
    }

    private boolean shouldUpgradeUser() {
        return !StringUtils.isEmpty(serviceSettings.getLegacyDeviceId());
    }

    private void upgradeUser() {
        clarabridgeChatApiClient.upgradeAppUser(
                serviceSettings.getLegacyDeviceId(),
                new ClarabridgeChatApiClientCallback<UpgradeDto>() {

                    @Override
                    public void onResult(boolean isSuccessful, int statusCode, @Nullable UpgradeDto responseBody) {
                        if (isSuccessful) {
                            if (responseBody == null || responseBody.getAppUser() == null) {
                                onUpgradeComplete();
                            } else {
                                upgradeUser(responseBody.getAppUser());
                                onUpgradeComplete();
                            }
                        } else {
                            if (isRetryableStatusCode(statusCode) && retryCount < retryConfiguration.getMaxRetries()) {
                                onInitFailure(statusCode, upgradeTask, null);
                            } else {
                                retryCount = 0;
                                onUpgradeComplete();
                            }
                        }
                    }
                });
    }

    private void upgradeUser(UpgradeAppUserDto upgradedAppUser) {
        if (appUserRemote == null) {
            appUserRemote = new AppUserDto();
        }

        appUserRemote.setAppUserId(upgradedAppUser.getAppUserId());
        sessionToken = upgradedAppUser.getSessionToken();

        sync();
    }

    private void onUpgradeComplete() {
        if (!StringUtils.isEmpty(serviceSettings.getLegacyDeviceId())) {
            serviceSettings.setClientId(serviceSettings.getLegacyDeviceId());
            serviceSettings.clearLegacyDeviceId();
        }
        completeInitialization();
    }

    private void completeInitialization() {
        if (isAuthenticatedUser()) {
            login(appUserRemote.getUserId(), persistenceFacade.getJwt(), loginOnInitCallback, false);
        } else if (isAnonymousUser()) {
            fetchUser();
        } else {
            onInitializationStatusChanged(InitializationStatus.SUCCESS);
        }
    }

    private boolean isAuthenticatedUser() {
        return appUserRemote != null && appUserRemote.getUserId() != null && persistenceFacade.getJwt() != null;
    }

    public void login(@NonNull final String userId,
                      @NonNull final String jwt,
                      @NonNull final ClarabridgeChatCallback<LoginResult> callback,
                      final boolean shouldNotifyDelegate) {

        forceAppUserSync(new Runnable() {
            @Override
            public void run() {
                if (appUserRemote == null) {
                    appUserRemote = new AppUserDto();
                }

                appUserRemote.setUserId(userId);

                disconnectConversationMonitor();

                if (!StringUtils.isEqual(jwt, persistenceFacade.getJwt())) {
                    persistenceFacade.saveJwt(jwt);
                }

                clarabridgeChatApiClient.login(
                        appUserRemote.getAppUserId(),
                        appUserRemote.getUserId(),
                        sessionToken,
                        new ClarabridgeChatApiClientCallback<SdkUserDto>() {
                            @Override
                            public void onResult(boolean isSuccessful, int statusCode,
                                                 @Nullable SdkUserDto responseBody) {

                                ClarabridgeChatCallback.Response.Builder<LoginResult> responseBuilder =
                                        new ClarabridgeChatCallback.Response.Builder<>(statusCode);

                                if (isSuccessful) {
                                    lastLoginResult = LoginResult.SUCCESS;
                                    disconnectConversationMonitor();
                                    retryCount = 0;
                                    sessionToken = null;
                                    sync();

                                    if (responseBody != null) {
                                        AppUserDto appUser = responseBody.getAppUser();

                                        // Credentials are valid and user exists
                                        if (appUser != null && !StringUtils.isEmpty(appUser.getAppUserId())) {
                                            // if the user has changed, clear messages
                                            if (!StringUtils.isEqual(appUser.getAppUserId(), getAppUserId())) {
                                                conversation.setMessages(new ArrayList<MessageDto>());
                                            }

                                            onUserUpdate(responseBody);
                                        } else {
                                            persistenceFacade.saveAppUserId(null);
                                        }
                                    } else {
                                        appUserRemote.setAppUserId(null);
                                        appUserRemote.setHasPaymentInfo(false);
                                        conversation = new ConversationDto();
                                        updateAndNotifyConversation(conversation);
                                        sync();
                                        persistenceFacade.saveAppUserId(null);
                                    }

                                    if (shouldNotifyDelegate) {
                                        // Explicit call to login, notify delegate
                                        clarabridgeChatObserver.onLoginComplete(LoginResult.SUCCESS);
                                    }

                                    responseBuilder.withData(LoginResult.SUCCESS);

                                    if (initializationStatus != InitializationStatus.SUCCESS
                                            && config != null && isAppValid()) {
                                        // Initialization is marked as failed because
                                        // login failed. Notify it as successful
                                        onInitializationStatusChanged(InitializationStatus.SUCCESS);
                                    }
                                } else {
                                    lastLoginResult = LoginResult.ERROR;
                                    conversation = new ConversationDto();
                                    updateAndNotifyConversation(conversation);

                                    if (shouldNotifyDelegate) {
                                        // Explicit call to login, notify delegate
                                        clarabridgeChatObserver.onLoginComplete(LoginResult.ERROR);
                                    }

                                    responseBuilder.withError("Error logging in")
                                            .withData(LoginResult.ERROR);
                                }

                                callback.run(responseBuilder.build());
                            }
                        });
            }
        });
    }

    public void logout(final ClarabridgeChatCallback<LogoutResult> callback) {
        removeCallbacks(startConversationMonitorTask);
        disconnectConversationMonitor();

        if (appUserExists()) {
            forceAppUserSync(new Runnable() {
                @Override
                public void run() {
                    clarabridgeChatApiClient.logout(appUserRemote.getAppUserId(),
                            new ClarabridgeChatApiClientCallback<Void>() {
                                @Override
                                public void onResult(boolean isSuccessful, int statusCode,
                                                     @Nullable Void responseBody) {
                                    onLogoutComplete(isSuccessful, statusCode, callback);
                                }
                            });
                }
            });
        } else {
            // User was never created, skip request and return successful response
            onLogoutComplete(true, 200, callback);
        }
    }

    private boolean appUserExists() {
        return !StringUtils.isEmpty(getAppUserId());
    }

    private void onLogoutComplete(boolean isSuccessful,
                                  int statusCode,
                                  @NonNull ClarabridgeChatCallback<LogoutResult> callback) {
        ClarabridgeChatCallback.Response.Builder<LogoutResult> responseBuilder =
                new ClarabridgeChatCallback.Response.Builder<>(statusCode);

        if (isSuccessful) {
            lastLoginResult = null;
            disconnectConversationMonitor();

            appUserLocal = new AppUserDto();
            appUserRemote = new AppUserDto();
            conversation = new ConversationDto();
            persistenceFacade.saveJwt(null);
            persistenceFacade.saveAppUserId(null);
            sync();

            clarabridgeChatObserver.onLogoutComplete(LogoutResult.SUCCESS);
            responseBuilder.withData(LogoutResult.SUCCESS);
        } else {
            clarabridgeChatObserver.onLogoutComplete(LogoutResult.ERROR);
            responseBuilder.withError("Error logging out")
                    .withData(LogoutResult.ERROR);
        }

        callback.run(responseBuilder.build());
    }

    private boolean isAnonymousUser() {
        return getAppUserId() != null && sessionToken != null && !isAuthenticatedUser();
    }

    private void fetchUser() {
        forceAppUserSync(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatApiClient.getAppUser(
                        appUserRemote.getAppUserId(),
                        new ClarabridgeChatApiClientCallback<SdkUserDto>() {
                            @Override
                            public void onResult(boolean isSuccessful, int statusCode,
                                                 @Nullable SdkUserDto responseBody) {
                                if (isSuccessful && responseBody != null) {
                                    retryCount = 0;
                                    onUserUpdate(responseBody);
                                    onInitializationStatusChanged(InitializationStatus.SUCCESS);
                                } else if (statusCode == 401) {
                                    // Session token invalid. Clear credentials and allow usage as new user
                                    sessionToken = null;
                                    appUserLocal = new AppUserDto();
                                    appUserRemote = new AppUserDto();
                                    conversation = new ConversationDto();
                                    persistenceFacade.saveAppUserId(null);
                                    sync();
                                    onInitializationStatusChanged(InitializationStatus.SUCCESS);
                                } else {
                                    onInitFailure(statusCode, fetchAppUserTask, null);
                                }
                            }
                        });
            }
        });
    }

    public void createConversation(@NonNull final String intent,
                                   @NonNull final ClarabridgeChatCallback<Void> callback) {
        onClarabridgeChatInitSuccess(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatApiClient.createConversation(
                        intent,
                        appUserRemote.getAppUserId(),
                        new ClarabridgeChatApiClientCallback<ConversationResponseDto>() {
                            @Override
                            public void onResult(boolean isSuccessful, int statusCode,
                                                 @Nullable ConversationResponseDto responseBody) {

                                ClarabridgeChatCallback.Response.Builder<Void> responseBuilder =
                                        new ClarabridgeChatCallback.Response.Builder<>(statusCode);

                                if (isSuccessful && responseBody != null) {
                                    persistenceFacade.saveAppUserRemote(appUserRemote);

                                    ConversationDto newConversation = responseBody.getConversation();
                                    newConversation.addMessages(conversation.getUnsentMessages());
                                    onConversationUpdated(newConversation);

                                    buildAndStartConversationMonitor();

                                } else {
                                    responseBuilder.withError("Error while creating conversation");
                                }

                                callback.run(responseBuilder.build());
                            }
                        });
            }
        });
    }

    public void createUser(@NonNull final String intent,
                           @NonNull final ClarabridgeChatCallback<Void> callback) {
        onClarabridgeChatInitSuccess(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatApiClient.createUser(
                        appUserLocal,
                        appUserRemote.getUserId(),
                        intent,
                        new ClarabridgeChatApiClientCallback<SdkUserDto>() {
                            @Override
                            public void onResult(boolean isSuccessful, int statusCode,
                                                 @Nullable SdkUserDto responseBody) {

                                ClarabridgeChatCallback.Response.Builder<Void> responseBuilder =
                                        new ClarabridgeChatCallback.Response.Builder<>(statusCode);

                                if (isSuccessful && responseBody != null) {
                                    appUserLocal = new AppUserDto();
                                    persistenceFacade.saveAppUserLocal(appUserLocal);

                                    sessionToken = responseBody.getSessionToken();

                                    onUserUpdate(responseBody);

                                } else {
                                    responseBuilder.withError("Error while creating user");
                                }

                                callback.run(responseBuilder.build());
                            }
                        });
            }
        });
    }

    @VisibleForTesting
    void onUserUpdate(SdkUserDto sdkUser) {
        userSettings = sdkUser.getSettings();
        persistenceFacade.saveUserSettings(userSettings);

        if (userSettings != null && userSettings.getProfile() != null) {
            serviceSettings.setSyncDelayInSecs(userSettings.getProfile().getUploadInterval());
        }

        appUserRemote = sdkUser.getAppUser();
        persistenceFacade.saveAppUserId(appUserRemote != null ? appUserRemote.getAppUserId() : null);

        registerForPushNotifications();

        persistenceFacade.saveConversationsList(sdkUser.getConversations());
        clarabridgeChatObserver.onConversationsListUpdated(persistenceFacade.getConversationsList());
        getMostRecentConversation(sdkUser);

        sync();

        if (shouldStartConversationMonitor()) {
            schedule(startConversationMonitorTask, userSettings.getRealtime().getConnectionDelay() * 1000);
        }
    }

    /***
     * Get the most recently active {@link ConversationDto} from the given {@link SdkUserDto} and
     * makes it the current loaded conversation by calling {@link #onConversationUpdated(ConversationDto)}.
     * If the conversation is out-of-date then the newest messages will be fetched from the network.
     * Previously unsent {@link MessageDto} are preserved given that the previous conversation had no
     * ID or that both conversations IDs match.
     *
     * @param sdkUser the current {@link SdkUserDto}
     */
    @VisibleForTesting
    void getMostRecentConversation(SdkUserDto sdkUser) {
        if (sdkUser.getConversations() == null || sdkUser.getConversations().isEmpty()) {
            return;
        }

        final ConversationDto mostRecentConversation = sdkUser.getConversations().get(0);

        if (mostRecentConversation.getMessages().isEmpty()) {
            addUnsentMessages(mostRecentConversation);
            onConversationUpdated(mostRecentConversation);
            return;
        }

        if (conversationManager.isSavedConversationUpToDate(mostRecentConversation.getId())) {
            ConversationDto localConversation = persistenceFacade.getConversationById(mostRecentConversation.getId());

            if (localConversation != null) {
                addUnsentMessages(localConversation);
                onConversationUpdated(localConversation);
            }

            return;
        }

        clarabridgeChatApiClient.getConversation(
                mostRecentConversation.getId(),
                new ClarabridgeChatApiClientCallback<ConversationResponseDto>() {
                    @Override
                    public void onResult(boolean isSuccessful, int statusCode,
                                         @Nullable ConversationResponseDto responseBody) {
                        if (!isSuccessful || responseBody == null || responseBody.getConversation() == null) {
                            // Load the conversation with what we know about it
                            addUnsentMessages(mostRecentConversation);
                            onConversationUpdated(mostRecentConversation);
                            return;
                        }

                        ConversationDto responseConversation = responseBody.getConversation();
                        responseConversation.setMessages(responseBody.getMessages());

                        addUnsentMessages(responseConversation);
                        updateAndNotifyConversation(responseConversation);
                    }
                }
        );
    }

    /**
     * Add all {@link MessageDto} that are still {@link MessageDto.Status#UNSENT} from the current
     * {@link ConversationDto} to the received conversation, given that the current conversation had
     * no ID or that both conversations ID match.
     *
     * @param conversation the {@link ConversationDto} to add messages to
     */
    @VisibleForTesting
    void addUnsentMessages(ConversationDto conversation) {
        if (this.conversation.getId() == null
                || StringUtils.isEqual(this.conversation.getId(), conversation.getId())) {
            conversation.addMessages(this.conversation.getUnsentMessages());
        }
    }

    private void lookForStripeIntegration(final ConfigDto config) {
        for (IntegrationDto integration : config.getIntegrations()) {
            if ("stripeConnect".equals(integration.getType())) {
                persistenceFacade.setStripeKey(integration.getPublicKey());
                break;
            }
        }
    }

    public AppUserDto getAppUserLocal() {
        if (appUserLocal == null) {
            appUserLocal = new AppUserDto();
        }

        return appUserLocal;
    }

    public AppUserDto getAppUserRemote() {
        if (appUserRemote == null) {
            appUserRemote = new AppUserDto();
        }

        return appUserRemote;
    }

    public String getAppUserId() {
        if (appUserRemote == null) {
            appUserRemote = new AppUserDto();
        }

        return appUserRemote.getAppUserId();
    }

    public String getConversationId() {
        if (conversation == null) {
            return null;
        }

        return conversation.getId();
    }

    public String getJwt() {
        return persistenceFacade.getJwt();
    }

    public ConversationDto getConversation() {
        return conversation;
    }

    /**
     * Gets a {@link ConversationDto} with the given conversation ID
     *
     * @param conversationId   the ID of the requested conversation
     * @param internalCallback a {@link ClarabridgeChatCallback} to be invoked when the request completes that will
     *                         contain a {@link ConversationDto} if successful, otherwise an error message
     */
    public void getConversationById(final String conversationId,
                                    @NonNull final ClarabridgeChatCallback<ConversationDto> internalCallback) {
        onClarabridgeChatInitSuccess(new Runnable() {
            @Override
            public void run() {
                if (conversationManager.isSavedConversationUpToDate(conversationId)) {
                    ConversationDto persistedConversation = persistenceFacade.getConversationById(conversationId);
                    ClarabridgeChatCallback.Response<ConversationDto> callbackResponse =
                            new ClarabridgeChatCallback.Response.Builder<ConversationDto>(200)
                                    .withData(persistedConversation)
                                    .build();
                    internalCallback.run(callbackResponse);
                } else {
                    fetchConversationFromNetwork(conversationId, internalCallback);
                }
            }
        });
    }

    /**
     * Retrieves the requested conversation from the network
     *
     * @param conversationId   the ID of the requested conversation
     * @param internalCallback a {@link ClarabridgeChatCallback} to be invoked when the request completes that will
     *                         contain a {@link ConversationDto} if successful, otherwise an error message
     */
    private void fetchConversationFromNetwork(@NonNull final String conversationId,
                                              @NonNull final ClarabridgeChatCallback<ConversationDto> internalCallback) {

        String appUserId = getAppUserId();
        if (appUserId == null) {
            ClarabridgeChatCallback.Response<ConversationDto> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<ConversationDto>(400)
                            .withError("Cannot get conversation for non-existing user.")
                            .build();
            internalCallback.run(callbackResponse);
            return;
        }

        clarabridgeChatApiClient.subscribe(
                conversationId,
                appUserId,
                new ClarabridgeChatApiClientCallback<ConversationResponseDto>() {
                    @Override
                    public void onResult(boolean isSuccessful,
                                         int statusCode,
                                         @Nullable ConversationResponseDto responseBody) {
                        ClarabridgeChatCallback.Response.Builder<ConversationDto> responseBuilder =
                                new ClarabridgeChatCallback.Response.Builder<>(statusCode);

                        if (isSuccessful && responseBody != null && responseBody.getConversation() != null) {
                            final ConversationDto conversation = responseBody.getConversation();
                            conversation.setMessages(responseBody.getMessages());
                            responseBuilder.withData(conversation);

                            persistenceFacade.saveConversationById(conversationId, conversation);
                        } else {
                            responseBuilder.withError("Failed to fetch conversation");

                            // The conversation may not exist so invalidate storage
                            persistenceFacade.saveConversationById(conversationId, null);
                        }

                        internalCallback.run(responseBuilder.build());
                    }
                }
        );
    }

    /**
     * Retrieve the conversations list from storage
     *
     * @param internalCallback a {@link ClarabridgeChatCallback} to be invoked when the list is ready
     */
    public void getConversationsList(@NonNull final ClarabridgeChatCallback<List<ConversationDto>> internalCallback) {
        onClarabridgeChatInitSuccess(new Runnable() {
            @Override
            public void run() {
                List<ConversationDto> conversationsList = persistenceFacade.getConversationsList();
                Collections.sort(conversationsList, Collections.reverseOrder());
                ClarabridgeChatCallback.Response<List<ConversationDto>> callbackResponse =
                        new ClarabridgeChatCallback.Response.Builder<List<ConversationDto>>(200)
                                .withData(conversationsList)
                                .build();
                internalCallback.run(callbackResponse);
            }
        });
    }

    public ConfigDto getConfig() {
        return config;
    }

    public String getFirebaseCloudMessagingProjectId() {
        if (config != null) {
            for (IntegrationDto integration : config.getIntegrations()) {
                if ("fcm".equals(integration.getType())) {
                    return integration.getSenderId();
                }
            }
        }

        return null;
    }

    public void setInitCallback(@NonNull ClarabridgeChatCallback<InitializationStatus> initCallback) {
        this.initCallback = initCallback;
    }

    public void sendMessage(final MessageDto message) {
        cancelTyping();
        messageQueue.push(message);

        onClarabridgeChatReady(new Runnable() {
            @Override
            public void run() {
                forceAppUserSync(new Runnable() {
                    @Override
                    public void run() {
                        processMessageQueue();
                    }
                });
            }
        });
    }

    public void uploadImage(final Message imageMessage, final ClarabridgeChatCallback<Message> callback) {
        cancelTyping();
        onClarabridgeChatReady(new Runnable() {
            @Override
            public void run() {
                if (config != null && getConversationId() != null) {
                    clarabridgeChatApiClient.uploadImage(
                            getConversationId(),
                            imageMessage,
                            getAppUserId(),
                            new ClarabridgeChatApiClientCallback<FileUploadDto>() {
                                @Override
                                public void onResult(boolean isSuccessful, int statusCode,
                                                     @Nullable FileUploadDto responseBody) {
                                    handleFileUploadResponse(isSuccessful, statusCode, responseBody,
                                            imageMessage, callback);
                                }
                            });
                }
            }
        });
    }

    public void uploadFile(final Message fileMessage, final ClarabridgeChatCallback<Message> callback) {
        cancelTyping();
        onClarabridgeChatReady(new Runnable() {
            @Override
            public void run() {
                if (config != null && getConversationId() != null) {
                    File file = fileMessage.getFile();
                    if (file == null) {
                        ClarabridgeChatCallback.Response<Message> callbackResponse =
                                new ClarabridgeChatCallback.Response.Builder<Message>(400)
                                        .withError("Error uploading file.")
                                        .withData(fileMessage)
                                        .build();
                        onFileUploadComplete(callbackResponse, null, callback);
                        return;
                    }

                    if (file.length() > MAX_FILE_SIZE_BYTES) {
                        ClarabridgeChatCallback.Response<Message> callbackResponse =
                                new ClarabridgeChatCallback.Response.Builder<Message>(413)
                                        .withError("Error uploading file.")
                                        .withData(fileMessage)
                                        .build();
                        onFileUploadComplete(callbackResponse, null, callback);
                        return;
                    }

                    clarabridgeChatApiClient.uploadFile(
                            getConversationId(),
                            fileMessage,
                            getAppUserId(),
                            new ClarabridgeChatApiClientCallback<FileUploadDto>() {
                                @Override
                                public void onResult(boolean isSuccessful, int statusCode,
                                                     @Nullable FileUploadDto responseBody) {
                                    handleFileUploadResponse(isSuccessful, statusCode, responseBody,
                                            fileMessage, callback);
                                }
                            });
                }
            }
        });
    }

    public void loadConversation(@NonNull final String conversationId,
                                 @NonNull final ClarabridgeChatCallback<ConversationDto> callback) {
        onClarabridgeChatInitSuccess(new Runnable() {
            @Override
            public void run() {
                if (getAppUserId() != null) {
                    if (StringUtils.isEqual(conversationId, getConversationId())) {
                        ClarabridgeChatCallback.Response<ConversationDto> callbackResponse =
                                new ClarabridgeChatCallback.Response.Builder<ConversationDto>(200)
                                        .withData(getConversation())
                                        .build();
                        callback.run(callbackResponse);
                        return;
                    }

                    clarabridgeChatApiClient.subscribe(
                            conversationId,
                            getAppUserId(),
                            new ClarabridgeChatApiClientCallback<ConversationResponseDto>() {
                                @Override
                                public void onResult(boolean isSuccessful, int statusCode,
                                                     @Nullable ConversationResponseDto responseBody) {

                                    ClarabridgeChatCallback.Response.Builder<ConversationDto> responseBuilder =
                                            new ClarabridgeChatCallback.Response.Builder<>(statusCode);

                                    if (isSuccessful && responseBody != null) {
                                        final ConversationDto conversation = responseBody.getConversation();

                                        if (conversation != null) {
                                            conversation.setMessages(responseBody.getMessages());
                                            updateAndNotifyConversation(conversation);
                                            responseBuilder.withData(conversation);
                                        } else {
                                            responseBuilder.withError("Conversation response was empty");
                                        }

                                    } else {
                                        responseBuilder.withError("Error setting conversation ID.");
                                    }

                                    callback.run(responseBuilder.build());
                                }
                            });
                } else {
                    ClarabridgeChatCallback.Response<ConversationDto> callbackResponse =
                            new ClarabridgeChatCallback.Response.Builder<ConversationDto>(400)
                                    .withError("Cannot set conversation for non-existing user.")
                                    .build();
                    callback.run(callbackResponse);
                }
            }
        });
    }

    public void setFirebaseCloudMessagingToken(final String token,
                                               @NonNull final ClarabridgeChatCallback<LoginResult> callback) {
        if (!StringUtils.isEqual(token, serviceSettings.getFirebaseCloudMessagingToken())) {
            Log.i(TAG, "Setting push token: " + token);
            serviceSettings.setFirebaseCloudMessagingToken(token);
            updatePushToken(callback);
        } else {
            callback.run(new ClarabridgeChatCallback.Response.Builder<LoginResult>(200).build());
        }
    }

    public String getFirebaseCloudMessagingToken() {
        return serviceSettings.getFirebaseCloudMessagingToken();
    }

    public void sync() {
        persistenceFacade.saveRetryConfiguration(retryConfiguration);
        persistenceFacade.saveUserSettings(userSettings);
        persistenceFacade.saveAppUserLocal(appUserLocal);
        persistenceFacade.saveAppUserRemote(appUserRemote);

        if (conversation.getId() != null) {
            synchronized (conversation.getMessages()) {
                persistenceFacade.saveConversationById(conversation.getId(), conversation);
            }
        }
        persistenceFacade.saveSessionToken(sessionToken);
        scheduleAppUserSync();
    }

    public void syncAppUser() {
        persistenceFacade.saveAppUserLocal(appUserLocal);
        scheduleAppUserSync();
    }

    private void syncAppUser(final Runnable runnable) {
        boolean isUserUpdateEnabled = userSettings != null && userSettings.getProfile() != null
                && userSettings.getProfile().getEnabled();

        if (isUserUpdateEnabled && appUserExists() && getAppUserLocal().getModified()) {
            initSyncScheduled = false;
            initSyncLastUploaded = System.currentTimeMillis();

            clarabridgeChatApiClient.updateAppUser(
                    appUserLocal,
                    appUserRemote.getAppUserId(),
                    new ClarabridgeChatApiClientCallback<Void>() {
                        @Override
                        public void onResult(boolean isSuccessful, int statusCode, @Nullable Void responseBody) {
                            if (isSuccessful) {
                                appUserRemote.merge(appUserLocal);
                                appUserLocal = new AppUserDto();

                                persistenceFacade.saveAppUserLocal(appUserLocal);
                                persistenceFacade.saveAppUserRemote(appUserRemote);
                            } else {
                                scheduleAppUserSync();
                            }

                            if (runnable != null) {
                                runnable.run();
                            }
                        }
                    });
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void processPayment(final CreditCard creditCard,
                               final MessageActionDto messageAction) {
        if (creditCard == null && cardSummary != null) {
            chargeCard(messageAction, null);
            return;
        }

        if (config != null && creditCard != null) {
            clarabridgeChatApiClient.getStripeToken(
                    creditCard,
                    new ClarabridgeChatApiClientCallback<StripeTokenDto>() {
                        @Override
                        public void onResult(
                                boolean isSuccessful,
                                int statusCode,
                                @Nullable StripeTokenDto responseBody) {

                            if (isSuccessful && responseBody != null) {
                                final String stripeToken = responseBody.getToken();

                                if (isAuthenticatedUser()) {
                                    storeStripeToken(messageAction, stripeToken);
                                } else {
                                    chargeCard(messageAction, stripeToken);
                                }
                            } else {
                                onPaymentProcessed(messageAction, PaymentStatus.ERROR);
                            }
                        }
                    });
        }
    }

    public void loadCardSummary() {
        if (cardSummary != null) {
            onCardSummaryLoaded();
            return;
        }

        if (!fetchCustomerGuard && appUserRemote != null && appUserRemote.getHasPaymentInfo()) {
            fetchCustomerGuard = true;

            clarabridgeChatApiClient.getStripeCustomer(
                    appUserRemote.getAppUserId(),
                    new ClarabridgeChatApiClientCallback<StripeCustomerDto>() {
                        @Override
                        public void onResult(boolean isSuccessful, int statusCode,
                                             @Nullable StripeCustomerDto responseBody) {
                            if (isSuccessful && responseBody != null) {
                                cardSummary = responseBody.getCard();
                            }
                            fetchCustomerGuard = false;
                            onCardSummaryLoaded();
                        }
                    });
        }
    }

    public void updateReadState() {
        sendConversationActivity("conversation:read", new ClarabridgeChatApiClientCallback<Void>() {
            @Override
            public void onResult(boolean isSuccessful, int statusCode, @Nullable Void responseBody) {
                if (!isSuccessful || conversation.getMessages().isEmpty()) {
                    return;
                }

                Double newTimestamp = conversation.getMessages()
                        .get(conversation.getMessages().size() - 1)
                        .getReceived();

                conversationManager.setParticipantLastRead(getConversationId(), newTimestamp, getAppUserId());
                clarabridgeChatObserver.onConversationsListUpdated(persistenceFacade.getConversationsList());

                ConversationDto updatedConversation = persistenceFacade.getConversationById(getConversationId());
                if (updatedConversation != null) {
                    conversation.update(updatedConversation);
                }
            }
        });
    }

    public void startTyping() {
        boolean isInvalidUser = StringUtils.isEmpty(getAppUserId());
        boolean isTypingDisabled = userSettings == null
                || userSettings.getTyping() == null
                || !userSettings.getTyping().isEnabled();

        if (isInvalidUser || isTypingDisabled) {
            return;
        }

        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        lastStartTypingEvent = currentTimeInMillis;

        boolean hasUploadedTypingStart = lastUploadedStartTypingEvent != null;
        boolean hasUploadedTypingStartRecently = lastUploadedStartTypingEvent != null
                && (currentTimeInMillis - lastUploadedStartTypingEvent <= TYPING_EVENT_TIMEOUT_MILLIS);

        if (!hasUploadedTypingStart || !hasUploadedTypingStartRecently) {
            lastUploadedStartTypingEvent = currentTimeInMillis;
            sendConversationActivity("typing:start", null);
        }

        schedule(stopTypingTask, TYPING_EVENT_TIMEOUT_MILLIS);
    }

    public void stopTyping() {
        final Long lastTypingStart = lastStartTypingEvent;

        removeCallbacks(stopTypingTask);
        schedule(new Runnable() {
            @Override
            public void run() {
                boolean isTypingAlreadyStopped = lastStartTypingEvent == null;
                boolean hasUserStartedTyping = lastTypingStart != null && !lastTypingStart.equals(lastStartTypingEvent);

                if (!isTypingAlreadyStopped && !hasUserStartedTyping) {
                    lastStartTypingEvent = null;
                    lastUploadedStartTypingEvent = null;

                    sendConversationActivity("typing:stop", null);
                }
            }
        }, STOP_TYPING_BUFFER_MILLIS);
    }

    void cancelTyping() {
        removeCallbacks(stopTypingTask);
        lastStartTypingEvent = null;
        lastUploadedStartTypingEvent = null;
    }

    public void sendConversationActivity(String type, @Nullable ClarabridgeChatApiClientCallback<Void> callback) {
        if (getConversationId() != null) {
            clarabridgeChatApiClient.sendConversationActivity(
                    getConversationId(),
                    type,
                    getAppUserId(),
                    callback);
        }
    }

    public void postback(final MessageActionDto messageAction,
                         @NonNull final ClarabridgeChatCallback<Void> callback) {
        if (getConversationId() != null) {
            clarabridgeChatApiClient.postback(
                    getConversationId(),
                    messageAction,
                    getAppUserId(),
                    new ClarabridgeChatApiClientCallback<Void>() {
                        @Override
                        public void onResult(boolean isSuccessful, int statusCode, @Nullable Void responseBody) {
                            ClarabridgeChatCallback.Response.Builder<Void> responseBuilder =
                                    new ClarabridgeChatCallback.Response.Builder<>(statusCode);
                            if (!isSuccessful) {
                                responseBuilder.withError("Error while calling postback");
                            }
                            callback.run(responseBuilder.build());
                        }
                    });
        }
    }

    public void forceAppUserSync(@NonNull final Runnable callback) {
        if (appUserExists() && getAppUserLocal().getModified()) {
            removeCallbacks(appUserSyncTask);

            syncAppUser(callback);
        } else {
            callback.run();
        }
    }

    public LoginResult getLastLoginResult() {
        return lastLoginResult;
    }

    public ClarabridgeChatConnectionStatus getClarabridgeChatConnectionStatus() {
        if (conversationMonitor != null) {
            return conversationMonitor.isConnected()
                    ? ClarabridgeChatConnectionStatus.CONNECTED
                    : ClarabridgeChatConnectionStatus.DISCONNECTED;
        }

        return ClarabridgeChatConnectionStatus.NOT_YET_INITIATED;
    }

    private void handleFileUploadResponse(
            boolean isSuccessful,
            int statusCode,
            FileUploadDto responseBody,
            Message message,
            ClarabridgeChatCallback<Message> callback) {
        if (isSuccessful && responseBody != null) {
            String messageId = responseBody.getMessageId();

            synchronized (getMessageIdLock(messageId)) {
                if (rejectedUploads.containsKey(messageId)) {
                    // faye rejection arrived before response
                    ClarabridgeChatCallback.Response<Message> rejectedResponse = rejectedUploads.remove(messageId);
                    if (rejectedResponse != null) {
                        onFileUploadComplete(rejectedResponse, null, callback);
                    }

                } else if (pendingUploads.containsKey(messageId)) {
                    // faye success arrived before response
                    MessageDto pendingUpload = pendingUploads.remove(messageId);
                    if (pendingUpload != null) {
                        message.getEntity().update(pendingUpload);
                        ClarabridgeChatCallback.Response<Message> callbackResponse =
                                new ClarabridgeChatCallback.Response.Builder<Message>(200)
                                        .withData(message)
                                        .build();
                        onFileUploadComplete(callbackResponse, pendingUpload, callback);
                    }

                } else {
                    message.getEntity().setId(messageId);
                    processingUploads.put(messageId, new ProcessingFileUpload(message, callback));
                }
            }
        } else {
            ClarabridgeChatCallback.Response<Message> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<Message>(statusCode)
                            .withError("Error uploading file.")
                            .withData(message)
                            .build();
            onFileUploadComplete(callbackResponse, null, callback);
        }
    }

    private boolean isConversationStarted() {
        return !StringUtils.isEmpty(getConversationId());
    }

    private void onInitializationStatusChanged(@NonNull final InitializationStatus status) {
        if (this.initializationStatus != status) {
            this.initializationStatus = status;

            schedule(new Runnable() {
                @Override
                public void run() {
                    clarabridgeChatObserver.onInitializationStatusChanged(status);
                }
            });

            if (status.equals(InitializationStatus.SUCCESS)) {
                schedule(new Runnable() {
                    @Override
                    public void run() {
                        ClarabridgeChatCallback.Response<InitializationStatus> callbackResponse =
                                new ClarabridgeChatCallback.Response.Builder<InitializationStatus>(200)
                                        .withData(InitializationStatus.SUCCESS)
                                        .build();
                        initCallback.run(callbackResponse);
                    }
                });
            }

            if (status.equals(InitializationStatus.SUCCESS) && onClarabridgeChatInitSuccessList.size() > 0) {
                runOnClarabridgeChatInitSuccessList();
            }

            if (!status.equals(InitializationStatus.UNKNOWN) && onClarabridgeChatInitCompleteList.size() > 0) {
                runOnClarabridgeChatInitCompleteList();
            }
        }
    }

    private synchronized void runOnClarabridgeChatReadyList() {
        for (final Runnable apiCall : onClarabridgeChatReadyList) {
            apiCall.run();
        }

        onClarabridgeChatReadyList.clear();
    }

    private synchronized void runOnClarabridgeChatInitSuccessList() {
        for (final Runnable apiCall : onClarabridgeChatInitSuccessList) {
            if (apiCall != null) {
                apiCall.run();
            }
        }

        onClarabridgeChatInitSuccessList.clear();
    }

    private synchronized void runOnClarabridgeChatInitCompleteList() {
        for (final Runnable apiCall : onClarabridgeChatInitCompleteList) {
            if (apiCall != null) {
                apiCall.run();
            }
        }

        onClarabridgeChatInitCompleteList.clear();
    }

    private void onInitialization(@NonNull final ConfigDto config) {
        this.config = config;

        if (config.getRetryConfiguration() != null) {
            retryConfiguration = config.getRetryConfiguration();
            persistenceFacade.saveRetryConfiguration(retryConfiguration);
        }
    }

    @VisibleForTesting
    void onConversationUpdated(final ConversationDto updatedConversation) {
        if (updatedConversation == null) {
            return;
        }

        synchronized (conversation.getMessages()) {
            conversation.update(updatedConversation);

            Collections.sort(conversation.getMessages());

            final String currentUserId = getAppUserId();
            for (MessageDto message : conversation.getMessages()) {
                message.setIsFromCurrentUser(StringUtils.isEqual(message.getAuthorId(),
                        currentUserId));
            }

            if (conversation.getId() != null) {
                persistenceFacade.saveConversationById(conversation.getId(), conversation);
            }
        }

        if (!StringUtils.isEmpty(updatedConversation.getId())) {
            schedule(new Runnable() {
                @Override
                public void run() {
                    conversationObserver.onConversationUpdated(updatedConversation.getId());
                }
            });
        }
    }

    public void onFileUploadComplete(@NonNull final ClarabridgeChatCallback.Response<Message> response,
                                     @Nullable final MessageDto uploadedMessage,
                                     @NonNull final ClarabridgeChatCallback<Message> callback) {
        schedule(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatObserver.onFileUploadComplete(response, uploadedMessage, callback);
            }
        });
    }

    @VisibleForTesting
    void schedule(final Runnable task) {
        if (handler != null) {
            handler.removeCallbacks(task);
            handler.post(task);
        }
    }

    private void schedule(final Runnable task, final long delayMillis) {
        if (handler != null) {
            handler.removeCallbacks(task);
            handler.postDelayed(task, delayMillis);
        }
    }

    public void onMessageSent(@NonNull final MessageDto message) {
        message.setIsFromCurrentUser(true);
        sync();

        schedule(new Runnable() {
            @Override
            public void run() {
                conversationObserver.onMessageSent(message);
            }
        });
    }

    private void onConnectionStatusChanged(final ConnectionStatus status) {
        switch (status) {
            case CONNECTED:
                if (retryInitOnConnect) {
                    init();
                } else if (isConversationStarted() && this.connectionStatus != ConnectionStatus.UNKNOWN) {
                    refreshConversation(null);
                }

                break;
            case DISCONNECTED:
                if (conversationMonitor != null) {
                    conversationMonitor.pause();
                }

                break;
        }

        this.connectionStatus = status;
    }

    private void onClarabridgeChatConnectionStatusChanged(@NonNull final ClarabridgeChatConnectionStatus status) {
        if (status == ClarabridgeChatConnectionStatus.CONNECTED) {
            refreshConversationList();
            refreshConversation(new Runnable() {
                @Override
                public void run() {
                    runOnClarabridgeChatReadyList();
                }
            });
        }

        schedule(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatObserver.onClarabridgeChatConnectionStatusChanged(status);
            }
        });
    }

    private void onPaymentProcessed(@NonNull final MessageActionDto processedAction,
                                    @NonNull final PaymentStatus status) {
        if (status == PaymentStatus.SUCCESS) {
            onConversationUpdated(conversation);
        }

        schedule(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatObserver.onPaymentProcessed(processedAction, status);
            }
        });
    }

    private void onCardSummaryLoaded() {
        schedule(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatObserver.onCardSummaryLoaded(cardSummary);
            }
        });
    }

    private void processMessageQueue() {
        onClarabridgeChatInitSuccess(new Runnable() {
            @Override
            public void run() {
                if (processingMessageQueue) {
                    return;
                }

                final MessageDto message = messageQueue.pollFirst();
                if (getConversationId() == null || message == null) {
                    return;
                }

                processingMessageQueue = true;
                clarabridgeChatApiClient.postMessage(
                        getConversationId(),
                        message,
                        getAppUserId(),
                        new ClarabridgeChatApiClientCallback<PostMessageDto>() {
                            @Override
                            public void onResult(boolean isSuccessful, int statusCode,
                                                 @Nullable PostMessageDto responseBody) {
                                if (isSuccessful && responseBody != null) {
                                    final List<MessageDto> messages = responseBody.getMessages();

                                    if (messages != null && messages.size() > 0) {
                                        for (MessageDto messageDto : messages) {
                                            if (!StringUtils.isEmpty(messageDto.getId())) {
                                                messageQueue.remove(message);
                                                message.update(messageDto);
                                                message.setStatus(MessageDto.Status.SENT);
                                            } else {
                                                message.setStatus(MessageDto.Status.SENDING_FAILED);
                                            }
                                        }
                                    }
                                } else {
                                    message.setStatus(MessageDto.Status.SENDING_FAILED);
                                }

                                conversationManager.updateTimestampsAndUnreadCount(
                                        conversation,
                                        message.getReceived(),
                                        message.getAuthorId(),
                                        getAppUserId(),
                                        true
                                );

                                onMessageSent(message);
                                processingMessageQueue = false;
                                processMessageQueue();
                            }
                        });
            }
        });
    }

    /**
     * If there's a conversation currently loaded, then a request is made to the server to fetch the
     * most recent messages. Unsent messages are preserved, being added at the end of the updated list.
     * The list of processing uploads will be updated as a result to remove any pending messages
     * that already exist in the server.
     *
     * @param runnable a {@link Runnable} to be invoked once the conversation is refreshed
     */
    void refreshConversation(final Runnable runnable) {
        if (getConversationId() == null) {
            // No conversation id so don't try to get messages
            return;
        }

        clarabridgeChatApiClient.getConversation(
                getConversationId(),
                new ClarabridgeChatApiClientCallback<ConversationResponseDto>() {
                    @Override
                    public void onResult(boolean isSuccessful, int statusCode,
                                         @Nullable ConversationResponseDto responseBody) {
                        if (!isSuccessful || responseBody == null || conversation == null) {
                            return;
                        }

                        ConversationDto responseConversation = responseBody.getConversation();
                        if (StringUtils.isNotNullAndNotEqual(getConversationId(), responseConversation.getId())) {
                            if (runnable != null) {
                                runnable.run();
                            }

                            return;
                        }

                        responseConversation.setMessages(responseBody.getMessages());
                        responseConversation.addMessages(conversation.getUnsentMessages());

                        cleanProcessingUploads(responseBody.getMessages());

                        updateAndNotifyConversation(responseConversation);
                        buildAndStartConversationMonitor();

                        if (runnable != null) {
                            runnable.run();
                        }
                    }
                });
    }

    @VisibleForTesting
    void refreshConversationList() {
        if (appUserRemote == null || appUserRemote.getAppUserId() == null) {
            Logger.w(TAG, "There was no app user id to fetch the conversations list");
            return;
        }

        clarabridgeChatApiClient.getConversations(
                appUserRemote.getAppUserId(),
                new ClarabridgeChatApiClientCallback<ConversationsListResponseDto>() {
                    @Override
                    public void onResult(
                            boolean isSuccessful,
                            int statusCode,
                            @Nullable ConversationsListResponseDto responseBody) {
                        if (!isSuccessful) {
                            Logger.e(TAG, "Unable to retrieve conversations list");
                            return;
                        }

                        List<ConversationDto> conversationsList = responseBody != null
                                ? responseBody.getConversations()
                                : new ArrayList<ConversationDto>();

                        persistenceFacade.saveConversationsList(conversationsList);
                        clarabridgeChatObserver.onConversationsListUpdated(conversationsList);
                    }
                }
        );
    }

    @VisibleForTesting
    void updateAndNotifyConversation(ConversationDto conversation) {
        notifyForUnread(conversation);
        onConversationUpdated(conversation);
    }

    @VisibleForTesting
    void cleanProcessingUploads(List<MessageDto> newMessages) {
        if (processingUploads.size() > 0) {
            for (MessageDto message : newMessages) {
                ProcessingFileUpload processingFileUpload = processingUploads.remove(message.getId());

                if (processingFileUpload != null) {
                    ClarabridgeChatCallback.Response<Message> callbackResponse =
                            new ClarabridgeChatCallback.Response.Builder<Message>(200)
                                    .withData(processingFileUpload.getMessage())
                                    .build();
                    onFileUploadComplete(callbackResponse, message, processingFileUpload.getCallback());
                }
            }
        }

        if (processingUploads.size() > 0) {
            for (ProcessingFileUpload processingFileUpload : processingUploads.values()) {
                ClarabridgeChatCallback.Response<Message> callbackResponse =
                        new ClarabridgeChatCallback.Response.Builder<Message>(400)
                                .withError("Error uploading file.")
                                .withData(processingFileUpload.getMessage())
                                .build();
                onFileUploadComplete(callbackResponse, null, processingFileUpload.getCallback());
            }

            processingUploads.clear();
        }
    }

    private void scheduleAppUserSync() {
        if (appUserExists() && getAppUserLocal().getModified()) {
            if (initSyncScheduled) {
                return;
            }

            final long syncDelayMillis = serviceSettings.getSyncDelayInSecs() * 1000;
            final long timeSinceLastUploadMillis = System.currentTimeMillis() - initSyncLastUploaded;
            final long delay;

            if (timeSinceLastUploadMillis > syncDelayMillis) {
                delay = 1000;
            } else {
                delay = syncDelayMillis - timeSinceLastUploadMillis;
            }

            schedule(appUserSyncTask, delay);
            initSyncScheduled = true;
        }
    }

    private void chargeCard(final MessageActionDto messageAction, final String stripeToken) {
        onClarabridgeChatInitSuccess(new Runnable() {
            @Override
            public void run() {
                clarabridgeChatApiClient.stripeCharge(
                        appUserRemote.getAppUserId(),
                        messageAction,
                        stripeToken,
                        new ClarabridgeChatApiClientCallback<Void>() {
                            @Override
                            public void onResult(boolean isSuccessful, int statusCode, @Nullable Void responseBody) {
                                if (isSuccessful) {
                                    messageAction.setState("paid");
                                    onPaymentProcessed(messageAction, PaymentStatus.SUCCESS);
                                } else {
                                    onPaymentProcessed(messageAction, PaymentStatus.ERROR);
                                }
                            }
                        });
            }
        });
    }

    private void storeStripeToken(final MessageActionDto messageAction, final String stripeToken) {
        if (appUserRemote != null) {
            clarabridgeChatApiClient.storeStripeToken(
                    appUserRemote.getAppUserId(),
                    stripeToken,
                    new ClarabridgeChatApiClientCallback<Void>() {
                        @Override
                        public void onResult(boolean isSuccessful, int statusCode, @Nullable Void responseBody) {
                            if (isSuccessful) {
                                appUserRemote.setHasPaymentInfo(true);
                                sync();
                                chargeCard(messageAction, null);
                            } else {
                                onPaymentProcessed(messageAction, PaymentStatus.ERROR);
                            }
                        }
                    });
        }
    }

    private void updatePushToken(@NonNull final ClarabridgeChatCallback<LoginResult> callback) {
        onClarabridgeChatInitSuccess(new Runnable() {
            @Override
            public void run() {
                if (getAppUserId() != null) {
                    final String clientId = serviceSettings.getClientId();
                    final String firebaseCloudMessagingToken = serviceSettings.getFirebaseCloudMessagingToken();

                    clarabridgeChatApiClient.updatePushToken(
                            appUserRemote.getAppUserId(),
                            clientId,
                            firebaseCloudMessagingToken,
                            new ClarabridgeChatApiClientCallback<Void>() {

                                @Override
                                public void onResult(boolean isSuccessful, int statusCode,
                                                     @Nullable Void responseBody) {
                                    ClarabridgeChatCallback.Response.Builder<LoginResult> responseBuilder =
                                            new ClarabridgeChatCallback.Response.Builder<>(statusCode);

                                    if (isSuccessful) {
                                        Log.i(TAG, "Push token successfully uploaded: " + firebaseCloudMessagingToken);

                                        responseBuilder.withData(LoginResult.SUCCESS);
                                    } else {
                                        serviceSettings.setFirebaseCloudMessagingToken(null);
                                        Log.e(TAG, "There was an error uploading the push token: "
                                                + firebaseCloudMessagingToken);

                                        responseBuilder.withError("Error uploading push token")
                                                .withData(LoginResult.ERROR);
                                    }

                                    callback.run(responseBuilder.build());
                                }
                            });
                } else {
                    Log.i(TAG, "Missing appUserId, push token cannot be uploaded. Undoing.");
                    serviceSettings.setFirebaseCloudMessagingToken(null);

                    ClarabridgeChatCallback.Response<LoginResult> callbackResponse =
                            new ClarabridgeChatCallback.Response.Builder<LoginResult>(200)
                                    .withData(LoginResult.SUCCESS)
                                    .build();
                    callback.run(callbackResponse);
                }
            }
        });
    }

    private void updateConnectionStatus() {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetInfo == null || !activeNetInfo.isConnected() || !activeNetInfo.isAvailable()) {
                onConnectionStatusChanged(ConnectionStatus.DISCONNECTED);
            } else {
                onConnectionStatusChanged(ConnectionStatus.CONNECTED);
            }
        }
    }

    /**
     * Notifies the user about new messages received from the server. A message is considered new
     * if it hasn't yet been persisted to the SDK storage.
     *
     * @param updatedConversation the {@link ConversationDto} received from the server
     */
    @VisibleForTesting
    void notifyForUnread(final ConversationDto updatedConversation) {
        int unreadCount = updatedConversation.getUnreadCount(getAppUserId());

        if (unreadCount == 0) {
            return;
        }

        ConversationDto persistedConversation = persistenceFacade.getConversationById(updatedConversation.getId());
        List<MessageDto> persistedMessages = persistedConversation != null
                ? persistedConversation.getMessages()
                : new ArrayList<MessageDto>();

        List<MessageDto> updatedMessages = updatedConversation.getMessages();

        if (updatedMessages.size() > persistedMessages.size()) {
            int lastMessageIndex = updatedMessages.size() - 1;

            // Loop from the end until the first unread message
            for (int i = lastMessageIndex; i >= lastMessageIndex - unreadCount && i >= 0; i--) {
                MessageDto candidateMessage = updatedMessages.get(i);

                boolean isFromCurrentUser = StringUtils.isNotNullAndEqual(candidateMessage.getAuthorId(),
                        getAppUserId());

                if (!isFromCurrentUser) {
                    triggerNotification(updatedConversation.getId(), candidateMessage);
                    return;
                }
            }
        }
    }

    private synchronized Object getMessageIdLock(String messageId) {
        Object lock = messageIdLocks.get(messageId);

        if (lock != null) {
            return lock;
        }

        lock = new Object();
        messageIdLocks.put(messageId, lock);

        return lock;
    }

    // region Test only methods
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    Long getLastStartTypingEvent() {
        return lastStartTypingEvent;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setLastStartTypingEvent(Long lastStartTypingEvent) {
        this.lastStartTypingEvent = lastStartTypingEvent;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    Long getLastUploadedStartTypingEvent() {
        return lastUploadedStartTypingEvent;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setLastUploadedStartTypingEvent(Long lastUploadedStartTypingEvent) {
        this.lastUploadedStartTypingEvent = lastUploadedStartTypingEvent;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    Runnable getStopTypingTask() {
        return stopTypingTask;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    ConversationMonitor getConversationMonitor() {
        return conversationMonitor;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setUserSettings(UserSettingsDto userSettings) {
        this.userSettings = userSettings;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setAppUserRemote(AppUserDto appUserRemote) {
        this.appUserRemote = appUserRemote;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setConversation(ConversationDto conversation) {
        this.conversation = conversation;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setConfig(ConfigDto config) {
        this.config = config;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setPersistenceFacade(PersistenceFacade persistenceFacade) {
        this.persistenceFacade = persistenceFacade;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setClarabridgeChatApiClient(ClarabridgeChatApiClient clarabridgeChatApiClient) {
        this.clarabridgeChatApiClient = clarabridgeChatApiClient;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setConversationManager(ConversationManager conversationManager) {
        this.conversationManager = conversationManager;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
    // endregion

    @VisibleForTesting
    void triggerNotification(String conversationId, MessageDto message) {
        Notifier.notify(this, conversationId, message);
    }

    interface FirebaseTokenCallback {
        void onTokenReceived(String token);
    }

    @VisibleForTesting
    void getFirebaseMessagingToken(final FirebaseTokenCallback callback) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            callback.onTokenReceived(null);
                            return;
                        }

                        // Get new Instance ID token
                        if (task.getResult() != null) {
                            String token = task.getResult().getToken();
                            callback.onTokenReceived(token);
                        }
                    }
                });
    }
}
