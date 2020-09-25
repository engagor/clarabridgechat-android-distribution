package com.clarabridge.features.common;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import com.clarabridge.ui.R;

public class ConversationAvatar extends FrameLayout {

    public ConversationAvatar(@NonNull Context context) {
        super(context);
        init();
    }

    public ConversationAvatar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConversationAvatar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private ImageView avatarView;

    private void init() {
        inflate(getContext(), R.layout.clarabridgechat_conversation_avatar, this);
        avatarView = findViewById(R.id.conversation_avatar);
    }

    /**
     * Shows the avatarData in the avatar view.
     */
    public void show(@NonNull AvatarData avatarData) {
        ConversationUtils.addAvatarDataToGlide(avatarData, Glide.with(this))
                .into(avatarView);
    }
}
