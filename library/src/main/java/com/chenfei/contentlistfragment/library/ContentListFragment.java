package com.chenfei.contentlistfragment.library;

/**
 * 用于向外暴露 ContentListInternalFragment 使用，其Config类需要制作一个明确泛型的类，以解决编译器、IDE报错
 * Created by MrFeng on 2016/7/1.
 */
public abstract class ContentListFragment extends ContentListInternalFragment<ContentListFragment.Config> {
    @Override
    final Config createConfigInternal() {
        return new Config();
    }

    protected static class Config extends ContentListInternalFragment.Config<Config> {
    }
}
