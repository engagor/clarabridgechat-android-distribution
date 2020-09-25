package com.clarabridge.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import com.clarabridge.core.ActionState;
import com.clarabridge.core.ConversationEvent;
import com.clarabridge.core.Coordinates;
import com.clarabridge.core.Message;
import com.clarabridge.core.MessageAction;
import com.clarabridge.core.MessageType;
import com.clarabridge.core.MessageUploadStatus;
import com.clarabridge.core.ClarabridgeChatCallback;
import com.clarabridge.core.utils.StringUtils;
import com.clarabridge.ui.R;
import com.clarabridge.ui.builder.MessageViewBuilder;
import com.clarabridge.ui.builder.MessageViewModel;
import com.clarabridge.ui.utils.DateTimeUtils;
import com.clarabridge.ui.utils.DpVisitor;
import com.clarabridge.ui.widget.AvatarImageView;
import com.clarabridge.ui.widget.MessageView;
import com.clarabridge.ui.widget.RepliesView;

import static com.clarabridge.ui.builder.MessageViewBuilder.bitmapCache;

public class MessageListAdapter extends RecyclerView.Adapter implements MessageView.Delegate, RepliesView.Delegate {
    private static final String TAG = "MessageListAdapter";
    private boolean creditCardLoaded = false;
    private static final int ANIMATION_DURATION = 500;
    private static final int SHORT_ANIMATION_DURATION = 200;
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_MESSAGE = VIEW_TYPE_HEADER + 1;
    private static final int VIEW_TYPE_FOOTER = VIEW_TYPE_MESSAGE + 1;
    private ArrayList<Item> messageGroupList = new ArrayList<>();
    private List<MessageAction> replies = new ArrayList<>();
    private Item typingActivityItem;
    private int headerViewResourceId = 0;
    private int hoursBetweenTimestamps = 0;
    private Delegate delegate;
    private String mapsApiKey;
    private int unreadCount = 0;
    private Message firstUnreadMessage = null;

    public interface Delegate {
        void onClick(Message message);

        void onActionClick(MessageAction action);

        void onMapClick(Coordinates coordinates);

        void onProductOffered();

        Long getLastRead();

