package com.clarabridge.ui.builder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.ActionState;
import com.clarabridge.core.MessageAction;
import com.clarabridge.core.MessageItem;
import com.clarabridge.ui.R;
import com.clarabridge.ui.adapter.CarouselAdapter;
import com.clarabridge.ui.widget.MessageView;
import com.clarabridge.ui.widget.ClarabridgeChatButton;

import static com.clarabridge.ui.widget.MessageView.ImageSize.LARGE;
import static com.clarabridge.ui.widget.MessageView.ImageSize.SMALL;

public class MessageViewBuilder {
    private static int imagesCurrentlyLoading = 0;
    // Map of carousel heights for a given screen breakpoint
    private static Map<String, HashMap<String, Integer>> carouselCacheMap = new HashMap<>();

    public static List<MessageAction> postbacksInProgress = new ArrayList<>();
    public static LruCache<String, Bitmap> bitmapCache = new LruCache<>(10);

    private static final int IMAGE_AUTOLOAD_LIMIT = 1024 * 1024 * 2; // 2MB

    public static void build(@NonNull final MessageViewModel messageViewModel, final MessageView messageView) {
        boolean hasActions;

        List<MessageAction> messageActions;
        TypedValue lineSpacing = new TypedValue();
        Resources resources = messageView.getResources();
        MessageViewModel.ViewStyle viewStyle = messageViewModel.getViewStyle();
        MessageViewModel.ViewStatus viewStatus = messageViewModel.getViewStatus();
        MessageViewModel.ImageStyle imageStyle = messageViewModel.getImageStyle();

        messageActions = messageViewModel.getMessageActions();
        hasActions = messageActions.size() >= 1;

        int sizeDiff = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_btnActionSizeDiff);

