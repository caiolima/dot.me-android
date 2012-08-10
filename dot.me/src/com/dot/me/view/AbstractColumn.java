package com.dot.me.view;

import java.util.Observer;
import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.adapter.FeddAdapter;
import com.dot.me.app.R;
import com.dot.me.command.AbstractCommand;
import com.dot.me.command.IMessageAction;
import com.dot.me.model.Account;
import com.dot.me.model.CollumnConfig;
import com.dot.me.model.Mensagem;
import com.dot.me.model.bd.DataBase;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;
import com.dot.me.utils.Separator;
import com.markupartist.android.widget.PullToRefreshListView;

public abstract class AbstractColumn {

	protected Context ctx;
	// protected ListView scrollView;
	protected ListView listView;
	protected LinearLayout list_twittes;
	protected FeddAdapter adapter;
	protected static Vector<AbstractColumn> instances = new Vector<AbstractColumn>();
	// protected Vector<Mensagem> mList = new Vector<Mensagem>();
	protected boolean firstPut = true;
	protected String columnTitle;
	protected Facade facade;
	private View loading;
	protected boolean isLoaddingNextPage = false;
	protected boolean isLoading=false;
	protected CollumnConfig config;
	protected AbstractCommand command;


	public String getColumnTitle() {
		return columnTitle;
	}

	public void setColumnTitle(String columnTitle) {
		this.columnTitle = columnTitle;
	}

