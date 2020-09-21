package com.clarabridge.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.clarabridge.ui.BuildConfig;
import com.clarabridge.ui.R;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER;

public class WebviewFragment extends Fragment {
    public static final String FRAGMENT_NAME = "WebviewFragment";

    private Handler handler = new Handler();
    private WebviewFragmentListener fragmentListener;

    public interface WebviewFragmentListener {
        void onWebviewShown();

        void onWebviewHidden();
    }

    public class WebViewJavaScriptApi {
        @JavascriptInterface
        public void setTitle(final String title) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    webviewToolbar.setTitle(title);
                }
            });
        }

        @JavascriptInterface
        public void close() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.popBackStack(FRAGMENT_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    fragmentListener.onWebviewHidden();
                }
            });
        }
    }

    private WebView webview;
    private FrameLayout fader;
    private Toolbar webviewToolbar;
    private FrameLayout webviewSpinner;
    private LinearLayout webviewContainer;

    private String url;
    private String size;

    private static final String SIZE_FULL = "full";
    private static final String SIZE_TALL = "tall";
    private static final String SIZE_COMPACT = "compact";

    private int previousOrientation;
    private int previousAvailableHeight;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            fragmentListener = (WebviewFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement WebviewFragmentListener");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.clarabridgechat_fragment_webview, container, false);
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        fader = view.findViewById(R.id.fader);
        webview = view.findViewById(R.id.webview);
        webviewSpinner = view.findViewById(R.id.webviewProgress);
        webviewToolbar = view.findViewById(R.id.webviewToolbar);
        webviewContainer = view.findViewById(R.id.webviewContainer);

        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    doLayout();
                }
            });
        }

        webviewToolbar.setTitle(R.string.ClarabridgeChat_webviewLoading);
        webviewToolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
        webviewToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack(FRAGMENT_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentListener.onWebviewHidden();
            }
        });

        webview.getSettings().setUserAgentString(
                webview.getSettings().getUserAgentString()
                        + " AndroidWebview/"
                        + BuildConfig.VERSION_NAME
        );

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        doLayout();
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        if (enter) {
            final Animation webviewAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.stripe_slide_in);

            webviewAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    webviewSpinner.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    webview.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            webviewToolbar.setTitle(view.getTitle());
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                            webviewSpinner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            if (URLUtil.isNetworkUrl(url)) {
                                return false;
                            }

                            // Otherwise allow the OS to handle it
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                            return true;
                        }

                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                            if (URLUtil.isNetworkUrl(request.getUrl().toString())) {
                                return false;
                            }

                            // Otherwise allow the OS to handle it
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                            startActivity(intent);
                            return true;
                        }
                    });

                    webview.setWebChromeClient(new WebChromeClient() {
                        public void onProgressChanged(WebView view, int progress) {
                            if (progress == 100) {
                                webviewSpinner.setVisibility(View.GONE);
                            }
                        }
                    });

                    webview.getSettings().setJavaScriptEnabled(true);
                    webview.addJavascriptInterface(new WebViewJavaScriptApi(), "AndroidWebviewInterface");
                    webview.loadUrl(url);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            webviewContainer.setAnimation(webviewAnim);
            fader.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));

            webviewContainer.animate();
            fader.animate();
        } else {
            webviewContainer.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.stripe_slide_out));
            webviewContainer.animate();

            return AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        }

        return null;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean goBack() {
        if (webview != null && webview.canGoBack()) {
            webview.goBack();
            return true;
        }

        return false;
    }

    private void doLayout() {
        if (!isAdded()) {
            return;
        }

        int statusBarHeight = 0;
        int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (statusBarResourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(statusBarResourceId);
        }

        Rect r = new Rect();
        webviewContainer.getRootView().getWindowVisibleDisplayFrame(r);
        int availableHeight = r.bottom - statusBarHeight;

        int orientation = getResources().getConfiguration().orientation;

        if (previousOrientation == orientation && previousAvailableHeight == availableHeight) {
            return;
        }

        previousOrientation = orientation;
        previousAvailableHeight = availableHeight;

        boolean isTablet = getResources().getBoolean(R.bool.ClarabridgeChat_isTablet);
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        if (isTablet) {
            params.gravity = CENTER;

            if (isLandscape) {
                params.width = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_webviewTabletWidthPortrait);
            } else {
                params.width = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_webviewTabletWidthLandscape);
            }
        } else {
            params.gravity = BOTTOM;
        }

        if (!isTablet && (isLandscape || size.equals(SIZE_FULL))) {
            // match parent height
        } else {
            switch (size) {
                case SIZE_FULL:
                    params.height = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_webviewTabletHeightFull);
                    break;
                case SIZE_TALL:
                    params.height = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_webviewHeightTall);
                    break;
                case SIZE_COMPACT:
                    params.height = getResources().getDimensionPixelSize(R.dimen.ClarabridgeChat_webviewHeightCompact);
                    break;
            }

            if (params.height > availableHeight) {
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }

        webviewContainer.setLayoutParams(params);
        fragmentListener.onWebviewShown();
    }
}

