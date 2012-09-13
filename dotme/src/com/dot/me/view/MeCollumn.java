package com.dot.me.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.assynctask.AssyncTaskManager;
import com.dot.me.exceptions.LostUserAccessException;
import com.dot.me.model.Account;
import com.dot.me.model.FacebookAccount;
import com.dot.me.model.Mensagem;
import com.dot.me.model.TwitterAccount;
import com.dot.me.utils.FacebookUtils;
import com.dot.me.utils.TwitterUtils;
import com.dot.me.utils.WebService;
import com.dot.me.utils.TwitterUtils.ResponseUpdate;
import com.facebook.android.Facebook;
import com.markupartist.android.widget.PullToRefreshListView;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

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

		try {
			if (acc != null)
				facebook = FacebookUtils.getFacebook(ctx, acc);

			new NotificationGetterTask().execute();
		} catch (LostUserAccessException e) {

		}

	}

	@Override
	public void init() {
		Vector<Mensagem> mensagens = facade
				.getMensagemOf(Mensagem.TIPO_FACEBOOK_NOTIFICATION);
		final Vector<Mensagem> toAdd = new Vector<Mensagem>();

		if (mensagens == null)
			return;

		for (final Mensagem m : mensagens) {
			toAdd.add(m);
		}
		TimelineActivity.h.post(new Runnable() {

			@Override
			public void run() {
				for (Mensagem m : toAdd)
					adapter.addItem(m);

//				notifyInitFinished();
			}
		});
		firstPut = false;
	}

	private class NotificationGetterTask extends AsyncTask<Void, Void, Void> {

		private Vector<Mensagem> mensagemAdded = new Vector<Mensagem>();
		private Vector<Mensagem> cachedMessages = new Vector<Mensagem>();

		@Override
		protected Void doInBackground(Void... v) {
			AssyncTaskManager.getInstance().addProccess(this);

			cachedMessages = facade
					.getMensagemOf(Mensagem.TIPO_FACEBOOK_NOTIFICATION);
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
					Paging paging = new Paging(currentPage, 10);

					ResponseList<twitter4j.Status> list = t.getMentions(paging);

					ResponseUpdate r = TwitterUtils.updateTweets(ctx, list,
							Mensagem.TIPO_FACEBOOK_NOTIFICATION);
					mensagemAdded.addAll(r.mensagens);

				} catch (TwitterException e) {

				}
			}

			FacebookAccount faceAcc = Account.getFacebookAccount(ctx);
			if (faceAcc != null) {

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
									.createFromFacebookNotification(metionJSON,ctx);
							if (m != null) {
								String link = m.getAddtions().getString("link");
								if (link.startsWith("http://www.facebook.com/")
										&& link.contains("/posts/")) {
									String temp = link;
									temp = temp.replace(
											"http://www.facebook.com/", "");
									temp = temp.substring(0, temp.indexOf("/"));
									Bundle b = new Bundle();
									b.putString("fields", "id");
									String userIDJSON = facebook.request(temp,
											b);
									if (userIDJSON.contains("id")) {
										JSONObject obj = new JSONObject(
												userIDJSON);
										m.getAddtions().put("id",
												obj.getString("id"));
									}
								}

								m.setIdMensagem(m.getIdMensagem().replace("__", "_"+faceAcc.getId()+"_"));
								
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
			} else {
				nextFace = "none";
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

				for (Mensagem m : cachedMessages) {
					if (!mensagemAdded.contains(m))
						facade.deleteMensagem(m.getIdMensagem(), m.getTipo());
				}
			}

			for (Mensagem m : mensagemAdded) {
				adapter.addItem(m);
				facade.insert(m);
			}

			adapter.sort();
			if (isLoaddingNextPage)
				notifyNextPageFinish();

			AssyncTaskManager.getInstance().removeProcess(this);
			
			isLoading=false;
		}

	}

	@Override
	protected void onGetNextPage() {

		if ((nextFace == null && currentPage == 0) || isLoaddingNextPage){
			
			return;
		}
		super.onGetNextPage();

		isLoaddingNextPage = true;
		new NotificationGetterTask().execute();

	}

	@Override
	public boolean isDeletable() {
		return false;
	}

}
