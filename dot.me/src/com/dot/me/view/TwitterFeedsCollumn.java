package com.dot.me.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.adapter.FeddAdapter;
import com.dot.me.app.R;
import com.dot.me.command.OpenTwitterWriter;
import com.dot.me.model.Account;
import com.dot.me.model.CollumnConfig;
import com.dot.me.model.Mensagem;
import com.dot.me.model.TwitterAccount;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;
import com.dot.me.utils.TwitterUtils;
import com.dot.me.utils.UpdateParams;
import com.dot.me.utils.TwitterUtils.ResponseUpdate;
import com.markupartist.android.widget.PullToRefreshListView;

import android.content.Context;
import android.os.AsyncTask;

public class TwitterFeedsCollumn extends AbstractColumn {

	private int currentPage;
	private boolean flagNextPage = false;

	public TwitterFeedsCollumn(Context ctx) {
		super(ctx, ctx.getString(R.string.main_column_name),true);
		command=new OpenTwitterWriter();
	}

	public TwitterFeedsCollumn(Context ctx, String title) {
		super(ctx, title,true);
		currentPage = 0;
		
	}

	@Override
	public void deleteColumn() {

	}

	@Override
	protected void updateList() {

		TwitterAccount user = (TwitterAccount) Account.getTwitterAccount(ctx);// verificar
																				// depois

		AccessToken token = new AccessToken(user.getToken(),
				user.getTokenSecret());

		if (flagNextPage)
			currentPage++;
		else
			currentPage = 1;
		UpdateParams params = new UpdateParams(currentPage,
				Constants.QTD_FEEDS, token);
		new UpdateTimelineTask(ctx, params, (PullToRefreshListView)listView, this).execute();
		
		JSONObject prop=config.getProprietes();
		if(prop!=null){
			prop.remove("nextPage");
			try {
				prop.put("nextPage", currentPage);
			} catch (JSONException e) {
				
			}
		}
		
	}

	@Override
	public void init() {
		final Vector<Mensagem> list = facade
				.getMensagemOf(Mensagem.TIPO_STATUS);
		TimelineActivity.h.post(new Runnable() {

			@Override
			public void run() {
				updateTwittes(list, true);
//				notifyInitFinished();
			}
		});
		
		if(list==null||list.isEmpty())
			return;
		Mensagem m=null;
		try{
		m=list.firstElement();
		}catch (Exception e) {
			// TODO: handle exception
		}
		if(m!=null){
			if(System.currentTimeMillis()-m.getData().getTime()>Constants.QTD_MINUTES){
				currentPage=0;
				JSONObject prop=config.getProprietes();
				if (prop!=null) {
					prop.remove("nextPage");
				}
			}
		}
	}
	
	

	@Override
	public boolean isDeletable() {
		return false;
	}

	@Override
	protected void onGetNextPage() {
		if (isLoaddingNextPage || currentPage == 0){
			
			return;
		}

		super.onGetNextPage();

		flagNextPage = true;
		updateList();
		isLoaddingNextPage = true;
	}

	@Override
	public void updateTwittes(Vector<Mensagem> list, boolean top) {
		if (list == null)
			return;

		addMensagens(list, top);

	}

	@Override
	public void setConfig(CollumnConfig config) {
		super.setConfig(config);
		try {
			currentPage=config.getProprietes().getInt("nextPage");
		} catch (JSONException e) {
			currentPage=0;
		}
	}

	private class UpdateTimelineTask extends AsyncTask<Void, Void, Void> {

		private Context ctx;
		private UpdateParams uParams;
		private PullToRefreshListView listView;
		private AbstractColumn column;
		private List<Mensagem> messages = new ArrayList<Mensagem>();
		private List<Mensagem> last = new ArrayList<Mensagem>();
		
