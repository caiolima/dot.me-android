package com.twittemarkup.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.android.Facebook;
import com.markupartist.android.widget.PullToRefreshListView;
import com.twittemarkup.app.R;
import com.twittemarkup.command.OpenFacebookWriter;
import com.twittemarkup.exceptions.LostUserAccessException;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.FacebookAccount;
import com.twittemarkup.model.Mensagem;
import com.twittemarkup.model.bd.Facade;
import com.twittemarkup.utils.FacebookUtils;
import com.twittemarkup.utils.WebService;

public class FacebookFeedsColumn extends AbstractColumn {

	private boolean isLoaddingNextPage = false;
	private String nextPage;
	private Facebook facebook;

	public FacebookFeedsColumn(Context ctx) {
		this(ctx, ctx.getString(R.string.news_fedd));
		command=new OpenFacebookWriter();
		
	}

	private FacebookFeedsColumn(Context ctx, String title) {
		super(ctx, title,true);
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

	/*
	 * private class FeedsRecieverListener extends BaseRequestListener{
	 * 
	 * private PullToRefreshListView listView; private Vector<Mensagem>
	 * messages=new Vector<Mensagem>();
	 * 
	 * public FeedsRecieverListener(PullToRefreshListView lst){
	 * this.listView=lst; }
	 * 
	 * @Override public void onComplete(String response, Object state) { try {
	 * JSONObject json=new JSONObject(response); JSONArray
	 * array=json.getJSONArray("data"); for(int i=0;i<array.length();i++){
	 * Mensagem m=Mensagem.createFromFacebookFeed(array.getJSONObject(i));
	 * if(m!=null){ Facade.getInstance(ctx).insert(m); messages.add(m); }
	 * 
	 * } TimelineActivity.h.post(new Runnable() {
	 * 
	 * @Override public void run() { listView.onRefreshComplete();
	 * 
	 * } });
	 * 
	 * updateTwittes(messages, true);
	 * 
	 * } catch (JSONException e) {
	 * 
	 * }
	 * 
	 * 
	 * }
	 * 
	 * }
	 */

	// private int num_new_feeds;

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
		protected Void doInBackground(Void... ins) {

			try {
				last=facade.getMensagemOf(Mensagem.TIPO_NEWS_FEEDS);
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
					
					/*JSONArray array = json.getJSONArray("data");
					for (int i = 0; i < array.length(); i++) {
						JSONObject object = array.getJSONObject(i);
						Mensagem m = facade.getOneMessage(
								object.getString("id"),
								Mensagem.TIPO_NEWS_FEEDS);
						if (m == null) {
							m = Mensagem.createFromFacebookFeed(object);

							if (m == null)
								continue;

							m.setTipo(Mensagem.TIPO_NEWS_FEEDS);

							String obString = null;

							if (object.getString("type").equals("photo")
									&& m.getPictureUrl() == null) {
								obString = object.getString("object_id");
								Bundle params = new Bundle();
								params.putString("fields",
										"picture,source,id,width,height");
								String response2 = facebook.request(obString,
										params);
								try {
									JSONObject pic_info = new JSONObject(
											response2);
									m.getAddtions().put("pic_info", pic_info);
								} catch (JSONException e) {

								}
							}

							facade.insert(m);

						}

						messages.add(m);

					}
					// messages.addAll(createdMessages);*/
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

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if(!messages.isEmpty()&&!flagNextPage){
				adapter.clear();
				for (Mensagem m : last) {
					facade.deleteMensagem(m.getIdMensagem(), m.getTipo());
			}
			}
			
			for (Mensagem m : messages) {
				adapter.addItem(m);
			}
			adapter.sort();
			
			if (flagNextPage) {
				notifyNextPageFinish();
			} else {
				((PullToRefreshListView)listView).onRefreshComplete();
			}
			
			

			isLoaddingNextPage = false;
		}

	}

	@Override
	protected void onGetNextPage() {
		if(nextPage==null||isLoaddingNextPage)
			return;
		
		super.onGetNextPage();

		
		if (nextPage != null) {
			if (!nextPage.equals("none")) {
				Log.w("facebook-collumn", "next_started");
				isLoaddingNextPage = true;
				new FacebookNewsFeedGetterTask(facebook, nextPage).execute();
			} else {
				notifyNextPageFinish();

			}
		}
	}

	@Override
	public void init() {
		final Vector<Mensagem> list=facade.getMensagemOf(Mensagem.TIPO_NEWS_FEEDS);
		listView.post(new Runnable() {
			
			@Override
			public void run() {
				updateTwittes(list, true);
			
				
			}
		});
		

	}

	@Override
	public boolean isDeletable() {
		return false;
	}

	
}