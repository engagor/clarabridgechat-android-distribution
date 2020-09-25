package com.clarabridge.core;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.util.Log;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.di.DaggerClarabridgeChatComponent;
import com.clarabridge.core.di.ClarabridgeChatComponent;
import com.clarabridge.core.model.AppUserDto;
import com.clarabridge.core.model.CardSummaryDto;
import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.model.ConversationEventDto;
import com.clarabridge.core.model.MessageActionDto;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.model.PostConversationMessageDto;
import com.clarabridge.core.service.ServiceObserver;
import com.clarabridge.core.service.ClarabridgeChatObserver;
import com.clarabridge.core.service.ClarabridgeChatService;
import com.clarabridge.core.service.ClarabridgeChatServiceBinder;
import com.clarabridge.core.utils.JavaUtils;
import com.clarabridge.core.utils.Predicate;
import com.clarabridge.core.utils.StringUtils;

import static androidx.annotation.VisibleForTesting.NONE;
import static com.clarabridge.core.ClarabridgeChat.INITIALIZATION_LOCK;

class ClarabridgeChatInternal implements Application.ActivityLifecycleCallbacks,
        ClarabridgeChatObserver, ServiceObserver, ServiceConnection {

    private static final String TAG = "ClarabridgeChatService";

    private final Application application;
    private final Context context;
    private final Settings settings;
    private final List<Runnable> serviceCallQueue = Collections.synchronizedList(new LinkedList<Runnable>());
    private final ClarabridgeChatComponent clarabridgeChatComponent;

    private boolean serviceCallBlocked;
    private int runningActivities;

    private ClarabridgeChatService service;
    private ConversationReal conversation;
    private String firebaseCloudMessagingToken;
    private AppUserDto pendingLocalUser;
    @NonNull
    private ClarabridgeChatCallback<InitializationStatus> initCallback = new StubbedCallback<>();

    ClarabridgeChatInternal(@NonNull Application application, @NonNull Settings settings, int runningActivities) {
        this.application = application;
        this.settings = settings;
        this.runningActivities = runningActivities;

        clarabridgeChatComponent = DaggerClarabridgeChatComponent.builder()
                .application(application)
                .settings(settings)
                .build();

        context = application.getApplicationContext();
        conversation = new ConversationReal(new ConversationDto());
        pendingLocalUser = new AppUserDto();
    }

    @VisibleForTesting(otherwise = NONE)
    ClarabridgeChatInternal(Application application) {
        this(application, new Settings(null), 0);
    }

    void init(@NonNull final ClarabridgeChatCallback<InitializationStatus> callback) {
        initCallback = callback;
        application.registerActivityLifecycleCallbacks(this);
        context.bindService(
                new Intent(context, ClarabridgeChatService.class),
                this,
                Context.BIND_AUTO_CREATE | Context.BIND_ABOVE_CLIENT
        );
    }

    void destroy(boolean unbindService) {
        application.unregisterActivityLifecycleCallbacks(this);

        if (unbindService && service != null) {
            context.unbindService(this);
            service = null;
        } else if (service != null) {
            service.pause();
        }
    }

    @Nullable
    String getUserId() {
        return service != null ? service.getUserId() : null;
    }

    @Nullable
    InitializationStatus getInitializationStatus() {
        return service != null ? service.getInitializationStatus() : null;
    }

    Conversation getConversation() {
        return conversation;
    }

    void getConversationById(@NonNull final String conversationId,
                             @NonNull final ClarabridgeChatCallback<Conversation> integratorCallback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                serviceCallBlocked = true;
                service.getConversationById(conversationId, new ClarabridgeChatCallback<ConversationDto>() {
                    @Override
                    public void run(@NonNull Response<ConversationDto> response) {
                        serviceCallBlocked = false;
                        Response.Builder<Conversation> responseBuilder =
                                new Response.Builder<Conversation>(response.getStatus())
                                        .withError(response.getError());

                        if (response.getData() != null) {
                            responseBuilder.withData(new ConversationProxy(response.getData()));
                        }

                        integratorCallback.run(responseBuilder.build());
                    }
                });
            }
        });
    }

    /**
     * Retrieve the the 10 most recently active {@link com.clarabridge.core.Conversation}s for the current user, sorted
     * from most recently updated to last.
     *
     * @param integratorCallback a {@link ClarabridgeChatCallback} to be invoked when the list is ready
     */
    void getConversationsList(@NonNull final ClarabridgeChatCallback<List<Conversation>> integratorCallback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                serviceCallBlocked = true;
                service.getConversationsList(new ClarabridgeChatCallback<List<ConversationDto>>() {
                    @Override
                    public void run(@NonNull Response<List<ConversationDto>> response) {
                        serviceCallBlocked = false;
                        Response.Builder<List<Conversation>> responseBuilder =
                                new Response.Builder<List<Conversation>>(response.getStatus())
                                        .withError(response.getError());

                        if (response.getData() != null && !response.getData().isEmpty()) {
                            List<Conversation> conversationList = new ArrayList<>();
                            for (ConversationDto entity : response.getData()) {
                                conversationList.add(new ConversationProxy(entity));
                            }
                            responseBuilder.withData(conversationList);
                        }

                        integratorCallback.run(responseBuilder.build());
                    }
                });
            }
        });
    }

    /**
     * Retrieve the 10 more recently active {@link com.clarabridge.core.Conversation}s for the current user, sorted
     * from most recently updated to last.
     *
     * @param integratorCallback a {@link ClarabridgeChatCallback} to be invoked when the list is ready
     */
    void getMoreConversationsList(@NonNull final ClarabridgeChatCallback<List<Conversation>> integratorCallback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.getMoreConversationsList(new ClarabridgeChatCallback<List<ConversationDto>>() {
                    @Override
                    public void run(@NonNull Response<List<ConversationDto>> response) {
                        Response.Builder<List<Conversation>> responseBuilder =
                                new Response.Builder<List<Conversation>>(response.getStatus())
                                        .withError(response.getError());

                        if (response.getData() != null && !response.getData().isEmpty()) {
                            List<Conversation> conversationList = new ArrayList<>();
                            for (ConversationDto entity : response.getData()) {
                                conversationList.add(new ConversationProxy(entity));
                            }
                            responseBuilder.withData(conversationList);
                        }

                        integratorCallback.run(responseBuilder.build());
                    }
                });
            }
        });
    }

    /**
     * Accessor method for knowing if there are more conversations to be fetched or not.
     *
     * @return true if there is more conversations to be fetched or false if not.
     */
    boolean hasMoreConversations() {
        return service.isHasMoreConversations();
    }

    @Nullable
    String getConversationId() {
        return service != null && service.getConversation() != null
                ? service.getConversation().getId()
                : null;
    }

    @Nullable
    Config getConfig() {
        return service != null && service.getConversation() != null && service.getConfig() != null
                ? new Config(service.getConfig())
                : null;
    }

    @Nullable
    String getFirebaseCloudMessagingProjectId() {
        return service != null ? service.getFirebaseCloudMessagingProjectId() : null;
    }

    @NonNull
    Settings getSettings() {
        return settings;
    }

    @Nullable
    AppUserDto getRemoteUser() {
        return service != null ? service.getAppUserRemote() : null;
    }

    @NonNull
    AppUserDto getLocalUser() {
        return service != null ? service.getAppUserLocal() : pendingLocalUser;
    }

    @Nullable
    String getExternalId() {
        return getRemoteUser() != null ? getRemoteUser().getExternalId() : null;
    }

    @Nullable
    String getJwt() {
        return service != null ? service.getJwt() : null;
    }

    void uploadImage(@NonNull final Message imageMessage, @NonNull final ClarabridgeChatCallback<Message> callback) {
        if (shouldCreateConversation()) {
            createConversationAndSendImage(imageMessage, callback);
        } else {
            addToServiceCallQueue(new Runnable() {
                @Override
                public void run() {
                    service.uploadImage(imageMessage, callback);
                }
            });
        }
    }

    void uploadFile(@NonNull final Message fileMessage, @NonNull final ClarabridgeChatCallback<Message> callback) {
        if (shouldCreateConversation()) {
            createConversationAndSendFile(fileMessage, callback);
        } else {
            addToServiceCallQueue(new Runnable() {
                @Override
                public void run() {
                    service.uploadFile(fileMessage, callback);
                }
            });
        }
    }

    void sendMessage(@NonNull final MessageDto entity) {
        if (shouldCreateConversation()) {
            createConversationAndSendMessage(entity);
        } else {
            addToServiceCallQueue(new Runnable() {
                @Override
                public void run() {
                    service.sendMessage(entity);
                }
            });
        }
    }

    /**
     * If a conversation needs to be started by the SDK.
     *
     * @return true if there's no conversation loaded in {@link #service}, false otherwise
     */
    private boolean shouldCreateConversation() {
        return service != null && service.getConversationId() == null;
    }

    private void createConversationAndSendImage(@NonNull final Message imageMessage,
                                                @NonNull final ClarabridgeChatCallback<Message> uploadCallback) {
        createConversation("message:appUser", null, null, null,
                Collections.<Message>emptyList(), Collections.<String, Object>emptyMap(),
                new ClarabridgeChatCallback<Void>() {
                    @Override
                    public void run(@NonNull Response<Void> response) {
                        if (response.getError() == null) {
                            addToServiceCallQueue(new Runnable() {
                                @Override
                                public void run() {
                                    service.uploadImage(imageMessage, uploadCallback);
                                }
                            });
                        } else {
                            Response<Message> callbackResponse =
                                    new Response.Builder<Message>(response.getStatus())
                                            .withError(response.getError())
                                            .withData(imageMessage)
                                            .build();
                            service.onFileUploadComplete(callbackResponse, null, uploadCallback);
                        }
                    }
                });
    }

    private void createConversationAndSendFile(@NonNull final Message fileMessage,
                                               @NonNull final ClarabridgeChatCallback<Message> uploadCallback) {
        createConversation("message:appUser", null, null, null,
                Collections.<Message>emptyList(), Collections.<String, Object>emptyMap(),
                new ClarabridgeChatCallback<Void>() {
                    @Override
                    public void run(@NonNull Response<Void> response) {
                        if (response.getError() == null) {
                            addToServiceCallQueue(new Runnable() {
                                @Override
                                public void run() {
                                    service.uploadFile(fileMessage, uploadCallback);
                                }
                            });
                        } else {
                            Response<Message> callbackResponse =
                                    new Response.Builder<Message>(response.getStatus())
                                            .withError(response.getError())
                                            .withData(fileMessage)
                                            .build();
                            service.onFileUploadComplete(callbackResponse, null, uploadCallback);
                        }
                    }
                });
    }

    private void createConversationAndSendMessage(@NonNull final MessageDto entity) {
        createConversation("message:appUser", null, null, null,
                Collections.<Message>emptyList(), Collections.<String, Object>emptyMap(),
                new ClarabridgeChatCallback<Void>() {
                    @Override
                    public void run(@NonNull Response<Void> response) {
                        if (response.getError() == null) {
                            addToServiceCallQueue(new Runnable() {
                                @Override
                                public void run() {
                                    service.sendMessage(entity);
                                }
                            });
                        } else {
                            entity.setStatus(MessageDto.Status.SENDING_FAILED);
                            service.onMessageSent(entity);
                        }
                    }
                });
    }

    void createConversation(@NonNull final String intent,
                            @Nullable final String name,
                            @Nullable final String description,
                            @Nullable final String iconUrl,
                            @NonNull final List<Message> messages,
                            @Nullable final Map<String, Object> metadata,
                            @NonNull final ClarabridgeChatCallback<Void> callback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                serviceCallBlocked = true;

                //check if messages contains any non text messages
                boolean containsOnlyTextMessage = JavaUtils.all(messages, new Predicate<Message>() {
                    @Override
                    public boolean apply(Message data) {
                        return MessageType.TEXT.getValue().equals(data.getType());
                    }
                });
                if (!containsOnlyTextMessage) {
                    ClarabridgeChatCallback.Response.Builder<Void> responseBuilder =
                            new ClarabridgeChatCallback.Response.Builder<Void>(HttpURLConnection.HTTP_BAD_REQUEST)
                                    .withError("Invalid MessageType. Only messages of type text are supported.");
                    callback.run(responseBuilder.build());
                    return;
                }

                //map messages to the MessageDto object
                final List<PostConversationMessageDto> messageDtos = new ArrayList<>(messages.size());
                for (Message message : messages) {
                    messageDtos.add(new PostConversationMessageDto(MessageType.TEXT.getValue(), message.getText()));
                }

                ClarabridgeChatCallback<Void> onCreationComplete = new ClarabridgeChatCallback<Void>() {
                    @Override
                    public void run(@NonNull Response<Void> response) {
                        conversation.updateEntity(service.getConversation());
                        serviceCallBlocked = false;
                        callback.run(response);
                    }
                };
                //check if user exists, create user and conversation in one go using createUser
                boolean userExists = getUserId() != null;
                if (!userExists) {
                    service.createUser(intent, name, description, iconUrl, messageDtos, onCreationComplete);
                } else {
                    // user exists, create conversation using createConversation
                    service.createConversation(name, description, iconUrl, messageDtos, metadata, onCreationComplete);
                }
            }
        });
    }

    void loadConversation(@NonNull final String conversationId,
                          @NonNull final ClarabridgeChatCallback<Conversation> callback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                serviceCallBlocked = true;

                service.onClarabridgeChatInitComplete(new Runnable() {
                    @Override
                    public void run() {
                        service.loadConversation(conversationId, new ClarabridgeChatCallback<ConversationDto>() {
                            @Override
                            public void run(@NonNull Response<ConversationDto> response) {
                                serviceCallBlocked = false;
                                Response.Builder<Conversation> responseBuilder =
                                        new Response.Builder<Conversation>(response.getStatus())
                                                .withError(response.getError());

                                if (response.getData() != null) {
                                    responseBuilder.withData(new ConversationProxy(response.getData()));
                                }

                                callback.run(responseBuilder.build());
                            }
                        });
                    }
                });
            }
        });
    }

    void updateConversationById(@NonNull final String conversationId,
                                @Nullable final String name,
                                @Nullable final String description,
                                @Nullable final String iconUrl,
                                @Nullable final Map<String, Object> metadata,
                                @NonNull final ClarabridgeChatCallback<Conversation> callback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                serviceCallBlocked = true;
                service.updateConversationById(conversationId, name, description, iconUrl, metadata,
                        new ClarabridgeChatCallback<ConversationDto>() {
                            @Override
                            public void run(@NonNull Response<ConversationDto> response) {
                                serviceCallBlocked = false;
                                Response.Builder<Conversation> responseBuilder =
                                        new Response.Builder<Conversation>(response.getStatus())
                                                .withError(response.getError());

                                if (response.getData() != null) {
                                    responseBuilder.withData(new ConversationProxy(response.getData()));
                                }

                                callback.run(responseBuilder.build());
                            }
                        });
            }
        });

    }

    void setFirebaseCloudMessagingToken(final String token,
                                        @NonNull final ClarabridgeChatCallback<LoginResult> callback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.setFirebaseCloudMessagingToken(token, callback);
            }
        });

        this.firebaseCloudMessagingToken = StringUtils.emptyIfNull(token);
    }

    void sync() {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.sync();
            }
        });
    }

    void syncAppUser() {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.syncAppUser();
            }
        });
    }

    void login(@NonNull final String externalId,
               @NonNull final String jwt,
               @NonNull final ClarabridgeChatCallback<LoginResult> callback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.isEqual(getExternalId(), externalId) && StringUtils.isEqual(getJwt(), jwt)) {
                    int status = getLastLoginResult() == LoginResult.SUCCESS ? 200 : 400;
                    String error = getLastLoginResult() == LoginResult.SUCCESS
                            ? null
                            : "Login called with same externalId/JWT combination. Ignoring!";

                    ClarabridgeChatCallback.Response<LoginResult> callbackResponse =
                            new ClarabridgeChatCallback.Response.Builder<LoginResult>(status)
                                    .withError(error)
                                    .build();
                    callback.run(callbackResponse);

                    return;
                }

                serviceCallBlocked = true;

                service.onClarabridgeChatInitComplete(new Runnable() {
                    @Override
                    public void run() {
                        service.login(externalId, jwt, new ClarabridgeChatCallback<LoginResult>() {
                            @Override
                            public void run(@NonNull Response<LoginResult> response) {
                                serviceCallBlocked = false;
                                conversation.updateEntity(service.getConversation());
                                callback.run(response);
                            }
                        }, true);
                    }
                });
            }
        });
    }

    void logout(@NonNull final ClarabridgeChatCallback<LogoutResult> callback) {
        if (StringUtils.isEmpty(getExternalId())) {
            ClarabridgeChatCallback.Response<LogoutResult> callbackResponse =
                    new ClarabridgeChatCallback.Response.Builder<LogoutResult>(400)
                            .withError("Logout called but no user was logged in. Ignoring!")
                            .build();
            callback.run(callbackResponse);
            return;
        }

        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                serviceCallBlocked = true;

                service.onClarabridgeChatInitComplete(new Runnable() {
                    @Override
                    public void run() {
                        service.logout(new ClarabridgeChatCallback<LogoutResult>() {
                            @Override
                            public void run(@NonNull Response<LogoutResult> response) {
                                serviceCallBlocked = false;

                                if (response.getError() == null) {
                                    conversation.updateEntity(service.getConversation());
                                }

                                callback.run(response);
                            }
                        });
                    }
                });
            }
        });
    }

    void processPayment(@NonNull final CreditCard creditCard, @NonNull final MessageActionDto action) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.processPayment(creditCard, action);
            }
        });
    }

    void loadCardSummary() {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.loadCardSummary();
            }
        });
    }

    void updateReadState() {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.updateReadState();
            }
        });
    }

    void startTyping() {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.startTyping();
            }
        });
    }

    void stopTyping() {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.stopTyping();
            }
        });
    }

    void postback(@NonNull final MessageActionDto messageActionDto,
                  @NonNull final ClarabridgeChatCallback<Void> callback) {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.postback(messageActionDto, callback);
            }
        });
    }

    void fcmMessageReceived() {
        addToServiceCallQueue(new Runnable() {
            @Override
            public void run() {
                service.onFcmMessageReceived();
            }
        });
    }

    private void addToServiceCallQueue(@NonNull final Runnable clarabridgeChatServiceCall) {
        synchronized (serviceCallQueue) {
            serviceCallQueue.add(clarabridgeChatServiceCall);
        }

        runServiceCallQueue();
    }

    private boolean serviceCallBlocked() {
        return service == null || !service.isRunning() || serviceCallBlocked;
    }

    private void runServiceCallQueue() {
        synchronized (serviceCallQueue) {
            if (serviceCallBlocked()) {
                return;
            }
            while (!serviceCallQueue.isEmpty()) {
                if (serviceCallBlocked()) {
                    break;
                }

                Runnable onServiceConnectedCall = serviceCallQueue.remove(0);
                onServiceConnectedCall.run();
            }
        }
    }

    @Nullable
    ClarabridgeChatConnectionStatus getClarabridgeChatConnectionStatus() {
        return service != null ? service.getClarabridgeChatConnectionStatus() : null;
    }

    @Nullable
    LoginResult getLastLoginResult() {
        return service != null ? service.getLastLoginResult() : null;
    }

    int getRunningActivities() {
        return runningActivities;
    }

    @VisibleForTesting(otherwise = NONE)
    void setService(@Nullable ClarabridgeChatService service) {
        this.service = service;
    }

    //region ActivityLifecycleCallbacks
    @Override
    public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        // Intentionally empty
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        runningActivities++;

        if (service == null || service.isRunning()) {
            return;
        }

        if (getInitializationStatus() == InitializationStatus.UNKNOWN) {
            service.init();
        }
        service.resume();
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        // Intentionally empty
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        // Intentionally empty
    }

    @Override
    public void onActivityStopped(final Activity activity) {
        runningActivities--;

        if (BuildConfig.DEBUG && runningActivities < 0) {
            throw new AssertionError("Expected running activities to be >=0 but was: " + runningActivities);
        }

        if (runningActivities < 0) {
            runningActivities = 0;
        }

        if (runningActivities == 0 && service != null && service.isRunning()) {
            service.pause();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Intentionally empty
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Intentionally empty
    }
    //endregion

    //region ServiceObserver
    @Override
    public void onServiceResumed() {
        runServiceCallQueue();
    }
    //endregion

    //region ClarabridgeChatObserver
    @Override
    public void onLoginComplete(LoginResult result) {
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onLoginComplete(result);
        }
    }

    @Override
    public void onLogoutComplete(LogoutResult result) {
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onLogoutComplete(result);
        }
    }

    @Override
    public void onPaymentProcessed(MessageActionDto messageActionEntity, PaymentStatus status) {
        MessageAction messageAction = new MessageAction(messageActionEntity);
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onPaymentProcessed(messageAction, status);
        }
    }

    @Override
    public void onClarabridgeChatConnectionStatusChanged(ClarabridgeChatConnectionStatus status) {
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onClarabridgeChatConnectionStatusChanged(status);
        }
    }

    @Override
    public void onConversationsListUpdated(@NonNull final List<ConversationDto> conversationEntities) {
        List<Conversation> conversationsList = new ArrayList<>();
        for (ConversationDto entity : conversationEntities) {
            conversationsList.add(new ConversationProxy(entity));
        }
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onConversationsListUpdated(conversationsList);
        }
    }

    @Override
    public void onCardSummaryLoaded(CardSummaryDto cardSummaryEntity) {
        CardSummary cardSummary = new CardSummary(cardSummaryEntity);
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onCardSummaryLoaded(cardSummary);
        }
    }

    @Override
    public void onInitializationStatusChanged(InitializationStatus status) {
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onInitializationStatusChanged(status);
        }
    }

    @Override
    public void onConversationEventReceived(ConversationEventDto conversationEventEntity) {
        ConversationEvent conversationEvent = new ConversationEvent(conversationEventEntity);
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onConversationEventReceived(conversationEvent);
        }
    }

    @Override
    public void onFileUploadComplete(
            @NonNull ClarabridgeChatCallback.Response<Message> response,
            @Nullable MessageDto uploadedMessage,
            @NonNull ClarabridgeChatCallback<Message> callback) {
        int statusCode = response.getStatus();
        boolean isSuccessful = statusCode >= 200 && statusCode < 300;
        Message fileMessage = response.getData();

        if (fileMessage != null && isSuccessful && uploadedMessage != null) {
            fileMessage.getEntity().update(uploadedMessage);
            fileMessage.getEntity().setStatus(MessageDto.Status.SENT);
        } else if (fileMessage != null) {
            fileMessage.getEntity().setStatus(MessageDto.Status.SENDING_FAILED);
        }

        callback.run(response);
    }
    //endregion

    //region ServiceConnection
    @Override
    public void onServiceConnected(final ComponentName name, final IBinder binder) {
        synchronized (INITIALIZATION_LOCK) {
            if (!(binder instanceof ClarabridgeChatServiceBinder)) {
                Log.e(TAG, "Unsupported onServiceConnected call from class name: " + binder.getClass());
                return;
            }

            final ClarabridgeChatServiceBinder clarabridgeChatServiceBinder = (ClarabridgeChatServiceBinder) binder;

            service = clarabridgeChatServiceBinder.getService();
            service.setInitCallback(initCallback);
            service.initializeService(settings, clarabridgeChatComponent);

            AppUserDto serviceUser = service.getAppUserLocal();
            serviceUser.merge(pendingLocalUser);
            serviceUser.setModified(serviceUser.getModified() || pendingLocalUser.getModified());

            if (firebaseCloudMessagingToken != null) {
                String token = firebaseCloudMessagingToken.isEmpty()
                        ? null
                        : firebaseCloudMessagingToken;
                service.setFirebaseCloudMessagingToken(token, new StubbedCallback<LoginResult>());
            }

            if (runningActivities > 0) {
                service.init();
                service.start();
            }

            conversation.updateEntity(service.getConversation());
            service.setClarabridgeChatObserver(this);
            service.setServiceObserver(this);
            service.setConversationObserver(conversation);

            runServiceCallQueue();
        }
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        synchronized (INITIALIZATION_LOCK) {
            service = null;
        }
    }
    //endregion
}

