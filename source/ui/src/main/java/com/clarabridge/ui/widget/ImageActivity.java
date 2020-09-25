package com.clarabridge.ui.widget;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

import com.clarabridge.ui.R;

public class ImageActivity extends AppCompatActivity implements SubsamplingScaleImageView.OnImageEventListener {
    public static final String MEDIA_URL = "MEDIA_URL";

    private String mediaUrl;
    private TextView errorView;
    private ProgressBar loadingSpinner;
    private SubsamplingScaleImageView imageView;

    private static final String IMAGE_FORMAT_NOT_SUPPORTED = "Image format not supported";
    private static final String GIF_FAILED_TO_LOAD = "Image failed to decode using GIF decoder";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clarabridgechat_activity_image);

        imageView = findViewById(R.id.imageView);
        imageView.setOnImageEventListener(this);

        loadingSpinner = findViewById(R.id.imageLoadingSpinner);
        loadingSpinner.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);

        errorView = findViewById(R.id.imageDisplayError);

        mediaUrl = getIntent().getExtras().getString(MEDIA_URL);

        Glide.with(this)
                .downloadOnly()
                .load(mediaUrl)
                .into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File file, @Nullable Transition<? super File> transition) {
                        imageView.setImage(ImageSource.uri(file.getAbsolutePath()));
                    }
                });
    }

    @Override
    public void onReady() {
    }

    @Override
    public void onImageLoaded() {
        loadingSpinner.setVisibility(View.GONE);
    }

    @Override
    public void onPreviewLoadError(Exception e) {
    }

    @Override
    public void onImageLoadError(Exception e) {
        int maxSize = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_imageMaxSize);

        if (IMAGE_FORMAT_NOT_SUPPORTED.equals(e.getMessage()) || GIF_FAILED_TO_LOAD.equals(e.getMessage())) {
            // loading failed (likely due to gif), load as bitmap instead
            Glide.with(this)
                    .asBitmap()
                    .load(mediaUrl)
                    .into(new SimpleTarget<Bitmap>(maxSize, maxSize) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            imageView.setImage(ImageSource.bitmap(resource));
                        }
                    });
        } else {
            errorView.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTileLoadError(Exception e) {
    }

    @Override
    public void onPreviewReleased() {
    }
}
