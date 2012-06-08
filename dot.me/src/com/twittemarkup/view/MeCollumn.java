package com.twittemarkup.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.facebook.android.Facebook;
import com.markupartist.android.widget.PullToRefreshListView;
import com.twittemarkup.activity.TimelineActivity;
import com.twittemarkup.app.R;
import com.twittemarkup.exceptions.LostUserAccessException;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.FacebookAccount;
import com.twittemarkup.model.Mensagem;
import com.twittemarkup.model.TwitterAccount;
import com.twittemarkup.utils.FacebookUtils;
import com.twittemarkup.utils.TwitterUtils;
import com.twittemarkup.utils.TwitterUtils.ResponseUpdate;
import com.twittemarkup.utils.WebService;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class MeCollumn extends AbstractColumn {

	private Facebook facebook;
	private String nextFace = null;
	private int currentPage = 0;

	public MeCollumn(Context ctx) {
		super(ctx, "@me", true);
	}

	private MeCollumn(Context ctx, String title) {
		super(ctx, title, true);

	}

	@Override
	public void deleteColumn() {

	}

	@Override
	protected void updateList() {
		FacebookAccount acc = Account.getFacebookAccount(ctx);
		if (acc != null) {
			try {
				facebook = FacebookUtils.getFacebook(ctx, acc);
				new NotificationGetterTask().execute();
			} catch (LostUserAccessException e) {

			}
		}

	}

	@Override
	public void init() {
		Vector<Mensagem> mensagens = facade
				.getMensagemOf(Mensagem.TIPO_FACEBOOK_NOTIFICATION);
		final Vector<Mensagem> toAdd = new Vector<Mensagem>();
		
		if(mensagens==null)
			return;
		
		for (final Mensagem m : mensagens) {
			toAdd.add(m);
		}
		TimelineActivity.h.post(new Runnable() {

			@Override
			public void run() {
				for (Mensagem m : toAdd)
					adapter.addItem(m);

			}
		});
		firstPut = false;
	}

	private class NotificationGetterTask extends AsyncTask<Void, Void, Void> {

		private Vector<Mensagem> mensagemAdded = new Vector<Mensagem>();
		private Vector<Mensagem> cachedMessages=new Vector<Mensagem>();

		@Override
		protected Void doInBackground(Void... v) {
			cachedMessages=facade.getMensagemOf(Mensagem.TIPO_FACEBOOK_NOTIFICATION);
			TwitterAccount twitterAcc = Account.getTwitterAccount(ctx);
			if (twitterAcc != null) {

				if (isLoaddingNextPage)
					currentPage++;
				else
					currentPage = 1;

				AccessToken token = new AccessToken(twitterAcc.getToken(),
						twitterAcc.getTokenSecret());
				Twitter t = TwitterUtils.getTwitter(token);
				try {
					Paging paging = new Paging(currentPage,10);

					ResponseList<twitter4j.Status> list = t.getMentions(paging);

					ResponseUpdate r = TwitterUtils.updateTweets(ctx, list,
							Mensagem.TIPO_FACEBOOK_NOTIFICATION);
					mensagemAdded.addAll(r.mensagens);

				} catch (TwitterException e) {

				}
			}

			FacebookAccount faceAcc = Account.getFacebookAccount(ctx);
			if (faceAcc == null)
				return null;
			Bundle params = new Bundle();
			params.putString("fields",
					"from.picture,from.name,updated_time,title,link,application.name");
			params.putString("limit", "10");
			params.putString("include_read", "1");
			try {
				String response = null;
				if (!isLoaddingNextPage) {
					response = facebook.request("me/notifications", params);
				} else if (!nextFace.equals("none")) {
					WebService service = new WebService(nextFace);
					response = service.webGet("", null);
				}

				if (response != null) {
					JSONObject responseJSON = new JSONObject(response);

					JSONArray array = responseJSON.getJSONArray("data");
					for (int i = 0; i < array.length(); i++) {
						JSONObject metionJSON = array.getJSONObject(i);
						Mensagem m = Mensagem
								.createFromFacebookNotification(metionJSON);
						if (m != null) {
							String link = m.getAddtions().getString("link");
							if (link.startsWith("http://www.facebook.com/")
									&& link.contains("/posts/")) {
								String temp = link;
								temp = temp.replace("http://www.facebook.com/",
										"");
								temp = temp.substring(0, temp.indexOf("/"));
								Bundle b = new Bundle();
								b.putString("fields", "id");
								String userIDJSON = facebook.request(temp, b);
								if (userIDJSON.contains("id")) {
									JSONObject obj = new JSONObject(userIDJSON);
									m.getAddtions().put("id",
											obj.getString("id"));
								}
							}
							
							mensagemAdded.add(m);
						}
					}

					JSONObject pagingJSON = responseJSON
							.getJSONObject("paging");
					if (pagingJSON != null) {
						try {
							nextFace = pagingJSON.getString("next");
						} catch (JSONException e) {
							nextFace = "none";
						}
					}

				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
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

			((PullToRefreshListView) listView).onRefreshComplete();
			// Collections.sort(messages);

			if (!isLoaddingNextPage && !mensagemAdded.isEmpty()) {
				adapter.clear();

				for(Mensagem m:cachedMessages){
					if(!mensagemAdded.contains(m))
						facade.deleteMensagem(m.getIdMensagem(), m.getTipo());
				}
			}

			for (Mensagem m : mensagemAdded) {
				adapter.addItem(m);
				facade.insert(m);
			}

			adapter.sort();
			if(isLoaddingNextPage)
				notifyNextPageFinish();
			
			
		}

	}

	@Override
	protected void onGetNextPage() {

		if (nextFace == null || currentPage == 0 || isLoaddingNextPage)
			return;
		super.onGetNextPage();
		
		if (!nextFace.equals("none")) {
			isLoaddingNextPage = true;
			new NotificationGetterTask().execute();
		} else {
			notifyNextPageFinish();
			Toast.makeText(ctx, ctx.getString(R.string.no_new_updates), Toast.LENGTH_SHORT);
		}
		
		

	}

	@Override
	public boolean isDeletable() {
		return false;
	}
	
	

}