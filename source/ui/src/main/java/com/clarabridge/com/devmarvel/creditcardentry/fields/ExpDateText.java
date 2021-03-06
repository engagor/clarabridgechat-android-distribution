package com.clarabridge.com.devmarvel.creditcardentry.fields;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;

import com.clarabridge.com.devmarvel.creditcardentry.internal.CreditCardUtil;

public class ExpDateText extends CreditEntryFieldBase {

    private String previousString;

    public ExpDateText(Context context) {
        super(context);
        init();
    }

    public ExpDateText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpDateText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {
        super.init();
        setHint("MM/YY");
    }

    /* TextWatcher Implementation Methods */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        previousString = s.toString();
    }

    public void afterTextChanged(Editable s) {
        String updatedString = s.toString();

        // if delete occurred do not format
        if (updatedString.length() > previousString.length()) {
            formatAndSetText(updatedString);
        }

        checkValidity(this.getText().toString());
    }

    public void formatAndSetText(String updatedString) {
        this.removeTextChangedListener(this);
        String formatted = CreditCardUtil.formatExpirationDate(updatedString);
        this.setText(formatted);
        this.setSelection(formatted.length());
        this.addTextChangedListener(this);

        if (formatted.length() == 5) {
            String remainder = null;
            if (updatedString.startsWith(formatted)) {
                remainder = updatedString.replace(formatted, "");
            }
            delegate.onExpirationDateValid(remainder);
        } else if (formatted.length() < updatedString.length()) {
            delegate.onBadInput(this);
        }
    }

    @Override
    public void checkValidity(final String formatted) {
        if (formatted.length() == 5) {
            setValid(true);
        } else {
            setValid(false);
        }
    }
}
