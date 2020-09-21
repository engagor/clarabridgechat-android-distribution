package com.clarabridge.ui.widget;

import android.animation.StateListAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.clarabridge.ui.R;
import com.clarabridge.ui.utils.DpVisitor;

public class ClarabridgeChatButton extends RelativeLayout {
    private Button button = new Button(getContext());
    private ProgressBar spinner = new ProgressBar(getContext());

    public ClarabridgeChatButton(final Context context) {
        super(context);

        final int spinnerSize = (int) DpVisitor.toPixels(getContext(), 25);

        spinner.setVisibility(GONE);
        spinner.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                PorterDuff.Mode.SRC_IN);

        this.addView(button);
        this.addView(spinner);

        final LayoutParams layoutParams = (LayoutParams) spinner.getLayoutParams();

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        layoutParams.height = spinnerSize;
        layoutParams.width = spinnerSize;

        spinner.setLayoutParams(layoutParams);

        button.setMaxLines(1);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.ClarabridgeChat_btnActionText)
        );
    }

    public void setText(final String buttonText) {
        button.setText(buttonText);
    }

    public void setText(final int resourceId) {
        button.setText(resourceId);
    }

    public String getText() {
        return (String) button.getText();
    }

    public void setTextColor(final int color) {
        button.setTextColor(color);
    }

    public void setAllCaps(final boolean allCaps) {
        button.setAllCaps(allCaps);
    }

    public void setMinHeight(final int minHeight) {
        button.setMinHeight(minHeight);
    }

    public void setSpinnerColor(final int color) {
        spinner.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setPadding(final int left, final int top, final int right, final int bottom) {
        button.setPadding(left, top, right, bottom);
    }

    @Override
    public void setMinimumHeight(final int minimumHeight) {
        button.setMinimumHeight(minimumHeight);
    }

    @Override
    public void setBackgroundResource(final int resourceId) {
        super.setBackgroundResource(resourceId);
        button.setBackgroundResource(resourceId);
    }

    @Override
    public void setOnClickListener(final OnClickListener listener) {
        button.setOnClickListener(listener);
    }

    @Override
    public void setStateListAnimator(final StateListAnimator stateListAnimator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setStateListAnimator(stateListAnimator);
        }
    }

    @Override
    public void setGravity(final int gravity) {
        button.setGravity(gravity);
    }

    public void setMaxWidth(final int maxWidth) {
        button.setMaxWidth(maxWidth);
    }

    public void setWidth(final int width) {
        button.getLayoutParams().width = width;
    }

    public void setTypeface(final Typeface typeface) {
        button.setTypeface(typeface);
    }

    public void showLoadingSpinner() {
        spinner.setVisibility(VISIBLE);
        button.setVisibility(INVISIBLE);
    }

    public void hideLoadingSpinner() {
        spinner.setVisibility(GONE);
        button.setVisibility(VISIBLE);
    }
}

