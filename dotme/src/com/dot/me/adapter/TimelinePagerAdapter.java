package com.dot.me.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.dot.me.model.CollumnConfig;
import com.dot.me.model.bd.Facade;
import com.dot.me.view.AbstractColumn;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

public class TimelinePagerAdapter extends PagerAdapter {

	private List<AbstractColumn> childs = new ArrayList<AbstractColumn>();
	private ViewPager parent;
	private List<ScrollState> states = new ArrayList<ScrollState>();

	public TimelinePagerAdapter(ViewPager pager) {
		parent = pager;
	}

	public void addView(AbstractColumn v) {
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
		if (object instanceof LinearLayout) {
			ListView listView = null;
			try {
				listView = childs.get(pos).getScrollView();
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (listView != null) {
				try {
					ScrollState state = states.get(pos);
					ListView vList = listView;
					int index = vList.getFirstVisiblePosition();
					View v = vList.getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();

					state.top = top;
					state.scrollTo = index;
				} catch (IndexOutOfBoundsException e) {
					// TODO: handle exception
				}

			}
			try {
				((ViewPager) view).removeView((View) object);
			} catch (Exception e) {
				// TODO: handle exception
			}
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
		View myView = childs.get(position).getCollumnView();
		try {
			((ViewPager) view).addView(myView);
		} catch (Exception e) {
			// TODO: handle exception
		}
		ListView listView = childs.get(position).getScrollView();
		if (listView != null) {
			Facade facade = Facade.getInstance(parent.getContext());
			CollumnConfig config = facade.getOneConfig(position);

			try {

				int top = states.get(position).top;

				int scrollTo = states.get(position).scrollTo;

				if (top != 0 && scrollTo != 0)
					listView.setSelectionFromTop(
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

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public void clear() {
		childs.clear();
		notifyDataSetChanged();

	}

	private class ScrollState {
		public int top;
		public int scrollTo;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return childs.get(position).getColumnTitle();
	}

}