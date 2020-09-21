package com.clarabridge.ui.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;

import com.clarabridge.com.devmarvel.creditcardentry.library.CardValidCallback;
import com.clarabridge.com.devmarvel.creditcardentry.library.CreditCard;
import com.clarabridge.com.devmarvel.creditcardentry.library.CreditCardForm;
import com.clarabridge.core.ActionState;
import com.clarabridge.core.CardSummary;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.MessageAction;
import com.clarabridge.core.PaymentStatus;
import com.clarabridge.core.ClarabridgeChat;
import com.clarabridge.core.User;
import com.clarabridge.ui.R;
import com.clarabridge.ui.drawable.CheckMarkDrawable;
import com.clarabridge.ui.drawable.CloseButtonDrawable;
import com.clarabridge.ui.utils.ResizeAnimation;
import com.clarabridge.ui.utils.SuperscriptSpanAdjuster;

public class StripeFragment extends Fragment implements CardValidCallback, View.OnClickListener {
    private final Conversation conversation = ClarabridgeChat.getConversation();

    private long cents;
    private long dollars;
    private boolean waitingForCardLoad = false;

    private boolean changeCardClicked = false;
    private boolean paymentRequestTimedOut = false;
    private String currency;
    private Bundle arguments;
    private Button buyButton;
    private Button closeButton;
    private ProgressBar payNowSpinner;
    private ProgressBar loadingSpinner;
    private TextView dollarAmount;
    private TextView errorMessage;
    private TextView savedFormText;
    private TextView creditMessage;
    private TextView savedCreditMessage;
    private Button changeCardButton;
    private ImageView savedFormImage;
    private MessageAction messageAction;
    private LinearLayout savedCardForm;
    private CreditCardForm creditCardForm;
    private StripeFragmentListener fragmentListener;
    private CardSummary savedCard;
    private Timer processPaymentTimeout;

    public interface StripeFragmentListener {
        void onStripeFragmentShown();

        void onStripeFragmentClose();

        void onPurchaseComplete();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.clarabridgechat_fragment_stripe, container, false);

        super.onCreate(savedInstanceState);

