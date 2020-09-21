package com.clarabridge.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import com.clarabridge.ui.R;
import com.clarabridge.ui.utils.BitmapVisitor;

public class AvatarImageView extends android.support.v7.widget.AppCompatImageView {
    private Bitmap defaultAvatarBitmap;
    private BitmapDrawable defaultAvatarPlaceholder;

    public AvatarImageView(Context context) {
        super(context);
    }

    public AvatarImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void show(String authorAvatarUrl) {
        setTag(R.id.avatar, authorAvatarUrl);
        setLayoutParams();
        setVisibility(View.VISIBLE);

        final Resources resources = getResources();

        if (defaultAvatarBitmap == null) {
            int avatarSize = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatar);
            Bitmap rawBitmap = BitmapFactory.decodeResource(resources, R.drawable.clarabridgechat_img_avatar);

            defaultAvatarBitmap = BitmapVisitor.createRoundedBitmap(rawBitmap, avatarSize);
            defaultAvatarPlaceholder = new BitmapDrawable(defaultAvatarBitmap);
        }

        setImageBitmap(defaultAvatarBitmap);
        setVisibility(View.VISIBLE);

        if (authorAvatarUrl != null) {
            Glide.with(this)
                    .load(authorAvatarUrl)
                    .circleCrop()
                    .placeholder(defaultAvatarPlaceholder)
                    .into(this);
        }
    }

    public void showInvisible() {
        setVisibility(View.INVISIBLE);
        setLayoutParams();
    }

    private void setLayoutParams() {
        Resources resources = getResources();

        int avatarSize = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatar);
        int avatarMargin = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatarMargin);
        int conversationMargin = resources.getDimensionPixelOffset(R.dimen.ClarabridgeChat_conversationMargin);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(avatarSize, avatarSize);

        params.setMargins(conversationMargin, 0, avatarMargin, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        setLayoutParams(params);
    }
}