        int btnActionMarginVertical = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_btnActionMarginVertical);
        int btnActionPaddingVertical = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_btnActionPaddingVertical);
        int btnActionPaddingHorizontal = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_btnActionPaddingHorizontal);

        int messagePaddingTop = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingTop);
        int messagePaddingBottom = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingBottom);
        int messagePaddingHorizontal = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingHorizontal);

        MessageView.ImageSize imageSize;
        MessageView.LayoutStyle layoutStyle;

        if (viewStyle == MessageViewModel.ViewStyle.ITEM && imageStyle == MessageViewModel.ImageStyle.SQUARE) {
            imageSize = LARGE;
        } else {
            imageSize = SMALL;
        }

        if (viewStyle == MessageViewModel.ViewStyle.ITEM) {
            layoutStyle = MessageView.LayoutStyle.FIXED;
        } else {
            layoutStyle = MessageView.LayoutStyle.RELATIVE;
        }

        messageView.reset(imageSize, layoutStyle);
        messageView.setVisibility(View.VISIBLE);
        messageView.setBackgroundResource(R.drawable.clarabridgechat_bg_message);

        switch (messageViewModel.getViewType()) {
            case COMPOUND: {
                // Render order is important
                // 1. Text - can sometimes affect width of action buttons
                List<TextView> textViews = new LinkedList<>();

                if (messageViewModel.hasMainText()) {
                    SpannableString textToUse = new SpannableString(messageViewModel.getMainText());
                    Linkify.addLinks(textToUse, Linkify.ALL);
                    messageView.setMainText(textToUse);

                    TextView textView = messageView.getMainTextView();

                    if (viewStyle == MessageViewModel.ViewStyle.MESSAGE) {
                        textView.setTypeface(null, Typeface.NORMAL);
                        textView.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX,
                                resources.getDimension(R.dimen.ClarabridgeChat_messageText)
                        );
                    } else {
                        textView.setTypeface(null, Typeface.BOLD);
                        textView.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX,
                                resources.getDimension(R.dimen.ClarabridgeChat_titleText)
                        );
                    }

                    textView.setTextColor(
                            resources.getColor(
                                    messageViewModel.isRemote()
                                            ? R.color.ClarabridgeChat_remoteMessageText
                                            : R.color.ClarabridgeChat_userMessageText
                            )
                    );
                    textViews.add(textView);
                } else {
                    messageView.getMainTextView().setVisibility(View.GONE);
                }

                if (messageViewModel.hasSubText()) {
                    SpannableString textToUse = new SpannableString(messageViewModel.getSubText());
                    Linkify.addLinks(textToUse, Linkify.ALL);
                    messageView.setSubText(textToUse);

                    TextView textView = messageView.getSubTextView();

                    textView.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            resources.getDimension(R.dimen.ClarabridgeChat_descriptionText)
                    );
                    textView.setTextColor(resources.getColor(R.color.ClarabridgeChat_descriptionText));
                    textViews.add(textView);
                } else {
                    messageView.getSubTextView().setVisibility(View.GONE);
                }

                for (TextView textView : textViews) {
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setLinkTextColor(
                            resources.getColor(messageViewModel.isRemote()
                                    ? R.color.ClarabridgeChat_accent
                                    : R.color.ClarabridgeChat_userMessageText
                            )
                    );

                    resources.getValue(R.dimen.ClarabridgeChat_lineSpacingMultiplier, lineSpacing, true);
                    textView.setLineSpacing(0, lineSpacing.getFloat());

                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
                    params.setMargins(messagePaddingHorizontal,
                            messagePaddingTop,
                            messagePaddingHorizontal,
                            hasActions ? 0 : messagePaddingBottom);

                    textView.setLayoutParams(params);
                }

                // 2. Actions - if default action is present, affects click for images
                MessageAction defaultAction = null;
                View.OnClickListener defaultActionClick = null;
                int maxMessageSize = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageMaxSize);

                if (hasActions) {
                    int buttonWidth;

                    ClarabridgeChatButton longestButton = new ClarabridgeChatButton(messageView.getContext());

                    // get longest button for simpler width calculation
                    for (MessageAction action : messageActions) {
                        String actionText = (action.getState() != null
                                && action.getState().equals(ActionState.PAID.getValue()))
                                ? resources.getString(R.string.ClarabridgeChat_btnPaymentCompleted) : action.getText();

                        String longestButtonText = longestButton.getText();

                        if (actionText.length() > longestButtonText.length()) {
                            longestButton.setText(actionText);
                        }
                    }

                    longestButton.measure(
                            View.MeasureSpec.makeMeasureSpec(
                                    maxMessageSize - sizeDiff,
                                    View.MeasureSpec.AT_MOST
                            ),
                            0
                    );
                    buttonWidth = longestButton.getMeasuredWidth();

                    for (int messageActionIndex = 0; messageActionIndex < messageActions.size(); messageActionIndex++) {
                        MessageAction messageAction = messageActions.get(messageActionIndex);
                        ClarabridgeChatButton clarabridgeChatButton = messageView.addClarabridgeChatButton(messageAction);

                        if (defaultAction == null && messageAction.isDefault()) {
                            defaultAction = messageAction;
                        }

                        clarabridgeChatButton.setTextColor(resources.getColor(R.color.ClarabridgeChat_userMessageText));
                        clarabridgeChatButton.setAllCaps(false);
                        clarabridgeChatButton.setPadding(btnActionPaddingHorizontal,
                                btnActionPaddingVertical,
                                btnActionPaddingHorizontal,
                                btnActionPaddingVertical);

                        clarabridgeChatButton.setMinHeight((int) resources.getDimension(R.dimen.ClarabridgeChat_btnActionHeight));
                        clarabridgeChatButton.setMinimumHeight((int) resources.getDimension(R.dimen.ClarabridgeChat_btnActionHeight));
                        clarabridgeChatButton.setMaxWidth(maxMessageSize);

                        if (Build.VERSION.SDK_INT >= 21) {
                            clarabridgeChatButton.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                            clarabridgeChatButton.setBackgroundResource(R.drawable.clarabridgechat_btn_action_ripple);
                            clarabridgeChatButton.setStateListAnimator(null);
                            clarabridgeChatButton.setElevation(resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_btnElevation));
                        } else {
                            clarabridgeChatButton.setBackgroundResource(R.drawable.clarabridgechat_btn_action);
                        }

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        params.gravity = Gravity.CENTER;

                        boolean isFirstActionIndex = messageActionIndex == 0;
                        boolean isLastActionIndex = messageActionIndex + 1 == messageActions.size();

                        int topMargin = isFirstActionIndex
                                ? (btnActionMarginVertical + messagePaddingTop)
                                : btnActionMarginVertical;
                        int bottomMargin = isLastActionIndex
                                ? btnActionMarginVertical + messagePaddingTop
                                : btnActionMarginVertical;

                        //noinspection ResourceType
                        params.setMargins(messagePaddingHorizontal, topMargin, messagePaddingHorizontal, bottomMargin);

                        if (messageAction.getState() != null
                                && messageAction.getState().equals(ActionState.PAID.getValue())) {
                            clarabridgeChatButton.setText(R.string.ClarabridgeChat_btnPaymentCompleted);
                            clarabridgeChatButton.setEnabled(false);
                            clarabridgeChatButton.setTextColor(resources.getColor(R.color.ClarabridgeChat_btnActionButtonPressed));
                            clarabridgeChatButton.setBackgroundResource(R.drawable.clarabridgechat_btn_action_disabled);
                        }

                        if (postbacksInProgress.contains(messageAction)) {
                            clarabridgeChatButton.showLoadingSpinner();
                        } else {
                            clarabridgeChatButton.hideLoadingSpinner();
                        }

                        if (viewStyle == MessageViewModel.ViewStyle.ITEM) {
                            clarabridgeChatButton.setWidth(maxMessageSize);
                        } else if (messageViewModel.hasImage()) {
                            clarabridgeChatButton.setWidth(maxMessageSize - sizeDiff);
                        } else if (messageView.hasText()) {
                            int measuredTextWidth;

                            messageView.getMainTextView().measure(
                                    View.MeasureSpec.makeMeasureSpec(
                                            maxMessageSize,
                                            View.MeasureSpec.AT_MOST
                                    ),
                                    0);
                            measuredTextWidth = messageView.getMainTextView().getMeasuredWidth();

                            if (buttonWidth > (measuredTextWidth - sizeDiff)) {
                                messageView.getMainTextView().setWidth(
                                        Math.min(buttonWidth + sizeDiff, maxMessageSize)
                                );

                                clarabridgeChatButton.setWidth(Math.min(buttonWidth, maxMessageSize - sizeDiff));
                            } else {
                                clarabridgeChatButton.setWidth(measuredTextWidth - sizeDiff);
                            }
                        } else {
                            clarabridgeChatButton.setWidth(Math.min(buttonWidth, maxMessageSize - sizeDiff));
                        }

                        if (viewStyle == MessageViewModel.ViewStyle.ITEM) {
                            params.setMargins(
                                    messagePaddingHorizontal,
                                    isFirstActionIndex ? topMargin : 0,
                                    messagePaddingHorizontal,
                                    0
                            );

                            clarabridgeChatButton.setSpinnerColor(resources.getColor(R.color.ClarabridgeChat_accent));
                            clarabridgeChatButton.setBackgroundResource(R.drawable.background_transparent_border_top);

                            if (messageAction.getState() == null
                                    || !messageAction.getState().equals(ActionState.PAID.getValue())) {
                                clarabridgeChatButton.setTextColor(resources.getColor(R.color.ClarabridgeChat_accent));
                            }
                        }

                        clarabridgeChatButton.setLayoutParams(params);
                    }

                    if (defaultAction != null) {
                        final MessageAction finalDefaultAction = defaultAction;

                        defaultActionClick = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                messageView.delegate.onActionClick(finalDefaultAction);
                            }
                        };

                        messageView.setOnClickListener(defaultActionClick);
                    }
                } else if (viewStyle == MessageViewModel.ViewStyle.ITEM) {
                    messageView.getMainTextView().setWidth(maxMessageSize);
                }

                // 3. Image - display the image
                if (messageViewModel.hasImage()) {
                    if (messageViewModel.getImage() != null) {
                        Bitmap imageToUse = messageViewModel.getImage();

                        messageView.setImage(imageToUse);

                        if (viewStatus == MessageViewModel.ViewStatus.FAILED) {
                            messageView.showFailedImageState();
                        } else {
                            messageView.showInProgressImageState();
                        }
                    } else {
                        String imageUrl = messageViewModel.getImageUrl();
                        Bitmap cachedBitmap = bitmapCache.get(imageUrl);

                        if (cachedBitmap != null) {
                            messageView.setImage(cachedBitmap);

                            if (defaultAction == null) {
                                messageView.prepareImagePreview(imageUrl);
                            }
                        } else {
                            setImageFromMediaUrl(
                                    messageView,
                                    messageViewModel,
                                    imageUrl,
                                    defaultAction != null ? defaultActionClick : null
                            );
                        }
                    }
                }

                // 4. File - display the file
                if (messageViewModel.hasFile()) {
                    long fileSize = messageViewModel.getMediaSize();

                    if (messageViewModel.getFile() != null) {
                        File file = messageViewModel.getFile();
                        messageView.setFile(file, fileSize, messageViewModel.isRemote());

                        if (viewStatus == MessageViewModel.ViewStatus.FAILED) {
                            messageView.setFileUploadFailed();
                        } else {
                            messageView.setFileUploadInProgress();
                        }
                    } else {
                        final String fileUrl = messageViewModel.getFileUrl();
                        messageView.setFileByUrl(fileUrl, fileSize, messageViewModel.isRemote());
                    }
                }

                break;
            }

            case LOCATION: {
                int width = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_imageDisplayWidth) / 3;
                // Add a bit of margin to fit Google logo in rounded corner
                int height = (int) ((resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_imageDisplayHeight) / 3) * 1.03);
                double latitude = messageViewModel.getCoordinates().getLat();
                double longitude = messageViewModel.getCoordinates().getLong();

                String mapLink = "https://maps.googleapis.com/maps/api/staticmap?" +
                        "size=" + width + "x" + height +
                        "&maptype=roadmap" +
                        "&zoom=15" +
                        "&markers=size:large%7Ccolor:red%7C" + latitude + "," + longitude +
                        "&key=" + messageViewModel.getMapsApiKey();

                setImageFromMediaUrl(
                        messageView,
                        messageViewModel,
                        mapLink,
                        viewStatus != MessageViewModel.ViewStatus.SENT ? null : new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                messageView.delegate.onMapClick(messageViewModel.getCoordinates());
                            }
                        });

                if (viewStatus == MessageViewModel.ViewStatus.SENDING) {
                    messageView.showInProgressImageState();
                } else if (viewStatus == MessageViewModel.ViewStatus.FAILED) {
                    messageView.showFailedImageState();
                }

                break;
            }

            case CAROUSEL: {
                CarouselAdapter adapter = messageView.showCarousel(messageViewModel.getMessageId());

                adapter.setImageStyle(messageViewModel.getImageStyle());
                adapter.setAvatarUrl(messageViewModel.getAvatarUrl());
                adapter.setShouldShowAvatar(messageViewModel.shouldShowAvatar());
                adapter.setMessageItems(messageViewModel.getMessageId(), messageViewModel.getMessageItems());

                messageView.setBackgroundResource(R.drawable.background_transparent);

                break;
            }

            case TYPING_INDICATOR:
                messageView.showTypingActivity();

                break;
        }

        messageView.mutateDrawable(
                messageViewModel.getMessageRoundedCorners(),
                messageViewModel.isRemote(),
                viewStatus != MessageViewModel.ViewStatus.SENT
        );
        messageView.updateImageCorners(messageViewModel.getMessageRoundedCorners());

        String widthBreakpoint = resources.getString(R.string.ClarabridgeChat_widthBreakpoint);

        HashMap<String, Integer> carouselHeightMap = carouselCacheMap.get(widthBreakpoint);

        if (carouselHeightMap != null) {
            Integer carouselItemHeight = carouselHeightMap.get(messageViewModel.getMessageId());

            if (viewStyle == MessageViewModel.ViewStyle.ITEM && carouselItemHeight != null) {
                messageView.setMinimumHeight(carouselItemHeight);
            }
        }

    }

    public static void precalculateCarouselHeight(Context context,
                                                  String messageId,
                                                  MessageViewModel.ImageStyle imageStyle,
                                                  List<MessageItem> messageItems) {
        Resources resources = context.getResources();
        String widthBreakpoint = resources.getString(R.string.ClarabridgeChat_widthBreakpoint);

        HashMap<String, Integer> carouselHeightMap = carouselCacheMap.get(widthBreakpoint);

        if (carouselHeightMap == null) {
            carouselHeightMap = new HashMap<>();
            carouselCacheMap.put(widthBreakpoint, carouselHeightMap);
        } else if (carouselHeightMap.get(messageId) != null) {
            return;
        }

        // prepopulate height to 0 to simplify comparison at the end
        carouselHeightMap.put(messageId, 0);

        for (MessageItem messageItem : messageItems) {
            MessageViewModel messageViewModel = new MessageViewModel(messageItem, imageStyle);

            int viewHeight = 0;

            boolean hasActions;

            List<MessageAction> messageActions;
            TypedValue lineSpacing = new TypedValue();

            messageActions = messageViewModel.getMessageActions();
            hasActions = messageActions.size() >= 1;

            int maxMessageSize = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageMaxSize);
            int btnActionFullHeight = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_btnActionHeight);
            int btnActionMarginVertical = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_btnActionMarginVertical);

            int messagePaddingTop = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingTop);
            int messagePaddingBottom = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingBottom);

            if (messageViewModel.hasImage() || messageViewModel.getViewType() == MessageViewModel.ViewType.LOCATION) {
                if (messageViewModel.getViewStyle() == MessageViewModel.ViewStyle.ITEM
                        && messageViewModel.getImageStyle() == MessageViewModel.ImageStyle.SQUARE) {
                    viewHeight += resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_imageDisplayWidth);
                } else {
                    viewHeight += resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_imageDisplayHeight);
                }
            }

            List<TextView> textViews = new LinkedList<>();

            if (messageViewModel.hasMainText()) {
                TextView textView = new TextView(context);
                textView.setText(new SpannableString(messageViewModel.getMainText()));

                if (messageViewModel.getViewStyle() == MessageViewModel.ViewStyle.MESSAGE) {
                    textView.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            resources.getDimension(R.dimen.ClarabridgeChat_messageText)
                    );
                } else {
                    textView.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            resources.getDimension(R.dimen.ClarabridgeChat_titleText)
                    );
                }

                textViews.add(textView);
            }

            if (messageViewModel.hasSubText()) {
                TextView textView = new TextView(context);
                textView.setText(new SpannableString(messageViewModel.getSubText()));
                textView.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        resources.getDimension(R.dimen.ClarabridgeChat_descriptionText)
                );

                textViews.add(textView);
            }

            for (TextView textView : textViews) {
                textView.setMaxWidth(maxMessageSize);
                resources.getValue(R.dimen.ClarabridgeChat_lineSpacingMultiplier, lineSpacing, true);
                textView.setLineSpacing(0, lineSpacing.getFloat());

                textView.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                viewHeight += textView.getMeasuredHeight();
                viewHeight += messagePaddingTop + (hasActions ? 0 : messagePaddingBottom);
            }

            viewHeight += btnActionFullHeight * messageActions.size();

            if (messageActions.size() > 0) {
                viewHeight += btnActionMarginVertical + messagePaddingTop;
            }

            if (carouselHeightMap.get(messageId) < viewHeight) {
                carouselHeightMap.put(messageId, viewHeight);
            }
        }
    }

    private static void setImageFromMediaUrl(final MessageView messageView,
                                             final MessageViewModel messageViewModel,
                                             final String mediaUrl,
                                             View.OnClickListener previewOverride) {
        boolean isRemote = messageViewModel.isRemote();
        long mediaSize = messageViewModel.getMediaSize();

        boolean loadImageFromCache = messageViewModel.getViewStyle() != MessageViewModel.ViewStyle.ITEM
                && (mediaSize == 0 || mediaSize > IMAGE_AUTOLOAD_LIMIT);

        if (imagesCurrentlyLoading < 7) {
            imagesCurrentlyLoading++;
            messageView.setImage(mediaUrl, mediaSize, loadImageFromCache, isRemote, new Runnable() {
                @Override
                public void run() {
                    imagesCurrentlyLoading--;
                }
            }, previewOverride);
        } else {
            messageView.loadOnTap(mediaUrl, mediaSize, loadImageFromCache, isRemote, previewOverride);
        }
    }
}
