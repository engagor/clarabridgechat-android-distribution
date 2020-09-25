package com.clarabridge.ui.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clarabridge.ui.R;

public class ShaderFragment extends Fragment implements View.OnClickListener {
    private ShaderFragmentListener fragmentListener;

    public interface ShaderFragmentListener {
        void onShadedAreaClick();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.clarabridgechat_bg_darkener, container, false);
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        view.setOnClickListener(this);
        view.setClickable(false);

        try {
            fragmentListener = (ShaderFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ShaderFragmentListener");
        }

        return view;
    }

    public void show() {
        final View view = getView();

        if (view != null) {
            view.animate().alpha(1.0f);
            view.setClickable(true);
        }
    }

    public void hide() {
        final View view = getView();

        if (view != null) {
            view.animate().alpha(0f);
            view.setClickable(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (fragmentListener != null) {
            fragmentListener.onShadedAreaClick();
        }
    }
}

