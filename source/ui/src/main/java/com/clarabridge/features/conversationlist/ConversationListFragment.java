package com.clarabridge.features.conversationlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.clarabridge.core.Conversation;

/**
 * Fragment for displaying a list of {@link Conversation}
 */
public class ConversationListFragment extends Fragment {

    public static final String ARGS_SHOW_NEW_CONVERSTATION = "ARGS_SHOW_NEW_CONVERSTATION";

    private ConversationListView view;
    private ConversationListPresenter presenter;

    public static ConversationListFragment create(boolean showCreateConversation) {
        final Bundle args = new Bundle();
        args.putBoolean(ARGS_SHOW_NEW_CONVERSTATION, showCreateConversation);
        final ConversationListFragment conversationListFragment = new ConversationListFragment();
        conversationListFragment.setArguments(args);
        return conversationListFragment;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        view = new ConversationListView(container.getContext());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        presenter = new ConversationListPresenter(view, new ConversationListUseCase(getActivity(), new ClarabridgeChatProxy()));
        view.setPresenter(presenter);
        presenter.onCreate();
        presenter.newConversationButtonState(getShowCreateConversation());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isAdded() && getActivity() != null) {
            presenter.onDestroy();
        }
    }

    private boolean getShowCreateConversation() {
        final Bundle args = getArguments();
        if (args != null) {
            return args.getBoolean(ARGS_SHOW_NEW_CONVERSTATION, false);
        } else {
            return false;
        }
    }

}
