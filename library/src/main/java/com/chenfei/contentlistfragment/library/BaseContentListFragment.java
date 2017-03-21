package com.chenfei.contentlistfragment.library;

import android.annotation.SuppressLint;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.chenfei.basefragment.RxJavaUtil;
import com.chenfei.contentlistfragment.util.BaseResult;
import com.chenfei.contentlistfragment.util.RecyclerViewScrollLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 使用Fragment实现视频列表，以便于在任何场合使用本类完成视频列表页面快速开发
 * 视频列表页面的Activity则再建立一个抽象类完成对其控制，之类使用时该Fragment则使用内部类完成
 * Created by Admin on 16/4/11.
 */
public abstract class BaseContentListFragment<Bean> extends ContentListInternalFragment<BaseContentListFragment.Config> {
    private static final String TAG = "KW_BaseContentListF";
    private final List<Bean> mList = new ArrayList<>(getConfig().pageSize);

    @Override
    Config createConfigInternal() {
        return new Config()
                .withStateViewClickListener(new BaseOnStateViewClickListener());
    }

    @NonNull
    protected List<Bean> getContentList() {
        return mList;
    }

    protected void clear() {
        mList.clear();
        if (getAdapter() != null)
            getAdapter().notifyDataSetChanged();
    }

    /**
     * 请求视频列表，第一次加载和刷新时传入true
     */
    @Override
    protected final void requestListImpl(boolean isRefresh, Observable<Boolean> operationChangedEvent) {
        Integer refresh = null;
        Integer offset = null;
        int page = 1; // 服务端为php，页码1和0一个效果
        //如果mData内有东西，则不是第一次打开，可能是刷新或加载更多
        if (mList.size() != 0) {
            if (isRefresh)
                refresh = getTimestamp(mList.get(0));
            else
                offset = getTimestamp(mList.get(mList.size() - 1));
            // 计算页码，如果是刷新，则页码是 1
            page = isRefresh ? 1 : getPage(mList.size(), getConfig().pageSize);
        }
        Log.i(TAG, isRefresh ? "刷新" : "加载更多");
        requestListImpl(refresh, offset, page, operationChangedEvent, success, error);
    }

    /**
     * 如需要进行刷新或加载更多的操作，重写此方法，进行获取时间戳
     *
     * @param bean 实体类
     * @return 时间戳
     */
    protected Integer getTimestamp(Bean bean) {
        return null;
    }

    /**
     * 请求列表
     *
     * @param refresh   用于刷新：refresh 的value
     * @param offset    用于偏移（加载更多）：offset 的value
     * @param page      页码数（Up主视频列表页会用到），当前页码数
     * @param takeUntil 用户操作（刷新、加载更多）变化的事件，用于中断上次请求
     * @param success   成功时的回调
     * @param error     失败时的回调
     */
    protected abstract void requestListImpl(@Nullable Integer refresh, @Nullable Integer offset, int page,
                                            Observable<Boolean> takeUntil, Action1<BaseResult<List<Bean>>> success,
                                            Action1<Throwable> error);

    private RecyclerView.Adapter createAdapterInternal(List<Bean> list) {
        return createAdapter(list);
    }

    /**
     * 创建适配器
     *
     * @param list 内容的List
     * @return 适配器
     */
    @NonNull
    protected abstract RecyclerView.Adapter createAdapter(List<Bean> list);

    @NonNull
    protected abstract RecyclerView.LayoutManager createLayoutManager();

    @NonNull
    protected abstract DiffUtil.Callback getDiffCallback(List<Bean> oldList, List<Bean> newList);

    @CallSuper
    protected void onLoad(int loadedSize) {
        if (getState() == RecyclerViewScrollLoadMoreListener.STATUS_LIST_LOADMORE
                && loadedSize != getConfig().pageSize) {
            /**
             * 一页{@link getConfig().pageSize} 个视频，服务器返回的如果不是 {@link getConfig().pageSize} 个视频，则取消上拉加载更多
             */
            regScrollLoadMore(false);
        } else if (getState() == RecyclerViewScrollLoadMoreListener.STATUS_LIST_REFRESH
                && getConfig().enableLoadMore && loadedSize == getConfig().pageSize) {
            regScrollLoadMore(true);
        }
        super.onLoad();
    }

    /**
     * @hide
     */
    @Override
    @Deprecated
    @SuppressLint("MissingSuperCall")
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    protected void onLoad() {
        onLoad(0);
    }

