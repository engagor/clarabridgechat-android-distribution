package com.clarabridge.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import com.clarabridge.core.MessageAction;
import com.clarabridge.ui.R;

public class RepliesView extends LinearLayout {

    public interface Delegate {
        void onReplySelected(MessageAction reply);
    }

    private Delegate delegate;

    public RepliesView(Context context) {
        super(context);
        init();
    }

    public RepliesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RepliesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public void setReplies(List<MessageAction> replies) {
        removeAllViews();
        LinearLayout currentRow = createButtonRow();

        for (final MessageAction reply : replies) {
            LinearLayout button = new LinearLayout(getContext());
            button.setOrientation(HORIZONTAL);
            LayoutParams buttonParams = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            buttonParams.setMargins(
                    getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingHorizontal),
                    0,
                    0,
                    0
            );
            button.setLayoutParams(buttonParams);

            boolean hasIcon = reply.getIconUrl() != null && !reply.getIconUrl().trim().isEmpty();
            boolean isLocationRequest = reply.getType().equals("locationRequest");

            if (isLocationRequest) {
                //noinspection deprecation
                ImageView icon = createReplyImageView(getResources().getDrawable(R.drawable.location));
                icon.getDrawable().setColorFilter(
                        getContext().getResources().getColor(R.color.ClarabridgeChat_accent),
                        PorterDuff.Mode.SRC_IN
                );
                button.addView(icon);
            } else if (hasIcon) {
                ImageView icon = createReplyImageView(reply.getIconUrl());
                button.addView(icon);
            }

            TextView textView = createReplyTextView();
            textView.setText(reply.getText());
            button.addView(textView);

            Drawable drawable = createReplyDrawable();

            if (Build.VERSION.SDK_INT >= 16) {
                button.setBackground(drawable);
            } else {
                button.setBackgroundDrawable(drawable);
            }

            currentRow.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            button.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (delegate != null) {
                        delegate.onReplySelected(reply);
                    }
                }
            });
            int rowPadding = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingHorizontal);
            int buttonWidth = button.getMeasuredWidth();
            if (hasIcon) {
                buttonWidth += getImageSize();
            }
            int totalWidth = rowPadding + currentRow.getMeasuredWidth() + buttonWidth;
            if (totalWidth > getResources().getDisplayMetrics().widthPixels) {
                addView(currentRow);
                currentRow = createButtonRow();
            }
            currentRow.addView(button);
        }
        addView(currentRow);
    }

    private ImageView createReplyImageView() {
        ImageView imageView = new ImageView(getContext());

        int imageSize = (int) getResources().getDimension(R.dimen.ClarabridgeChat_btnIconSize);

        LayoutParams layoutParams = new LayoutParams(imageSize, imageSize);
        layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_btnIconMargin), 0, 0, 0);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;

        imageView.setLayoutParams(layoutParams);

        return imageView;
    }

    private ImageView createReplyImageView(String iconUrl) {
        ImageView imageView = createReplyImageView();

        Glide.with(this)
                .load(iconUrl)
                .into(imageView);

        return imageView;
    }

    private ImageView createReplyImageView(Drawable drawable) {
        ImageView imageView = createReplyImageView();
        imageView.setImageDrawable(drawable);

        return imageView;
    }

    public int getImageSize() {
        return getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_btnIconMargin)
                + (int) getResources().getDimension(R.dimen.ClarabridgeChat_btnIconSize);
    }

    private Drawable createReplyDrawable() {
        int saturatedColor = getSaturatedColor(getResources().getColor(R.color.ClarabridgeChat_accent));

        GradientDrawable defaultDrawable = new GradientDrawable();
        defaultDrawable.setColor(saturatedColor);
        defaultDrawable.setCornerRadius(getResources().getDimension(R.dimen.ClarabridgeChat_messageRadius));
        defaultDrawable.setStroke(
                getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_replyActionBorder),
                getResources().getColor(R.color.ClarabridgeChat_accent)
        );

        if (Build.VERSION.SDK_INT < 21) {
            return createStateListDrawable(defaultDrawable);
        }

        return createRippleDrawable(defaultDrawable);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable createRippleDrawable(GradientDrawable defaultDrawable) {
        int[] unfocusedPressedState = new int[]{-android.R.attr.state_focused, android.R.attr.state_pressed};
        int[] focusedPressedState = new int[]{android.R.attr.state_focused, android.R.attr.state_pressed};
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        unfocusedPressedState,
                        focusedPressedState
                },
                new int[]{
                        getResources().getColor(R.color.ClarabridgeChat_accentDark),
                        getResources().getColor(R.color.ClarabridgeChat_accentDark)
                }
        );
        return new RippleDrawable(colorStateList, defaultDrawable, null);
    }

    private Drawable createStateListDrawable(Drawable defaultDrawable) {
        int[] unfocusedPressedState = new int[]{-android.R.attr.state_focused, android.R.attr.state_pressed};
        int[] focusedPressedState = new int[]{android.R.attr.state_focused, android.R.attr.state_pressed};
        int[] defaultState = new int[]{};

        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setColor(getResources().getColor(R.color.ClarabridgeChat_accentDark));
        pressedDrawable.setCornerRadius(getResources().getDimension(R.dimen.ClarabridgeChat_messageRadius));

        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(unfocusedPressedState, pressedDrawable);
        drawable.addState(focusedPressedState, pressedDrawable);
        drawable.addState(defaultState, defaultDrawable);
        return drawable;
    }

    private TextView createReplyTextView() {
        TextView textView = new TextView(getContext());
        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.ClarabridgeChat_btnActionText));
        textView.setTextColor(getResources().getColor(R.color.ClarabridgeChat_accent));
        LinearLayout.LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(
                getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingHorizontal),
                getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingTop),
                getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingHorizontal),
                getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingTop)
        );

        textView.setLayoutParams(params);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.ClarabridgeChat_messageText));

        return textView;
    }

    private LinearLayout createButtonRow() {
        LinearLayout buttonRow = new LinearLayout(getContext());
        LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.RIGHT;
        buttonRow.setPadding(0, 0, getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingHorizontal),
                getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingTop));
        buttonRow.setLayoutParams(layoutParams);

        return buttonRow;
    }

    private int getSaturatedColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = isDarkColor(color) ? .04f : .1f;
        hsv[2] = 1;
        return Color.HSVToColor(hsv);
    }

    private boolean isDarkColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        // Same equation used on the other SDKs. Equation extracted from https://24ways.org/2010/calculating-color-contrast/
        float yiq = (red * 299 + green * 587 + blue * 114) / 1000;
        return yiq <= 128;
    }

}
