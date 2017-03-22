package com.chenfei.contentlistfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.chenfei.GithubApi;
import com.chenfei.ListStateView;
import com.chenfei.User;
import com.chenfei.contentlistfragment.library.ContentListFragment;
import com.chenfei.contentlistfragment.library.LazyLoadFragment;
import com.chenfei.contentlistfragment.util.RecyclerViewScrollLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BannerListActivity extends FragmentContentActivity<BannerListActivity.Fragment> {
    private static final String TAG = "ListActivity";

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new Fragment();
    }

    public static class Fragment extends ContentListFragment {
        private final int sPageSize = 10;
        private int mCurrent = -1;
        private final List<User> mList = new ArrayList<>();

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ListStateView stateView = (ListStateView) mStateView;
            stateView.setEmptyHint(0, "Empty");
            stateView.setNetworkErrorHint(0, "Network Error");
        }

        @Override
        protected void requestListImpl(boolean isRefresh, Observable<Boolean> takeUntil) {
            String keyword;
            int page = getPage(mList.size(), sPageSize);
            if (isRefresh) {
                keyword = GithubApi.keywords[++mCurrent % GithubApi.keywords.length];
                getActivity().setTitle(keyword + " - Github User");
            } else {
                keyword = GithubApi.keywords[mCurrent];
            }
            GithubApi.mApi.searchUser(keyword, page, sPageSize)
                    .subscribeOn(Schedulers.io())
                    .takeUntil(takeUntil)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(baseListResultNetResult -> {
                        RecyclerView.Adapter adapter = getAdapter();
                        final boolean isFirstLoad = adapter == null;
                        if (isFirstLoad) {
                            setLayoutManager(new LinearLayoutManager(getContext()));
                            adapter = createAdapter();
                            setAdapter(adapter);
                        }

                        // 必须先设置好适配器，否则在 onLoad 统计时会认为是网络请求错误没有进入success
                        final List<User> contentList = baseListResultNetResult.getContent();
                        // 根据本次网络事件类型进行不同的集合追加操作
                        if (getState() == RecyclerViewScrollLoadMoreListener.STATUS_LIST_LOADMORE) {
                            // 加载更多
                            mList.addAll(contentList);
                        } else if (getState() == RecyclerViewScrollLoadMoreListener.STATUS_LIST_REFRESH) {
                            // 刷新
                            //首次加载，直接设置进视图
                            mList.clear();
                            mList.addAll(contentList);
                        }
                        adapter.notifyDataSetChanged();
                        onLoad();
                    }, error);
        }

        @NonNull
        @Override
        protected Config config(Config config) {
            return config
                    .withWhatToLoadMore(3)
                    .withStateViewId(R.layout.list_state_view)
                    .withWhenToRequest(LazyLoadFragment.Config.ON_START)
                    .withEmptyContentClickRefresh(false);
        }

        private RecyclerView.Adapter createAdapter() {
            return new RecyclerView.Adapter<UserAdapter.ViewHolder>() {
                private final int BANNER = 0;
                private final int USER = 1;

                @Override
                public int getItemViewType(int position) {
                    if (position == 0)
                        return BANNER;
                    return USER;
                }

                @Override
                public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    if (viewType == USER) {
                        return new UserAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
                    } else {
                        return new UserAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false));
                    }
                }

                @Override
                public void onBindViewHolder(UserAdapter.ViewHolder holder, int position) {
                    if (holder.getItemViewType() == USER) {
                        User user = mList.get(position);
                        holder.name.setText(user.getLogin());
                        Glide.with(holder.itemView.getContext())
                                .load(user.getAvatar_url())
                                .into(holder.cover);
                    }
                }

                @Override
                public int getItemCount() {
                    return mList.size() + 1;
                }
            };
        }
    }
}