		public UpdateTimelineTask(Context ctx, UpdateParams params,
				PullToRefreshListView view, AbstractColumn column) {
			this.ctx = ctx;
			this.uParams = params;
			this.listView = view;
			this.column = column;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				last = Facade.getInstance(ctx).getMensagemOf(Mensagem.TIPO_STATUS);
				AccessToken accessToken = uParams.getToken();
				ResponseList<twitter4j.Status> list;

				int n_page = 1;
				int qtd_feeds = Constants.QTD_FEEDS;

				if (uParams != null) {
					n_page = uParams.getPage();
					qtd_feeds = uParams.getQtdFeeds();
				}
				Paging page = new Paging(n_page, qtd_feeds);
				list = TwitterUtils.getTwitter(accessToken).getHomeTimeline(page);

				ResponseUpdate response = TwitterUtils.updateTweets(ctx,list,Mensagem.TIPO_STATUS);
				messages = response.mensagens;
				/*
				 * Mensagem lastMessage = response.lastMessage;
				 * 
				 * boolean top = true; if (n_page > 1) top = false;
				 * 
				 * if (mensagens.size() < Constants.QTD_FEEDS && !top) { // Atuliza
				 * no topo page = new Paging(1, qtd_feeds); list =
				 * TwitterUtils.getTwitter(accessToken).getHomeTimeline( page);
				 * response = updateTweets(list); mensagens = response.mensagens;
				 * 
				 * if (TimelineActivity.getCurrent() != null)
				 * TimelineActivity.getCurrent().setCurrentList(mensagens); Intent
				 * intent = new Intent( "com.twittemarkup.reciever.UPDATE_MSG");
				 * 
				 * Bundle b = new Bundle(); b.putBoolean("top", true);
				 * intent.putExtras(b); ctx.sendBroadcast(intent);
				 * 
				 * // atualiza nova pagina int num_feeds =
				 * Facade.getInstance(ctx).getCountMensagem( Mensagem.TIPO_STATUS);
				 * int currentPage = num_feeds / Constants.QTD_FEEDS;
				 * 
				 * page = new Paging(currentPage + 1, qtd_feeds); list =
				 * TwitterUtils.getTwitter(accessToken).getHomeTimeline( page);
				 * response = updateTweets(list); messages = response.mensagens;
				 * /*if (TimelineActivity.getCurrent() != null)
				 * TimelineActivity.getCurrent().setCurrentList(mensagens); Intent
				 * intent2 = new Intent( "com.twittemarkup.reciever.UPDATE_MSG");
				 * 
				 * Bundle b2 = new Bundle(); b.putBoolean("top", false);
				 * intent2.putExtras(b2); ctx.sendBroadcast(intent2);
				 * 
				 * return null; } else if (mensagens.size() >= Constants.QTD_FEEDS)
				 * { Facade.getInstance(ctx).deleteAllTo(
				 * lastMessage.getData().getTime()); } if
				 * (TimelineActivity.getCurrent() != null)
				 * TimelineActivity.getCurrent().setCurrentList(mensagens); Intent
				 * intent = new Intent("com.twittemarkup.reciever.UPDATE_MSG");
				 * 
				 * Bundle b = new Bundle(); b.putBoolean("top", top);
				 * intent.putExtras(b); ctx.sendBroadcast(intent);
				 */

			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (listView != null && uParams.getPage() == 1) {
				listView.onRefreshComplete();

			}
			FeddAdapter adapter = ((FeddAdapter) listView.getMyAdapter());
			if (column != null) {

				if (uParams.getPage() == 1 && !messages.isEmpty()) {
					adapter.clear();
					for (Mensagem m : last) {
						if (!messages.contains(m))
							Facade.getInstance(ctx).deleteMensagem(
									m.getIdMensagem(), m.getTipo());
					}
				}

				for (Mensagem m : messages) {
					adapter.addItem(m);
				}

				if (uParams.getPage() > 1) {
					column.notifyNextPageFinish();
				}

				adapter.sort();
				isLoading=false;
			}

		}	
	}
	
}