	public AbstractColumn(Context ctx, String title, boolean isToRefresh) {
		this.ctx = ctx;
		facade = Facade.getInstance(ctx);
		instances.add(this);
		adapter = new FeddAdapter(ctx, this);
		
		if (isToRefresh) {
			listView = new PullToRefreshListView(ctx){

				@Override
				public void onRefreshComplete() {
					DataBase.getInstance(AbstractColumn.this.ctx).setExecuting(false);
					super.onRefreshComplete();
				}
				
			};
			
			listView.setDividerHeight(0);
			((PullToRefreshListView)listView).getmRefreshView().setBackgroundResource(R.color.backgorung);
			

			((PullToRefreshListView) listView)
					.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {

						@Override
						public void onRefresh() {
							if(isLoading){
								((PullToRefreshListView)listView).onRefreshComplete();
								return;
							}
							DataBase.getInstance(AbstractColumn.this.ctx).setExecuting(true);
							isLoading=true;
							updateList();
						}
					});

			listView.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					if(isLoading)
						return;
						
					if ((totalItemCount >=5)
							&& (totalItemCount - visibleItemCount) == firstVisibleItem
							&& !isLoaddingNextPage) {

						isLoading=true;
						onGetNextPage();
					}

				}

			});
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					try {
						Mensagem m = (Mensagem) adapter.getItem(position - 1);
						IMessageAction action = m.getAction();
						if (action != null)
							action.execute(m, AbstractColumn.this.ctx);
					} catch (ClassCastException e) {
						// TODO: handle exception
					} catch (Exception e) {
						// TODO: handle exception
					}

				}
			});
		} else {
			listView = new ListView(ctx);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					try {
						Mensagem m = (Mensagem) adapter.getItem(position);
						IMessageAction action = m.getAction();
						if (action != null)
							action.execute(m, AbstractColumn.this.ctx);
					} catch (ClassCastException e) {
						// TODO: handle exception
					} catch (Exception e) {
						// TODO: handle exception
					}

				}
			});
		}

		listView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		listView.setAdapter(adapter);

		columnTitle = title;

		/*
		 * scrollView = new ListView(ctx); scrollView.setLayoutParams(new
		 * LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		 * 
		 * adapter=new MessageAdapter(ctx); list_twittes = new
		 * LinearLayout(ctx);
		 * list_twittes.setOrientation(LinearLayout.VERTICAL);
		 * list_twittes.setLayoutParams(new
		 * LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		 * scrollView.addView(list_twittes);
		 */
	}

	public void updateTwittes(final Vector<Mensagem> list, final boolean top) {
		TimelineActivity.h.post(new Runnable() {

			@Override
			public void run() {
				// adapter.removeAll();

				for (Mensagem m : list) {
					adapter.addItem(m);
				}

				adapter.sort();
				// addMensagens(list, top);
				// adapter.sort();
			}
		});

	}

	public void setScrollView(PullToRefreshListView listView) {
		this.listView = listView;
	}

	public void addMensagens(Vector<Mensagem> tweets, boolean top) {

		if (firstPut) {

			for (int i = 0; i < tweets.size(); i++) {

				Mensagem status = tweets.get(i);

				createAndAddMensage(status, 0);

			}

		} /*
		 * else { if (tweets.size() >= Constants.QTD_FEEDS && top &&
		 * columnTitle.equals(ctx .getString(R.string.main_column_name))) {
		 * MarkupColunm.cleanAllColumn();
		 * MarkupColunm.addSeparatorToAllMarcadores(0, new Separator(2,
		 * Constants.QTD_FEEDS)); } int pos = 0; if (!top) pos =
		 * adapter.getCount(); for (int i = tweets.size() - 1; i >= 0; i--) {
		 * Mensagem status = tweets.get(i);
		 * 
		 * createAndAddMensage(status, pos); } if ((!top || tweets.size() <
		 * Constants.QTD_FEEDS) && columnTitle.equals(ctx
		 * .getString(R.string.main_column_name))) { int num_feeds =
		 * Facade.getInstance(ctx).getCountMensagem( Mensagem.TIPO_STATUS); int
		 * currentPage = num_feeds / Constants.QTD_FEEDS;
		 * 
		 * // adapter.addSeparator(new Separator(currentPage+1, //
		 * Constants.QTD_FEEDS)); }
		 * 
		 * }
		 */

		// list_twittes.invalidate();

		firstPut = false;
	}

	public void createAndAddMensage(Mensagem status, int pos) {

		if (firstPut) {
			adapter.addItem(status);
		} else {
			adapter.addItem(pos, status);
		}

		/*
		 * LayoutInflater inflater = (LayoutInflater) ctx
		 * .getSystemService(Context.LAYOUT_INFLATER_SERVICE); View row =
		 * inflater.inflate(R.layout.twitte_row, null); ImageView img =
		 * (ImageView) row.findViewById(R.id.profile_img); TextView screenName =
		 * (TextView) row .findViewById(R.id.screen_name); TextView time =
		 * (TextView) row.findViewById(R.id.time); TextView tweetText =
		 * (TextView) row.findViewById(R.id.twitte);
		 * 
		 * 
		 * URL imageURL=status.getImagePath(); ImageUtils.imageLoadMap.put(img,
		 * imageURL); screenName.setText(status.getNome_usuario());
		 * time.setText(TwitterUtils.friendlyFormat(status.getData()));
		 * tweetText.setText(status.getMensagem());
		 * 
		 * TimelineActivity.addImageViewURL(imageURL, img); if(firstPut){
		 * list_twittes.addView(row); mList.add(status); }else{
		 * list_twittes.addView(row,0); mList.add(0, status); }
		 */
	}

	public ListView getScrollView() {
		return listView;
	}

	public FeddAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(FeddAdapter adapter) {
		this.adapter = adapter;

	}

	public abstract void deleteColumn();

	protected abstract void updateList();

	protected void onGetNextPage() {
		DataBase.getInstance(ctx).setExecuting(true);
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.loading, null);

		loading = row;
		listView.addFooterView(loading);
	}

	public abstract void init();

	public void notifyNextPageFinish() {
		listView.removeFooterView(loading);
		isLoaddingNextPage = false;
		isLoading=false;
		DataBase.getInstance(ctx).setExecuting(false);
	}
	
	public CollumnConfig getConfig() {
		return config;
	}

	public void setConfig(CollumnConfig config) {
		this.config = config;
	}

	public int getSelection(){
		return adapter.getSelection();
	}
	
	public boolean isDeletable(){
		return true;
	}
	
	public AbstractCommand getCommand(){
		return command;
	}
	
}
