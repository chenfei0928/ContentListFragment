package com.chenfei.contentlistfragment.library;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chenfei.base.fragment.BaseFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 懒加载，关注首次载入、刷新，不关注加载更多
 * Created by MrFeng on 2016/8/22.
 */
public abstract class LazyLoadFragment<Cfg extends LazyLoadFragment.Config> extends BaseFragment {
    static final Handler sHandler = new Handler();
    private Cfg mConfig;
    private boolean mIsFirstLoaded = false;
    private boolean mIsFirstRequest = true;
    private ViewGroup mRootView;
    protected SwipeRefreshLayout mRefresh;
    @Nullable
    protected StateView mStateView;

    private final Runnable mRequestRunner = () -> {
        // 修复偶尔会出现的 100 ms 延时之后view被destroy的问题
        if (mRefresh == null || !isVisible())
            return;
        mRefresh.setRefreshing(false);
        mRefresh.setRefreshing(true);
        // 如果是首次请求
        if (mIsFirstRequest) {
            firstRequest();
            mIsFirstRequest = false;
        }
    };

    @SuppressWarnings("unchecked")
    Cfg createConfigInternal() {
        return (Cfg) new Config();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = (ViewGroup) view.findViewById(R.id.baseList_root);
        mRefresh = (SwipeRefreshLayout) view.findViewById(R.id.baseList_swipe);
        mStateView = initStateView();
        mRefresh.setOnRefreshListener(() -> {
            if (getConfig().enableRefresh) {
                requestList(true);
            } else {
                mRefresh.setEnabled(false);
                mRefresh.setRefreshing(false);
            }
        });
        // 修复部分情况下setUserVisibleHint会在onCreate之前调用的问题
        if (getUserVisibleHint()) {
            checkToRequestData(Cfg.VISIBLE);
        }
    }

    private StateView initStateView() {
        StateView stateView = null;
        if (getConfig().stateViewId != 0) {
            View view = LayoutInflater.from(getContext()).inflate(getConfig().stateViewId, mRootView, false);
            stateView = (StateView) view;
            mRootView.addView(view, 0);
        }
        mRefresh.setEnabled(getConfig().enableRefresh);
        return stateView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // 修复部分情况下 setUserVisibleHint 会比 onCreateView 更早调用的问题
        if (isVisibleToUser && isVisible()) {
            checkToRequestData(Cfg.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkToRequestData(Cfg.ON_RESUME);
    }

    @Override
    public void onStart() {
        super.onStart();
        checkToRequestData(Cfg.ON_START);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRefresh = null;
        mStateView = null;
    }

    /**
     * 检查并请求数据
     * 检查，如果需要刷新则调用发送首次加载、刷新的方法
     *
     * @param when 调用的生命周期时机
     */
    private void checkToRequestData(@Config.WhenToAsk String when) {
        // 使用post方式，以解决调用此方法时子类 onViewCreated 代码尚未执行导致的 NPE 的问题
        if (when.equals(getConfig().whenToRequest) && !mIsFirstLoaded) {
            mIsFirstLoaded = true;
            sHandler.post(this::postFirstLoadData);
        } else if (when.equals(getConfig().whenToRefresh) && mIsFirstLoaded) {
            sHandler.post(this::postRefreshLoadData);
        }
    }

    /**
     * 首次加载数据
     */
    void postFirstLoadData() {
        sHandler.postDelayed(mRequestRunner, 100);
    }

    /**
     * 刷新数据
     */
    void postRefreshLoadData() {
        sHandler.postDelayed(mRequestRunner, 100);
    }

    final Cfg getConfig() {
        if (mConfig == null) {
            mConfig = config(createConfigInternal());
        }
        return mConfig;
    }

    /**
     * 可以对StateView、Fragment加载进行设置
     *
     * @param config StateView
     */
    @NonNull
    protected abstract Cfg config(Cfg config);

    /**
     * 首次载入内容
     */
    protected void firstRequest() {
        requestList(true);
    }

    /**
     * 请求视频列表，第一次加载和刷新时传入 true
     */
    @CallSuper
    public void requestList(boolean isRefresh) {
        // 如果是手动调用，设置SwipeRefreshLayout为刷新状态
        if (isRefresh) {
            mRefresh.setRefreshing(true);
            mRefresh.getChildAt(1).setVisibility(View.VISIBLE);
        } else {
            mRefresh.setRefreshing(false);
        }
        requestListImpl(isRefresh);
    }

    protected abstract void requestListImpl(boolean isRefresh);

    @CallSuper
    protected void onLoad() {
        if (mRefresh != null) {
            mRefresh.setRefreshing(false);
            mRefresh.setEnabled(getConfig().enableRefresh);
        }
    }


    /**
     * 状态标识View需实现接口
     */
    public interface StateView {
        void show();

        void hide();

        void onEmptyContent();

        void onNetworkError();
    }

    /**
     * 懒加载Fragment相关设置
     *
     * @param <T> 子类需扩展该设置内容的话将子设置类泛型，以便于设置类型
     */
    @SuppressWarnings("unchecked")
    protected static class Config<T extends Config> {
        static final String NONE = "none";
        public static final String ON_RESUME = "onResume";
        public static final String ON_START = "onStart";
        public static final String VISIBLE = "setUserVisibleHint";

        boolean enableRefresh = true;
        boolean emptyContentClickRefresh = true;
        /**
         * 什么时候刷新数据
         */
        @Config.WhenToAsk
        String whenToRequest = ON_RESUME;
        /**
         * 生命周期事件要判断时候要请求数据请求时，忽略对列表是否有数据的逻辑判断
         * 用于回到页面时的刷新数据，例如收藏列表
         */
        @Config.WhenToAsk
        String whenToRefresh = NONE;

        @LayoutRes
        int stateViewId = DefaultConfig.sStateViewId;

        @StringDef({ON_RESUME, ON_START, VISIBLE, NONE})
        @Retention(RetentionPolicy.SOURCE)
        @interface WhenToAsk {
        }

        public final T withEnableRefresh(boolean enableRefresh) {
            this.enableRefresh = enableRefresh;
            return (T) this;
        }

        public final T withEmptyContentClickRefresh(boolean emptyContentClickBack) {
            this.emptyContentClickRefresh = emptyContentClickBack;
            return (T) this;
        }

        /**
         * 什么时候去第一次请求数据（用于懒加载）
         */
        public final T withWhenToRequest(@WhenToAsk String whenToRequest) {
            this.whenToRequest = whenToRequest;
            return (T) this;
        }

        /**
         * 什么时候去刷新数据（刷新时就算列表中有内容也会被忽略内容判空）
         */
        public final T withWhenToRefresh(@WhenToAsk String whenToRefresh) {
            this.whenToRefresh = whenToRefresh;
            return (T) this;
        }

        public final T withStateViewId(@LayoutRes int stateViewId) {
            this.stateViewId = stateViewId;
            return (T) this;
        }
    }

    public static final class DefaultConfig {
        @LayoutRes
        public static int sStateViewId = 0;
    }
}
