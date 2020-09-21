package com.clarabridge.ui.builder;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.Coordinates;
import com.clarabridge.core.Message;
import com.clarabridge.core.MessageAction;
import com.clarabridge.core.MessageItem;
import com.clarabridge.core.MessageType;
import com.clarabridge.ui.R;
import com.clarabridge.ui.widget.MessageView;

public class MessageViewModel {
    private ViewType viewType;
    private ViewStyle viewStyle;
    private ViewStatus viewStatus;
    private ImageStyle imageStyle;

    private File file;
    private Bitmap image;
    private long mediaSize;
    private String fileUrl;
    private String mainText;
    private String imageUrl;
    private String avatarUrl;
    private String messageId;
    private String subText;
    private String mapsApiKey;
    private Coordinates coordinates;
    private List<MessageItem> messageItems;
    private List<MessageAction> messageActions = new LinkedList<>();
    private Map<MessageView.ViewCorner, Boolean> messageRoundedCorners = new HashMap<>();

    private boolean isRemote;
    private boolean isFirstInList;
    private boolean isLastInList;
    private boolean shouldShowAvatar;

    public enum ViewType {
        COMPOUND,
        CAROUSEL,
        LOCATION,
        TYPING_INDICATOR,
        ;
    }

    public enum ImageStyle {
        HORIZONTAL,
        SQUARE,
        ;
    }

    public enum ViewStyle {
        MESSAGE,
        ITEM,
        ;
    }

    public enum ViewStatus {
        SENT,
        SENDING,
        FAILED,
        ;
    }

    public MessageViewModel(Message message, Resources resources, boolean isRemote, boolean isFirstInList,
                            boolean isLastInList, boolean shouldShowAvatar, boolean isFailed,
                            boolean isUnsent, String avatarUrl, String mapsApiKey) {
        boolean isUnknownType = true;

        this.isRemote = isRemote;
        this.avatarUrl = avatarUrl;
        this.isLastInList = isLastInList;
        this.isFirstInList = isFirstInList;
        this.shouldShowAvatar = shouldShowAvatar;
        this.mapsApiKey = mapsApiKey;

        if (message != null) {
            boolean isFile = MessageType.FILE.getValue().equals(message.getType());
            boolean isImage = MessageType.IMAGE.getValue().equals(message.getType());
            boolean isLocation = MessageType.LOCATION.getValue().equals(message.getType());
            boolean isCarousel = message.isCarousel();

            for (MessageType type : MessageType.values()) {
                if (type.getValue().equals(message.getType())) {
                    isUnknownType = false;
                    break;
                }
            }

            messageId = message.getId();

            viewType = ViewType.COMPOUND;
            viewStyle = ViewStyle.MESSAGE;
            imageStyle = message.getDisplaySettings().getImageAspectRatio().equals("square")
                    ? ImageStyle.SQUARE
                    : ImageStyle.HORIZONTAL;

            if (isLocation && !TextUtils.isEmpty(mapsApiKey)) {
                viewType = ViewType.LOCATION;
            } else if (isCarousel) {
                viewType = ViewType.CAROUSEL;
            }

            if (message.getText() != null && !message.getText().trim().isEmpty()
                    || (isLocation && isFailed)
                    || isUnknownType) {
                mainText = message.getText();

                if (isLocation && isFailed) {
                    mainText = resources.getString(R.string.ClarabridgeChat_locationSendingFailed);
                } else if (isUnknownType) {
                    if (message.getTextFallback() != null && !message.getTextFallback().trim().isEmpty()) {
                        mainText = message.getTextFallback().trim();
                    } else {
                        mainText = resources.getString(R.string.ClarabridgeChat_unsupportedMessageType);
                    }
                }
            }

            if (message.getMessageActions() != null && !message.hasReplies() && !message.hasLocationRequest()) {
                messageActions = message.getMessageActions();
            }

            if (isImage) {
                image = message.getImage();
                imageUrl = message.getMediaUrl();
                mediaSize = message.getMediaSize();
            }

            if (isFile) {
                file = message.getFile();
                fileUrl = message.getMediaUrl();
                mediaSize = message.getMediaSize();
            }

            if (isLocation && message.hasValidCoordinates()) {
                coordinates = message.getCoordinates();
            }

            messageItems = message.getMessageItems();

            if (isUnsent) {
                viewStatus = ViewStatus.SENDING;
            } else if (isFailed) {
                viewStatus = ViewStatus.FAILED;
            } else {
                viewStatus = ViewStatus.SENT;
            }
        } else {
            viewType = ViewType.TYPING_INDICATOR;
        }

        setMessageCorners();
    }

