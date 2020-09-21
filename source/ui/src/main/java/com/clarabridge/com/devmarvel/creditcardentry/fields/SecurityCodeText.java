package com.clarabridge.com.devmarvel.creditcardentry.fields;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;

import com.clarabridge.com.devmarvel.creditcardentry.internal.CreditCardUtil;
import com.clarabridge.com.devmarvel.creditcardentry.library.CardType;

public class SecurityCodeText extends CreditEntryFieldBase {
    private CardType type;

    // set length to default to prevent false validity
    private int length = 3;

    public SecurityCodeText(Context context) {
        super(context);
        init();
    }

    public SecurityCodeText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SecurityCodeText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {
        super.init();
        setHint("CVV");
    }

    /* TextWatcher Implementation Methods */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable s) {
        super.afterTextChanged(s);

        if (type == null) {
            this.removeTextChangedListener(this);
            this.setText("");
            this.addTextChangedListener(this);
        }
    }

    public void formatAndSetText(String s) {
        setText(s);
    }

    public void textChanged(CharSequence s, int start, int before, int count) {
        if (type != null) {
            if (s.length() >= length) {
                String remainder = null;
                if (s.length() > length()) {
                    remainder = String.valueOf(s).substring(length);
                }
                this.removeTextChangedListener(this);
                setText(String.valueOf(s).substring(0, length));
                setSelection(length);
                this.addTextChangedListener(this);
                delegate.onSecurityCodeValid(remainder);
            }
        }
    }

    @SuppressWarnings("unused")
    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
        this.length = CreditCardUtil.securityCodeValid(type);
    }

    @Override
    public void checkValidity(final String text) {
        if (text.length() >= length) {
            setValid(true);
        } else {
            setValid(false);
        }
    }
}
