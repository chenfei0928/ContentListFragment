package com.chenfei;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by MrFeng on 2017/3/22.
 */
public interface GithubApi {
    public static final String HOST = "https://api.github.com/";
    String[] keywords =  new String[]{
            "Android",
            "Java",
            "C",
            "Cpp",
            "C-Sharp",
            "Python",
            "VB.NET",
            "PHP",
            "JavaScript",
            "Pascal",
            "Swift",
            "Perl",
            "Ruby",
            "Assembly",
            "R",
            "VB",
            "Objective-C",
            "Go",
            "MATLAB",
            "SQL",
            "Scratch"
    };

    GithubApi mApi = new Retrofit.Builder()
            .client(new OkHttpClient.Builder()
                    .addInterceptor(
                            new HttpLoggingInterceptor()
                                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                    .build())
            .baseUrl(GithubApi.HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build()
            .create(GithubApi.class);

    @GET("search/users")
    Observable<NetResult<List<User>>> searchUser(@Query("q") String keyword, @Query("page") int page, @Query("per_page") int per_page);
}
