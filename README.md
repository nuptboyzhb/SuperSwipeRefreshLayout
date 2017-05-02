
# SuperSwipeRefreshLayout

A custom SwipeRefreshLayout to support the pull-to-refresh featrue.You can custom your header view and footer view. RecyclerView，ListView，GridView，NestedScrollView，ScrollView are supported.

## aar

```
allprojects {
    repositories {
        jcenter()
        maven {
            url  "http://dl.bintray.com/nuptboyzhb/maven"
        }
    }
}
```

```
compile 'com.github.nuptboyzhb.lib.uikit:superswiperefreshlayout:1.0.0'
```


## Feature
- 支持下拉刷新和上拉加载更多，使用极其方便。
- 非侵入式，对原来的ListView、RecyclerView没有任何影响,用法和SwipeRefreshLayout类似。
- 可自定义头部View的样式，调用setHeaderView方法即可。
- 可自定义页尾View的样式，调用setFooterView方法即可。
- 支持RecyclerView，ListView，ScrollView，GridView,NestedScrollView等等。
- 被包含的View(RecyclerView,ListView etc.)可跟随手指的滑动而滑动<br>
  默认是跟随手指的滑动而滑动，也可以设置为不跟随：setTargetScrollWithLayout(false)
- 回调方法更多<br>
  比如：onRefresh() onPullDistance(int distance)和onPullEnable(boolean enable)<br>
  开发人员可以根据下拉过程中distance的值做一系列动画。
<br>

## Pull To Refresh(How to Use)

### Step 1: In XML

```xml
<com.github.nuptboyzhb.lib.SuperSwipeRefreshLayout
		android:id="@+id/swipe_refresh"
		android:layout_width="match_parent"
		android:layout_height="match_parent" >

		<android.support.v7.widget.RecyclerView
			android:id="@+id/recycler_view"
			android:layout_width="match_parent"
			android:layout_height="match_parent" />
</com.github.nuptboyzhb.lib.SuperSwipeRefreshLayout>
```
### Step 2: Init and setListener

```java

swipeRefreshLayout = (SuperSwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setHeaderView(createHeaderView());// add headerView
		swipeRefreshLayout
				.setOnPullRefreshListener(new OnPullRefreshListener() {

					@Override
					public void onRefresh() {
						//TODO 开始刷新
					}

					@Override
					public void onPullDistance(int distance) {
						//TODO 下拉距离
					}

					@Override
					public void onPullEnable(boolean enable) {
						//TODO 下拉过程中，下拉的距离是否足够出发刷新
					}
				});

```

加载完成之后：
swipeRefreshLayout.setRefresh(false);

### More
<br>
以上已经能够满足大部分需求，当然，你也可以这样：

- Customized your header view<br>
  自定义自己的下拉刷新头部View

```java
swipeRefreshLayout.setHeaderView(createHeaderView());// add headerView

/**
 * create Header View
 */
private View createHeaderView(){
   //TODO 创建下拉刷新头部的View样式
}
```

- setTargetScrollWithLayout(false/true);//default true
  <br>
  设置下拉时，被包含的View是否随手指的移动而移动

```java 
swipeRefreshLayout.setTargetScrollWithLayout(true);
```

- setHeaderViewBackgroundColor
  <br>
  设置下拉刷新头部背景色

```java
swipeRefreshLayout.setHeaderViewBackgroundColor(0xff888888);
```

- setDefaultCircleProgressColor
  <br>
  设置默认圆形进度条颜色

- setDefaultCircleBackgroundColor
  <br>
  设置默认圆形背景色
- setDefaultCircleShadowColor
  <br>
  设置默认圆形的阴影颜色
- setEnable
  <br>
  设置是否禁用下拉刷新，默认是使用

## Push to Load More
当拉倒底部时，上拉加载更多

### setListener

```java
swipeRefreshLayout
				.setOnPushLoadMoreListener(new OnPushLoadMoreListener() {

					@Override
					public void onLoadMore() {
						...
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								...
                                //set false when finished
								swipeRefreshLayout.setLoadMore(false);
							}
						}, 5000);
					}

					@Override
					public void onPushEnable(boolean enable) {
						//TODO 上拉过程中，上拉的距离是否足够出发刷新
					}

					@Override
					public void onPushDistance(int distance) {
						// TODO 上拉距离

					}

				});

```
### Customized your footer view

```java

swipeRefreshLayout.setFooterView(createFooterView());

```


## Support View
- RecyclerView.
- ListView
- SrcollView
- GridView
- etc.
## Demo

![gif](https://github.com/nuptboyzhb/SuperSwipeRefreshLayout/blob/master/demo.gif)


## About
@Author: Zheng Haibo 莫川<br>
@Website: [https://github.com/nuptboyzhb](https://github.com/nuptboyzhb)<br>

# License

Copyright 2015-2016  [Zheng Haibo](https://github.com/nuptboyzhb/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.