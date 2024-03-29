package com.dot.me.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.app.R;
import com.dot.me.assynctask.AssyncTaskManager;
import com.dot.me.command.OpenFacebookWriter;
import com.dot.me.exceptions.LostUserAccessException;
import com.dot.me.model.Account;
import com.dot.me.model.CollumnConfig;
import com.dot.me.model.FacebookAccount;
import com.dot.me.model.Mensagem;
import com.dot.me.utils.Constants;
import com.dot.me.utils.FacebookUtils;
import com.dot.me.utils.WebService;
import com.facebook.android.Facebook;
import com.markupartist.android.widget.PullToRefreshListView;

public class FacebookFeedsColumn extends LabelListenerCollunm{

	private boolean isLoaddingNextPage = false;
	private String nextPage;
	private Facebook facebook;

	public FacebookFeedsColumn(Context ctx) {
		this(ctx, ctx.getString(R.string.news_fedd));
		command = new OpenFacebookWriter();

	}

	private FacebookFeedsColumn(Context ctx, String title) {
		super(ctx, title, true);
	}

	@Override
	public void deleteColumn() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateList() {
		FacebookAccount faceAcc = Account.getFacebookAccount(ctx);
		if (faceAcc != null) {
			try {
				Facebook facebook = FacebookUtils.getFacebook(ctx, faceAcc);
				new FacebookNewsFeedGetterTask(facebook).execute();

			} catch (LostUserAccessException e) {

			}

		}
	}

	public class FacebookNewsFeedGetterTask extends AsyncTask<Void, Void, Void> {

		private Vector<Mensagem> messages = new Vector<Mensagem>();
		private String graphPath;
		private boolean flagNextPage;
		private List<Mensagem> last;

		public FacebookNewsFeedGetterTask(Facebook face) {
			facebook = face;
			flagNextPage = false;
		}

		public FacebookNewsFeedGetterTask(Facebook face, String graphPathPage) {
			facebook = face;
			this.graphPath = graphPathPage;
			flagNextPage = true;

		}
		

		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			
		}

		@Override
		protected Void doInBackground(Void... ins) {
			AssyncTaskManager manager=AssyncTaskManager.getInstance();
			manager.addProccess(this);
			
			try {
				last = facade.getMensagemOf(Mensagem.TIPO_NEWS_FEEDS);
				String response = null;
				if (graphPath != null) {
					WebService wservice = new WebService(graphPath);
					response = wservice.webGet("", null);
				} else {

					response = facebook.request("me/home",
							FacebookUtils.getStandartFeedsBundle());
				}
				if (response != null) {
					JSONObject json = new JSONObject(response);
					messages.addAll(FacebookUtils.createListOfFeeds(facade,
							facebook, json, Mensagem.TIPO_NEWS_FEEDS));

					try {
						JSONObject pagingObject = json.getJSONObject("paging");
						nextPage = pagingObject.getString("next");
					} catch (JSONException e) {
						nextPage = "none";
					}
				}

			} catch (MalformedURLException e) {

			} catch (IOException e) {

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (!messages.isEmpty() && !flagNextPage) {

				for (Mensagem m : last) {
					if (!messages.contains(m))
						facade.deleteMensagem(m.getIdMensagem(), m.getTipo());
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (!messages.isEmpty() && !flagNextPage) {
				adapter.clear();
			}

			for (Mensagem m : messages) {
				adapter.addItem(m);
			}
			//adapter.sort();

			if (flagNextPage) {
				notifyNextPageFinish();
			} else {
				((PullToRefreshListView) listView).onRefreshComplete();
			}

			isLoaddingNextPage = false;
			JSONObject prop = config.getProprietes();
			if (prop != null) {
				prop.remove("nextPage");
				try {
					prop.put("nextPage", nextPage);
				} catch (JSONException e) {

				}
			}

			AssyncTaskManager.getInstance().removeProcess(this);
			isLoading=false;
		}

	}

	@Override
	protected void onGetNextPage() {
		if (nextPage == null || isLoaddingNextPage)
			return;

		super.onGetNextPage();

		FacebookAccount faceAcc = Account.getFacebookAccount(ctx);
		if (faceAcc != null) {
			try {
				Facebook facebook = FacebookUtils.getFacebook(ctx, faceAcc);
				if (nextPage != null) {
					if (!nextPage.equals("none")) {
						Log.w("facebook-collumn", "next_started");
						isLoaddingNextPage = true;
						new FacebookNewsFeedGetterTask(facebook, nextPage)
								.execute();
					} else {
						notifyNextPageFinish();

					}
				}

			} catch (LostUserAccessException e) {

			}

		}

	}

	@Override
	public void init() {
		final Vector<Mensagem> list = facade
				.getMensagemOf(Mensagem.TIPO_NEWS_FEEDS);
		Handler h = TimelineActivity.h;
		if (h != null) {
			h.post(new Runnable() {

				@Override
				public void run() {
					updateTwittes(list, true);
//					notifyInitFinished();
				}
			});
		}

		try {
			Mensagem m = list.firstElement();
			if (m != null) {
				if (System.currentTimeMillis() - m.getData().getTime() > Constants.QTD_MINUTES) {
					nextPage = null;
					JSONObject prop = config.getProprietes();
					if (prop != null) {
						prop.remove("nextPage");
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Override
	public boolean isDeletable() {
		return false;
	}

	@Override
	public void setConfig(CollumnConfig config) {
		super.setConfig(config);
		try {
			nextPage = config.getProprietes().getString("nextPage");
		} catch (JSONException e) {

		}
	}
}
