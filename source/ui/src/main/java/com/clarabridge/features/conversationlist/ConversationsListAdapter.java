package com.clarabridge.features.conversationlist;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.clarabridge.core.Config;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.utils.JavaUtils;
import com.clarabridge.features.common.AvatarData;
import com.clarabridge.features.common.ConversationAvatar;
import com.clarabridge.features.common.ConversationUtils;
import com.clarabridge.ui.R;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.NORMAL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Adapter for the conversations list {@link RecyclerView}
 */
class ConversationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int viewTypeItem = 0;
    private final int viewTypeLoading = 1;

    private final List<Conversation> conversationsList = new ArrayList<>(0);
    private final OnItemClickListener clickListener;
    private Config config;

    /**
     * View holder for each of the conversation items in the conversations list
     */
    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private ConversationAvatar avatarView;
        private TextView conversationName;
        private ConversationListDateView timestamp;
        private TextView messageView;
        private TextView unreadCountView;
        private LinearLayout conversationHeading;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.avatarView = itemView.findViewById(R.id.clarabridgechat_conversations_list_avatar_icon);
            this.conversationName = itemView.findViewById(R.id.conversation_name);
            this.timestamp = itemView.findViewById(R.id.conversationLastUpdated_timestamp);
            this.messageView = itemView.findViewById(R.id.clarabridgechat_conversations_list_message_text);
            this.unreadCountView = itemView.findViewById(R.id.clarabridgechat_conversations_list_unread_count);
            this.conversationHeading = itemView.findViewById(R.id.conversation_heading);
        }

        /**
         * Binds the given conversation and click listener to a {@link RecyclerView} cell
         *
         * @param config        the {@link Config} from ClarabridgeChat
         * @param conversation  the {@link Conversation} belonging to this cell
         * @param clickListener the {@link OnItemClickListener} to be invoked when this item is clicked
         */
        void bind(final Config config,
                  final Conversation conversation,
                  final OnItemClickListener clickListener) {
            Resources resources = messageView.getResources();

            final AvatarData avatarData = AvatarData.from(config, conversation);
            avatarView.show(avatarData);

            conversationName.setText(ConversationUtils.getTitle(conversation, config));

            Long lastUpdatedTime = conversation.getLastUpdatedAt() == null ? null
                    : conversation.getLastUpdatedAt().getTime();
            if (lastUpdatedTime == null) {
                timestamp.setVisibility(GONE);
            } else {
                timestamp.setVisibility(VISIBLE);
            }
            timestamp.setDate(lastUpdatedTime);

            messageView.setText(ConversationUtils.getLastMessage(conversation, itemView.getResources()));

            int unreadCount = conversation.getUnreadCount();
            if (unreadCount == 0) {
                removeUnreadStylingFromConversation();
            } else if (unreadCount <= 9) {
                unreadCountView.setText(String.valueOf(unreadCount));
                addUnreadStylingToConversation();
            } else {
                unreadCountView.setText(resources.getString(R.string.ClarabridgeChat_badgeCountMoreThanOneDigit));
                addUnreadStylingToConversation();
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClicked(conversation);
                }
            });
        }

        private void addUnreadStylingToConversation() {
            messageView.setTypeface(null, BOLD);
            unreadCountView.setVisibility(VISIBLE);
            conversationHeading.setPadding(0, 0, 44, 0);
        }

        private void removeUnreadStylingFromConversation() {
            unreadCountView.setVisibility(GONE);
            messageView.setTypeface(null, NORMAL);
            conversationHeading.setPadding(0, 0, 0, 0);
        }

    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * Definition of a click listener to be invoked when an item in the list is clicked
     */
    interface OnItemClickListener {
        void onItemClicked(Conversation conversation);

    }

    ConversationsListAdapter(@NonNull final OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == viewTypeItem) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.clarabridgechat_conversations_list_item, viewGroup, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.clarabridgechat_conversations_list_loading_item, viewGroup, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof ItemViewHolder) {
            Conversation conversation = conversationsList.get(i);
            ((ItemViewHolder) viewHolder).bind(config, conversation, clickListener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return conversationsList.get(position) == null ? viewTypeLoading : viewTypeItem;
    }

    @Override
    public int getItemCount() {
        return conversationsList.size();
    }

    void updateDataSet(@Nullable List<Conversation> conversationsList) {
        if (conversationsList != null && !conversationsList.isEmpty()
                && !this.conversationsList.containsAll(conversationsList)) {
            this.conversationsList.addAll(conversationsList);
            notifyDataSetChanged();
        }
    }

    void updateConversationInList(Conversation newConversation) {
        final List<Conversation> newList = new ArrayList<>(conversationsList);

        boolean updated = false;
        for (int i = 0; i < newList.size(); i++) {
            Conversation conversation = newList.get(i);
            if (JavaUtils.equals(conversation.getId(), newConversation.getId())) {
                newList.set(i, newConversation);
                updated = true;
                break;
            }
        }

        if (!updated) {
            newList.add(newConversation);
        }
        replaceConversationList(newList);
    }

    void replaceConversationList(@NonNull final List<Conversation> newList) {
        final List<Conversation> oldList = new ArrayList<>(conversationsList);

        //set the new list as the adapter data
        this.conversationsList.clear();
        this.conversationsList.addAll(newList);

        //sort the list with new Conversation at the top
        Collections.sort(this.conversationsList, new Comparator<Conversation>() {
            @Override
            public int compare(Conversation left, Conversation right) {
                if (left == null || left.getLastUpdatedAt() == null) {
                    return 0;
                }
                if (right == null || right.getLastUpdatedAt() == null) {
                    return 0;
                }
                return (left.getLastUpdatedAt().compareTo(right.getLastUpdatedAt())) * -1;
            }
        });

        //calc animations and diffs
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return conversationsList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldListPos, int newListPos) {
                return areConversationsTheSame(oldList.get(oldListPos), conversationsList.get(newListPos));
            }

            @Override
            public boolean areContentsTheSame(int oldListPos, int newListPos) {
                return false;
            }
        }, true);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    List<Conversation> getConversationList() {
        return conversationsList;
    }

    void showLoadMoreView() {
        if (conversationsList.get(conversationsList.size() - 1) != null) {
            conversationsList.add(null);
            notifyItemInserted(conversationsList.size() - 1);
        }
    }

    void hideLoadMoreView() {
        if (!conversationsList.isEmpty()) {
            int position = conversationsList.size() - 1;
            Conversation item = conversationsList.get(position);
            if (item == null) {
                conversationsList.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    private static boolean areConversationsTheSame(Conversation left, Conversation right) {
        return left != null && right != null && JavaUtils.equals(left.getId(), right.getId());
    }

}
