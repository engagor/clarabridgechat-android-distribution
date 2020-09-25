package com.clarabridge.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.clarabridge.core.Coordinates;
import com.clarabridge.core.MessageAction;
import com.clarabridge.ui.R;
import com.clarabridge.ui.adapter.CarouselAdapter;
import com.clarabridge.ui.utils.CarouselSnapHelper;
import com.clarabridge.ui.utils.FileSize;

import static com.clarabridge.ui.fragment.ConversationFragment.KEY_RECYCLER_STATE;
import static com.clarabridge.ui.widget.ClarabridgeChatImageView.ROUNDED_CORNER_BOTTOM_LEFT;
import static com.clarabridge.ui.widget.ClarabridgeChatImageView.ROUNDED_CORNER_BOTTOM_RIGHT;
import static com.clarabridge.ui.widget.ClarabridgeChatImageView.ROUNDED_CORNER_NONE;
import static com.clarabridge.ui.widget.ClarabridgeChatImageView.ROUNDED_CORNER_TOP_LEFT;
import static com.clarabridge.ui.widget.ClarabridgeChatImageView.ROUNDED_CORNER_TOP_RIGHT;

public class MessageView extends RelativeLayout {
    public interface Delegate {
        void onActionClick(MessageAction action);

        void onMapClick(Coordinates coordinates);

        void onFileClick(String url);
    }

    private Path path;
    private Paint paint;

    private RecyclerView carouselContainer;
    private LinearLayout messageContainer;
    private LinearLayout buttonContainer;
    private FrameLayout messageSeparator;
    private RelativeLayout imageContainer;
    private LinearLayout fileContainer;
    private LinearLayout fileContents;

    public Delegate delegate;
    private ImageView paperClip;
    private ImageSize imageSize;
    private TextView subTextView;
    private TextView mainTextView;
    private SnapHelper snapHelper;
    private RelativeLayout overlay;
    private LayoutStyle layoutStyle;
    private TextView fileNameTextView;
    private TextView fileSizeTextView;
    private ClarabridgeChatImageView imageView;
    private ProgressBar loadingSpinner;
    private LinearLayout loadImageContainer;
    private TypingActivityView typingActivityView;