    protected ListUpdateCallback getListUpdateCallback() {
        if (getAdapter() instanceof ListUpdateCallback) {
            return (ListUpdateCallback) getAdapter();
        } else {
            return new UpdateCallBack(getAdapter());
        }
    }

    private Action1<BaseResult<List<Bean>>> success = baseListResultNetResult -> {
        RecyclerView.Adapter adapter = getAdapter();
        final boolean isFirstLoad = adapter == null;
        if (isFirstLoad) {
            setLayoutManager(createLayoutManager());
            adapter = createAdapterInternal(mList);
            setAdapter(adapter);
        }

        // 必须先设置好适配器，否则在 onLoad 统计时会认为是网络请求错误没有进入success
        final List<Bean> contentList = baseListResultNetResult.getContent();
        if (contentList == null) {
            onLoad(0);
            return;
        }

        final Config config = getConfig();
        // 根据本次网络事件类型进行不同的集合追加操作
        if (getState() == RecyclerViewScrollLoadMoreListener.STATUS_LIST_LOADMORE) {
            int oldSize = mList.size();
            // 加载更多
            mList.addAll(contentList);
            if (config.optimizationDataSetChange) {
                getListUpdateCallback().onInserted(oldSize, contentList.size());
            } else {
                adapter.notifyDataSetChanged();
            }
            onLoad(contentList.size());
        } else if (getState() == RecyclerViewScrollLoadMoreListener.STATUS_LIST_REFRESH) {
            // 刷新
            if (isFirstLoad) {
                //首次加载，直接设置进视图
                mList.clear();
                mList.addAll(contentList);
                adapter.notifyDataSetChanged();
                onLoad(contentList.size());
            } else if (!config.onNetRefreshLoadRemoveOldData) {
                // 非首次加载，并且刷新时是根据当前显示数据作为偏移值获取的刷新数据（追加数据）
                mList.addAll(0, contentList);
                // 计算集合变化
                if (config.optimizationDataSetChange) {
                    getListUpdateCallback().onInserted(0, contentList.size());
                } else {
                    adapter.notifyDataSetChanged();
                }
                onLoad(contentList.size());
            } else if (!config.optimizationDataSetChange) {
                // 非首次加载且需要刷新时移除旧数据，不计算集合变化
                mList.clear();
                mList.addAll(contentList);
                adapter.notifyDataSetChanged();
                onLoad(contentList.size());
            } else {
                // 非首次加载且需要刷新时移除旧数据，计算集合变化
                Observable.fromCallable(() -> DiffUtil.calculateDiff(getDiffCallback(mList, contentList), true))
                        .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(diffResult -> {
                            // 将计算到的结果刷新到布局
                            mList.clear();
                            mList.addAll(contentList);
                            diffResult.dispatchUpdatesTo(getListUpdateCallback());
                            onLoad(contentList.size());
                        }, RxJavaUtil.onError());
            }
        }
    };

    public static abstract class DiffCallback<Bean> extends DiffUtil.Callback {
        protected List<Bean> mOldList;
        protected List<Bean> mNewList;

        protected DiffCallback(List<Bean> oldList, List<Bean> newList) {
            mOldList = oldList;
            mNewList = newList;
        }

        @Override
        public int getOldListSize() {
            return mOldList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewList.size();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
        }
    }

    protected static class Config extends ContentListInternalFragment.Config<BaseContentListFragment.Config> {
        int pageSize = 20;
        // 刷新时清空列表，设置 true 后刷新请求时传入的page总会为1
        boolean onNetRefreshLoadRemoveOldData = false;
        boolean optimizationDataSetChange = true;

        public final Config withPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public final Config withOnNetRefreshLoadRemoveOldData(boolean onNetRefreshLoadRemoveOldData) {
            this.onNetRefreshLoadRemoveOldData = onNetRefreshLoadRemoveOldData;
            return this;
        }

        public final Config withOptimizationDataSetChange(boolean calculateDataSetChange) {
            this.optimizationDataSetChange = calculateDataSetChange;
            return this;
        }
    }

    private static class UpdateCallBack implements ListUpdateCallback {
        private RecyclerView.Adapter adapter;

        UpdateCallBack(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onInserted(int position, int count) {
            adapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            adapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            adapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            adapter.notifyItemRangeChanged(position, count, payload);
        }
    }
}
