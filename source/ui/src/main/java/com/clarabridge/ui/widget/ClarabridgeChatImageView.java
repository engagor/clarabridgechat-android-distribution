package com.clarabridge.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;

import com.clarabridge.ui.R;

public class ClarabridgeChatImageView extends android.support.v7.widget.AppCompatImageView {
    public static final int ROUNDED_CORNER_NONE = 1;
    public static final int ROUNDED_CORNER_TOP_RIGHT = 1 << 1;
    public static final int ROUNDED_CORNER_TOP_LEFT = 1 << 2;
    public static final int ROUNDED_CORNER_BOTTOM_RIGHT = 1 << 3;
    public static final int ROUNDED_CORNER_BOTTOM_LEFT = 1 << 4;

    private final Resources resources = getContext().getResources();
    private final float messageRadius = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageRadius);
    private final float messageCornerRadius = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageCornerRadius);
    private final Path clipPath = new Path();
    private final RectF rect = new RectF();
    private boolean doClip = true;
    private int roundedCorners = ROUNDED_CORNER_NONE;
    private float[] radii;

    public ClarabridgeChatImageView(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT < 18) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        setId(R.id.clarabridgechat_image_view_id);
    }

    public ClarabridgeChatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClarabridgeChatImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setRoundedCorners(int roundedCorners) {
        this.roundedCorners = roundedCorners;
        updateRadii();
    }

    private void updateRadii() {
        float topLeft = (roundedCorners & ROUNDED_CORNER_TOP_LEFT) > 0 ? messageRadius : messageCornerRadius;
        float topRight = (roundedCorners & ROUNDED_CORNER_TOP_RIGHT) > 0 ? messageRadius : messageCornerRadius;
        float bottomRight = (roundedCorners & ROUNDED_CORNER_BOTTOM_RIGHT) > 0 ? messageRadius : messageCornerRadius;
        float bottomLeft = (roundedCorners & ROUNDED_CORNER_BOTTOM_LEFT) > 0 ? messageRadius : messageCornerRadius;

        radii = new float[]{topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft};
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (radii == null) {
            updateRadii();
        }

        rect.set(0, 0, this.getWidth(), this.getHeight());
        clipPath.reset();
        clipPath.addRoundRect(rect, radii, Path.Direction.CW);

        if (doClip) {
            try {
                canvas.clipPath(clipPath);
            } catch (UnsupportedOperationException e) {
                doClip = false;
            }
        }

        super.onDraw(canvas);
    }
}
