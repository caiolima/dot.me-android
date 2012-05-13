package com.twittemarkup.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook;
import com.markupartist.android.widget.PullToRefreshListView;
import com.twittemarkup.activity.TimelineActivity;
import com.twittemarkup.app.R;
import com.twittemarkup.exceptions.LostUserAccessException;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.FacebookAccount;
import com.twittemarkup.model.Mensagem;
import com.twittemarkup.utils.FacebookUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

public class NotificationsCollumn extends AbstractColumn{

	private Facebook facebook;
	
	public NotificationsCollumn(Context ctx){
		super(ctx, ctx.getString(R.string.notifications),true);
	}
	
	private NotificationsCollumn(Context ctx, String title) {
		super(ctx, title,true);
		
	}

	@Override
	public void deleteColumn() {
		
		
	}

	@Override
	protected void updateList() {
		FacebookAccount acc = Account.getFacebookAccount(ctx);
		if (acc != null) {
			try {
				facebook=FacebookUtils.getFacebook(ctx, acc);
				new NotificationGetterTask().execute();
			} catch (LostUserAccessException e) {

			}
		}
		
	}

	@Override
	public void init() {
		Vector<Mensagem> mensagens = facade
				.getMensagemOf(Mensagem.TIPO_FACEBOOK_NOTIFICATION);
		final Vector<Mensagem> toAdd=new Vector<Mensagem>();
		for (final Mensagem m : mensagens) {
			toAdd.add(m);
		}
		TimelineActivity.h.post(new Runnable() {
			
			@Override
			public void run() {
				for(Mensagem m:toAdd)
					adapter.addItem(m);
				
				
			}
		});
		firstPut = false;
	}
	
	private class NotificationGetterTask extends AsyncTask<Void, Void, Void>{

		private Vector<Mensagem> mensagemAdded=new Vector<Mensagem>();
		
		@Override
		protected Void doInBackground(Void... v) {
			
			Bundle params=new Bundle();
			params.putString("fields", "from.picture,from.name,updated_time,title,link,application.name");
			
			try {
				String response=facebook.request("me/notifications",params);
				JSONObject responseJSON=new JSONObject(response);
				
				JSONArray array=responseJSON.getJSONArray("data");
				for(int i=0;i<array.length();i++){
					JSONObject metionJSON=array.getJSONObject(i);
					Mensagem m=Mensagem.createFromFacebookNotification(metionJSON);
					if(m!=null){
						String link=m.getAddtions().getString("link");
						if(link.startsWith("http://www.facebook.com/")&&link.contains("/posts/")){
							String temp=link;
							temp=temp.replace("http://www.facebook.com/", "");
							temp=temp.substring(0,temp.indexOf("/"));
							Bundle b=new Bundle();
							b.putString("fields", "id");
							String userIDJSON=facebook.request(temp, b);
							if(userIDJSON.contains("id")){
								JSONObject obj=new JSONObject(userIDJSON);
								m.getAddtions().put("id", obj.getString("id"));
							}
						}
						if(facade.insert(m))
							mensagemAdded.add(m);
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
			
			((PullToRefreshListView)listView).onRefreshComplete();
			
			for(Mensagem m:mensagemAdded){
				adapter.addItem(m);
			}
			
			adapter.sort();
			
		}
		
		
		
	}

	@Override
	protected void onGetNextPage() {
	}

	
	
	
}
