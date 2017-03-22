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
