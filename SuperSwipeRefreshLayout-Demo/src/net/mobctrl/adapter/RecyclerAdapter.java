package net.mobctrl.adapter;

import java.util.ArrayList;
import java.util.List;

import net.mobctrl.treerecyclerview.R;
import net.mobctrl.viewholder.BaseViewHolder;
import net.mobctrl.viewholder.ChildViewHolder;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @Author Zheng Haibo
 * @PersonalWebsite http://www.mobctrl.net
 * @Description
 */
public class RecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

	private Context mContext;
	private List<String> mDataSet;

	public RecyclerAdapter(Context context) {
		mContext = context;
		mDataSet = new ArrayList<String>();
	}

	@Override
	public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.item_recycler_child, parent, false);
		return new ChildViewHolder(view);
	}

	@Override
	public void onBindViewHolder(BaseViewHolder holder, int position) {
		ChildViewHolder textViewHolder = (ChildViewHolder) holder;
		textViewHolder.bindView(mDataSet.get(position), position);
	}


	@Override
	public int getItemCount() {
		return mDataSet.size();
	}

	/**
	 * 从position开始删除，删除
	 * 
	 * @param position
	 * @param itemCount
	 *            删除的数目
	 */
	protected void removeAll(int position, int itemCount) {
		for (int i = 0; i < itemCount; i++) {
			mDataSet.remove(position);
		}
		notifyItemRangeRemoved(position, itemCount);
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	public void add(String text, int position) {
		mDataSet.add(position, text);
		notifyItemInserted(position);
	}

	public void addAll(List<String> list, int position) {
		mDataSet.addAll(position, list);
		notifyItemRangeInserted(position, list.size());
	}

}
