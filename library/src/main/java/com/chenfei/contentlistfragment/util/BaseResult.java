package com.chenfei.contentlistfragment.util;

/**
 * 用于{@link .BaseContentListFragment}的获取请求成功与否、请求内容体的接口
 * Created by MrFeng on 2017/1/17.
 */
public interface BaseResult<T> {
    boolean requestSuccess();
    T getContent();
}