    public enum ViewCorner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        ;
    }

    public enum ImageSize {
        SMALL,
        LARGE,
        ;
    }

    public enum LayoutStyle {
        RELATIVE,
        FIXED,
        ;
    }

    private Map<MessageAction, ClarabridgeChatButton> clarabridgeChatButtons = new HashMap<>();

    public MessageView(Context context, Delegate delegate) {
        super(context);
        this.delegate = delegate;
        paint = new Paint();
        path = new Path();
        imageSize = ImageSize.SMALL;

        createContainers();
    }

    public void reset() {
        reset(ImageSize.SMALL, LayoutStyle.RELATIVE);
    }

    public void reset(ImageSize imageSize, LayoutStyle layoutStyle) {
        this.imageSize = imageSize;
        this.layoutStyle = layoutStyle;

        // remove and recreate (if necessary) all visible views
        fileContainer.removeAllViews();
        imageContainer.removeAllViews();
        buttonContainer.removeAllViews();
        messageContainer.removeAllViews();
        carouselContainer.removeAllViews();

        createImageView();
        createPaperClip();
        createFileContents();
        createMessageTextView();
        createDescriptionTextView();

        hideSpinner();
        hideImageOverlay();
        hideLoadImageMessage();
        hideSeparator();

        // reset button list
        clarabridgeChatButtons.clear();

        // hide all views
        fileContainer.setVisibility(GONE);
        imageContainer.setVisibility(GONE);
        buttonContainer.setVisibility(GONE);
        messageContainer.setVisibility(GONE);
        carouselContainer.setVisibility(GONE);
        typingActivityView.setVisibility(GONE);

        // set image layout based on size
        imageContainer.setLayoutParams(getImageParams());
        buttonContainer.setLayoutParams(createButtonContainerLayoutParams());

        // reset click listeners
        setOnClickListener(null);
    }

    private void createContainers() {
        createCarouselView();
        createImageContainer();
        createMessageContainer();
        createFileContainer();
        createButtonContainer();
        createTypingActivityView();
    }

    private void createImageView() {
        imageView = new ClarabridgeChatImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(getImageParams());

        imageContainer.addView(imageView);
    }

    private void createPaperClip() {
        int iconWidth = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_fileIconWidth);
        int iconHeight = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_fileIconHeight);

        paperClip = new ImageView(getContext());
        paperClip.setTag(R.drawable.clarabridgechat_btn_paperclip);
        paperClip.setImageResource(R.drawable.clarabridgechat_btn_paperclip);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconWidth, iconHeight);
        layoutParams.gravity = Gravity.CENTER;
        paperClip.setLayoutParams(layoutParams);

        fileContainer.addView(paperClip);
    }

    private void createFileNameTextView() {
        int maxWidth = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_fileNameMaxWidth);

        fileNameTextView = new TextView(getContext());

        fileNameTextView.setSingleLine();
        fileNameTextView.setMaxWidth(maxWidth);
        fileNameTextView.setTypeface(null, Typeface.BOLD);
        fileNameTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        fileNameTextView.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.ClarabridgeChat_messageText)
        );

        fileContents.addView(fileNameTextView);
    }

    private void createFileSizeTextView() {
        fileSizeTextView = new TextView(getContext());
        fileSizeTextView.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.ClarabridgeChat_descriptionText)
        );

        fileContents.addView(fileSizeTextView);
    }

    private void createFileContainer() {
        fileContainer = new LinearLayout(getContext());
        fileContainer.setOrientation(LinearLayout.HORIZONTAL);
        fileContainer.setId(R.id.clarabridgechat_file_container_view_id);
        fileContainer.setLayoutParams(createFileContainerLayoutParams());

        int padding = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_fileContainerMargin);
        fileContainer.setPadding(padding, padding, padding, padding);

        addView(fileContainer);
    }

    private void createFileContents() {
        fileContents = new LinearLayout(getContext());
        fileContents.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_fileNameMargin);
        fileContents.setLayoutParams(layoutParams);

        fileContainer.addView(fileContents);

        createFileNameTextView();
        createFileSizeTextView();
    }

    private void createImageContainer() {
        imageContainer = new RelativeLayout(getContext());
        imageContainer.setId(R.id.clarabridgechat_image_container_view_id);

        addView(imageContainer);
    }

    private void createMessageContainer() {
        messageContainer = new LinearLayout(getContext());

        messageContainer.setOrientation(LinearLayout.VERTICAL);
        messageContainer.setId(R.id.clarabridgechat_message_container_view_id);
        messageContainer.setLayoutParams(createMessageContainerLayoutParams());

        addView(messageContainer);
    }

    private void createButtonContainer() {
        buttonContainer = new LinearLayout(getContext());

        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setId(R.id.clarabridgechat_button_container_view_id);
        buttonContainer.setLayoutParams(createButtonContainerLayoutParams());

        addView(buttonContainer);
    }

    private void createCarouselView() {
        carouselContainer = new RecyclerView(getContext());
        carouselContainer.setId(R.id.clarabridgechat_carousel_container_view_id);

        snapHelper = new CarouselSnapHelper();
        snapHelper.attachToRecyclerView(carouselContainer);

        addView(carouselContainer);
    }

    private LayoutParams getImageParams() {
        LayoutParams params;

        int imageDisplayWidth = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_imageDisplayWidth);
        int imageDisplayHeight = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_imageDisplayHeight);

        if (imageSize == ImageSize.SMALL) {
            params = new LayoutParams(imageDisplayWidth, imageDisplayHeight);
        } else {
            params = new LayoutParams(imageDisplayWidth, imageDisplayWidth);
        }

        return params;
    }

    @NonNull
    private LayoutParams createMessageContainerLayoutParams() {
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(ALIGN_LEFT);
        params.addRule(BELOW, imageContainer.getId());
        return params;
    }

    @NonNull
    private LayoutParams createFileContainerLayoutParams() {
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(BELOW, messageContainer.getId());

        return params;
    }

    @NonNull
    private LayoutParams createButtonContainerLayoutParams() {
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(CENTER_HORIZONTAL);

        if (layoutStyle == LayoutStyle.RELATIVE) {
            params.addRule(BELOW, messageContainer.getId());
        } else {
            params.addRule(ALIGN_PARENT_BOTTOM);
        }

        return params;
    }

    private void createMessageTextView() {
        mainTextView = new TextView(getContext());
        mainTextView.setMaxWidth(getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messageMaxSize));

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_LEFT);

        messageContainer.addView(mainTextView, params);
    }

    private void createDescriptionTextView() {
        subTextView = new TextView(getContext());
        subTextView.setMaxWidth(getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messageMaxSize));
        messageContainer.addView(subTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        subTextView.setVisibility(GONE);
    }

    private void createTypingActivityView() {
        typingActivityView = new TypingActivityView(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(CENTER_IN_PARENT);
        addView(typingActivityView, layoutParams);
        typingActivityView.setVisibility(GONE);
    }

    public TextView getMainTextView() {
        return this.mainTextView;
    }

    public TextView getSubTextView() {
        return this.subTextView;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = canvas.getHeight();

        path.reset();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(0, height);
        path.close();

        paint.setAntiAlias(true);
        paint.setDither(true);

        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);

        if (messageContainer != null) {
            messageContainer.setOnClickListener(l);
        }

        if (buttonContainer != null) {
            buttonContainer.setOnClickListener(l);
        }

        if (mainTextView != null) {
            mainTextView.setOnClickListener(l);
        }

        if (subTextView != null) {
            subTextView.setOnClickListener(l);
        }

        if (imageContainer != null) {
            imageContainer.setOnClickListener(l);
        }

        if (loadImageContainer != null) {
            loadImageContainer.setOnClickListener(l);
        }

        if (fileContainer != null) {
            fileContainer.setOnClickListener(l);
        }
    }

    public void setMainText(SpannableString text) {
        mainTextView.setText(text);
        mainTextView.setVisibility(VISIBLE);
        messageContainer.setVisibility(VISIBLE);
    }

    public void setSubText(SpannableString text) {
        subTextView.setText(text);
        subTextView.setVisibility(VISIBLE);
        messageContainer.setVisibility(VISIBLE);
    }

    public void setImage(final String imageUrl,
                         final long imageSize,
                         final boolean loadImageFromCache,
                         final boolean isRemote,
                         final Runnable onLoadingComplete,
                         final OnClickListener previewOverride) {
        if (imageContainer.getVisibility() == VISIBLE && onLoadingComplete != null) {
            onLoadingComplete.run();
            return;
        }

        showSpinner(isRemote);

        RequestBuilder glideRequestBuilder = Glide.with(this)
                .load(imageUrl);

        if (loadImageFromCache) {
            glideRequestBuilder.onlyRetrieveFromCache(true);
        }

        glideRequestBuilder.listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e,
                                        Object model,
                                        Target<Drawable> target,
                                        boolean isFirstResource) {
                loadOnTap(imageUrl, imageSize, loadImageFromCache, isRemote, previewOverride);

                if (onLoadingComplete != null) {
                    onLoadingComplete.run();
                }

                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource,
                                           Object model,
                                           Target<Drawable> target,
                                           DataSource dataSource,
                                           boolean isFirstResource) {
                if (previewOverride != null) {
                    imageContainer.setOnClickListener(previewOverride);
                } else if (!hasOnClickListeners()) {
                    prepareImagePreview(imageUrl);
                }

                hideSpinner();

                if (onLoadingComplete != null) {
                    onLoadingComplete.run();
                }

                return false;
            }
        }).into(this.imageView);

        imageView.setVisibility(VISIBLE);
        imageView.setTag(R.id.clarabridgechat_image_view_id, imageUrl);
        imageContainer.setVisibility(VISIBLE);
    }

    public void setImage(Bitmap image) {
        imageView.setImageBitmap(image);
        imageContainer.setVisibility(VISIBLE);
    }

    public void setFile(File file, long sizeInBytes, boolean isRemote) {
        fileNameTextView.setText(file.getName());
        showFile(sizeInBytes, isRemote);
    }

    public void setFileByUrl(final String fileUrl, long sizeInBytes, boolean isRemote) {
        String fileName = null;

        try {
            fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
        } catch (Exception ignored) {
            // Exception should never be thrown during decoding
            // In case it does somehow, fallback to encoded file name
        }

        fileNameTextView.setText(fileName);
        showFile(sizeInBytes, isRemote);

        fileContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.onFileClick(fileUrl);
            }
        });
    }

    public void showFile(long sizeInBytes, boolean isRemote) {
        Resources resources = getResources();
        fileContainer.setVisibility(VISIBLE);
        int userTextColor = resources.getColor(R.color.ClarabridgeChat_userMessageText);
        int remoteTextColor = resources.getColor(R.color.ClarabridgeChat_remoteMessageText);
        int inputHintColor = resources.getColor(R.color.ClarabridgeChat_inputTextColorHint);

        if (!isRemote) {
            paperClip.setColorFilter(userTextColor, android.graphics.PorterDuff.Mode.SRC_IN);
            fileNameTextView.setTextColor(userTextColor);
            fileSizeTextView.setTextColor(userTextColor);
        } else {
            paperClip.setColorFilter(remoteTextColor, android.graphics.PorterDuff.Mode.SRC_IN);
            fileNameTextView.setTextColor(remoteTextColor);
            fileSizeTextView.setTextColor(inputHintColor);
        }

        if (sizeInBytes == 0) {
            fileSizeTextView.setVisibility(GONE);
        } else {
            fileSizeTextView.setVisibility(VISIBLE);
            fileSizeTextView.setText(FileSize.getReadableSize(sizeInBytes));
        }

        if (hasText()) {
            showMessageSeparator(isRemote);
        }
    }

    public ClarabridgeChatImageView getImage() {
        return this.imageView;
    }

    public boolean hasText() {
        return mainTextView != null && mainTextView.getText().length() > 0;
    }

    public void showInProgressImageState() {
        showImageOverlay();
        showSpinner(false);
    }

    public void showFailedImageState() {
        showImageOverlay();
        hideSpinner();
    }

    public void setFileUploadInProgress() {
        showSpinner(false);
    }

    public void setFileUploadFailed() {
        hideSpinner();
    }

    public void loadOnTap(String imageUrl,
                          long imageSize,
                          boolean failedFromCache,
                          boolean isRemote,
                          OnClickListener previewOverride) {
        hideSpinner();
        imageView.setVisibility(GONE);
        showLoadImageMessage(imageUrl, imageSize, failedFromCache, isRemote, previewOverride);
    }

    public ClarabridgeChatButton addClarabridgeChatButton(final MessageAction messageAction) {
        if (!clarabridgeChatButtons.containsKey(messageAction)) {
            ClarabridgeChatButton clarabridgeChatButton = new ClarabridgeChatButton(getContext());
            clarabridgeChatButton.setText(messageAction.getText());

            clarabridgeChatButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    delegate.onActionClick(messageAction);
                }
            });

            buttonContainer.addView(clarabridgeChatButton);
            buttonContainer.setVisibility(VISIBLE);

            clarabridgeChatButtons.put(messageAction, clarabridgeChatButton);
        }

        return clarabridgeChatButtons.get(messageAction);
    }

    public void prepareImagePreview(final String mediaUrl) {
        imageContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageActivityIntent = new Intent(getContext(), ImageActivity.class);
                imageActivityIntent.putExtra(ImageActivity.MEDIA_URL, mediaUrl);
                getContext().startActivity(imageActivityIntent);
            }
        });
    }

    private void showSpinner(boolean isRemote) {
        showSpinner(isRemote, getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_imageSpinner));
    }

    public void showSpinner(boolean isRemote, int spinnerSize) {
        int messagePaddingTop = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingTop);
        int messagePaddingBottom = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingBottom);
        int messagePaddingHorizontal = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingHorizontal);

        if (this.loadingSpinner == null) {
            LayoutParams params = new LayoutParams(spinnerSize, spinnerSize);

            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.setMargins(messagePaddingHorizontal,
                    messagePaddingTop,
                    messagePaddingHorizontal,
                    messagePaddingBottom);

            this.loadingSpinner = new ProgressBar(getContext());
            imageContainer.addView(this.loadingSpinner);

            this.loadingSpinner.setLayoutParams(params);

            if (!isRemote) {
                this.loadingSpinner.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                        android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }

    public CarouselAdapter showCarousel(String messageId) {
        CarouselAdapter carouselAdapter = new CarouselAdapter(getContext(), delegate);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );

        carouselContainer.setVisibility(VISIBLE);
        carouselContainer.setAdapter(carouselAdapter);
        carouselContainer.setLayoutManager(layoutManager);

        Bundle instanceState = CarouselSnapHelper.getRecyclerViewInstanceState(messageId);

        if (instanceState != null) {
            Parcelable listState = instanceState.getParcelable(KEY_RECYCLER_STATE);
            layoutManager.onRestoreInstanceState(listState);
        }

        return carouselAdapter;
    }

    private void hideSpinner() {
        if (loadingSpinner != null) {
            imageContainer.removeView(loadingSpinner);
            loadingSpinner = null;
        }
    }

    private void showImageOverlay() {
        if (overlay == null) {
            overlay = new RelativeLayout(getContext());
            overlay.setLayoutParams(new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            imageContainer.addView(overlay);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                overlay.setBackground(getResources().getDrawable(R.drawable.clarabridgechat_bg_list));
            } else {
                overlay.setBackgroundColor(Color.WHITE);
            }
        }

        overlay.getBackground().setAlpha(175);
    }

    private void hideImageOverlay() {
        if (overlay != null) {
            imageContainer.removeView(overlay);
            overlay = null;
        }
    }

    private void showLoadImageMessage(final String imageUrl,
                                      final long mediaSize,
                                      final boolean failedFromCache,
                                      final boolean isRemote,
                                      final OnClickListener previewOverride) {
        showLoadImageContainer(isRemote, mediaSize, failedFromCache);

        loadImageContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLoadImageMessage();
                setImage(imageUrl, mediaSize, false, isRemote, null, previewOverride);

                setOnClickListener(null);
            }
        });
    }

    public void showLoadImageContainer(boolean isRemote, long mediaSize, boolean failedFromCache) {
        if (loadImageContainer == null) {
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            loadImageContainer = new LinearLayout(getContext());
            loadImageContainer.setLayoutParams(getImageParams());
            loadImageContainer.setId(R.id.clarabridgechat_load_image_container_view_id);

            loadImageContainer.setOrientation(LinearLayout.VERTICAL);
            loadImageContainer.setAnimationCacheEnabled(true);
            loadImageContainer.setAlpha(0);

            TextView previewNotAvailableMessage = new TextView(getContext());
            previewNotAvailableMessage.setText(R.string.ClarabridgeChat_imageUploadPreviewNotAvailable);
            previewNotAvailableMessage.setLayoutParams(params);

            TextView loadMessage = new TextView(getContext());

            if (failedFromCache) {
                loadMessage.setText(getResources().getString(R.string.ClarabridgeChat_imageUploadTapToView,
                        mediaSize == 0 ? "" : " " + FileSize.getReadableSize(mediaSize)));
            } else {
                loadMessage.setText(getResources().getString(R.string.ClarabridgeChat_imageUploadRetry));
            }

            loadMessage.setLayoutParams(params);

            ImageView cameraIcon = new ImageView(getContext());
            cameraIcon.setImageResource(android.R.drawable.ic_menu_camera);
            cameraIcon.setLayoutParams(params);

            loadImageContainer.addView(cameraIcon);
            loadImageContainer.addView(previewNotAvailableMessage);
            loadImageContainer.addView(loadMessage);
            loadImageContainer.setGravity(Gravity.CENTER);

            if (isRemote) {
                previewNotAvailableMessage.setTextColor(getResources().getColor(R.color.ClarabridgeChat_accent));
                loadMessage.setTextColor(getResources().getColor(R.color.ClarabridgeChat_accent));
                cameraIcon.setColorFilter(getResources().getColor(R.color.ClarabridgeChat_accent),
                        PorterDuff.Mode.SRC_IN);
            } else {
                previewNotAvailableMessage.setTextColor(Color.WHITE);
                loadMessage.setTextColor(Color.WHITE);
                cameraIcon.setColorFilter(Color.WHITE);
            }

            imageContainer.addView(loadImageContainer);
            loadImageContainer.animate().alpha(1.0f).setDuration(200);
        }

        imageContainer.setVisibility(VISIBLE);
    }

    private void hideLoadImageMessage() {
        if (loadImageContainer != null) {
            imageContainer.removeView(loadImageContainer);
            loadImageContainer = null;
        }
    }

    private void hideSeparator() {
        if (messageSeparator != null) {
            removeView(messageSeparator);
            messageSeparator = null;
        }
    }

    private void showMessageSeparator(boolean isRemote) {
        if (messageSeparator == null) {
            messageSeparator = new FrameLayout(getContext());

            if (isRemote) {
                messageSeparator.setBackgroundResource(R.drawable.clarabridgechat_message_separator_remote);
            } else {
                messageSeparator.setBackgroundResource(R.drawable.clarabridgechat_message_separator_user);
            }

            messageSeparator.setAlpha(0.3f);
            fileContainer.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            messageContainer.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            int messagePaddingHorizontal = getResources()
                    .getDimensionPixelSize(R.dimen.ClarabridgeChat_messagePaddingHorizontal);
            int messageSeparatorWidth = Math.max(
                    messageContainer.getMeasuredWidth(),
                    fileContainer.getMeasuredWidth()
            ) - messagePaddingHorizontal * 2;

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    messageSeparatorWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(messagePaddingHorizontal, 0, messagePaddingHorizontal, 0);
            layoutParams.addRule(BELOW, messageContainer.getId());
            messageSeparator.setLayoutParams(layoutParams);

            addView(messageSeparator);
        }
    }

    public void showTypingActivity() {
        typingActivityView.setVisibility(VISIBLE);
        typingActivityView.animateCircles();
    }

    public void mutateDrawable(Map<ViewCorner, Boolean> messageRoundedCorners, boolean isRemote, boolean isUnsent) {
        Resources resources = getResources();
        Drawable background = getBackground();

        float messageRadius = resources.getDimension(R.dimen.ClarabridgeChat_messageRadius);
        float messageCornerRadius = resources.getDimension(R.dimen.ClarabridgeChat_messageCornerRadius);

        int bgColor;

        if (isRemote) {
            bgColor = resources.getColor(R.color.ClarabridgeChat_remoteMessageBackground);
        } else if (isUnsent) {
            bgColor = resources.getColor(R.color.ClarabridgeChat_userMessageUnsentBackground);
        } else {
            bgColor = resources.getColor(R.color.ClarabridgeChat_userMessageBackground);
        }

        background.mutate();

        float topLeft = messageRoundedCorners.get(ViewCorner.TOP_LEFT) ? messageRadius : messageCornerRadius;
        float topRight = messageRoundedCorners.get(ViewCorner.TOP_RIGHT) ? messageRadius : messageCornerRadius;
        float bottomLeft = messageRoundedCorners.get(ViewCorner.BOTTOM_LEFT) ? messageRadius : messageCornerRadius;
        float bottomRight = messageRoundedCorners.get(ViewCorner.BOTTOM_RIGHT) ? messageRadius : messageCornerRadius;

        if (background instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) background;

            for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
                if (layerDrawable.getDrawable(i) instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(0);

                    gradientDrawable.setCornerRadii(new float[]{
                            topLeft, topLeft,
                            topRight, topRight,
                            bottomRight, bottomRight,
                            bottomLeft, bottomLeft});

                    gradientDrawable.setColor(bgColor);

                    break;
                }
            }
        }
    }

    public void updateImageCorners(Map<ViewCorner, Boolean> imageRoundedCorners) {
        int roundedCorners = ROUNDED_CORNER_NONE;

        if (imageRoundedCorners.get(ViewCorner.TOP_LEFT)) {
            roundedCorners |= ROUNDED_CORNER_TOP_LEFT;
        }

        if (imageRoundedCorners.get(ViewCorner.TOP_RIGHT)) {
            roundedCorners |= ROUNDED_CORNER_TOP_RIGHT;
        }

        boolean hasExtraContent = messageContainer.getVisibility() == VISIBLE
                || (clarabridgeChatButtons != null && clarabridgeChatButtons.size() > 0);

        // Don't round the corners if there is content below
        if (!hasExtraContent) {
            if (imageRoundedCorners.get(ViewCorner.BOTTOM_LEFT)) {
                roundedCorners |= ROUNDED_CORNER_BOTTOM_LEFT;
            }

            if (imageRoundedCorners.get(ViewCorner.BOTTOM_RIGHT)) {
                roundedCorners |= ROUNDED_CORNER_BOTTOM_RIGHT;
            }
        }

        imageView.setRoundedCorners(roundedCorners);
    }
}

