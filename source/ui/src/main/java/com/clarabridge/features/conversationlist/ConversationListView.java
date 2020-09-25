package com.clarabridge.features.conversationlist;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import com.clarabridge.core.Config;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.ClarabridgeChatCallback;
import com.clarabridge.ui.R;
import com.clarabridge.ui.utils.DpVisitor;
import com.clarabridge.ui.widget.EndlessRecyclerViewScrollListener;

class ConversationListView extends RelativeLayout {

    private static final int ERROR_STATE_HIDDEN = 0;
    private static final int ERROR_STATE_NO_INTERNET = 1;
    private static final int ERROR_STATE_LIST_FAILED = 2;

    private TextView emptyState;

    private TextView errorText;
    private ImageView errorRetryButton;
    private View errorStateContainer;

    private RecyclerView recyclerView;
    private Button newConversationButton;
    private ProgressBar progressBar;

    private ConversationListPresenter presenter;
    private ConversationsListAdapter conversationsListAdapter;

    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

    private int currentErrorState = ERROR_STATE_HIDDEN;

    private final Runnable scrollToTopList = new Runnable() {
        @Override
        public void run() {
            recyclerView.scrollToPosition(0);
        }
    };

    ConversationListView(@NonNull Context context) {
        super(context);

        inflate(context, R.layout.clarabridgechat_conversation_list_view, this);

        emptyState = findViewById(R.id.clarabridgechat_conversation_list_empty_state_text_view);

        errorStateContainer = findViewById(R.id.clarabridgechat_conversation_list_error_state_container);
        errorRetryButton = findViewById(R.id.clarabridgechat_conversation_list_error_retry);
        errorText = findViewById(R.id.clarabridgechat_conversation_list_error_text_view);

        recyclerView = findViewById(R.id.clarabridgechat_conversation_list_recycler_view);
        newConversationButton = findViewById(R.id.clarabridgechat_new_conversation_button);
        progressBar = findViewById(R.id.clarabridgechat_conversation_list_loading_spinner);

        setupNewConversationButton();
        setupRecyclerView();

        errorRetryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.retryConversationList();
            }
        });

    }

    void setPresenter(ConversationListPresenter presenter) {
        this.presenter = presenter;
    }

    void showConversationList(@NonNull List<Conversation> conversationList) {
        if (conversationList.isEmpty()) {
            conversationsListAdapter.updateDataSet(Collections.<Conversation>emptyList());
            showEmptyView();
        } else {
            conversationsListAdapter.updateDataSet(conversationList);
            showList();
            loadSecondPageIfNeededToFillList();
        }
    }

    void updateConversationInList(Conversation conversation) {
        if (conversation == null || conversation.getId() == null) {
            return;
        }
        showList();
        loadSecondPageIfNeededToFillList();
        conversationsListAdapter.updateConversationInList(conversation);
    }

    void updateConversationsInList(@Nullable List<Conversation> conversations) {
        if (conversations == null) {
            return;
        }
        showList();
        if (conversations.size() <= 10) {
            endlessRecyclerViewScrollListener.resetState();
        }
        conversationsListAdapter.replaceConversationList(conversations);
        loadSecondPageIfNeededToFillList();
    }

    void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        hideErrorMessage();
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    void showList() {
        progressBar.setVisibility(View.GONE);
        hideErrorMessage();
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    void showEmptyView() {
        emptyState.setVisibility(View.VISIBLE);
        hideErrorMessage();
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideErrorMessage() {
        errorStateContainer.setVisibility(View.GONE);
        currentErrorState = ERROR_STATE_HIDDEN;
    }

    void showErrorMessage() {
        //show the empty state
        emptyState.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        //only change the banner when it is not showing the no internet banner
        if (currentErrorState != ERROR_STATE_NO_INTERNET) {
            errorText.setText(R.string.ClarabridgeChat_conversationListError);
            errorStateContainer.setVisibility(View.VISIBLE);
            currentErrorState = ERROR_STATE_LIST_FAILED;
        }
    }

    void showNewConversationButton() {
        recyclerView.setPadding(0, recyclerView.getPaddingTop(),
                0, (int) DpVisitor.toPixels(getContext(), 70));
        newConversationButton.setVisibility(View.VISIBLE);
    }

    void hideNewConversationButton() {
        recyclerView.setPadding(0, recyclerView.getPaddingTop(),
                0, recyclerView.getPaddingBottom());
        newConversationButton.setVisibility(View.GONE);
    }

    void internetViewStatus(boolean connected) {
        //always show no internet state
        errorText.setText(R.string.ClarabridgeChat_errorUserOffline);
        if (!connected) {
            errorStateContainer.setVisibility(View.VISIBLE);
            errorRetryButton.setVisibility(View.GONE);
            currentErrorState = ERROR_STATE_NO_INTERNET;
        } else {
            errorStateContainer.setVisibility(View.GONE);
            currentErrorState = ERROR_STATE_HIDDEN;
        }
    }

    void showLoadMoreView() {
        conversationsListAdapter.showLoadMoreView();
    }

    void hideLoadMoreView() {
        conversationsListAdapter.hideLoadMoreView();
    }

    void setConfig(Config config) {
        conversationsListAdapter.setConfig(config);
    }

    @SuppressWarnings("rawtypes")
    void showToastForResponse(ClarabridgeChatCallback.Response response) {
        Toast.makeText(getContext(), response.getError(), Toast.LENGTH_SHORT).show();
    }

    private void setupNewConversationButton() {
        // Doing this that way to support android version below 23 and 21. Since, setting tint color from xml
        // directly is only support from 21 and above.
        Drawable newConversationDrawable = newConversationButton.getCompoundDrawables()[0];
        if (newConversationDrawable != null) {
            Drawable wrappedNewConversationDrawable = DrawableCompat.wrap(newConversationDrawable);
            DrawableCompat.setTint(
                    wrappedNewConversationDrawable,
                    ContextCompat.getColor(getContext(), R.color.ClarabridgeChat_btnNewConversationIconColor)
            );
        }

        newConversationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onNewConversationButtonClicked();
            }
        });
    }

    private void setupRecyclerView() {
        conversationsListAdapter = new ConversationsListAdapter(new ConversationsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(Conversation conversation) {
                presenter.conversationListItemClicked(conversation.getId());
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(conversationsListAdapter);


        endlessRecyclerViewScrollListener =
                new EndlessRecyclerViewScrollListener((LinearLayoutManager) recyclerView.getLayoutManager()) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        presenter.loadMoreConversationList();
                    }
                };
        recyclerView.addOnScrollListener(endlessRecyclerViewScrollListener);
    }

    private void loadSecondPageIfNeededToFillList() {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (!isRecyclerScrollable()) {
                    presenter.loadMoreConversationList();
                }
            }
        });
    }

    public boolean isRecyclerScrollable() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (layoutManager == null || adapter == null) {
            return false;
        }

        return layoutManager.findLastVisibleItemPosition() < adapter.getItemCount() - 1;
    }
}
