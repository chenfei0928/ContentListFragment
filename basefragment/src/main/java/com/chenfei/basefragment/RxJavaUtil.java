package com.chenfei.basefragment;

import android.util.Log;

import java.util.Locale;

import rx.functions.Action1;
import rx.functions.Actions;

/**
 * User: ChenFei(chenfei0928@gmail.com)
 * Date: 2016-06-04
 * Time: 19:24
 */
public class RxJavaUtil {
    private static final String TAG = "RxJavaUtil";

    public static Action1<Throwable> onError() {
        if (!BuildConfig.DEBUG)
            return Actions.empty();
        String strings = generateTags(4);
        return throwable -> {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, strings + "\ndefaultOnErrorHandler: ", throwable);
            }
        };
    }

    /**
     * @return 返回格式为 类名 类名.调用方法名(文件名.java:行号)
     */
    private static String generateTags(int level) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[level];
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);

        return String.format(Locale.getDefault(), "%s.%s(%s:%d)",
                callerClazzName, caller.getMethodName(),
                caller.getFileName(), caller.getLineNumber());
    }
}
