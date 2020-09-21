package com.clarabridge.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.clarabridge.core.Conversation;
import com.clarabridge.core.ConversationEvent;
import com.clarabridge.core.Coordinates;
import com.clarabridge.core.InitializationStatus;
import com.clarabridge.core.LoginResult;
import com.clarabridge.core.Message;
import com.clarabridge.core.MessageAction;
import com.clarabridge.core.MessageItem;
import com.clarabridge.core.MessageType;
import com.clarabridge.core.MessageUploadStatus;
import com.clarabridge.core.Settings;
import com.clarabridge.core.ClarabridgeChat;
import com.clarabridge.core.ClarabridgeChatCallback;
import com.clarabridge.core.ClarabridgeChatConnectionStatus;
import com.clarabridge.core.utils.FileUtils;
import com.clarabridge.ui.ConnectionStatus;
import com.clarabridge.ui.R;
import com.clarabridge.ui.adapter.MessageListAdapter;
import com.clarabridge.ui.utils.DpVisitor;
import com.clarabridge.ui.widget.BackEventEditText;
import com.clarabridge.ui.widget.EditTextBackListener;
import com.clarabridge.ui.widget.ClarabridgeChatImageView;

public class ConversationFragment extends Fragment implements MessageListAdapter.Delegate {
    private static final int TAKE_PHOTO = 100;
    private static final int PICK_FILE = TAKE_PHOTO + 1;
    private static final int LOCATION_PERMISSIONS_REQUEST = 200;
    private static final int TAKE_PHOTO_PERMISSIONS_REQUEST = LOCATION_PERMISSIONS_REQUEST + 1;
    private static final int PICK_PHOTO_PERMISSIONS_REQUEST = TAKE_PHOTO_PERMISSIONS_REQUEST + 1;
    private static final int PICK_FILE_PERMISSIONS_REQUEST = PICK_PHOTO_PERMISSIONS_REQUEST + 1;

    private static final int BANNER_DURATION = 3000;
    private static final int SEND_BUTTON_ANIMATION_DURATION = 400;
    private static final float SEND_BUTTON_ANIMATION_CIRCLE_MAX_SCALE = 3;
    private static final float SEND_BUTTON_ANIMATION_CIRCLE_MIN_SCALE = 0.7f;
    private static final float SEND_BUTTON_ANIMATION_ROUND_MAX_SCALE = 2;
    private static final String CLARABRIDGECHAT_IMAGE_URL = "CLARABRIDGECHAT_IMG";
    private static final String TAG = "ConversationFragment";
    private static final Object lock = new Object();
    private static boolean running = false;
    private static int pendingNotifications = 0;
    public static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String VIRUS_DETECTED_CODE = "virus_detected";

    private final Runnable scrollToBottomTask = new Runnable() {
        @Override
        public void run() {
            layoutManager.scrollToPosition(messageListAdapter.getItemCount() - 1);
        }
    };

    private final Runnable smoothScrollToBottomTask = new Runnable() {
        @Override
        public void run() {
            layoutManager.smoothScrollToPosition(lstMessages, null, messageListAdapter.getItemCount() - 1);
        }
    };

    private final Runnable smoothScrollToFirstUnreadTask = new Runnable() {
        @Override
        public void run() {
            if (getContext() != null) {
                layoutManager.scrollToPositionWithOffset(
                        messageListAdapter.getItemCount() - messageListAdapter.getUnreadCount(),
                        (int) DpVisitor.toPixels(getContext(), 150)
                );
            }
        }
    };

