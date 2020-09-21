package com.clarabridge.ui.widget;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import com.clarabridge.ui.R;

public class TypingActivityView extends FrameLayout {

    private List<View> circles = new ArrayList<>();

    public TypingActivityView(Context context) {
        super(context);
        inflate(context, R.layout.clarabridgechat_list_message_typing_activity, this);
        circles.add(findViewById(R.id.clarabridgechat_typing_indicator_1));
        circles.add(findViewById(R.id.clarabridgechat_typing_indicator_2));
        circles.add(findViewById(R.id.clarabridgechat_typing_indicator_3));
    }

    public void animateCircles() {
        for (int i = 0; i < circles.size(); i++) {
            final View circle = circles.get(i);
            long delay = i * 250L;
            final Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(500);
            anim.setStartOffset(delay);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    anim.setStartOffset(0);
                }
            });
            circle.startAnimation(anim);
        }

    }

}
