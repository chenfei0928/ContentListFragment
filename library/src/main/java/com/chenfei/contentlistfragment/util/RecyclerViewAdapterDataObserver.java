package com.chenfei.contentlistfragment.util;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chenfei.contentlistfragment.library.LazyLoadFragment;

/**
 * Created by MrFeng on 2016/6/7.
 */
public class RecyclerViewAdapterDataObserver extends RecyclerView.AdapterDataObserver {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LazyLoadFragment.StateView mEmptyView;

    public RecyclerViewAdapterDataObserver(SwipeRefreshLayout refreshLayout, RecyclerView recyclerView, LazyLoadFragment.StateView emptyView) {
        mSwipeRefreshLayout = refreshLayout;
        mRecyclerView = recyclerView;
        mEmptyView = emptyView;
    }

    @Override
    public void onChanged() {
        super.onChanged();
        onDataChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        super.onItemRangeChanged(positionStart, itemCount);
        onDataChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        super.onItemRangeChanged(positionStart, itemCount, payload);
        onDataChanged();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        onDataChanged();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        super.onItemRangeRemoved(positionStart, itemCount);
        onDataChanged();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        onDataChanged();
    }

    private void onDataChanged() {
        boolean emptyContent = mRecyclerView.getAdapter().getItemCount() == 0;
        mRecyclerView.setVisibility(emptyContent ? View.GONE : View.VISIBLE);
        if (emptyContent) {
            mEmptyView.onEmptyContent();
            mEmptyView.show();
        } else {
            mEmptyView.hide();
        }
        // 如果空内容，设置刷新布局为不可用
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setEnabled(!emptyContent);
    }
}
