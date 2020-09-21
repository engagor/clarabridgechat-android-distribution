package com.clarabridge.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class BackEventEditText extends EditText {
    private EditTextBackListener editTextBackListener;

    public BackEventEditText(final Context context) {
        super(context);
    }

    public BackEventEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public BackEventEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(final int keyCode, final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (editTextBackListener != null) {
                editTextBackListener.onEditTextBack(this, this.getText().toString());
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void setEditTextBackListener(final EditTextBackListener listener) {
        editTextBackListener = listener;
    }
}

