package com.twittemarkup.view;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.markupartist.android.widget.PullToRefreshListView;
import com.twittemarkup.activity.SearchResultActivity;
import com.twittemarkup.activity.TimelineActivity;
import com.twittemarkup.adapter.MessageAdapter;
import com.twittemarkup.assynctask.UpdateAction;
import com.twittemarkup.command.OpenTwitterWriter;
import com.twittemarkup.interfaces.IGetUpdateAction;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.Mensagem;
import com.twittemarkup.model.TwitterAccount;
import com.twittemarkup.model.User;
import com.twittemarkup.model.UsuarioTwitter;
import com.twittemarkup.model.bd.Facade;

import com.twittemarkup.utils.TwitterUtils;

public class SearchColumn extends AbstractColumn implements IGetUpdateAction {

	private String search;
	private Context ctx;
	private Handler h = TimelineActivity.h;
	private int currentpage;
	private boolean flagNextPage = false;

	public SearchColumn(Context ctx, String search) {
		super(ctx, search, true);
		this.ctx = ctx;
		this.search = search;
		currentpage = 0;

		command=new OpenTwitterWriter(search);
		// addCacheResult();
	}

	@Override
	public void onGetUpdate(AccessToken token) {
		final List<Mensagem> last = new ArrayList<Mensagem>();

		for (Mensagem m : facade.getMensagemOf(Mensagem.TIPO_TWEET_SEARCH)) {
			try {
				Vector<String> parts = new Vector<String>(Arrays.asList(m
						.getMensagem().toLowerCase().split(" ")));
				if (parts.contains(search.toLowerCase()))
					last.add(m);
			} catch (Exception e) {

			}

		}

		Twitter twitter = TwitterUtils.getTwitter(token);
		Query q = new Query(search);
		if (flagNextPage)
			currentpage++;
		else
			currentpage = 1;

		flagNextPage = false;
		q.setPage(currentpage);
		try {
			QueryResult result = twitter.search(q);
			final Vector<Mensagem> mensagens = new Vector<Mensagem>();

			for (Tweet tweet : result.getTweets()) {
				Mensagem m = Mensagem.createFromTweet(tweet);

				Facade facade = Facade.getInstance(ctx);

				if (!facade.exsistsStatus(m.getIdMensagem(), m.getTipo())) {

					facade.insert(m);
					mensagens.add(m);

				}

			}

			listView.post(new Runnable() {

				@Override
				public void run() {

					for (Mensagem m : mensagens)
						adapter.addItem(m);

					if (currentpage == 1) {
						((PullToRefreshListView) listView).onRefreshComplete();
						if (!mensagens.isEmpty()) {
							adapter.clear();
							for(Mensagem m:last)
								facade.deleteMensagem(m.getIdMensagem(), m.getTipo());

						}
					} else if (currentpage > 1) {
						notifyNextPageFinish();
						adapter.sort();
					}

				}
			});

		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void cleanAll() {
		TimelineActivity.h.post(new Runnable() {

			@Override
			public void run() {
				for (AbstractColumn a : instances)
					if (a instanceof SearchColumn)
						a.adapter.removeAll();

			}
		});

	}

	@Override
	protected void updateList() {
		TwitterAccount user = (TwitterAccount) Account.getTwitterAccount(ctx);
		if (user == null)
			return;
		AccessToken token = new AccessToken(user.getToken(),
				user.getTokenSecret());

		new GetSearchTweetsTask(token).execute();

	}

	@Override
	public void deleteColumn() {
		Facade facade = Facade.getInstance(ctx);
		facade.deleteSearch(search);
		UpdateAction.unRegisterUpdateRequest(this);
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItemViewType(i) != MessageAdapter.TYPE_SEPARATOR)
				continue;

			Mensagem m = (Mensagem) adapter.getItem(i);
			facade.deleteMensagem(m.getIdMensagem(), m.getTipo());

		}

	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SearchColumn) {
			SearchColumn other = (SearchColumn) o;
			if (other.search.equals(this.search))
				return true;
		}
		return false;
	}

	@Override
	protected void onGetNextPage() {
		if (isLoaddingNextPage)
			return;

		super.onGetNextPage();
		isLoaddingNextPage = true;
		flagNextPage = true;
		updateList();
	}

	@Override
	public void init() {

		final Vector<Mensagem> toAdd=new Vector<Mensagem>();
		Facade facade = Facade.getInstance(ctx);
		for (final Mensagem m : facade
				.getMensagemOf(Mensagem.TIPO_TWEET_SEARCH)) {
			try {
				Vector<String> parts = new Vector<String>(Arrays.asList(m
						.getMensagem().toLowerCase().split(" ")));
				if (parts.contains(search.toLowerCase()))
					toAdd.add(m);
					
			} catch (Exception e) {

			}
		}
		
		listView.post(new Runnable() {

			@Override
			public void run() {
				for(Mensagem m:toAdd)
					adapter.addItem(m);
						
				
			}
		});
		
		firstPut = false;
	}

	private class GetSearchTweetsTask extends AsyncTask<Void, Void, Void> {

		private AccessToken token;
		private Vector<Mensagem> mensagens = new Vector<Mensagem>();;

		public GetSearchTweetsTask(AccessToken token) {
			this.token = token;
		}

		@Override
		protected Void doInBackground(Void... params) {

			Twitter twitter = TwitterUtils.getTwitter(token);
			Query q = new Query(search);
			if (flagNextPage)
				currentpage++;
			else
				currentpage = 1;

			flagNextPage = false;
			q.setPage(currentpage);
			try {
				QueryResult result = twitter.search(q);

				for (Tweet tweet : result.getTweets()) {
					Mensagem m = Mensagem.createFromTweet(tweet);

					Facade facade = Facade.getInstance(ctx);

					if (!facade.exsistsStatus(m.getIdMensagem(), m.getTipo())) {

						facade.insert(m);
						mensagens.add(m);

					}

				}

			} catch (TwitterException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			for (Mensagem m : mensagens)
				adapter.addItem(m);

			if (currentpage == 1) {
				((PullToRefreshListView) listView).onRefreshComplete();
			} else if (currentpage > 1) {
				notifyNextPageFinish();
				adapter.sort();
			}

		}

	}

}