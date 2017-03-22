package com.chenfei.contentlistfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chenfei.GithubApi;
import com.chenfei.ListStateView;
import com.chenfei.User;
import com.chenfei.contentlistfragment.library.BaseContentListFragment;
import com.chenfei.contentlistfragment.library.LazyLoadFragment;
import com.chenfei.contentlistfragment.util.BaseResult;

import java.util.List;

import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class ListActivity extends FragmentContentActivity<ListActivity.Fragment> {
    private static final String TAG = "ListActivity";

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new Fragment();
    }

    public static class Fragment extends BaseContentListFragment<User> {
        private final int sPageSize = 10;
        private int mCurrent = -1;

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ListStateView stateView = (ListStateView) mStateView;
            stateView.setEmptyHint(0, "Empty");
            stateView.setNetworkErrorHint(0, "Network Error");
        }

        @Override
        protected void requestListImpl(@Nullable Integer refresh, @Nullable Integer offset, int page,
                                       SingleSource<Boolean> takeUntil,
                                       Consumer<BaseResult<List<User>>> success,
                                       Consumer<Throwable> error) {
            String keyword;
            if (page == 1) {
                keyword = GithubApi.keywords[++mCurrent % GithubApi.keywords.length];
                getActivity().setTitle(keyword + " - Github User");
            } else {
                keyword = GithubApi.keywords[mCurrent];
            }
            GithubApi.mApi.searchUser(keyword, page, sPageSize)
                    .subscribeOn(Schedulers.io())
                    .takeUntil(takeUntil)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success, error);
        }

        @NonNull
        @Override
        protected RecyclerView.Adapter createAdapter(List<User> list) {
            return new UserAdapter(list);
        }

        @NonNull
        @Override
        protected RecyclerView.LayoutManager createLayoutManager() {
            return new LinearLayoutManager(getContext());
        }

        @NonNull
        @Override
        protected DiffUtil.Callback getDiffCallback(List<User> oldList, List<User> newList) {
            return new DiffCallback<User>(oldList, newList) {
                @Override
                protected boolean areItemsTheSame(User oldItem, User newItem) {
                    return oldItem.getId() == newItem.getId();
                }
            };
        }

        @NonNull
        @Override
        protected Config config(Config config) {
            return config
                    .withPageSize(sPageSize)
                    .withWhatToLoadMore(3)
                    .withStateViewId(R.layout.list_state_view)
                    .withWhenToRequest(LazyLoadFragment.Config.ON_START)
                    .withOnNetRefreshLoadRemoveOldData(true)
                    .withEmptyContentClickRefresh(false);
        }
    }
}
