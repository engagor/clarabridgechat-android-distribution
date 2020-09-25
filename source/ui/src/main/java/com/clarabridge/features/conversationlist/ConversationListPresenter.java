package com.clarabridge.features.conversationlist;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.clarabridge.core.Conversation;
import com.clarabridge.core.ConversationDelegateAdapter;
import com.clarabridge.core.ClarabridgeChatCallback;

import static java.net.HttpURLConnection.HTTP_CREATED;

class ConversationListPresenter {

    private final AtomicBoolean hasInternet = new AtomicBoolean(true);
    private final ConversationListView view;
    private final ConversationListUseCase useCase;

    ConversationListPresenter(@NonNull ConversationListView view, @NonNull ConversationListUseCase useCase) {
        this.view = view;
        this.useCase = useCase;
    }

    void onCreate() {
        updateConfig();
        useCase.notifyViewDelegateOnStartActivity();
        loadConversationList();
        listenToConversationChanges();
        listenToNetworkChanges();
    }

    void onDestroy() {
        useCase.removeConversationChangeDelegate();
        useCase.unRegisterForNetworkChanges();
    }

    /**
     * Load the 10 most recently active {@link Conversation}s for the current user, sorted
     * from most recently updated to last.
     */
    void loadConversationList() {
        view.showLoading();
        useCase.getConversationList(new ClarabridgeChatCallback<List<Conversation>>() {
            @Override
            public void run(@NonNull Response<List<Conversation>> response) {
                if (response.getStatus() == 200 && response.getData() != null && !response.getData().isEmpty()) {
                    // in case of a conversation list is returned.
                    view.showConversationList(response.getData());
                } else if (response.getStatus() != 200) {
                    // in case of an error is returned.
                    view.showErrorMessage();
                } else {
                    // in case of no error happened and empty conversation list is returned.
                    view.showEmptyView();
                }
            }
        });
    }

    /**
     * Get the list of the 10 more recently active {@link Conversation}s for the current user, sorted
     * from most recently updated to last.
     */
    void loadMoreConversationList() {
        if (hasInternet.get() && hasMoreConversations()) {
            view.showLoadMoreView();
            useCase.getMoreConversationsList(new ClarabridgeChatCallback<List<Conversation>>() {
                @Override
                public void run(@NonNull Response<List<Conversation>> response) {
                    view.hideLoadMoreView();
                    if (response.getStatus() == 200 && response.getData() != null && !response.getData().isEmpty()) {
                        // in case of a conversation list is returned.
                        view.showConversationList(response.getData());
                    } else if (response.getStatus() != 200) {
                        // in case of an error is returned.
                        view.showErrorMessage();
                    }
                }
            });
        }
    }

    /**
     * Retry getting the current conversations page that failed.
     */
    void retryConversationList() {
        if (hasInternet.get() && hasMoreConversations()) {
            view.showLoadMoreView();
            loadMoreConversationList();
        } else if (hasInternet.get()) {
            loadConversationList();
        }
    }

    /**
     * Accessor method for knowing if there are more conversations to be fetched or not.
     *
     * @return true if there is more conversations to be fetched or false if not.
     */
    private boolean hasMoreConversations() {
        return useCase.hasMoreConversations();
    }

    /**
     * Specifies the create conversation boolean used for showing or hiding the create conversation button in
     * the Conversation List Screen.
     *
     * @param showNewConversationButton the create conversation boolean used for showing or hiding
     *                                  the create conversation button
     */
    void newConversationButtonState(boolean showNewConversationButton) {
        if (useCase.isNewConversationButtonShown(showNewConversationButton)) {
            view.showNewConversationButton();
        } else {
            view.hideNewConversationButton();
        }
    }

    /**
     * This method is for navigating to {@link com.clarabridge.ui.ConversationActivity} screen.
     *
     * @param conversationId the ID of the conversation
     */
    void conversationListItemClicked(String conversationId) {
        useCase.navigateToConversationActivity(conversationId, new ClarabridgeChatCallback<Conversation>() {
            @Override
            public void run(@NonNull Response<Conversation> response) {
                if (response.getError() != null) {
                    view.showToastForResponse(response);
                } else {
                    useCase.navigateToConversationActivity();
                }
            }
        });
    }

    void onNewConversationButtonClicked() {
        //if true, the integrator has a registered and interceptor
        if (useCase.callIntegratorNewConversationInterceptor()) {
            return;
        }

        if (hasInternet.get()) {
            useCase.createNewConversation(new ClarabridgeChatCallback<Void>() {
                @Override
                public void run(@NonNull Response<Void> response) {
                    if (response.getStatus() == HTTP_CREATED) {
                        useCase.navigateToConversationActivity();
                    } else {
                        view.showToastForResponse(response);
                    }
                }
            });
        }
    }

    /**
     * This method is for register to {@link ConversationDelegateAdapter} and overriding
     * {@link ConversationDelegateAdapter#onConversationsListUpdated(List)} for handling
     * the updated list.
     */
    private void listenToConversationChanges() {
        useCase.addConversationChangeDelegate(new ConversationDelegateAdapter() {
            @Override
            public void onConversationsListUpdated(@NonNull List<Conversation> conversationsList) {
                view.updateConversationsInList(conversationsList);
                updateConfig();
            }

            @Override
            public void onUnreadCountChanged(@NonNull Conversation conversation, int unreadCount) {
                view.updateConversationInList(conversation);
            }
        });
    }

    private void listenToNetworkChanges() {
        useCase.registerForNetworkChanges(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean connected) {
                hasInternet.set(connected);
                view.internetViewStatus(connected);
            }
        });
    }

    private void updateConfig() {
        view.setConfig(useCase.getConfig());
    }

}
