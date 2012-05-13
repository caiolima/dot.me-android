package com.twittemarkup.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONException;

import com.markupartist.android.widget.PullToRefreshListView;
import com.twittemarkup.model.CollumnConfig;
import com.twittemarkup.model.bd.Facade;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class TimelinePagerAdapter extends PagerAdapter {

	private List<View> childs = new ArrayList<View>();
	private ViewPager parent;
	private List<ScrollState> states = new ArrayList<ScrollState>();

	public TimelinePagerAdapter(ViewPager pager) {
		parent = pager;
	}

	
	
	public void addView(View v) {
		Facade facade = Facade.getInstance(parent.getContext());
		CollumnConfig config = facade.getOneConfig(childs.size());
		int top = 0;
		int scrollTo = 0;
		try {

			if (config != null) {
				top = config.getProprietes().getInt("top");

				scrollTo = config.getProprietes().getInt("scrollTo");
			}

		} catch (JSONException e) {
			// TODO: handle exception
		} finally {
			ScrollState state = new ScrollState();
			state.scrollTo = scrollTo;
			state.top = top;

			states.add(state);
		}

		childs.add(v);

	}

	public void removeViewAtPosition(int position) {
		childs.remove(position);

		states.remove(position);

	}

	@Override
	public void destroyItem(View view, int pos, Object object) {
		if (object instanceof PullToRefreshListView) {
			try {
				ScrollState state = states.get(pos);
				PullToRefreshListView vList = (PullToRefreshListView) object;
				int index = vList.getFirstVisiblePosition();
				View v = vList.getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();

				state.top = top;
				state.scrollTo = index;
			} catch (IndexOutOfBoundsException e) {
				// TODO: handle exception
			}

		}
		try{
			((ViewPager) view).removeView((View) object);
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void finishUpdate(View arg0) {

	}

	@Override
	public int getCount() {
		return childs.size();
	}

	@Override
	public Object instantiateItem(View view, int position) {
		View myView = childs.get(position);
		try {
			((ViewPager) view).addView(myView);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (myView instanceof PullToRefreshListView) {
			Facade facade = Facade.getInstance(parent.getContext());
			CollumnConfig config = facade.getOneConfig(position);

			try {

				int top = states.get(position).top;

				int scrollTo = states.get(position).scrollTo;

				if (top != 0 && scrollTo != 0)
					((PullToRefreshListView) myView).setSelectionFromTop(
							scrollTo, top);
			} catch (IndexOutOfBoundsException e) {
				// TODO: handle exception
			}

		}
		return myView;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {

	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {

	}

	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public void clear(){
		childs.clear();
		notifyDataSetChanged();
		
	}
	
	private class ScrollState {
		public int top;
		public int scrollTo;
	}
	
	
	
	

}
