package com.chenfei.contentlistfragment.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chenfei.contentlistfragment.library.LazyLoadFragment;
import com.chenfei.contentlistfragment.library.R;

/**
 * 用于显示界面中内容填充状态的帮助View
 * Created by Admin on 2015/10/28.
 */
public class ListStateView extends FrameLayout implements LazyLoadFragment.StateView {
    private TextView mNetworkError;
    private TextView mEmpty;
    private LazyLoadFragment.OnStateViewClickListener mOnStateViewClickListener;

    public ListStateView(Context context) {
        super(context);
    }

    public ListStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListStateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        mNetworkError = (TextView) findViewById(R.id.networkError);
        mEmpty = (TextView) findViewById(R.id.emptyHint);

        mNetworkError.setVisibility(View.GONE);
        mEmpty.setVisibility(View.GONE);
    }

    @Override
    public void setOnStateViewClickListener(LazyLoadFragment.OnStateViewClickListener onStateViewClickListener) {
        mOnStateViewClickListener = onStateViewClickListener;
        if (mOnStateViewClickListener == null) {
            setClickable(false);
        } else {
            setOnClickListener(view -> {
                if (mOnStateViewClickListener == null)
                    return;
                if (mEmpty.getVisibility() == VISIBLE) {
                    mOnStateViewClickListener.onEmptyContentClick();
                }
                if (mNetworkError.getVisibility() == VISIBLE) {
                    mOnStateViewClickListener.onNetWorkErrorClick();
                }
            });
        }
    }

    @Override
    public void setEmptyHint(@DrawableRes int drawable, @StringRes int string) {
        setEmptyHint(drawable, getContext().getString(string));
    }

    @Override
    public void setNetworkErrorHint(@DrawableRes int drawable, @StringRes int string) {
        setNetworkErrorHint(drawable, getContext().getString(string));
    }

    @Override
    public void setEmptyHint(@DrawableRes int drawable, CharSequence string) {
        mEmpty.setCompoundDrawablesWithIntrinsicBounds(0, drawable, 0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mEmpty.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawable, 0, 0);
        }
        mEmpty.setText(string);
    }

    @Override
    public void setNetworkErrorHint(@DrawableRes int drawable, CharSequence string) {
        mNetworkError.setCompoundDrawablesWithIntrinsicBounds(0, drawable, 0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mNetworkError.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawable, 0, 0);
        }
        mNetworkError.setText(string);
    }

    @Override
    public void onNetworkError() {
        mNetworkError.setVisibility(View.VISIBLE);
        mEmpty.setVisibility(View.GONE);
    }

    @Override
    public void onEmptyContent() {
        mNetworkError.setVisibility(View.GONE);
        mEmpty.setVisibility(View.VISIBLE);
    }
}
