package com.chenfei;

import com.chenfei.contentlistfragment.util.BaseResult;

/**
 * Created by MrFeng on 2017/3/22.
 */
public class NetResult<T> implements BaseResult<T> {
    private int total_count;
    private boolean incomplete_results;
    private T items;

    @Override
    public T getContent() {
        return items;
    }
}
