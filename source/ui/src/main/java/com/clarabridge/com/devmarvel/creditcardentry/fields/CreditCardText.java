package com.clarabridge.com.devmarvel.creditcardentry.fields;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.Gravity;

import com.clarabridge.com.devmarvel.creditcardentry.internal.CreditCardUtil;
import com.clarabridge.com.devmarvel.creditcardentry.library.CardType;

public class CreditCardText extends CreditEntryFieldBase {
    private CardType type;

    public CreditCardText(Context context) {
        super(context);
        init();
    }

    public CreditCardText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CreditCardText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @SuppressLint("RtlHardcoded")
    @Override
    void init() {
        super.init();
        setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
    }

    /* TextWatcher Implementation Methods */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String number = s.toString();

        if (number.length() >= CreditCardUtil.CC_LEN_FOR_TYPE) {
            formatAndSetText(number);
        } else {
            if (this.type != null) {
                this.type = null;
                delegate.onCardTypeChange(CardType.INVALID);
            }
        }

        checkValidity(number);
    }

    @Override
    public void formatAndSetText(String number) {
        CardType type = CreditCardUtil.findCardType(number);

        if (type.equals(CardType.INVALID)) {
            delegate.onBadInput(this);
            return;
        }

        if (this.type != type) {
            delegate.onCardTypeChange(type);
        }
        this.type = type;

        String formatted = CreditCardUtil.formatForViewing(number, type);
        if (!number.equalsIgnoreCase(formatted)) {
            this.removeTextChangedListener(this);
            this.setText(formatted);
            this.setSelection(formatted.length());
            this.addTextChangedListener(this);
        }

        if (formatted.length() >= CreditCardUtil.lengthOfFormattedStringForType(type)) {

            String remainder = null;
            if (number.startsWith(formatted)) {
                remainder = number.replace(formatted, "");
            }
            if (CreditCardUtil.isValidNumber(formatted)) {
                delegate.onCreditCardNumberValid(remainder);
            } else {
                delegate.onBadInput(this);
            }
        }
    }

    public CardType getType() {
        return type;
    }

    public void checkValidity(String number) {
        if (getType() != null && number.length() > CreditCardUtil.lengthOfFormattedStringForType(getType())) {
            number = number.substring(0, number.length() - 1);
        }
        if (CreditCardUtil.isValidNumber(number)) {
            setValid(true);
        } else {
            setValid(false);
        }
    }
}
