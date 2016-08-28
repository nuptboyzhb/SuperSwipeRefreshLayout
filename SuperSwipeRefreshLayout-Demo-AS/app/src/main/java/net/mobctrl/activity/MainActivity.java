package net.mobctrl.activity;

import net.mobctrl.treerecyclerview.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @Author Zheng Haibo
 * @PersonalWebsite http://www.mobctrl.net
 * @Description
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.recyclerview_tv).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(MainActivity.this,
								RecyclerViewActivity.class);
						startActivity(intent);
					}
				});
		findViewById(R.id.listview_tv).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View view) {
						Intent intent = new Intent(MainActivity.this,
								ListViewActivity.class);
						startActivity(intent);
					}
				});
		findViewById(R.id.scrollview_tv).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(MainActivity.this,
								ScrollViewActivity.class);
						startActivity(intent);
					}
				});
		findViewById(R.id.nested_scrollview_tv).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent  = new Intent(MainActivity.this,NestedScrollViewActivity.class);
				startActivity(intent);
			}
		});
	}

}
