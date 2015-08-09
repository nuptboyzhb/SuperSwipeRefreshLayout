package net.mobctrl.activity;

import java.util.ArrayList;
import java.util.List;

import net.mobctrl.treerecyclerview.R;
import net.mobctrl.views.SuperSwipeRefreshLayout;
import net.mobctrl.views.SuperSwipeRefreshLayout.OnPullRefreshListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @Author Zheng Haibo
 * @PersonalWebsite http://www.mobctrl.net
 * @Description
 */
public class ListViewActivity extends Activity {

	private SuperSwipeRefreshLayout swipeRefreshLayout;

	private ProgressBar progressBar;

	private TextView textView;

	private ImageView imageView;

	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listview);

		listView = (ListView) findViewById(R.id.list_view);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, getData()));
		swipeRefreshLayout = (SuperSwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		View child = LayoutInflater.from(swipeRefreshLayout.getContext())
				.inflate(R.layout.layout_head, null);
		progressBar = (ProgressBar) child.findViewById(R.id.pb_view);
		textView = (TextView) child.findViewById(R.id.text_view);
		textView.setText("下拉刷新");
		imageView = (ImageView) child.findViewById(R.id.image_view);
		imageView.setVisibility(View.VISIBLE);
		imageView.setImageResource(R.drawable.down_arrow);
		RelativeLayout layoutHead = (RelativeLayout) child
				.findViewById(R.id.head_container);
		RelativeLayout.LayoutParams marginLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		marginLayoutParams.topMargin = 800;
		layoutHead.setLayoutParams(marginLayoutParams);
		progressBar.setVisibility(View.GONE);
		swipeRefreshLayout.setHeaderView(child);
		swipeRefreshLayout
				.setOnPullRefreshListener(new OnPullRefreshListener() {

					@Override
					public void onRefresh() {
						textView.setText("正在刷新");
						imageView.setVisibility(View.GONE);
						progressBar.setVisibility(View.VISIBLE);
						System.out.println("debug:onRefresh");
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								swipeRefreshLayout.setRefreshing(false);
								progressBar.setVisibility(View.GONE);
								System.out.println("debug:stopRefresh");
							}
						}, 2000);
					}

					@Override
					public void onPullDistance(int distance) {
						System.out.println("debug:distance = " + distance);
						// myAdapter.updateHeaderHeight(distance);
					}

					@Override
					public void onPullEnable(boolean enable) {
						textView.setText(enable ? "松开刷新" : "下拉刷新");
						imageView.setVisibility(View.VISIBLE);
						imageView.setRotation(enable ? 180 : 0);
					}
				});

	}

	private List<String> getData() {
		List<String> data = new ArrayList<String>();
		data.add("item 1");
		data.add("item 2");
		data.add("item 3");
		data.add("item 4");
		data.add("item 5");
		data.add("item 6");
		data.add("item 7");
		data.add("item 8");
		return data;
	}

}
