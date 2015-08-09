package net.mobctrl.activity;

import java.util.ArrayList;
import java.util.List;

import net.mobctrl.adapter.RecyclerAdapter;
import net.mobctrl.treerecyclerview.R;
import net.mobctrl.views.SuperSwipeRefreshLayout;
import net.mobctrl.views.SuperSwipeRefreshLayout.OnPullRefreshListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @Author Zheng Haibo
 * @PersonalWebsite http://www.mobctrl.net
 * @Description
 */
public class RecyclerViewActivity extends Activity {

	private RecyclerView recyclerView;
	private RecyclerAdapter myAdapter;
	private LinearLayoutManager linearLayoutManager;
	private SuperSwipeRefreshLayout swipeRefreshLayout;

	// Header View
	private ProgressBar progressBar;
	private TextView textView;
	private ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recyclerview);
		/** init recyclerView */
		recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		linearLayoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(linearLayoutManager);
		myAdapter = new RecyclerAdapter(this);
		recyclerView.setAdapter(myAdapter);

		// init SuperSwipeRefreshLayout
		swipeRefreshLayout = (SuperSwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setHeaderView(createHeaderView());// add headerView
		swipeRefreshLayout
				.setOnPullRefreshListener(new OnPullRefreshListener() {

					@Override
					public void onRefresh() {
						textView.setText("正在刷新");
						imageView.setVisibility(View.GONE);
						progressBar.setVisibility(View.VISIBLE);
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								swipeRefreshLayout.setRefreshing(false);
								progressBar.setVisibility(View.GONE);
							}
						}, 2000);
					}

					@Override
					public void onPullDistance(int distance) {
						// pull distance
					}

					@Override
					public void onPullEnable(boolean enable) {
						textView.setText(enable ? "松开刷新" : "下拉刷新");
						imageView.setVisibility(View.VISIBLE);
						imageView.setRotation(enable ? 180 : 0);
					}
				});
		initDatas();
	}

	private View createHeaderView() {
		View headerView = LayoutInflater.from(swipeRefreshLayout.getContext())
				.inflate(R.layout.layout_head, null);
		progressBar = (ProgressBar) headerView.findViewById(R.id.pb_view);
		textView = (TextView) headerView.findViewById(R.id.text_view);
		textView.setText("下拉刷新");
		imageView = (ImageView) headerView.findViewById(R.id.image_view);
		imageView.setVisibility(View.VISIBLE);
		imageView.setImageResource(R.drawable.down_arrow);
		progressBar.setVisibility(View.GONE);
		return headerView;
	}

	private void initDatas() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < 10; i++) {
			list.add("item " + i);
		}
		myAdapter.addAll(list, 0);
	}

}
