package com.chenfei;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by MrFeng on 2017/3/22.
 */
public interface GithubApi {
    public static final String HOST = "https://api.github.com/";

    @GET("search/users")
    Observable<NetResult<List<User>>> searchUser(@Query("q") String keyword, @Query("page") int page, @Query("per_page") int per_page);
}
