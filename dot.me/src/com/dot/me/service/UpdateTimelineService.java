package com.dot.me.service;

import java.util.Date;
import java.util.Vector;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.model.Mensagem;
import com.dot.me.model.TwitterAccount;
import com.dot.me.model.User;
import com.dot.me.model.UsuarioTwitter;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;
import com.dot.me.utils.ImageUtils;
import com.dot.me.utils.MensageList;
import com.dot.me.utils.Separator;
import com.dot.me.utils.TwitterUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class UpdateTimelineService extends Service {

	private Intent intent;
	

	@Override
	public void onStart(Intent intent, int startId) {
		this.intent = intent;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nInfo = cm.getActiveNetworkInfo();
		if (nInfo != null) {
			if (nInfo.isConnected()) {
				new UpdateTask().execute(this);
			}
		} else {
			Toast t = Toast.makeText(this, getString(R.string.unable_update),
					Toast.LENGTH_LONG);
			t.show();
		}

	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	class UpdateTask extends AsyncTask<Service, Void, Void> {

		@Override
		protected Void doInBackground(Service... params) {
			try {
				TwitterAccount user = 
						(TwitterAccount) Account.getLoggedUsers(UpdateTimelineService.this).get(0); //verificar depois

				AccessToken accessToken = new AccessToken(user.getToken(),
						user.getTokenSecret());
				ResponseList<twitter4j.Status> list;

				int n_page = 1;
				int qtd_feeds = Constants.QTD_FEEDS;
				
				if (intent != null) {
					Bundle bundle = intent.getExtras();
					if (bundle != null) {
						n_page = bundle.getInt("page");
						qtd_feeds = bundle.getInt("qtd_feeds");
					}
				}
				Paging page = new Paging(n_page, qtd_feeds);
				list = TwitterUtils.getTwitter(accessToken).getHomeTimeline(
						page);

				ResponseUpdate response=updateTweets(list);
				Vector<Mensagem> mensagens=response.mensagens;
				Mensagem lastMessage=response.lastMessage;

				boolean top=true;
				if(n_page>1)
					top=false;
				
				if(mensagens.size()<Constants.QTD_FEEDS&&!top){
					//Atuliza no topo
					page = new Paging(1, qtd_feeds);
					list = TwitterUtils.getTwitter(accessToken).getHomeTimeline(
							page);
					response=updateTweets(list);
					mensagens=response.mensagens;
					
					if (TimelineActivity.getCurrent() != null)
						TimelineActivity.getCurrent().setCurrentList(mensagens);
					Intent intent = new Intent(
							"com.twittemarkup.reciever.UPDATE_MSG");
					
					Bundle b=new Bundle();
					b.putBoolean("top", true);
					intent.putExtras(b);
					sendBroadcast(intent);
					
					//atualiza nova pagina
					int num_feeds=Facade.getInstance(UpdateTimelineService.this).getCountMensagem(Mensagem.TIPO_STATUS);
					int currentPage=num_feeds/Constants.QTD_FEEDS;
					
					page = new Paging(currentPage+1, qtd_feeds);
					list = TwitterUtils.getTwitter(accessToken).getHomeTimeline(
							page);
					response=updateTweets(list);
					mensagens=response.mensagens;
					if (TimelineActivity.getCurrent() != null)
						TimelineActivity.getCurrent().setCurrentList(mensagens);
					Intent intent2 = new Intent(
							"com.twittemarkup.reciever.UPDATE_MSG");
					
					Bundle b2=new Bundle();
					b.putBoolean("top", false);
					intent2.putExtras(b2);
					sendBroadcast(intent2);
					
					return null;
				}else if (mensagens.size() >= Constants.QTD_FEEDS) {
					Facade.getInstance(UpdateTimelineService.this).deleteAllTo(
							lastMessage.getData().getTime());
				}
				if (TimelineActivity.getCurrent() != null)
					TimelineActivity.getCurrent().setCurrentList(mensagens);
				Intent intent = new Intent(
						"com.twittemarkup.reciever.UPDATE_MSG");
				
				Bundle b=new Bundle();
				b.putBoolean("top", top);
				intent.putExtras(b);
				sendBroadcast(intent);

			} catch (TwitterException e) {

			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			stopSelf();
		}
		
		private ResponseUpdate updateTweets(ResponseList<twitter4j.Status> list){
			//ImageUtils.loadImages(list);
			Mensagem lastMessage = null;
			Vector<Mensagem> mensagens = new Vector<Mensagem>();
			for (twitter4j.Status status : list) {
				Mensagem m = Mensagem.creteFromTwitterStatus(status);
				
				
				Facade facade = Facade
						.getInstance(UpdateTimelineService.this);
				User u=User.createFromTwitterUser(status.getUser());
				facade.insert(u);
				
				if (!facade.exsistsStatus(m.getIdMensagem(),m.getTipo())) {

					facade.insert(m);
					mensagens.add(m);
					lastMessage = m;
				}

			}
			ResponseUpdate response=new ResponseUpdate();
			response.lastMessage=lastMessage;
			response.mensagens=mensagens;
			return response;
		}

		private class ResponseUpdate{
			public Vector<Mensagem> mensagens;
			public Mensagem lastMessage;
		}

	}

}