        void onFileClick(String url);
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class MessageViewHolder extends RecyclerView.ViewHolder {
        AvatarImageView authorAvatar;
        TextView authorName;
        RelativeLayout unreadCountContainer;
        TextView unreadCount;
        TextView time;
        TextView statusTextView;
        LinearLayout contentPanel;
        MessageView messageView;
        Item item;

        MessageViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            unreadCountContainer = itemView.findViewById(R.id.unreadCountContainer);
            unreadCount = itemView.findViewById(R.id.unreadCount);
            authorName = itemView.findViewById(R.id.text);
            authorAvatar = itemView.findViewById(R.id.avatar);
            statusTextView = itemView.findViewById(R.id.status);
            contentPanel = itemView.findViewById(R.id.contentPanel);
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {
        RepliesView repliesView;

        FooterViewHolder(View itemView) {
            super(itemView);
            repliesView = (RepliesView) itemView;
        }
    }

    private static class Item {
        MessageView messageView;
        TextView statusTextView;

        Message message;
        boolean currentUser = false;
        String authorAvatarUrl;
        String authorName;
        String userId;
        String userRole;
        Date date;
        boolean animate = false;
        boolean isFirstMessage = false;
        boolean isLastMessage = false;
        boolean isTypingActivity = false;
        boolean isSelected = false;
        boolean shouldShowAvatar = false;
        boolean shouldShowAuthorName = false;

        //checks if the author is different by checking the name,id,role and avatar url
        boolean isDifferentAuthor(Item item) {
            if (item == null) {
                return true;
            }
            return !StringUtils.isEqual(item.authorName, authorName)
                    || !StringUtils.isEqual(item.authorAvatarUrl, authorAvatarUrl)
                    || !StringUtils.isEqual(item.userRole, userRole)
                    || !StringUtils.isEqual(item.userId, userId);
        }
    }

    private enum AnimationState {
        DISABLED,
        ENABLED,
        ;
    }

    public MessageListAdapter(Delegate delegate) {
        this.delegate = delegate;
    }

    public void setMapsApiKey(String mapsApiKey) {
        this.mapsApiKey = mapsApiKey;
    }

    public void setHoursBetweenTimestamps(int hoursBetweenTimestamps) {
        this.hoursBetweenTimestamps = hoursBetweenTimestamps;
    }

    public void setHeaderViewResourceId(int headerViewResourceId) {
        this.headerViewResourceId = headerViewResourceId;
        notifyItemInserted(0);
    }

    public void setReplies(@NonNull List<MessageAction> replies) {
        this.replies = replies;
        notifyItemChanged(getItemCount() - 1);
        // Update last two messages for message grouping
        notifyRemoteMessageChanged(getItemCount() - 2);
        notifyRemoteMessageChanged(getItemCount() - 3);
    }

    public void setTypingActivity(@NonNull ConversationEvent conversationEvent) {
        boolean hasCurrentActivity = hasTypingActivity();
        this.typingActivityItem = createTypingActivityItem(conversationEvent);

        if (messageGroupList.size() > 0) {
            Item lastItem = messageGroupList.get(messageGroupList.size() - 1);

            if (!lastItem.currentUser) {
                String lastItemName = lastItem.authorName == null ? "" : lastItem.authorName;
                String typingActivityName = typingActivityItem.authorName == null ? "" : typingActivityItem.authorName;
                boolean isDifferentAuthorName = !typingActivityName.isEmpty()
                        && !typingActivityName.equals(lastItemName);
                lastItem.isLastMessage = isDifferentAuthorName;
                typingActivityItem.isFirstMessage = isDifferentAuthorName;
                typingActivityItem.shouldShowAuthorName = isDifferentAuthorName;
                notifyItemChanged(getItemCount() - 2);
            }
        }

        if (hasCurrentActivity) {
            notifyItemChanged(getItemCount() - 1);
        } else {
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;

        setFirstUnreadMessage();
    }

    private void setFirstUnreadMessage() {
        if (unreadCount == 0) {
            firstUnreadMessage = null;
            return;
        }

        if (firstUnreadMessage != null) {
            return;
        }

        int unreadMessageCount = 0;

        for (int i = messageGroupList.size() - 1; i >= 0; i--) {
            Message message = messageGroupList.get(i).message;

            if (!message.isFromCurrentUser()) {
                unreadMessageCount++;
            }

            if (unreadMessageCount == unreadCount) {
                firstUnreadMessage = message;
                break;
            }
        }
    }

    private Item createTypingActivityItem(ConversationEvent conversationEvent) {
        Item item = new Item();

        item.isFirstMessage = true;
        item.isLastMessage = true;
        item.animate = true;
        item.authorAvatarUrl = conversationEvent.getAvatarUrl();
        item.authorName = conversationEvent.getName();
        item.userId = conversationEvent.getUserId();
        item.userRole = conversationEvent.getRole();
        item.currentUser = false;
        item.isTypingActivity = true;
        item.message = null;
        item.shouldShowAvatar = true;
        item.shouldShowAuthorName = true;

        return item;
    }

    public void removeTypingActivity() {
        if (hasTypingActivity()) {
            int typingActivityIndex = getItemCount() - 1;

            if (shouldDisplayFooter()) {
                typingActivityIndex--;
            }

            notifyItemRemoved(typingActivityIndex);
            typingActivityItem = null;

            for (int i = messageGroupList.size() - 1; i >= 0; i--) {
                Item item = messageGroupList.get(i);
                if (!item.currentUser) {
                    if (!item.isLastMessage) {
                        // Notify only if needed
                        item.isLastMessage = true;
                        int lastMessageIndex = i;

                        if (shouldDisplayHeader()) {
                            lastMessageIndex++;
                        }

                        notifyRemoteMessageChanged(lastMessageIndex);
                    }
                    break;
                }
            }
        }
    }

    private void notifyRemoteMessageChanged(int index) {
        Item remoteMessage = getItem(index);
        if (remoteMessage != null && !remoteMessage.currentUser) {
            notifyItemChanged(index);
        }
    }

    public void removeReplies() {
        if (replies != null) {
            replies = null;
            notifyItemRemoved(getItemCount() - 1);
        }
    }

    private View createRepliesView(@NonNull Context context) {
        RepliesView repliesView = new RepliesView(context);
        repliesView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        repliesView.setDelegate(this);
        return repliesView;
    }

    public void addMessage(Message message) {
        if (message != null) {
            addMessageInternal(message, AnimationState.DISABLED);
            notifyMessageAdded();
        }
    }

    public void addMessageWithAnimation(Message message) {
        if (message != null) {
            addMessageInternal(message, AnimationState.ENABLED);
            notifyMessageAdded();
        }
    }

    public void refreshLastMessage() {
        int indexToUpdate = messageGroupList.size() - 1;

        if (shouldDisplayHeader()) {
            indexToUpdate++;
        }

        notifyItemChanged(indexToUpdate);
    }

    private void notifyMessageAdded() {
        notifyMessagesAdded(1);
    }

    private void notifyMessagesAdded(int size) {
        int startingPosition = getItemCount() - size;

        if (hasTypingActivity()) {
            startingPosition--;
        }

        if (startingPosition > 0) {
            Item lastItem = getItem(startingPosition - 1);
            if (lastItem != null) {
                Item firstInsertedItem = getItem(startingPosition);
                if (firstInsertedItem != null) {
                    notifyItemChanged(startingPosition - 1); // Update grouping layout and remove timestamp
                } else if (hasTypingActivity() && !lastItem.message.isFromCurrentUser()) {
                    typingActivityItem.isFirstMessage = true;
                    lastItem.isLastMessage = true;
                    notifyItemChanged(startingPosition - 1);
                }
            }
        }

        notifyItemRangeInserted(startingPosition, size);

        // Order matters
        if (hasTypingActivity()) {
            notifyItemChanged(getItemCount() - (shouldDisplayFooter() ? 2 : 1));
        }
    }

    private void addMessages(List<Message> v) {
        if (v != null) {
            for (Message message : v) {
                addMessageInternal(message, AnimationState.DISABLED);
            }

            notifyMessagesAdded(v.size());
        }
    }

    public void addMessagesWithAnimation(List<Message> v) {
        if (v != null && !v.isEmpty()) {
            for (int i = 0; i < v.size(); i++) {
                boolean isLastMessage = i + 1 == v.size();

                addMessageInternal(v.get(i), isLastMessage ? AnimationState.ENABLED : AnimationState.DISABLED);
            }

            notifyMessagesAdded(v.size());
        }
    }

    public void setMessages(List<Message> messages) {
        messageGroupList.clear();
        notifyDataSetChanged();

        if (messages != null) {
            addMessages(messages);
        }
    }

    public void removeMessage(Message message) {
        if (message != null) {
            for (int indexToRemove = messageGroupList.size() - 1; indexToRemove >= 0; indexToRemove--) {
                Item item = messageGroupList.get(indexToRemove);
                if (item.message.equals(message)) {
                    messageGroupList.remove(indexToRemove);
                    notifyDataSetChanged();

                    return;
                }
            }
        }
    }

    public void uploadStart(Message message) {
        addMessageWithAnimation(message);
    }

    public void uploadRetry(Message message) {
        updateMessage(message);
    }

    public void uploadEnd(Message message, ClarabridgeChatCallback.Response<Message> response) {
        int statusCode = response.getStatus();

        if (statusCode >= 200 && statusCode < 300) {
            if (message.getImage() != null) {
                bitmapCache.put(message.getMediaUrl(), message.getImage());
                message.setImage(null);
            }

            if (message.getFile() != null) {
                message.setFile(null);
            }
        }

        updateMessage(message);
    }

    public void actionPostbackStart(MessageAction messageAction) {
        if (!MessageViewBuilder.postbacksInProgress.contains(messageAction)) {
            MessageViewBuilder.postbacksInProgress.add(messageAction);
            notifyDataSetChanged();
        }
    }

    public void actionPostbackEnd(MessageAction messageAction) {
        if (MessageViewBuilder.postbacksInProgress.contains(messageAction)) {
            MessageViewBuilder.postbacksInProgress.remove(messageAction);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        int count = messageGroupList.size();

        if (shouldDisplayHeader()) {
            count++;
        }

        if (shouldDisplayFooter()) {
            count++;
        }

        if (hasTypingActivity()) {
            count++;
        }

        return count;
    }

    private boolean shouldDisplayHeader() {
        return headerViewResourceId != 0;
    }

    private Item getItem(int index) {
        if (shouldDisplayHeader()) {
            index--;
        }

        if (index >= 0 && index < messageGroupList.size()) {
            return messageGroupList.get(index);
        }

        if (index == messageGroupList.size() && hasTypingActivity()) {
            return typingActivityItem;
        }

        return null;
    }

    private boolean hasTypingActivity() {
        return typingActivityItem != null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflate(null, parent, headerViewResourceId);
            return new HeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View view = createRepliesView(parent.getContext());
            return new FooterViewHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE) {
            View row = inflate(null, parent, R.layout.clarabridgechat_list_message_item);
            return new MessageViewHolder(row);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE) {
            getItemView(position, (MessageViewHolder) holder);
        } else if (holder.getItemViewType() == VIEW_TYPE_FOOTER) {
            ((FooterViewHolder) holder).repliesView.setReplies(replies);
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        // Recycler view may disable recurring animations on a ViewHolder during recycling
        // Calling `showTypingIndicator` forces a restart
        if (holder instanceof MessageViewHolder) {
            MessageViewHolder row = (MessageViewHolder) holder;
            if (row.item != null && row.item.isTypingActivity) {
                row.messageView.showTypingActivity();
            }
        }
    }

    public void updateMessage(Message message) {
        int foundMessageIndex = -1;

        for (int i = messageGroupList.size() - 1; i >= 0; i--) {
            if (messageGroupList.get(i).message.equals(message)) {
                Item item = messageGroupList.get(i);
                item.message = message;

                if (foundMessageIndex > -1) {
                    removeMessage(messageGroupList.get(foundMessageIndex).message);
                    return;
                }

                foundMessageIndex = i;

                buildMessageView(item);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && headerViewResourceId != 0) {
            return VIEW_TYPE_HEADER;
        }

        if (position == getItemCount() - 1 && shouldDisplayFooter()) {
            return VIEW_TYPE_FOOTER;
        }

        return VIEW_TYPE_MESSAGE;
    }

    private boolean shouldDisplayFooter() {
        return replies != null && !replies.isEmpty();
    }

    @Override
    public void onActionClick(MessageAction action) {
        delegate.onActionClick(action);
    }

    @Override
    public void onMapClick(Coordinates coordinates) {
        delegate.onMapClick(coordinates);
    }

    @Override
    public void onFileClick(String url) {
        delegate.onFileClick(url);
    }

    @Override
    public void onReplySelected(MessageAction reply) {
        delegate.onActionClick(reply);
    }

    private void getItemView(int position, MessageViewHolder row) {
        Resources resources = row.itemView.getResources();

        if (row.messageView == null) {
            row.messageView = createMessageView(row.contentPanel);
        } else if (row.item.message == null) {
            row.messageView.reset();
        }

        row.item = getItem(position);

        if (row.item == null) {
            Log.e(TAG, "Got back null item");
            return;
        }

        row.item.messageView = row.messageView;
        row.item.statusTextView = row.statusTextView;

        boolean isRemote = !row.item.currentUser;

        row.authorAvatar.setVisibility(View.GONE);

        if (shouldPrintDate(row.item)) {
            // Consider messages separated by the date header as different groups
            row.item.isFirstMessage = true;
            row.item.shouldShowAuthorName = true;

            Item previousItem = getItem(position - 1);

            if (previousItem != null) {
                previousItem.isLastMessage = true;
                previousItem.shouldShowAvatar = true;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(
                    resources.getString(R.string.ClarabridgeChat_settings_timestampFormat),
                    Locale.getDefault()
            );
            String formattedDate = sdf.format(row.item.date);

            row.time.setText(formattedDate);

            if (Build.VERSION.SDK_INT >= 21) {
                row.time.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            }

            row.time.setVisibility(View.VISIBLE);
        } else {
            row.time.setVisibility(View.GONE);
        }

        updateStatusAlignment(row.item, row.statusTextView, row.contentPanel);
        row.statusTextView.setVisibility(View.GONE);

        updateMessageContent(row.item, row.contentPanel, resources, (ViewGroup) row.itemView);

        row.item.animate = false;

        int avatarSize = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatar);
        int avatarMargin = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatarMargin);
        int conversationMargin = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_conversationMargin);

        if (row.item.message == null || !row.item.message.isCarousel()) {
            if (isRemote && row.item.shouldShowAvatar) {
                row.authorAvatar.show(row.item.authorAvatarUrl);
            } else {
                row.authorAvatar.showInvisible();
            }

            row.authorName.setPadding(0, 0, 0, 0);
        } else {
            row.authorName.setPadding(avatarSize + avatarMargin + conversationMargin, 0, 0, 0);
        }

        //noinspection ResourceType
        row.itemView.setPadding(
                row.itemView.getPaddingLeft(),
                row.itemView.getPaddingTop(),
                row.itemView.getPaddingRight(),
                row.item.isLastMessage ? (int) DpVisitor.toPixels(row.itemView.getContext(), 8) : 0
        );

        if (isRemote && row.item.shouldShowAuthorName
                && row.item.authorName != null && !row.item.authorName.isEmpty()) {
            row.authorName.setText(row.item.authorName);
            row.authorName.setVisibility(View.VISIBLE);
        } else {
            row.authorName.setVisibility(View.GONE);
        }

        boolean productOffered = false;

        if (row.item.message != null && row.item.message.getMessageActions() != null) {
            for (MessageAction messageAction : row.item.message.getMessageActions()) {
                if (messageAction.getState() != null
                        && messageAction.getState().equals(ActionState.OFFERED.getValue())) {
                    productOffered = true;
                    break;
                }
            }
        }

        if (!creditCardLoaded && productOffered) {
            creditCardLoaded = true;
            delegate.onProductOffered();
        }

        if (unreadCount > 0 && row.item.message != null && row.item.message.equals(firstUnreadMessage)) {
            row.unreadCount.setText(
                    row.itemView.getResources().getQuantityString(
                            R.plurals.ClarabridgeChat_unreadMessagesText,
                            unreadCount,
                            unreadCount
                    )
            );
            row.unreadCountContainer.setVisibility(View.VISIBLE);
        } else {
            row.unreadCountContainer.setVisibility(View.GONE);
        }

    }

    private void buildMessageView(final Item item) {
        final Message message = item.message;

        boolean isRemote = !item.currentUser;
        boolean isLastInList = item.isLastMessage;
        boolean isFirstInList = item.isFirstMessage;
        boolean isTypingActivity = item.isTypingActivity;
        boolean shouldShowAvatar = item.shouldShowAvatar;

        boolean isFile = false;
        boolean isImage = false;
        boolean isFailed = false;
        boolean isUnsent = false;

        if (item.messageView == null || item.statusTextView == null) {
            return;
        }

        Resources resources = item.messageView.getResources();

        if (message != null) {
            isFile = MessageType.FILE.getValue().equals(message.getType());
            isImage = MessageType.IMAGE.getValue().equals(message.getType());
            isFailed = message.getUploadStatus() == MessageUploadStatus.FAILED
                    || (MessageType.LOCATION.getValue().equals(message.getType())
                    && !message.hasValidCoordinates());
            isUnsent = message.getUploadStatus() == MessageUploadStatus.UNSENT;
        }

        MessageViewBuilder.build(new MessageViewModel(
                message, resources, isRemote, isFirstInList, isLastInList,
                shouldShowAvatar, isFailed, isUnsent, item.authorAvatarUrl, mapsApiKey
        ), item.messageView);

        boolean isLastMessageItem = messageGroupList.size() > 0
                && item.equals(messageGroupList.get(messageGroupList.size() - 1));
        boolean shouldDisplayStatus = isLastMessageItem
                && !isFailed && !isUnsent && (!isRemote || !hasTypingActivity());

        if (shouldDisplayStatus) {
            Long sentTime = item.message == null || item.message.getDate() == null
                    ? null
                    : item.message.getDate().getTime();
            String relativeTimestampText = getRelativeTimestampText(sentTime, resources);

            boolean isMessageRead = delegate.getLastRead() != null
                    && item.message.getDate() != null
                    && delegate.getLastRead() >= item.message.getDate().getTime();

            if (relativeTimestampText != null) {
                final SpannableStringBuilder statusText;

                if (isRemote) {
                    statusText = new SpannableStringBuilder(relativeTimestampText);
                } else {
                    statusText = isMessageRead
                            ? getSeenStatusText(delegate.getLastRead(), resources)
                            : getDeliveredStatusText(relativeTimestampText, resources);
                }
                final String shortTimestampText = getShortTimestampText(message, resources);

                if (shortTimestampText != null && !isImage && !isFile) {
                    item.messageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.isSelected = !item.isSelected;
                            if (item.isSelected) {
                                item.statusTextView.setText(shortTimestampText);
                            } else {
                                item.statusTextView.setText(statusText);
                            }
                        }
                    });
                }

                item.statusTextView.setText(item.isSelected ? shortTimestampText : statusText);
                item.statusTextView.setVisibility(View.VISIBLE);
            } else {
                item.statusTextView.setVisibility(View.GONE);
            }
        } else if (isUnsent) {
            item.statusTextView.setText(R.string.ClarabridgeChat_sendingMessage);
            item.statusTextView.setVisibility(View.VISIBLE);
        } else if (!isFailed && !isImage && !isFile && !isTypingActivity) {
            final String shortTimestampText = getShortTimestampText(message, resources);

            item.statusTextView.setVisibility(View.GONE);

            if (shortTimestampText != null) {
                item.messageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.isSelected = !item.isSelected;
                        if (item.isSelected) {
                            animateTimestampExpand(item.statusTextView, shortTimestampText);
                        } else {
                            animateTimestampCollapse(item.statusTextView);
                        }
                    }
                });
            } else {
                item.isSelected = false;
                item.statusTextView.setVisibility(View.GONE);
            }

            if (item.isSelected) {
                item.statusTextView.setText(shortTimestampText);
                item.statusTextView.setVisibility(View.VISIBLE);
            }
        }

        if (!isRemote && isFailed) {
            item.statusTextView.setText(resources.getText(R.string.ClarabridgeChat_errorSendingMessage));
            item.statusTextView.setVisibility(View.VISIBLE);
            item.messageView.setClickable(true);
            item.messageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (delegate != null) {
                        delegate.onClick(message);
                    }

                    item.messageView.setClickable(false);
                    item.messageView.setOnClickListener(null);
                }
            });
        }
    }

    @SuppressLint("RtlHardcoded")
    private void updateMessageContent(final Item item,
                                      LinearLayout contentPanel,
                                      Resources resources,
                                      ViewGroup parent) {
        boolean isLastInList = item.isLastMessage;
        boolean animate = item.isLastMessage && item.animate;
        boolean isRemote = !item.currentUser;

        buildMessageView(item);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) item.messageView.getLayoutParams();

        layoutParams.gravity = isRemote ? Gravity.LEFT : Gravity.RIGHT;
        //noinspection ResourceType
        layoutParams.setMargins(0,
                0,
                !isRemote ? resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_conversationMargin) : 0,
                !isLastInList ? (int) DpVisitor.toPixels(parent.getContext(), 4) : 0);

        item.messageView.setLayoutParams(layoutParams);

        if (animate) {
            animate(contentPanel, item.messageView, isRemote);
        }
    }

    private void animateTimestampExpand(final TextView statusTextView, String timestampText) {
        statusTextView.setText(timestampText);
        statusTextView.setVisibility(View.VISIBLE);
        statusTextView.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int finalHeight = statusTextView.getMeasuredHeight();
        statusTextView.setAlpha(0);

        animateHeight(statusTextView, 0, finalHeight, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animateAlpha(statusTextView, 1.0f, null);
            }
        });
    }

    private void animateTimestampCollapse(final TextView statusTextView) {
        final int height = statusTextView.getHeight();

        animateAlpha(statusTextView, 0f, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                animateHeight(statusTextView, height, 0, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        statusTextView.setText(null);
                        statusTextView.setVisibility(View.GONE);
                        // Restore height and alpha for recycled views that should display timestamps
                        statusTextView.getLayoutParams().height = height;
                        statusTextView.setAlpha(1.0f);
                    }
                });
            }
        });
    }

    private void animateAlpha(View view, float to, Animator.AnimatorListener listener) {
        view.animate()
                .alpha(to)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(listener)
                .setDuration(SHORT_ANIMATION_DURATION)
                .start();
    }

    private void animateHeight(final View view, int from, int to,
                               Animator.AnimatorListener listener) {
        final int height = Math.max(from, to);

        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.removeAllListeners();
        animator.removeAllUpdateListeners();
        animator.setDuration(SHORT_ANIMATION_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (Float) animation.getAnimatedValue() / height;
                view.getLayoutParams().height = (int) (height * progress);
                view.requestLayout();
            }
        });
        animator.addListener(listener);
        animator.start();
    }

    private void updateStatusAlignment(Item item, TextView statusTextView, LinearLayout contentPanel) {
        Resources resources = statusTextView.getResources();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) statusTextView.getLayoutParams();

        if (item != null && item.currentUser) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ALIGN_LEFT, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                layoutParams.removeRule(RelativeLayout.ALIGN_START);
            }
        } else {
            layoutParams.addRule(RelativeLayout.ALIGN_LEFT, contentPanel.getId());
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutParams.addRule(RelativeLayout.ALIGN_START, contentPanel.getId());
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
            }
        }

        boolean isLastMessage = item != null && item.isLastMessage;
        int messageViewOffset = (int) DpVisitor.toPixels(statusTextView.getContext(), 4);

        int avatarSize = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatar);
        int avatarMargin = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatarMargin);
        int conversationMargin = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_conversationMargin);

        layoutParams.rightMargin = conversationMargin;
        layoutParams.leftMargin = avatarSize + avatarMargin + conversationMargin;
        layoutParams.bottomMargin = isLastMessage ? 0 : messageViewOffset;
        layoutParams.topMargin = isLastMessage ? 0 : -1 * messageViewOffset;

        statusTextView.setLayoutParams(layoutParams);
    }

    private String getRelativeTimestampText(Long time, Resources resources) {
        if (time == null) {
            return null;
        }

        long timeDifference = System.currentTimeMillis() - time;

        final int minute = 60 * 1000;
        final int hour = 60 * minute;
        final int day = 24 * hour;
        final int week = 7 * day;

        if (timeDifference < minute) {
            return resources.getString(R.string.ClarabridgeChat_relativeTimeJustNow);
        } else if (timeDifference < hour) {
            return resources.getString(R.string.ClarabridgeChat_relativeTimeMinute, Math.round(timeDifference / minute));
        } else if (timeDifference < day) {
            return resources.getString(R.string.ClarabridgeChat_relativeTimeHour, Math.round(timeDifference / hour));
        } else if (timeDifference <= week) {
            return resources.getString(R.string.ClarabridgeChat_relativeTimeDay, Math.round(timeDifference / day));
        }

        return null;
    }

    private String getShortTimestampText(Message message, Resources resources) {
        if (message == null || message.getDate() == null) {
            return null;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                resources.getString(R.string.ClarabridgeChat_settings_shortTimestampFormat),
                Locale.getDefault()
        );
        return simpleDateFormat.format(message.getDate());
    }

    private SpannableStringBuilder getDeliveredStatusText(String timestamp, Resources resources) {
        String status = resources.getString(R.string.ClarabridgeChat_messageStatusDelivered);

        SpannableString boldSpannableString = new SpannableString(status);
        boldSpannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, status.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return new SpannableStringBuilder().append(timestamp).append(". ").append(boldSpannableString);
    }

    private SpannableStringBuilder getSeenStatusText(Long businessLastRead, Resources resources) {
        String relativeTimestamp = getRelativeTimestampText(businessLastRead, resources);

        String status = resources.getString(R.string.ClarabridgeChat_messageStatusSeen);

        SpannableString boldSpannableString = new SpannableString(status);
        boldSpannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, status.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return new SpannableStringBuilder()
                .append(boldSpannableString)
                .append(" ")
                .append(relativeTimestamp.toLowerCase());
    }

    private void animate(View parent, MessageView messageView, boolean isRemote) {
        int width;
        int height;

        messageView.measure(View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), View.MeasureSpec.AT_MOST), 0);
        width = messageView.getMeasuredWidth();
        height = messageView.getMeasuredHeight();

        messageView.setPivotX(isRemote ? 0 : width);
        messageView.setPivotY(height);
        messageView.setScaleX(0);
        messageView.setScaleY(0);
        messageView.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    private void addMessageInternal(Message message, AnimationState animationState) {
        Item item = new Item();

        messageGroupList.add(item);
        item.message = message;

        item.currentUser = message.isFromCurrentUser();
        item.authorAvatarUrl = message.getAvatarUrl();
        item.authorName = message.getName();
        item.userId = message.getUserId();
        item.userRole = message.getUserRole();
        item.date = message.getDate();
        item.animate = animationState == AnimationState.ENABLED;
        item.shouldShowAuthorName = true;

        updateItemPositioning(item, messageGroupList.size() - 1, message);
    }

    private void updateItemPositioning(Item item, int position, Message message) {
        if (messageGroupList.size() > 1) {
            boolean isDifferentAuthor = true;
            Item previousItem = messageGroupList.get(position - 1);

            if (previousItem == null) {
                return;
            }

            if (previousItem.currentUser == item.currentUser) {
                //if the author is not different the messages will be grouped differently
                isDifferentAuthor = item.isDifferentAuthor(previousItem);
                previousItem.shouldShowAvatar = isDifferentAuthor;
                item.shouldShowAuthorName = isDifferentAuthor;
            }

            boolean previousItemIsCarousel = previousItem.message.isCarousel();

            if (previousItemIsCarousel) {
                item.isFirstMessage = true;
                item.isLastMessage = true;
            } else if (previousItem.currentUser == item.currentUser) {
                boolean itemIsCarousel = message.isCarousel();

                if (itemIsCarousel) {
                    item.isFirstMessage = true;
                    previousItem.isLastMessage = true;
                } else if (!item.currentUser) {
                    item.isFirstMessage = isDifferentAuthor;
                    previousItem.isLastMessage = isDifferentAuthor;
                } else {
                    previousItem.isLastMessage = false;
                }
            } else {
                item.isFirstMessage = true;
            }
        } else {
            item.isFirstMessage = true;
        }

        if (position == messageGroupList.size() - 1) {
            item.isLastMessage = true;
            item.shouldShowAvatar = true;
        }
    }

    private boolean shouldPrintDate(Item item) {
        if (item.isTypingActivity) {
            return false;
        }

        ListIterator<Item> itemListIterator = messageGroupList.listIterator();
        Item currentPrintedDate = null;

        while (itemListIterator.hasNext()) {
            Item currentItem = itemListIterator.next();

            if (currentPrintedDate == null
                    || DateTimeUtils.getDeltaHours(currentPrintedDate.date, currentItem.date)
                    > hoursBetweenTimestamps) {
                currentPrintedDate = currentItem;
            }

            if (item == currentPrintedDate) {
                return true;
            }
        }

        return false;
    }

    private MessageView createMessageView(ViewGroup parent) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        MessageView messageView = new MessageView(parent.getContext(), this);
        parent.addView(messageView, layoutParams);

        return messageView;
    }

    private View inflate(View recyclingView, ViewGroup parent, int resourceId) {
        Context applicationContext = parent.getContext().getApplicationContext();

        View view = recyclingView;

        if (view == null || view.getTag(resourceId) == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(resourceId, parent, false);

            if (resourceId == R.layout.clarabridgechat_list_message_header) {
                CharSequence appName = applicationContext.getPackageManager()
                        .getApplicationLabel(applicationContext.getApplicationInfo());
                String welcomeTextFormatted = parent.getResources()
                        .getString(R.string.ClarabridgeChat_startOfConversation, appName);
                TextView welcomeMessageView = view.findViewById(R.id.clarabridgechat_welcome_message);

                if (welcomeMessageView != null) {
                    welcomeMessageView.setText(welcomeTextFormatted);
                }
            }
        }

        return view;
    }
}