        try {
            fragmentListener = (StripeFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement StripeFragmentListener");
        }

        arguments = getArguments();
        buyButton = view.findViewById(R.id.creditCardBuy);
        closeButton = view.findViewById(R.id.closeButton);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        payNowSpinner = view.findViewById(R.id.payNowSpinner);
        dollarAmount = view.findViewById(R.id.dollarAmount);
        errorMessage = view.findViewById(R.id.errorMessage);
        creditMessage = view.findViewById(R.id.creditMessage);
        savedCreditMessage = view.findViewById(R.id.savedCreditMessage);
        savedFormText = view.findViewById(R.id.savedFormText);
        savedFormImage = view.findViewById(R.id.savedFormImage);
        messageAction = (MessageAction) arguments.getSerializable("action");
        changeCardButton = view.findViewById(R.id.changeCardButton);
        creditCardForm = view.findViewById(R.id.creditCardForm);
        savedCardForm = view.findViewById(R.id.savedCreditCardForm);

        cents = messageAction.getAmount() % 100;
        dollars = messageAction.getAmount() / 100;
        currency = messageAction.getCurrency() != null ? messageAction.getCurrency().toUpperCase() : "USD";

        setup();
        showCorrectView(view);

        setRetainInstance(true);

        return view;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.stripe_slide_in);
        } else {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.stripe_slide_out);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (messageAction != null && messageAction.getState().equals(ActionState.PAID.getValue())) {
            onPaymentProcessed(PaymentStatus.SUCCESS);
        } else if (paymentRequestTimedOut) {
            onPaymentProcessed(PaymentStatus.ERROR);
            paymentRequestTimedOut = false;
        }
    }

    private void setup() {
        final String centString = cents < 10 ? "0" + cents : "" + cents;
        final String amountString = "$" + dollars + (cents == 0 ? "" : centString);
        final String prettyString = "$" + dollars + (cents == 0 ? "" : "." + centString) + " " + currency;

        fragmentListener.onStripeFragmentShown();

        setupSpinner();
        setupCloseButton();
        setupPriceDisplay(amountString);
        setupCreditMessage(prettyString);

        buyButton.setOnClickListener(this);
        changeCardButton.setOnClickListener(this);

        ColorStateList csl = new ColorStateList(
                new int[][]{new int[0]},
                new int[]{getResources().getColor(R.color.ClarabridgeChat_accent)}
        );
        ((AppCompatButton) buyButton).setSupportBackgroundTintList(csl);

        creditCardForm.setOnCardValidCallback(this);
    }

    private void showSavedCreditCardView(final View view, final CardSummary cardSummary) {
        final Drawable brandDrawable = getBrandDrawableFromString(cardSummary.getBrand());

        clearUI();

        buyButton.setVisibility(View.VISIBLE);
        dollarAmount.setVisibility(View.VISIBLE);
        savedCardForm.setVisibility(View.VISIBLE);
        changeCardButton.setVisibility(View.VISIBLE);
        savedCreditMessage.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= 16) {
            savedFormImage.setBackground(brandDrawable);
        } else {
            savedFormImage.setBackgroundDrawable(brandDrawable);
        }

        savedFormText.setText(String.format("• • • •  • • • •  • • • •  %s", cardSummary.getLast4()));

        resizeView(view);
    }

    private void showEnterCreditCardView(final View view, final boolean fromSavedCardUI) {
        clearUI();

        if (fromSavedCardUI) {
            final Display display = getActivity().getWindowManager().getDefaultDisplay();
            final Point size = new Point();

            display.getSize(size);

            int height = size.y;

            ResizeAnimation resizeAnimation = new ResizeAnimation(
                    view,
                    height,
                    getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_stripeSavedCardHeight)
            );

            resizeAnimation.setDuration(700);
            resizeAnimation.setInterpolator(getActivity(), android.R.anim.decelerate_interpolator);
            view.startAnimation(resizeAnimation);

            buyButton.setVisibility(View.VISIBLE);
            buyButton.setTranslationY(0);
            buyButton.animate().translationY(buyButton.getHeight());
        }

        creditCardForm.setVisibility(View.VISIBLE);
        creditMessage.setVisibility(View.VISIBLE);
        dollarAmount.setVisibility(View.VISIBLE);

        focusCard();
    }

    private void showLoadingView(final View view) {
        clearUI();

        loadingSpinner.setVisibility(View.VISIBLE);
        changeCardButton.setVisibility(View.VISIBLE);

        resizeView(view);

        waitingForCardLoad = true;
    }

    // Fragment state is kept alive on orientation change, must ensure correct view is displayed
    private void showCorrectView(final View view) {
        if (changeCardClicked) {
            showEnterCreditCardView(view, false);
            return;
        }

        if (savedCard != null) {
            showSavedCreditCardView(view, savedCard);
        } else if (User.getCurrentUser().hasPaymentInfo() && conversation != null) {
            conversation.loadCardSummary();
            showLoadingView(view);
        } else {
            showEnterCreditCardView(view, false);
        }
    }

    private void setupCloseButton() {
        final CloseButtonDrawable closeButtonDrawable = new CloseButtonDrawable(getActivity());

        if (Build.VERSION.SDK_INT >= 16) {
            closeButton.setBackground(closeButtonDrawable);
        } else {
            closeButton.setBackgroundDrawable(closeButtonDrawable);
        }

        closeButton.setOnClickListener(this);
    }

    private void setupSpinner() {
        payNowSpinner.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                android.graphics.PorterDuff.Mode.SRC_IN);

        loadingSpinner.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.ClarabridgeChat_accent),
                android.graphics.PorterDuff.Mode.SRC_IN);
    }

    private void setupCreditMessage(final String prettyString) {
        final Context applicationContext = getActivity().getApplicationContext();
        final CharSequence appName = applicationContext
                .getPackageManager().getApplicationLabel(applicationContext.getApplicationInfo());
        final String creditString = getResources()
                .getString(R.string.ClarabridgeChat_creditMessage, prettyString, appName);
        final String savedCreditString = getResources()
                .getString(R.string.ClarabridgeChat_savedCreditMessage, prettyString, appName);
        final Spannable spannableCreditMessage = new SpannableString(creditString);
        final Spannable spannableSavedCreditMessage = new SpannableString(savedCreditString);

        spannableCreditMessage.setSpan(
                new ForegroundColorSpan(Color.BLACK),
                creditString.indexOf(prettyString),
                creditString.indexOf(prettyString) + prettyString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableSavedCreditMessage.setSpan(
                new ForegroundColorSpan(Color.BLACK),
                savedCreditString.indexOf(prettyString),
                savedCreditString.indexOf(prettyString) + prettyString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        creditMessage.setText(spannableCreditMessage);
        savedCreditMessage.setText(spannableSavedCreditMessage);
    }

    private void resizeView(final View view) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();

        params.height = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_stripeSavedCardHeight);
    }

    private void showErrorMessage() {
        creditMessage.setVisibility(View.GONE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    private void hideErrorMessage() {
        // orientation change triggers
        if (errorMessage.getVisibility() == View.VISIBLE) {
            creditMessage.setVisibility(View.VISIBLE);
            errorMessage.setVisibility(View.GONE);
        }
    }

    private void setupPriceDisplay(final String amountString) {
        final Spannable spannableAmount = new SpannableString(amountString);

        spannableAmount.setSpan(new RelativeSizeSpan(0.45f), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableAmount.setSpan(new SuperscriptSpanAdjuster(0.82f), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (cents > 0) {
            spannableAmount.setSpan(
                    new RelativeSizeSpan(0.6f),
                    amountString.length() - 2,
                    amountString.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spannableAmount.setSpan(
                    new SuperscriptSpanAdjuster(0.48f),
                    amountString.length() - 2,
                    amountString.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        dollarAmount.setText(spannableAmount);
    }

    private void clearUI() {
        creditCardForm.setVisibility(View.GONE);
        creditMessage.setVisibility(View.GONE);
        savedCardForm.setVisibility(View.GONE);
        savedCreditMessage.setVisibility(View.GONE);
        dollarAmount.setVisibility(View.GONE);
        changeCardButton.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);
        buyButton.setVisibility(View.GONE);
    }

    private Drawable getBrandDrawableFromString(String string) {
        final Drawable drawable;
        string = string.toLowerCase();

        if (string.equals("visa")) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.visa);
        } else if (string.equals("american express")) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.amex);
        } else if (string.equals("mastercard")) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.master_card);
        } else if (string.equals("discover")) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.discover);
        } else if (string.equals("diners club")) {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.diners_club);
        } else {
            drawable = ContextCompat.getDrawable(getActivity(), R.drawable.unknown_cc);
        }

        return drawable;
    }

    @Override
    public void cardValid(CreditCard card) {
        if (buyButton.getVisibility() != View.VISIBLE) {
            buyButton.setVisibility(View.VISIBLE);
            buyButton.setTranslationY(buyButton.getHeight());

            buyButton.animate().translationY(0);
        }
    }

    @Override
    public void cardInvalid(CreditCard card) {
        buyButton.setVisibility(View.GONE);
        hideErrorMessage();
    }

    @Override
    public void onClick(View clickedView) {
        final CreditCard uiCard;
        com.clarabridge.core.CreditCard creditCard = null;

        if (clickedView.getId() == buyButton.getId()) {
            uiCard = creditCardForm.getCreditCard();

            if (uiCard.getCardNumber().length() != 0) {
                creditCard = new com.clarabridge.core.CreditCard(
                        uiCard.getCardNumber(),
                        uiCard.getExpYear(),
                        uiCard.getExpMonth(),
                        uiCard.getSecurityCode());
            }

            buyButton.setText("");
            buyButton.setEnabled(false);
            payNowSpinner.setVisibility(View.VISIBLE);
            changeCardButton.setVisibility(View.GONE);

            if (conversation != null) {
                conversation.processPayment(creditCard, messageAction);
                startProcessPaymentTimeout();
            }
        }

        if (clickedView.getId() == closeButton.getId()) {
            fragmentListener.onStripeFragmentClose();
        }

        if (clickedView.getId() == changeCardButton.getId()) {
            changeCardClicked = true;
            showEnterCreditCardView(getView(), true);
        }
    }

    public void onPaymentProcessed(final PaymentStatus status) {
        processPaymentTimeout.cancel();
        processPaymentTimeout.purge();

        if (status == PaymentStatus.SUCCESS) {
            final CheckMarkDrawable checkmarkDrawable = new CheckMarkDrawable(getActivity());

            if (Build.VERSION.SDK_INT >= 16) {
                buyButton.setBackground(checkmarkDrawable);
            } else {
                buyButton.setBackgroundDrawable(checkmarkDrawable);
            }

            payNowSpinner.setVisibility(View.GONE);

            fragmentListener.onPurchaseComplete();
        } else if (status == PaymentStatus.ERROR) {
            buyButton.setEnabled(true);
            buyButton.setText(R.string.ClarabridgeChat_btnPayNow);
            payNowSpinner.setVisibility(View.GONE);

            showErrorMessage();
        }
    }

    public void onCardSummaryLoaded(final CardSummary cardSummary) {
        if (!waitingForCardLoad) {
            return;
        }

        waitingForCardLoad = false;
        savedCard = cardSummary;

        if (cardSummary != null) {
            showSavedCreditCardView(getView(), cardSummary);
        } else {
            showEnterCreditCardView(getView(), true);
        }
    }

    private void focusCard() {
        final InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        creditCardForm.focusCreditCard();
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void startProcessPaymentTimeout() {
        processPaymentTimeout = new Timer();
        processPaymentTimeout.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                            onPaymentProcessed(PaymentStatus.ERROR);
                        } else {
                            paymentRequestTimedOut = true;
                        }
                    }
                },
                10000
        );
    }
}