    MessageViewModel(MessageItem messageItem, ImageStyle imageStyle) {
        this(messageItem, null, imageStyle, false, false);
    }

    public MessageViewModel(MessageItem messageItem,
                            String messageId,
                            ImageStyle imageStyle,
                            boolean isFirstInList,
                            boolean isLastInList) {
        isRemote = true;

        // inherited from parent model
        this.messageId = messageId;
        this.imageStyle = imageStyle;

        this.isFirstInList = isFirstInList;
        this.isLastInList = isLastInList;

        viewStyle = ViewStyle.ITEM;
        viewType = ViewType.COMPOUND;
        viewStatus = ViewStatus.SENT;

        mainText = messageItem.getTitle();
        imageUrl = messageItem.getMediaUrl();
        subText = messageItem.getDescription();
        messageActions = messageItem.getMessageActions();

        setMessageCorners();
    }

    ViewType getViewType() {
        return viewType;
    }

    ViewStatus getViewStatus() {
        return viewStatus;
    }

    ViewStyle getViewStyle() {
        return viewStyle;
    }

    ImageStyle getImageStyle() {
        return imageStyle;
    }

    String getMainText() {
        return mainText;
    }

    String getSubText() {
        return subText;
    }

    List<MessageItem> getMessageItems() {
        return messageItems;
    }

    List<MessageAction> getMessageActions() {
        return messageActions;
    }

    String getImageUrl() {
        return imageUrl;
    }

    String getFileUrl() {
        return fileUrl;
    }

    public long getMediaSize() {
        return mediaSize;
    }

    Coordinates getCoordinates() {
        return coordinates;
    }

    Bitmap getImage() {
        return image;
    }

    File getFile() {
        return file;
    }

    String getMessageId() {
        return messageId;
    }

    String getAvatarUrl() {
        return avatarUrl;
    }

    void setImage(Bitmap image) {
        this.image = image;
    }

    boolean hasImage() {
        return image != null || imageUrl != null;
    }

    boolean hasFile() {
        return file != null || fileUrl != null;
    }

    boolean hasCoordinates() {
        return coordinates != null;
    }

    boolean hasMainText() {
        return mainText != null && !mainText.trim().isEmpty();
    }

    boolean hasSubText() {
        return subText != null && !subText.trim().isEmpty();
    }

    boolean isRemote() {
        return isRemote;
    }

    public boolean shouldShowAvatar() {
        return shouldShowAvatar;
    }

    public String getMapsApiKey() {
        return mapsApiKey;
    }

    Map<MessageView.ViewCorner, Boolean> getMessageRoundedCorners() {
        return messageRoundedCorners;
    }

    private void setMessageCorners() {
        if (viewStyle == ViewStyle.ITEM && !isFirstInList && !isLastInList) {
            messageRoundedCorners.put(MessageView.ViewCorner.TOP_LEFT, false);
            messageRoundedCorners.put(MessageView.ViewCorner.TOP_RIGHT, false);
            messageRoundedCorners.put(MessageView.ViewCorner.BOTTOM_LEFT, false);
            messageRoundedCorners.put(MessageView.ViewCorner.BOTTOM_RIGHT, false);
        } else {
            if (viewStyle == ViewStyle.ITEM) {
                messageRoundedCorners.put(MessageView.ViewCorner.TOP_LEFT, isFirstInList);
                messageRoundedCorners.put(MessageView.ViewCorner.TOP_RIGHT, isLastInList);
                messageRoundedCorners.put(MessageView.ViewCorner.BOTTOM_LEFT, isFirstInList);
                messageRoundedCorners.put(MessageView.ViewCorner.BOTTOM_RIGHT, isLastInList);
            } else {
                messageRoundedCorners.put(MessageView.ViewCorner.TOP_LEFT, !isRemote || isFirstInList);
                messageRoundedCorners.put(MessageView.ViewCorner.TOP_RIGHT, isRemote || isFirstInList);
                messageRoundedCorners.put(MessageView.ViewCorner.BOTTOM_LEFT, !isRemote || isLastInList);
                messageRoundedCorners.put(MessageView.ViewCorner.BOTTOM_RIGHT, isRemote || isLastInList);
            }
        }
    }
}
