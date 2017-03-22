package com.chenfei.contentlistfragment.util;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 简单的RecyclerView上拉加载监听器
 * 清空RecyclerView状态时需要先移除监听器
 * 设置LayoutManager、Adapter时会{@link RecyclerView#stopScroll()}中会调用监听器，可能会出现不想要的问题
 * Created by Admin on 2016/1/19.
 */
public class RecyclerViewScrollLoadMoreListener extends RecyclerView.OnScrollListener {
    public static final int STATUS_LIST_IDLE = 0;
    public static final int STATUS_LIST_REFRESH = 1;
    public static final int STATUS_LIST_LOADMORE = 2;
    // 标记：请勿加载
    public static final int STATUS_LIST_DONT_LOADMORE = 3;

    @IntDef({STATUS_LIST_IDLE, STATUS_LIST_LOADMORE, STATUS_LIST_REFRESH, STATUS_LIST_DONT_LOADMORE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ListViewState {
    }

    private final OnRecyclerViewLoadMoreListener lis;
    private final boolean emptyAutoCall;
    private final int pageSize;
    private final int loadmoreOffset;
    @RecyclerViewScrollLoadMoreListener.ListViewState
    private int mState = RecyclerViewScrollLoadMoreListener.STATUS_LIST_IDLE;

    public RecyclerViewScrollLoadMoreListener(boolean emptyAutoCall, int pageSize, int loadmoreOffset,
                                              OnRecyclerViewLoadMoreListener lis) {
        this.emptyAutoCall = emptyAutoCall;
        this.pageSize = pageSize;
        this.loadmoreOffset = loadmoreOffset;
        this.lis = lis;
    }

    /**
     * 最后一个的位置，仅用于GridLayoutManager
     */
    private int[] mLastPositions;
    /**
     * 最后一个可见的item的位置
     */
    private int mLastVisibleItemPosition;
    /**
     * 第一个可见的item的位置
     */
    private int mFirstVisibleItemPosition;


    public interface OnRecyclerViewLoadMoreListener {
        void onLoadMore(RecyclerView lv);
    }

    @RecyclerViewScrollLoadMoreListener.ListViewState
    public final int getState() {
        return mState;
    }

    public final void setState(@RecyclerViewScrollLoadMoreListener.ListViewState int state) {
        mState = state;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE && lis != null
                && mState == RecyclerViewScrollLoadMoreListener.STATUS_LIST_IDLE) {
            // 预计算RecyclerView内容数量
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            // 可见Item数量统计
            int visibleItemCount = layoutManager.getChildCount();
            // 总内容数量统计，等同于：recyclerView.getAdapter().getCount()
            int totalItemCount = layoutManager.getItemCount();
            // 获取或计算第一个和最后一个Item的Position
            calculateFirstVisibleItemPos(recyclerView);
            checkLoadMore(lis, recyclerView, mFirstVisibleItemPosition, visibleItemCount,
                    mLastVisibleItemPosition, totalItemCount);
        }
    }

    /**
     * 检查是否需要进行刷新并调用监听器
     *
     * @param lis              监听器
     * @param recyclerView     RecyclerView 视图对象
     * @param firstVisibleItem 第一个可见的Item
     * @param visibleItemCount 可见Item的总和
     * @param lastVisibleItem  最后一个可见的item
     * @param totalItemCount   适配器内总计有的item总和
     */
    private void checkLoadMore(@NonNull OnRecyclerViewLoadMoreListener lis, @NonNull RecyclerView recyclerView,
                               int firstVisibleItem, int visibleItemCount, int lastVisibleItem, int totalItemCount) {
//        Log.i(t, "first " + firstVisibleItem + " total " + totalItemCount + " last " + lastVisibleItem + " visible " + visibleItemCount);
        // 内容判空、内容一页之内显示完
        if (recyclerView.getAdapter() == null || totalItemCount == 0) {
            // 如果在内容为空时
            if (emptyAutoCall) {
                // 自动加载更多
                lis.onLoadMore(recyclerView);
            }
        } else if (firstVisibleItem == 0 && totalItemCount == visibleItemCount) {
            // 如果有数据，并且所有数据都在一屏幕内显示
            if (emptyAutoCall) {
                lis.onLoadMore(recyclerView);
            }
        } else if (visibleItemCount > 0 && (lastVisibleItem > totalItemCount - loadmoreOffset) || totalItemCount < pageSize) {
            // 如果最后一条在ListView中在最下方5行之内，或总数据不满5行，则进行加载
            lis.onLoadMore(recyclerView);
        }
    }

    /**
     * 计算第一个元素的位置
     */
    private void calculateFirstVisibleItemPos(RecyclerView rec) {
        if (rec.getChildCount() == 0) {
            mFirstVisibleItemPosition = 0;
            mLastVisibleItemPosition = 0;
            return;
        }
        RecyclerView.LayoutManager layoutManager = rec.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            mLastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            mFirstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if (layoutManager instanceof GridLayoutManager) {
            mLastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            mFirstVisibleItemPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            if (mLastPositions == null) {
                mLastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            }
            mLastPositions = staggeredGridLayoutManager.findLastVisibleItemPositions(mLastPositions);
            mLastVisibleItemPosition = findMax(mLastPositions);
            staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(mLastPositions);
            mFirstVisibleItemPosition = findMin(mLastPositions);
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            max = Math.max(max, value);
        }
        return max;
    }

    private int findMin(int[] lastPositions) {
        int min = lastPositions[0];
        for (int value : lastPositions) {
            min = Math.min(min, value);
        }
        return min;
    }
}
