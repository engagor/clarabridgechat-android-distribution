package com.clarabridge.ui.utils;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import com.clarabridge.ui.R;
import com.clarabridge.ui.adapter.CarouselAdapter;

import static com.clarabridge.ui.fragment.ConversationFragment.KEY_RECYCLER_STATE;

public class CarouselSnapHelper extends LinearSnapHelper {
    private RecyclerView recyclerView;
    private OrientationHelper horizontalHelper;

    private static Map<String, Bundle> recyclerViewInstanceStateMap = new HashMap<>();

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {
        int[] out = new int[2];

        out[0] = findScrollDistance(targetView, layoutManager, getHorizontalHelper(layoutManager));
        out[1] = 0;

        return out;
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return getTargetSnapView(layoutManager, getHorizontalHelper(layoutManager));
    }

    private View getTargetSnapView(RecyclerView.LayoutManager layoutManager, OrientationHelper helper) {
        int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        int lastCompletelyVisibleItemPosition =
                ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();

        boolean isLastItem = lastCompletelyVisibleItemPosition == layoutManager.getItemCount() - 1;

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION) {
            return null;
        }

        if (isLastItem) {
            return layoutManager.findViewByPosition(lastCompletelyVisibleItemPosition);
        }

        View child = layoutManager.findViewByPosition(firstVisibleItemPosition);

        int decoratedEnd = helper.getDecoratedEnd(child);
        int decoratedMeasurement = helper.getDecoratedMeasurement(child);

        if (decoratedEnd > 0 && decoratedEnd >= decoratedMeasurement / 2 + getAvatarWidth()) {
            return child;
        } else {
            return layoutManager.findViewByPosition(firstVisibleItemPosition + 1);
        }
    }

    private int findScrollDistance(View targetView,
                                   RecyclerView.LayoutManager layoutManager,
                                   OrientationHelper helper) {
        int targetPosition = layoutManager.getPosition(targetView);

        boolean addMessageItemMargin = targetPosition != 1 && targetPosition != layoutManager.getItemCount() - 1;

        int scrollDistance = helper.getDecoratedStart(targetView) - helper.getStartAfterPadding() - getAvatarWidth();

        if (addMessageItemMargin) {
            scrollDistance += getMessageItemMargin();
        }

        try {
            CarouselAdapter.MessageItemViewHolder itemViewHolder =
                    (CarouselAdapter.MessageItemViewHolder) recyclerView.getChildViewHolder(targetView);
            Bundle recyclerViewState = new Bundle();
            Parcelable listState = recyclerView.getLayoutManager().onSaveInstanceState();
            recyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);

            recyclerViewInstanceStateMap.put(itemViewHolder.item.messageId, recyclerViewState);
        } catch (Exception ignored) {
            // Intentionally empty
        }

        return scrollDistance;
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (horizontalHelper == null) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }

        return horizontalHelper;
    }

    private int getAvatarWidth() {
        Resources resources = recyclerView.getResources();
        return resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatar)
                + resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatarMargin)
                + resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_conversationMargin);
    }

    private int getMessageItemMargin() {
        Resources resources = recyclerView.getResources();
        return resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageItemMargin);
    }

    private int getCarouselItemWidth() {
        Resources resources = recyclerView.getResources();
        return resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_imageDisplayWidth);
    }

    public static Bundle getRecyclerViewInstanceState(String messageId) {
        return recyclerViewInstanceStateMap.get(messageId);
    }
}
