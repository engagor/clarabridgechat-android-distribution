package com.clarabridge.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import com.clarabridge.core.MessageItem;
import com.clarabridge.ui.R;
import com.clarabridge.ui.builder.MessageViewBuilder;
import com.clarabridge.ui.builder.MessageViewModel;
import com.clarabridge.ui.widget.AvatarImageView;
import com.clarabridge.ui.widget.MessageView;

public class CarouselAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final MessageView.Delegate delegate;
    private final ArrayList<Item> messageItemGroupList = new ArrayList<>();

    private static final int VIEW_TYPE_AVATAR = 0;
    private static final int VIEW_TYPE_MESSAGE_ITEM = VIEW_TYPE_AVATAR + 1;

    private boolean shouldShowAvatar;
    private MessageViewModel.ImageStyle imageStyle = MessageViewModel.ImageStyle.HORIZONTAL;
    private String avatarUrl;

    public static class Item {
        MessageItem messageItem;
        boolean isFirstInList = false;
        boolean isLastInList = false;
        public String messageId;
    }

    public static class MessageItemViewHolder extends RecyclerView.ViewHolder {
        public Item item;
        MessageView messageView;
        FrameLayout messageItemContainer;

        MessageItemViewHolder(FrameLayout itemView) {
            super(itemView);
            messageItemContainer = itemView;
        }
    }

    private static class AvatarImageHolder extends RecyclerView.ViewHolder {
        RelativeLayout avatarImageContainer;
        AvatarImageView avatarImageView;

        AvatarImageHolder(RelativeLayout itemView) {
            super(itemView);
            this.avatarImageContainer = itemView;
        }
    }


    public CarouselAdapter(Context context, MessageView.Delegate delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AVATAR) {
            RelativeLayout row = new RelativeLayout(parent.getContext());
            row.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            return new AvatarImageHolder(row);
        } else if (viewType == VIEW_TYPE_MESSAGE_ITEM) {
            FrameLayout row = new FrameLayout(parent.getContext());
            return new MessageItemViewHolder(row);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_AVATAR) {
            AvatarImageHolder viewHolder = (AvatarImageHolder) holder;

            if (viewHolder.avatarImageView == null) {
                viewHolder.avatarImageView = createAvatarView(viewHolder.avatarImageContainer);
            }

            if (shouldShowAvatar) {
                viewHolder.avatarImageView.show(avatarUrl);
            } else {
                viewHolder.avatarImageView.showInvisible();
            }
        } else if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_ITEM) {
            MessageItemViewHolder viewHolder = (MessageItemViewHolder) holder;
            viewHolder.item = getItem(position);

            if (viewHolder.messageView == null) {
                viewHolder.messageView = createMessageView(viewHolder.messageItemContainer);
            }

            int itemMargin = viewHolder.messageView.getResources()
                    .getDimensionPixelSize(R.dimen.ClarabridgeChat_messageItemMargin);
            int conversationMargin = viewHolder.messageView.getResources()
                    .getDimensionPixelSize(R.dimen.ClarabridgeChat_conversationMargin);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(
                    viewHolder.item.isFirstInList ? 0 : itemMargin,
                    0,
                    viewHolder.item.isLastInList ? conversationMargin : itemMargin,
                    0
            );
            viewHolder.messageView.setLayoutParams(layoutParams);

            MessageViewBuilder.build(new MessageViewModel(
                    viewHolder.item.messageItem,
                    viewHolder.item.messageId,
                    imageStyle,
                    viewHolder.item.isFirstInList,
                    viewHolder.item.isLastInList
            ), viewHolder.messageView);
        }
    }

    @Override
    public int getItemCount() {
        return messageItemGroupList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_AVATAR;
        }

        return VIEW_TYPE_MESSAGE_ITEM;
    }

    public void setImageStyle(MessageViewModel.ImageStyle imageStyle) {
        this.imageStyle = imageStyle;
    }

    public void setShouldShowAvatar(boolean shouldShowAvatar) {
        this.shouldShowAvatar = shouldShowAvatar;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setMessageItems(String messageId, List<MessageItem> messageItems) {
        messageItemGroupList.clear();

        if (messageItems != null) {
            addMessageItems(messageId, messageItems);
        } else {
            notifyDataSetChanged();
        }
    }

    private Item getItem(int index) {
        // Avatar will always be the first index
        index--;

        if (index >= 0 && index < messageItemGroupList.size()) {
            return messageItemGroupList.get(index);
        }

        return null;
    }

    private MessageView createMessageView(ViewGroup view) {
        MessageView messageView = new MessageView(view.getContext(), delegate);
        view.addView(messageView);

        return messageView;
    }

    private AvatarImageView createAvatarView(ViewGroup view) {
        AvatarImageView avatarImageView = new AvatarImageView(view.getContext());
        view.addView(avatarImageView);

        return avatarImageView;
    }

    private void addMessageItems(String messageId, List<MessageItem> messageItems) {
        if (messageItems != null) {
            for (final MessageItem messageItem : messageItems) {
                addMessageItemInternal(messageItem, messageId);
            }

            MessageViewBuilder.precalculateCarouselHeight(context, messageId, imageStyle, messageItems);

            notifyItemRangeInserted(0, messageItems.size());
        }
    }

    private void addMessageItemInternal(final MessageItem messageItem, String messageId) {
        final Item item = new Item();

        messageItemGroupList.add(item);

        item.messageItem = messageItem;
        item.messageId = messageId;

        if (messageItemGroupList.size() > 1) {
            final Item previousItem = messageItemGroupList.get(messageItemGroupList.size() - 2);
            previousItem.isLastInList = false;
        } else {
            item.isFirstInList = true;
        }

        item.isLastInList = true;
    }
}
