package com.chenfei.contentlistfragment.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chenfei.contentlistfragment.util.RecyclerViewAdapterDataObserver;
import com.chenfei.contentlistfragment.util.RecyclerViewScrollLoadMoreListener;

import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * 用于首页之类上方需要显示轮播图的Fragment页
 * Created by MrFeng on 2016/7/1.
 */
abstract class ContentListInternalFragment<Cfg extends ContentListInternalFragment.Config> extends LazyLoadFragment<Cfg> {
    private static final String TAG = "KW_ContentListFragment";
    final Subject<Boolean> mRefreshEventBus = PublishSubject.create();
    @Nullable
    private RecyclerView mRecycler;
    @Nullable
    private RecyclerView.LayoutManager mLayoutManager;
    @Nullable
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.AdapterDataObserver mDataObserver;

    RecyclerViewScrollLoadMoreListener mLoadMoreListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecycler = (RecyclerView) view.findViewById(R.id.baseList_recyclerView);
        // 一旦View重新创建，则尝试恢复现场
        if (mAdapter != null && mLayoutManager != null) {
            setAdapter(mAdapter);
            setLayoutManager(mLayoutManager);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (mRecycler == null)
            return;
        if (mLayoutManager != null) {
            mRecycler.setLayoutManager(mLayoutManager);
        }
        if (mAdapter != null) {
            mRecycler.setAdapter(mAdapter);
            observerAdapterData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mRecycler != null) {
            // setLayoutManager 会调用 ScrollListener
            regScrollLoadMore(false);
            mRecycler.setLayoutManager(null);
            mRecycler.setAdapter(null);
            unObserverAdapterData();
            mRecycler = null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    Cfg createConfigInternal() {
        return (Cfg) new Config();
    }

    /**
     * 打开/显示页面时首次加载
     */
    @Override
    void postFirstLoadData() {
        if (mRecycler == null || mRecycler.getAdapter() == null || mRecycler.getAdapter().getItemCount() == 0) {
            super.postFirstLoadData();
        }
    }

    /**
     * 回到页面时刷新
     */
    @Override
    void postRefreshLoadData() {
        if (mRecycler != null && mRecycler.getAdapter() != null) {
            super.postRefreshLoadData();
        }
    }

    RecyclerViewScrollLoadMoreListener getLoadMoreListener() {
        if (mLoadMoreListener == null) {
            mLoadMoreListener = new RecyclerViewScrollLoadMoreListener(true, 20, getConfig().whatToLoadMore,
                    lv -> requestList(false));
        }
        return mLoadMoreListener;
    }

    @SuppressWarnings("unused")
    protected final RecyclerView getRecyclerView() {
        return mRecycler;
    }

    protected final void setLayoutManager(@Nullable RecyclerView.LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        if (mRecycler != null)
            mRecycler.setLayoutManager(layoutManager);
    }

    protected final void setAdapter(@NonNull RecyclerView.Adapter adapter) {
        mAdapter = adapter;
        if (mRecycler != null)
            mRecycler.setAdapter(adapter);
        observerAdapterData();
    }

    @Nullable
    protected final RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    @RecyclerViewScrollLoadMoreListener.ListViewState
    protected final int getState() {
        return getLoadMoreListener().getState();
    }

    protected final void setState(@RecyclerViewScrollLoadMoreListener.ListViewState int state) {
        getLoadMoreListener().setState(state);
        if (mRefresh != null) {
            mRefresh.setEnabled(state != RecyclerViewScrollLoadMoreListener.STATUS_LIST_LOADMORE);
        }
    }

    /**
     * 请求视频列表，第一次加载和刷新时传入 true
     */
    @Override
    public final void requestList(boolean isRefresh) {
        if (mRecycler == null)
            return;
        // 如果列表中没有数据一定是刷新
        isRefresh = isRefresh || mRecycler.getAdapter() == null || mRecycler.getAdapter().getItemCount() == 0;
        setState(isRefresh
                ? RecyclerViewScrollLoadMoreListener.STATUS_LIST_REFRESH
                : RecyclerViewScrollLoadMoreListener.STATUS_LIST_LOADMORE);
        super.requestList(isRefresh);
    }

    @Override
    protected final void requestListImpl(boolean isRefresh) {
        mRefreshEventBus.onNext(isRefresh);
        requestListImpl(isRefresh, mRefreshEventBus.hide().distinctUntilChanged().firstElement().toSingle());
    }

    protected abstract void requestListImpl(boolean isRefresh, SingleSource<Boolean> takeUntil);

    private void observerAdapterData() {
        if (mAdapter == null)
            throw new IllegalStateException("mAdapter can not is null");
        if (mStateView == null)
            return;
        mDataObserver = new RecyclerViewAdapterDataObserver(
                getConfig().emptyContentClickRefresh ? mRefresh : null,
                mRecycler, mStateView);
        mAdapter.registerAdapterDataObserver(mDataObserver);
    }

    private void unObserverAdapterData() {
        if (mAdapter != null && mDataObserver != null) {
            mAdapter.unregisterAdapterDataObserver(mDataObserver);
            mDataObserver = null;
        }
    }

    protected void onLoad() {
        super.onLoad();
        setState(RecyclerViewScrollLoadMoreListener.STATUS_LIST_IDLE);

        // 如果view被回收，不进行view显示设置
        if (getView() == null || mRecycler == null)
            return;
        if (mStateView == null) {
            // 如果没有 StateView
            if (mRecycler.getVisibility() != View.VISIBLE)
                mRecycler.setVisibility(View.VISIBLE);
        } else if (mAdapter == null) {
            // 如果第一次加载加载失败，则告诉用户网络错误
            mStateView.onNetworkError();
            mRecycler.setVisibility(View.GONE);
            mStateView.show();
        } else if (mAdapter.getItemCount() == 0) {
            // 如果不是第一次加载时候加载失败，或者适配器内容被清空，则告诉用户空内容
            mStateView.onEmptyContent();
            mRecycler.setVisibility(View.GONE);
            mStateView.show();
            // 如果需要点击空内容时返回上一页，设置RefreshLayout不可用
            if (getConfig().emptyContentClickRefresh && mRefresh != null) {
                mRefresh.setEnabled(false);
            }
        } else {
            mRecycler.setVisibility(View.VISIBLE);
            mStateView.hide();
        }
    }

    protected final void regScrollLoadMore(boolean reg) {
        RecyclerView recyclerView = getRecyclerView();
        if (recyclerView == null)
            return;
        RecyclerViewScrollLoadMoreListener loadMoreListener = getLoadMoreListener();
        recyclerView.removeOnScrollListener(loadMoreListener);
        if (reg) {
            recyclerView.addOnScrollListener(loadMoreListener);
        }
    }

    protected final Consumer<Throwable> error = throwable -> {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, ContentListInternalFragment.this.getClass().getSimpleName() + " : ", throwable);
        }
        onLoad();
    };

    /**
     * 获取加载分页的页码
     *
     * @param contentSize 内容数量
     * @param pageSize    分页大小
     * @return 本次要加载的页码
     */
    protected static int getPage(int contentSize, int pageSize) {
        return contentSize / pageSize + (contentSize % pageSize > 0 ? 2 : 1);
    }

    protected static class Config<T extends ContentListInternalFragment.Config> extends LazyLoadFragment.Config<T> {
        int whatToLoadMore = 5;
        boolean emptyContentClickRefresh = true;

        @SuppressWarnings("unchecked")
        public final T withWhatToLoadMore(int offset) {
            this.whatToLoadMore = offset;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public final T withEmptyContentClickRefresh(boolean emptyContentClickBack) {
            this.emptyContentClickRefresh = emptyContentClickBack;
            return (T) this;
        }
    }
}