    private final Runnable removeTypingActivityTask = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                messageListAdapter.removeTypingActivity();
            }
        }
    };

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateConnectionStatus();
        }
    };

    private RecyclerView lstMessages;
    private LinearLayoutManager layoutManager;
    private Parcelable storedInstanceState;
    private BackEventEditText edtText;
    private TextView txtServerBanner;
    private ImageButton btnSend;
    private View btnSendHollow;
    private View btnSendHollowBorder;
    private MessageListAdapter messageListAdapter;
    private Conversation conversation;
    private boolean btnSendEnabled = false;
    private boolean menuEnabled = false;
    private InitializationStatus initializationStatus = InitializationStatus.UNKNOWN;
    private ConnectionStatus connectionStatus = ConnectionStatus.UNKNOWN;
    private ClarabridgeChatConnectionStatus clarabridgeChatConnectionStatus = ClarabridgeChatConnectionStatus.NOT_YET_INITIATED;
    private Handler handler = new Handler();
    private GoogleApiClient googleApiClient;
    private MessageAction pendingLocationAction;
    private Map<BannerState, Runnable> hideBannerRunnables = new HashMap<>();
    private Map<BannerState, String> bannerTexts = new HashMap<>();
    private BannerState currentBannerState = BannerState.ERROR_COULD_NOT_CONNECT;
    private String startingText;

    private enum BannerState {
        NONE(null),
        ERROR_POSTBACK(R.string.ClarabridgeChat_errorPostback),
        ERROR_FILE_TYPE_REJECTED(R.string.ClarabridgeChat_errorFileTypeRejected),
        ERROR_USER_OFFLINE(R.string.ClarabridgeChat_errorUserOffline),
        ERROR_FILE_TOO_LARGE(R.string.ClarabridgeChat_errorFileTooLarge),
        ERROR_VIRUS_DETECTED(R.string.ClarabridgeChat_errorVirusDetected),
        ERROR_COULD_NOT_CONNECT(R.string.ClarabridgeChat_errorCouldNotConnect),
        ;

        private Integer resId;

        BannerState(Integer resId) {
            this.resId = resId;
        }

        public Integer getResId() {
            return resId;
        }
    }

    private final Runnable showConnectionErrorBanner = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) {
                return;
            }

            setServerBanner(BannerState.ERROR_COULD_NOT_CONNECT);
        }
    };

    private void showBanner(final BannerState bannerState) {
        Runnable existingRunnable = hideBannerRunnables.get(bannerState);

        if (existingRunnable != null) {
            handler.removeCallbacks(existingRunnable);
            hideBannerRunnables.remove(bannerState);
        }

        Runnable newRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }

                if (currentBannerState == bannerState) {
                    setServerBanner(BannerState.NONE);
                }

                hideBannerRunnables.remove(bannerState);
            }
        };

        hideBannerRunnables.put(bannerState, newRunnable);
        handler.postDelayed(newRunnable, BANNER_DURATION);

        setServerBanner(bannerState);
    }

    private String currentPhotoPath;

    public static boolean isRunning() {
        synchronized (lock) {
            return running;
        }
    }

    public static int incrementPendingNotifications() {
        synchronized (lock) {
            return ++pendingNotifications;
        }
    }

    private class LocationDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Context context = getContext();

            if (context == null) {
                return;
            }

            Intent i = new Intent();

            i.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.setData(Uri.parse("package:" + context.getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            startActivity(i);
        }
    }

    //================================================================================
    // View overrides
    //================================================================================

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        messageListAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.clarabridgechat_fragment_conversation, container, false);

        messageListAdapter = new MessageListAdapter(this);
        messageListAdapter.setMapsApiKey(ClarabridgeChat.getSettings().getMapsApiKey());
        messageListAdapter.setHoursBetweenTimestamps(
                getResources().getInteger(R.integer.ClarabridgeChat_settings_hoursBetweenTimestamps)
        );
        messageListAdapter.setHeaderViewResourceId(R.layout.clarabridgechat_list_message_header);
        lstMessages = view.findViewById(R.id.scrollView);
        lstMessages.getItemAnimator().setChangeDuration(0L);
        lstMessages.setAdapter(messageListAdapter);
        layoutManager = new LinearLayoutManager(getContext());
        lstMessages.setLayoutManager(layoutManager);
        lstMessages.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                hideKeyboardAndRemoveFocus();
                return false;
            }
        });
        lstMessages.post(scrollToBottomTask);

        lstMessages.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                boolean isKeyboardOpen = bottom < oldBottom;

                if (isKeyboardOpen) {
                    lstMessages.scrollBy(0, oldBottom - bottom);
                }
            }
        });

        edtText = view.findViewById(R.id.ClarabridgeChat_inputText);

        if (startingText != null) {
            edtText.setText(startingText);
        }

        edtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        });
        edtText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    conversation.stopTyping();
                }
            }
        });
        edtText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    handled = true;
                }

                return handled;
            }
        });
        edtText.setEditTextBackListener(new EditTextBackListener() {
            @Override
            public void onEditTextBack(BackEventEditText ctrl, String text) {
                edtText.clearFocus();
            }
        });
        btnSend = view.findViewById(R.id.ClarabridgeChat_btnSend);
        btnSendHollow = view.findViewById(R.id.ClarabridgeChat_btnSendHollow);
        btnSendHollowBorder = view.findViewById(R.id.ClarabridgeChat_btnSendHollowBorder);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (btnSendEnabled) {
                    playSendAnimation();
                    sendMessage();
                }
            }
        });
        btnSend.setAlpha(0.3f);
        hideAnimatedView(btnSendHollow);
        hideAnimatedView(btnSendHollowBorder);

        ImageButton btnMenu = view.findViewById(R.id.ClarabridgeChat_btnMenu);

        if (!shouldShowMenu()) {
            btnMenu.setVisibility(View.GONE);
        } else {
            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (menuEnabled) {
                        promptMenu();
                    }
                }
            });
        }

        txtServerBanner = view.findViewById(R.id.ClarabridgeChat_serverBanner);

        if (Build.VERSION.SDK_INT >= 21) {
            edtText.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        } else {
            lstMessages.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        }

        return view;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .build();
        }

        for (BannerState bannerState : BannerState.values()) {
            if (bannerState.getResId() != null) {
                String bannerString = getString(bannerState.getResId());

                if (bannerState.equals(BannerState.ERROR_FILE_TOO_LARGE)) {
                    bannerString = getString(bannerState.getResId(), "25MB");
                }

                bannerTexts.put(bannerState, bannerString);
            }
        }
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        final NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        super.onResume();

        synchronized (lock) {
            running = true;
            pendingNotifications = 0;
        }

        conversation = ClarabridgeChat.getConversation();

        if (conversation != null) {
            if (lastMessageHasReplies(conversation)) {
                messageListAdapter.setReplies(getLastMessageActions(conversation));
            } else {
                messageListAdapter.removeReplies();
            }
            messageListAdapter.setMessages(getFilteredMessages(conversation.getMessages()));
            messageListAdapter.setUnreadCount(conversation.getUnreadCount());

            conversation.markAllAsRead();

            notificationManager.cancel(getString(R.string.ClarabridgeChat_settings_notificationTag) + "." + conversation.getId(),
                    getResources().getInteger(R.integer.ClarabridgeChat_settings_notificationId));

            if (messageListAdapter.getUnreadCount() > 0) {
                lstMessages.post(smoothScrollToFirstUnreadTask);
            }
        }

        getActivity().registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        updateConnectionStatus();
        updateClarabridgeChatConnectionStatus();
        updateInitializationStatus();
        restoreScroll();
    }

    @Override
    public void onPause() {
        super.onPause();

        removeTypingActivity();

        synchronized (lock) {
            running = false;
        }

        edtText.clearFocus();
        getActivity().unregisterReceiver(connectivityReceiver);
        preserveScroll();
    }

    private void scheduleStopTypingActivityTimer() {
        handler.removeCallbacks(removeTypingActivityTask);
        handler.postDelayed(removeTypingActivityTask, 10000L);
    }

    private void removeTypingActivity() {
        handler.removeCallbacks(removeTypingActivityTask);
        messageListAdapter.removeTypingActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent dataReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, dataReturnedIntent);

        switch (requestCode) {
            case ConversationFragment.PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri fileUri = dataReturnedIntent.getData();
                    onFileSelected(fileUri);
                }
                break;
            case ConversationFragment.TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    onPhotoTaken();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        boolean permissionGranted = true;

        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    final Map<String, Object> messageMetadata = pendingLocationAction != null
                            ? pendingLocationAction.getMetadata() : null;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            sendLocation(messageMetadata);
                        }
                    });
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(R.string.ClarabridgeChat_locationServicesDeniedTitle)
                            .setMessage(R.string.ClarabridgeChat_locationServicesDenied)
                            .setPositiveButton(R.string.ClarabridgeChat_openSettings, new LocationDialogListener());

                    builder.show();
                }
                break;
            case TAKE_PHOTO_PERMISSIONS_REQUEST:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        permissionGranted = false;
                        break;
                    }
                }

                if (permissionGranted) {
                    dispatchTakePhotoIntent();
                }

                break;
            case PICK_PHOTO_PERMISSIONS_REQUEST:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        permissionGranted = false;
                        break;
                    }
                }

                if (permissionGranted) {
                    dispatchPickPhotoIntent();
                }

                break;
            case PICK_FILE_PERMISSIONS_REQUEST:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        permissionGranted = false;
                        break;
                    }
                }

                if (permissionGranted) {
                    dispatchPickFileIntent();
                }

                break;
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        notifyStartActivityCalled(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        notifyStartActivityCalled(intent);
    }

    private void notifyStartActivityCalled(@NonNull Intent intent) {
        if (ClarabridgeChat.getConversationViewDelegate() != null) {
            ClarabridgeChat.getConversationViewDelegate().onStartActivityCalled(new Intent(intent));
        }
    }

    private void requestAndNotifyPermissions(String[] permissions, int requestCode) {
        requestPermissions(permissions, requestCode);
        notifyRequestPermissionsCalled(permissions);
    }

    private void notifyRequestPermissionsCalled(String[] permissions) {
        if (ClarabridgeChat.getConversationViewDelegate() != null) {
            ClarabridgeChat.getConversationViewDelegate().onRequestPermissionsCalled(permissions);
        }
    }

    //================================================================================
    // MessageListAdapter Delegate overrides
    //================================================================================

    @Override
    public void onClick(final Message message) {
        MessageUploadStatus messageUploadStatus = message.getUploadStatus();

        boolean isLocation = MessageType.LOCATION.getValue().equals(message.getType());
        boolean isFailed = messageUploadStatus == MessageUploadStatus.FAILED
                || (isLocation && !message.hasValidCoordinates());

        if (isFailed) {
            if (message.getImage() != null) {
                uploadImage(message, true);
            } else if (message.getFile() != null) {
                uploadFile(message, true);
            } else if (isLocation && !message.hasValidCoordinates()) {
                // Reset the status so that sendMessage doesn't throw
                message.setUploadStatus(MessageUploadStatus.UNSENT);

                messageListAdapter.removeMessage(message);
                conversation.removeMessage(message);
                sendLocation(message.getMetadata());
            } else {
                conversation.retryMessage(message);
            }
        } else {
            hideKeyboardAndRemoveFocus();
        }
    }

    @Override
    public void onActionClick(MessageAction action) {
        if (conversation != null) {
            conversation.triggerAction(action);
        }
    }

    @Override
    public void onFileClick(String url) {
        openUrl(url);
    }

    @Override
    public void onMapClick(Coordinates coordinates) {
        Double latitude = coordinates.getLat();
        Double longitude = coordinates.getLong();
        Uri uri = Uri.parse(String.format("https://maps.google.com/maps?q=%s,%s", latitude, longitude));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onProductOffered() {
        if (conversation != null) {
            conversation.loadCardSummary();
        }
    }

    @Override
    public Long getLastRead() {
        if (conversation == null || conversation.getLastRead() == null) {
            return null;
        }

        return conversation.getLastRead().getTime();
    }

    //================================================================================
    // Public methods
    //================================================================================

    public void onMessagesReceived(final Conversation conversation, final List<Message> messages) {
        int newAppMakerMessagesCount = 0;

        for (Message message : messages) {
            if (!message.isFromCurrentUser()) {
                messageListAdapter.removeTypingActivity();
                newAppMakerMessagesCount++;
            }
        }

        if (messageListAdapter.getUnreadCount() > 0) {
            messageListAdapter.setUnreadCount(messageListAdapter.getUnreadCount() + newAppMakerMessagesCount);
            messageListAdapter.notifyDataSetChanged();
        }

        if (lastMessageHasReplies(conversation)) {
            messageListAdapter.setReplies(getLastMessageActions(conversation));
        } else {
            messageListAdapter.removeReplies();
        }

        messageListAdapter.addMessagesWithAnimation(getFilteredMessages(messages));

        if (messages.size() > 3) {
            lstMessages.post(scrollToBottomTask);
        } else {
            lstMessages.post(smoothScrollToBottomTask);
        }

        conversation.markAllAsRead();
    }

    public void onMessageSent(Message message, MessageUploadStatus status) {
        message = beforeDisplay(message.copy());

        if (status == MessageUploadStatus.FAILED || status == MessageUploadStatus.SENT) {
            messageListAdapter.updateMessage(message);
        }

        lstMessages.post(scrollToBottomTask);
    }

    public void onMessagesReset(final Conversation conversation, final List<Message> messages) {
        if (lastMessageHasReplies(conversation)) {
            messageListAdapter.setReplies(getLastMessageActions(conversation));
        } else {
            messageListAdapter.removeReplies();
        }
        messageListAdapter.setMessages(getFilteredMessages(messages));
        lstMessages.post(scrollToBottomTask);
        conversation.markAllAsRead();
    }

    public void onConversationEventReceived(ConversationEvent conversationEvent) {
        if (!conversationEvent.getConversationId().equals(conversation.getId())) {
            return;
        }

        switch (conversationEvent.getType()) {
            case TYPING_START:
                messageListAdapter.setTypingActivity(conversationEvent);
                lstMessages.post(smoothScrollToBottomTask);
                scheduleStopTypingActivityTimer();
                break;
            case TYPING_STOP:
                removeTypingActivity();
                break;
            case CONVERSATION_READ:
                messageListAdapter.refreshLastMessage();
            default:
                // Intentionally empty
        }
    }

    public void onInitializationStatusChanged(final InitializationStatus status) {
        if (this.initializationStatus != status) {
            this.initializationStatus = status;
            updateConnectionState();
        }
    }

    public void onClarabridgeChatConnectionStatusChanged(final ClarabridgeChatConnectionStatus status) {
        if (clarabridgeChatConnectionStatus != status) {
            clarabridgeChatConnectionStatus = status;
            updateConnectionState();
        }
    }

    public void onPaymentProcessed() {
        messageListAdapter.notifyDataSetChanged();
    }

    public void triggerAction(final MessageAction messageAction) {
        if (messageAction.getType().equals("postback")) {
            messageListAdapter.actionPostbackStart(messageAction);
            conversation.postback(messageAction, onPostbackComplete(messageAction));
        } else if (messageAction.getType().equals("reply")) {
            Message message = new Message(
                    messageAction.getText(),
                    messageAction.getPayload(),
                    messageAction.getMetadata()
            );
            conversation.sendMessage(message);
            addToAdapter(message);
        } else if (messageAction.getType().equals("locationRequest")) {
            pendingLocationAction = messageAction;
            sendLocation(messageAction.getMetadata());
        } else if (messageAction.getType().equals("webview")) {
            View fragmentContainer = getView().findViewById(R.id.webview_fragment_container);

            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTx = manager.beginTransaction();

            fragmentContainer.setVisibility(View.VISIBLE);

            WebviewFragment webviewFragment = new WebviewFragment();
            webviewFragment.setUrl(messageAction.getUri());
            webviewFragment.setSize(messageAction.getSize());

            fragmentTx.add(R.id.webview_fragment_container, webviewFragment, WebviewFragment.FRAGMENT_NAME);
            fragmentTx.addToBackStack(WebviewFragment.FRAGMENT_NAME);
            fragmentTx.commit();
        } else if (messageAction.getType().equals("link")) {
            openUrl(messageAction.getUri());
        } else if (!TextUtils.isEmpty(messageAction.getFallback())) {
            openUrl(messageAction.getFallback());
        } else {
            Toast.makeText(getActivity(), R.string.ClarabridgeChat_unsupportedActionType, Toast.LENGTH_SHORT).show();
        }
    }

    public void openUrl(String uri) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        PackageManager packageManager = getActivity().getApplicationContext().getPackageManager();
        List activities = packageManager.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (isIntentSafe) {
            startActivity(i);
        }
    }

    public void onFileSelected(final Uri uri) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (FileUtils.getLocalAuthority() == null) {
                    FileUtils.setLocalAuthority(getProviderAuthorities());
                }

                String filePath = FileUtils.getPath(getContext(), uri);

                // Path found, file exists at accessible path
                if (filePath != null) {
                    File file = new File(filePath);
                    processFile(file);
                } else {
                    // Path not found, file is consumable via input stream
                    processFileStream(uri);
                }
            }
        });
    }

    public void onPhotoTaken() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                restorePreferences();

                int rotationAngle = getRotationAngle(currentPhotoPath);
                Bitmap resizedImage = smartDecode(currentPhotoPath, rotationAngle);

                if (resizedImage == null) {
                    Toast.makeText(getActivity(), R.string.ClarabridgeChat_problemSavingPhoto, Toast.LENGTH_SHORT).show();
                    return;
                }

                galleryAddPic();
                uploadImage(resizedImage);

                Uri photoUri = new Uri.Builder().encodedPath(currentPhotoPath).build();
                revokeUriPermissions(photoUri);
            }
        });
    }

    public void setStartingText(String startingText) {
        this.startingText = startingText;
    }

    //================================================================================
    // Private methods
    //================================================================================

    private void processFileStream(final Uri uri) {
        final Toast retrievingFileToast = Toast.makeText(
                getContext(),
                getString(R.string.ClarabridgeChat_retrievingFile),
                Toast.LENGTH_SHORT
        );
        retrievingFileToast.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final File file;

                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                    String mimeType = getContext().getContentResolver().getType(uri);

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

                    String fileName = extension != null ? String.format("%s.%s", timeStamp, extension) : timeStamp;
                    file = new File(getContext().getCacheDir(), fileName);

                    int maxBufferSize = 1024 * 1024;
                    int bytesAvailable = inputStream.available();

                    int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    final byte[] buffers = new byte[bufferSize];

                    FileOutputStream outputStream = new FileOutputStream(file);

                    int read;
                    while ((read = inputStream.read(buffers)) != -1) {
                        outputStream.write(buffers, 0, read);
                    }

                    inputStream.close();
                    outputStream.close();
                } catch (Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            retrievingFileToast.cancel();
                            Toast.makeText(
                                    getContext(),
                                    "There was an error processing the file you selected",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

                    return;
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        retrievingFileToast.cancel();
                        processFile(file);
                    }
                });
            }
        }).start();
    }

    private void processFile(File file) {
        if (FileUtils.getMimeType(file).startsWith("image")) {
            String filePath = file.getPath();

            int rotationAngle = getRotationAngle(filePath);
            Bitmap image = smartDecode(filePath, rotationAngle);

            if (image == null) {
                Toast.makeText(getActivity(), R.string.ClarabridgeChat_problemGettingPhoto, Toast.LENGTH_SHORT).show();
                return;
            }

            showPhotoConfirmationDialog(image);
        } else {
            uploadFile(file);
        }
    }

    private int getRotationAngle(String imageLocation) {
        try {
            return getRotationAngle(new ExifInterface(imageLocation));
        } catch (Exception ignored) {
            // Intentionally empty
        }

        return 0;
    }

    private int getRotationAngle(ExifInterface exif) {
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        }

        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        }

        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }

        return 0;
    }

    private Bitmap scaleAndRotate(Bitmap bitmap, int rotationAngle) {
        if (bitmap == null) {
            return null;
        }

        if (rotationAngle != 0) {
            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        return scaleBitmap(bitmap);
    }

    private Bitmap smartDecode(final String fileLocation, int rotationAngle) {
        Bitmap decodedFile = BitmapFactory.decodeFile(fileLocation, getDecodeOptions(fileLocation));
        return scaleAndRotate(decodedFile, rotationAngle);
    }

    private Bitmap scaleBitmap(final Bitmap bitmap) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        final int maxSize = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_imageMaxSize);
        final float scaleFactor = Math.max((float) bitmapWidth / maxSize, (float) bitmapHeight / maxSize);

        return Bitmap.createScaledBitmap(
                bitmap,
                (int) (bitmapWidth / scaleFactor),
                (int) (bitmapHeight / scaleFactor),
                false
        );
    }

    private BitmapFactory.Options getDecodeOptions(String fileLocation) {
        // Get the dimensions of the bitmap
        final int maxSize = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_imageMaxSize);
        final BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        bmOptions.inJustDecodeBounds = true;

        if (fileLocation != null) {
            BitmapFactory.decodeFile(fileLocation, bmOptions);
        } else {
            Log.e(TAG, "No source provided to decode file.");
        }

        int inSampleSize = 1;

        final int halfHeight = bmOptions.outHeight / 2;
        final int halfWidth = bmOptions.outWidth / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > maxSize || (halfWidth / inSampleSize) > maxSize) {
            inSampleSize *= 2;
        }

        // Determine how much to scale down the image
        bmOptions.inSampleSize = inSampleSize;
        bmOptions.inJustDecodeBounds = false;

        return bmOptions;
    }

    private ClarabridgeChatCallback<Void> onPostbackComplete(final MessageAction messageAction) {
        return new ClarabridgeChatCallback<Void>() {
            @Override
            public void run(@NonNull final Response<Void> response) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) {
                            return;
                        }

                        messageListAdapter.actionPostbackEnd(messageAction);

                        if (response.getStatus() < 200 || response.getStatus() >= 300) {
                            if (currentBannerState == BannerState.NONE) {
                                showBanner(BannerState.ERROR_POSTBACK);
                            }
                        }
                    }
                }, 500);
            }
        };
    }

    private void onConnectionStatusChanged(final ConnectionStatus status) {
        if (this.connectionStatus != status) {
            this.connectionStatus = status;
            updateConnectionState();
        }
    }

    private void sendMessage() {
        if (conversation != null) {
            final String text = edtText.getText().toString();

            if (!text.trim().isEmpty()) {
                Message message = new Message(text);

                conversation.sendMessage(message);
                addToAdapter(message);

                edtText.setText("");

                if (messageListAdapter.getUnreadCount() > 0) {
                    messageListAdapter.setUnreadCount(0);
                    messageListAdapter.notifyDataSetChanged();
                }
            }
            handler.post(scrollToBottomTask);
        }
    }

    private void addToAdapter(Message message) {
        message = beforeDisplay(message.copy());

        if (message != null) {
            messageListAdapter.addMessageWithAnimation(message);
            messageListAdapter.removeReplies();
        }
    }

    private void sendLocation(final Map<String, Object> messageMetadata) {
        if (requiresLocationPermission()) {
            requestAndNotifyPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    LOCATION_PERMISSIONS_REQUEST);
            return;
        }

        @SuppressLint("MissingPermission")
        Task<Location> locationTask = LocationServices
                .getFusedLocationProviderClient(getContext()).getLastLocation();
        locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();

                Double latitude = null;
                Double longitude = null;

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }

                final Message message = new Message(new Coordinates(latitude, longitude), messageMetadata);

                if (message.hasValidCoordinates()) {
                    conversation.sendMessage(message);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            addToAdapter(message);
                            scrollToBottomTask.run();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.ClarabridgeChat_locationRetrieveFailed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void playSendAnimation() {
        final int width = btnSend.getWidth();
        final int height = btnSend.getHeight();
        final TimeInterpolator interpolator = new AccelerateInterpolator();

        initAnimatedView(btnSendHollow, width, height);
        initAnimatedView(btnSendHollowBorder, width, height);

        btnSendHollow.animate()
                .scaleX(SEND_BUTTON_ANIMATION_ROUND_MAX_SCALE)
                .scaleY(SEND_BUTTON_ANIMATION_ROUND_MAX_SCALE)
                .alpha(0)
                .setInterpolator(interpolator)
                .setDuration(SEND_BUTTON_ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(final Animator animation) {
                        hideAnimatedView(btnSendHollowBorder);
                        hideAnimatedView(btnSendHollow);
                    }

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        hideAnimatedView(btnSendHollowBorder);
                        hideAnimatedView(btnSendHollow);
                    }
                })
                .start();

        btnSendHollowBorder.animate()
                .scaleX(SEND_BUTTON_ANIMATION_CIRCLE_MAX_SCALE)
                .scaleY(SEND_BUTTON_ANIMATION_CIRCLE_MAX_SCALE)
                .alpha(0)
                .setInterpolator(interpolator)
                .setDuration(SEND_BUTTON_ANIMATION_DURATION)
                .start();
    }

    private void hideAnimatedView(final View view) {
        view.setVisibility(View.INVISIBLE);
        view.setScaleX(1);
        view.setScaleY(1);
    }

    private void initAnimatedView(final View view, final int width, final int height) {
        final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();

        lp.width = width;
        lp.height = height;
        view.setLayoutParams(lp);
        view.setPivotX(width / 2f);
        view.setPivotY(height / 2f);
        view.setScaleX(SEND_BUTTON_ANIMATION_CIRCLE_MIN_SCALE);
        view.setScaleY(SEND_BUTTON_ANIMATION_CIRCLE_MIN_SCALE);
        view.setAlpha(1);
        view.setVisibility(View.VISIBLE);
    }

    private void setServerBanner(BannerState bannerState) {
        if (bannerState != BannerState.NONE) {
            txtServerBanner.setVisibility(View.VISIBLE);
            txtServerBanner.setText(bannerTexts.get(bannerState));
        } else {
            txtServerBanner.setVisibility(View.GONE);
        }

        currentBannerState = bannerState;
    }

    private void setSendButtonEnabled(final boolean enabled) {
        if (btnSendEnabled != enabled) {
            this.btnSendEnabled = enabled;

            if (enabled) {
                btnSend.setEnabled(true);

                btnSendHollow.animate()
                        .alpha(1)
                        .setDuration(SEND_BUTTON_ANIMATION_DURATION)
                        .start();

                btnSendHollowBorder.animate()
                        .alpha(1)
                        .setDuration(SEND_BUTTON_ANIMATION_DURATION)
                        .start();

                btnSend.animate()
                        .alpha(1)
                        .setDuration(SEND_BUTTON_ANIMATION_DURATION)
                        .start();
            } else {
                btnSend.setEnabled(false);

                btnSendHollow.animate()
                        .alpha(0.3f)
                        .setDuration(SEND_BUTTON_ANIMATION_DURATION)
                        .start();

                btnSendHollowBorder.animate()
                        .alpha(0.3f)
                        .setDuration(SEND_BUTTON_ANIMATION_DURATION)
                        .start();

                btnSend.animate()
                        .alpha(0.3f)
                        .setDuration(SEND_BUTTON_ANIMATION_DURATION)
                        .start();
            }
        }
    }

    private void updateConnectionStatus() {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetInfo == null || !activeNetInfo.isConnected() || !activeNetInfo.isAvailable()) {
                onConnectionStatusChanged(ConnectionStatus.DISCONNECTED);
            } else {
                onConnectionStatusChanged(ConnectionStatus.CONNECTED);
            }
        }
    }

    private void updateClarabridgeChatConnectionStatus() {
        this.clarabridgeChatConnectionStatus = ClarabridgeChat.getClarabridgeChatConnectionStatus();
        updateConnectionState();
    }

    private void updateInitializationStatus() {
        this.initializationStatus = ClarabridgeChat.getInitializationStatus();
        updateConnectionState();
    }

    private void updateSendButtonState() {
        final String text = edtText.getText().toString().trim();

        boolean shouldEnableButton = !text.isEmpty()
                && initializationStatus == InitializationStatus.SUCCESS
                && connectionStatus == ConnectionStatus.CONNECTED
                && ClarabridgeChat.getLastLoginResult() != LoginResult.ERROR;

        setSendButtonEnabled(shouldEnableButton);

        if (shouldEnableButton) {
            if (startingText != null) {
                startingText = null;
                return;
            }

            conversation.startTyping();
        }
    }

    private void updateMenuState() {
        menuEnabled = initializationStatus == InitializationStatus.SUCCESS
                && connectionStatus == ConnectionStatus.CONNECTED
                && ClarabridgeChat.getLastLoginResult() != LoginResult.ERROR;
    }

    private boolean shouldShowMenu() {
        return shouldShowTakePhoto() || shouldShowUploadFile() || shouldShowChooseMedia() || shouldShowShareLocation();
    }

    private boolean shouldShowTakePhoto() {
        return shouldShowMenuItem(getString(R.string.ClarabridgeChat_settings_takePhotoMenuKey));
    }

    private boolean shouldShowUploadFile() {
        return shouldShowMenuItem(getString(R.string.ClarabridgeChat_settings_uploadFileMenuKey));
    }

    private boolean shouldShowChooseMedia() {
        return shouldShowMenuItem(getString(R.string.ClarabridgeChat_settings_chooseMediaMenuKey));
    }

    private boolean shouldShowShareLocation() {
        return shouldShowMenuItem(getString(R.string.ClarabridgeChat_settings_shareLocationMenuKey));
    }

    private boolean shouldShowMenuItem(String itemKey) {
        String[] menuItems = getResources().getStringArray(R.array.ClarabridgeChat_settings_showMenuOptions);

        for (String item : menuItems) {
            if (itemKey.equals(item)) {
                return true;
            }
        }

        return false;
    }

    private void promptMenu() {
        boolean hasCamera = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        final List<CharSequence> options = new LinkedList<>();

        final String takePhoto = getString(R.string.ClarabridgeChat_takePhoto);
        final String chooseFromLibrary = getString(R.string.ClarabridgeChat_chooseFromLibrary);
        final String chooseFile = getString(R.string.ClarabridgeChat_chooseFile);
        final String shareLocation = getString(R.string.ClarabridgeChat_shareLocation);

        if (shouldShowTakePhoto() && hasCamera) {
            options.add(takePhoto);
        }

        if (shouldShowChooseMedia()) {
            options.add(chooseFromLibrary);
        }

        if (shouldShowUploadFile()) {
            options.add(chooseFile);
        }

        if (shouldShowShareLocation()) {
            options.add(shareLocation);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.ClarabridgeChat_chooseOption)
                .setItems(options.toArray(new CharSequence[options.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final CharSequence selectedOption = options.get(which);

                        if (selectedOption.equals(takePhoto)) {
                            dispatchTakePhotoIntent();
                        } else if (selectedOption.equals(chooseFromLibrary)) {
                            dispatchPickPhotoIntent();
                        } else if (selectedOption.equals(chooseFile)) {
                            dispatchPickFileIntent();
                        } else if (selectedOption.equals(shareLocation)) {
                            sendLocation(null);
                        }

                        hideKeyboardAndRemoveFocus();
                    }
                });

        builder.show();
    }

    private boolean requiresCameraPermission() {
        return hasPermissionInManifest(Manifest.permission.CAMERA)
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean requiresWritePermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean requiresReadPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean requiresLocationPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasPermissionInManifest(final String permissionName) {
        final String packageName = getContext().getPackageName();

        try {
            final PackageInfo packageInfo = getContext().getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            final String[] declaredPermissions = packageInfo.requestedPermissions;

            if (declaredPermissions != null && declaredPermissions.length > 0) {
                for (final String p : declaredPermissions) {
                    if (p.equals(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            // Intentionally empty
        }

        return false;
    }

    private void grantUriPermissions(Intent intent, Uri uri) {
        if (getContext() == null) {
            return;
        }

        List<ResolveInfo> resolvedIntentActivities = getContext().getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;

            int flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContext().grantUriPermission(packageName, uri, flags);
        }
    }

    private void revokeUriPermissions(Uri uri) {
        if (getContext() == null) {
            return;
        }

        int flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION;
        getContext().revokeUriPermission(uri, flags);
    }

    private void dispatchTakePhotoIntent() {
        boolean requiresCameraPermission = requiresCameraPermission();
        boolean requiresWritePermission = requiresWritePermission();

        if (requiresCameraPermission || requiresWritePermission) {
            List<String> requiredPermissions = new LinkedList<>();

            if (requiresCameraPermission) {
                requiredPermissions.add(Manifest.permission.CAMERA);
            }

            if (requiresWritePermission) {
                requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            requestAndNotifyPermissions(
                    requiredPermissions.toArray(new String[requiredPermissions.size()]),
                    TAKE_PHOTO_PERMISSIONS_REQUEST
            );

            return;
        }

        final Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            final File imageFile;

            try {
                imageFile = createImageFile();
            } catch (IOException e) {
                Log.e(TAG, "There was a problem saving the file.");
                Toast.makeText(getActivity(), R.string.ClarabridgeChat_problemLaunchingCamera, Toast.LENGTH_SHORT).show();

                return;
            }

            String clarabridgeChatProviderAuthorities = getProviderAuthorities();
            Uri photoUri;

            try {
                photoUri = FileProvider.getUriForFile(getActivity(), clarabridgeChatProviderAuthorities, imageFile);
            } catch (Exception e) {
                Log.e(TAG, "There was a problem accessing the file. Please ensure your " +
                        "FileProviderAuthorities have been correctly set.");
                Toast.makeText(getActivity(), R.string.ClarabridgeChat_problemLaunchingCamera, Toast.LENGTH_SHORT).show();

                return;
            }

            savePreferences();

            grantUriPermissions(takePhotoIntent, photoUri);

            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePhotoIntent, TAKE_PHOTO);
        }
    }

    private String getProviderAuthorities() {
        final Settings settings = ClarabridgeChat.getSettings();

        String clarabridgeChatProviderAuthorities = null;

        if (settings != null) {
            clarabridgeChatProviderAuthorities = settings.getFileProviderAuthorities();
        }

        if (clarabridgeChatProviderAuthorities == null) {
            clarabridgeChatProviderAuthorities = getActivity().getPackageName() + "." +
                    getString(R.string.ClarabridgeChat_settings_fileProvider);
        }

        return clarabridgeChatProviderAuthorities;
    }

    private void dispatchPickPhotoIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (requiresReadPermission()) {
                requestAndNotifyPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PICK_PHOTO_PERMISSIONS_REQUEST
                );
                return;
            }
        }

        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");

        if (photoPickerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(photoPickerIntent, PICK_FILE);
        }
    }

    private void dispatchPickFileIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (requiresReadPermission()) {
                requestAndNotifyPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PICK_FILE_PERMISSIONS_REQUEST
                );
                return;
            }
        }

        final Intent filePickerIntent;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            filePickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        filePickerIntent.setType("*/*");
        filePickerIntent.addCategory(Intent.CATEGORY_OPENABLE);

        if (filePickerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(filePickerIntent, PICK_FILE);
        }
    }

    private void showPhotoConfirmationDialog(final Bitmap image) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.clarabridgechat_dialog_confirm_photo, null);
        ClarabridgeChatImageView imageView = dialogView.findViewById(R.id.clarabridgechat_confirm_photo_view);
        imageView.setImageBitmap(image);
        imageView.setRoundedCorners(
                ClarabridgeChatImageView.ROUNDED_CORNER_TOP_LEFT
                        | ClarabridgeChatImageView.ROUNDED_CORNER_TOP_RIGHT
                        | ClarabridgeChatImageView.ROUNDED_CORNER_BOTTOM_LEFT
                        | ClarabridgeChatImageView.ROUNDED_CORNER_BOTTOM_RIGHT
        );

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.ClarabridgeChat_sendPhoto)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ClarabridgeChat_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (conversation != null) {
                            uploadImage(image);
                        }
                    }
                }).show();
    }

    private void galleryAddPic() {
        restorePreferences();

        final Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final File imageFile = new File(currentPhotoPath);
        final Uri contentUri = Uri.fromFile(imageFile);

        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void uploadImage(final Bitmap image) {
        final Message message = new Message(image);
        uploadImage(message, false);
    }

    private void uploadImage(final Message message, boolean isRetry) {
        if (conversation != null) {
            fileUploadStart(message, isRetry);

            conversation.uploadImage(message, new ClarabridgeChatCallback<Message>() {
                @Override
                public void run(@NonNull final Response<Message> response) {
                    fileUploadEnd(message, response);
                    lstMessages.post(scrollToBottomTask);
                }
            });
        }
    }

    private void uploadFile(final File file) {
        final Message message = new Message(file);
        uploadFile(message, false);
    }

    private void uploadFile(final Message message, boolean isRetry) {
        if (conversation != null) {
            fileUploadStart(message, isRetry);

            conversation.uploadFile(message, new ClarabridgeChatCallback<Message>() {
                @Override
                public void run(@NonNull final Response<Message> response) {
                    fileUploadEnd(message, response);
                    lstMessages.post(scrollToBottomTask);
                }
            });
        }
    }

    private void fileUploadStart(final Message message, final boolean isRetry) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRetry) {
                    messageListAdapter.uploadRetry(message);
                } else {
                    conversation.addMessage(message);
                    messageListAdapter.uploadStart(message);
                    messageListAdapter.removeReplies();
                }
            }
        });

        handler.post(scrollToBottomTask);
    }

    private void fileUploadEnd(Message message, ClarabridgeChatCallback.Response<Message> response) {
        if (response.getStatus() == 413) {
            conversation.removeMessage(message);
            messageListAdapter.removeMessage(message);

            showBanner(BannerState.ERROR_FILE_TOO_LARGE);
        } else if (response.getStatus() == 415) {
            conversation.removeMessage(message);
            messageListAdapter.removeMessage(message);

            showBanner(BannerState.ERROR_FILE_TYPE_REJECTED);
        } else if (response.getStatus() == 400 && VIRUS_DETECTED_CODE.equals(response.getError())) {
            conversation.removeMessage(message);
            messageListAdapter.removeMessage(message);

            showBanner(BannerState.ERROR_VIRUS_DETECTED);
        } else {
            messageListAdapter.uploadEnd(message, response);
        }
    }

    private File createImageFile() throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        final String imageFileName = CLARABRIDGECHAT_IMAGE_URL + "_" + timeStamp;
        final File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        final File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void savePreferences() {
        final SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("currentPhotoPath", currentPhotoPath);

        editor.apply();
    }

    private void restorePreferences() {
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        currentPhotoPath = settings.getString("currentPhotoPath", "");
    }

    private void hideKeyboardAndRemoveFocus() {
        final InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        edtText.clearFocus();
        imm.hideSoftInputFromWindow(edtText.getWindowToken(), 0);
    }

    private void updateConnectionState() {
        handler.removeCallbacks(showConnectionErrorBanner);

        if (connectionStatus != ConnectionStatus.CONNECTED) {
            setServerBanner(BannerState.ERROR_USER_OFFLINE);
        } else if (initializationStatus != InitializationStatus.SUCCESS
                || clarabridgeChatConnectionStatus == ClarabridgeChatConnectionStatus.DISCONNECTED
                || ClarabridgeChat.getLastLoginResult() == LoginResult.ERROR) {
            handler.postDelayed(showConnectionErrorBanner, 1000);
        } else {
            boolean connectErrorShown = currentBannerState == BannerState.ERROR_COULD_NOT_CONNECT
                    || currentBannerState == BannerState.ERROR_USER_OFFLINE;

            if (connectErrorShown) {
                setServerBanner(BannerState.NONE);
            }
        }

        updateSendButtonState();
        updateMenuState();
    }

    private List<Message> getFilteredMessages(List<Message> messages) {
        List<Message> filteredMessages = new ArrayList<>();
        for (Message message : messages) {
            message = beforeDisplay(message.copy());

            if (message != null) {
                filteredMessages.add(message);
            }
        }
        return filteredMessages;
    }

    private Message beforeDisplay(Message message) {
        boolean hasText = message.getText() != null && !message.getText().trim().isEmpty();
        boolean hasTextFallback = !hasText
                && message.getTextFallback() != null
                && !message.getTextFallback().trim().isEmpty();
        boolean hasFile = message.getMediaUrl() != null
                && !message.getMediaUrl().trim().isEmpty()
                || message.getImage() != null
                || message.getFile() != null;
        boolean hasNonReplyActions = !message.getMessageActions().isEmpty()
                && !message.hasReplies()
                && !message.hasLocationRequest();
        boolean isLocation = MessageType.LOCATION.getValue().equals(message.getType());
        boolean shouldDisplay = hasNonReplyActions || hasText || hasFile || isLocation || hasTextFallback;

        if (ClarabridgeChat.getMessageModifierDelegate() != null) {
            message = ClarabridgeChat.getMessageModifierDelegate().beforeDisplay(conversation, message);
        }

        if (!shouldDisplay || message == null) {
            return null;
        }

        filterMessageActionsWithoutText(message);

        return message;
    }

    private void filterMessageActionsWithoutText(Message message) {
        if (message.getMessageActions() != null && !message.getMessageActions().isEmpty()) {

            for (int i = message.getMessageActions().size() - 1; i >= 0; i--) {
                MessageAction messageAction = message.getMessageActions().get(i);

                if (TextUtils.isEmpty(messageAction.getText())) {
                    message.removeMessageAction(messageAction);
                }
            }
        }

        if (message.getMessageItems() != null && !message.getMessageItems().isEmpty()) {
            for (MessageItem messageItem : message.getMessageItems()) {
                filterMessageItemActionsWithoutText(messageItem);
            }
        }
    }

    private void filterMessageItemActionsWithoutText(MessageItem messageItem) {
        if (messageItem.getMessageActions() != null && !messageItem.getMessageActions().isEmpty()) {

            for (int i = messageItem.getMessageActions().size() - 1; i >= 0; i--) {
                MessageAction messageAction = messageItem.getMessageActions().get(i);

                if (TextUtils.isEmpty(messageAction.getText())) {
                    messageItem.removeMessageAction(messageAction);
                }
            }
        }
    }

    private boolean lastMessageHasReplies(@NonNull Conversation conversation) {
        List<Message> messages = conversation.getMessages();
        if (messages == null || messages.isEmpty()) {
            return false;
        }

        Message lastMessage = messages.get(messages.size() - 1);

        return lastMessage.hasReplies() || lastMessage.hasLocationRequest();
    }

    private List<MessageAction> getLastMessageActions(@NonNull Conversation conversation) {
        List<Message> messages = conversation.getMessages();
        Message lastMessage = messages.get(messages.size() - 1);

        return lastMessage.getMessageActions();
    }

    public void preserveScroll() {
        // save RecyclerView state
        storedInstanceState = layoutManager.onSaveInstanceState();
    }

    public void restoreScroll() {
        // restore RecyclerView state
        if (storedInstanceState != null) {
            layoutManager.onRestoreInstanceState(storedInstanceState);
        }
    }
}
