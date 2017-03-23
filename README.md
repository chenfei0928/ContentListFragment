# ContentListFragment

基于 Fragment、SwipeRefreshLayout、RecyclerView、RxJava 的快速列表页面开发工具类
在界面风格一致的情况下提供：
 - ViewPager容器懒加载
 - 初始化设置
 - 封装页面UI
 - 封装刷新回调
 - 封装加载更多回调
 - 提供页码、刷新/加载更多偏移值的请求数据时所需参数
 - 获取到网络请求数据后的处理
 - 使用`DiffUtil.Callback`进行RecyclerView局部刷新

# 使用
[ ![Download](https://api.bintray.com/packages/chenfei/maven/ContentListFragment/images/download.svg) ](https://bintray.com/chenfei/maven/ContentListFragment/_latestVersion)
1. 导入依赖
```` gradle
compile 'com.chenfei:ContentListFragment:1.0.0'
````

2. 设置基类

由于本框架是由Fragment实现，故部分操作是继承由Fragment
但是大部分app都会要求有自己的基类，故将基类抽离，使用时需放置Fragment基类到 `com.chenfei.basefragment.BaseFragment`
创建Java源代码文件到`com.chenfei.basefragment.BaseFragment`，继承你自己的基类
库的抽象类会继承它

3. 添加错误处理类

 创建Java源代码文件到`com.chenfei.basefragment.RxJavaUtil`，并添加静态方法以处理error事件（RxJava）：
 ````java
package com.chenfei.basefragment;
public class RxJavaUtil {
    public static rx.functions.Action1<Throwable> onError() {
    }
}
 ````

4. 详情可参考demo
