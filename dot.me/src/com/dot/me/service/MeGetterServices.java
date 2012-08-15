package com.dot.me.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.activity.FacebookMessageActivity;
import com.dot.me.app.R;
import com.dot.me.exceptions.LostUserAccessException;
import com.dot.me.model.Account;
import com.dot.me.model.FacebookAccount;
import com.dot.me.model.Mensagem;
import com.dot.me.model.bd.DataBase;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;
import com.dot.me.utils.FacebookUtils;
import com.dot.me.utils.WebService;
import com.facebook.android.Facebook;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

@SuppressLint("NewApi")
@TargetApi(11)
public class MeGetterServices extends Service {

	private NotificationManager nManager;
	private static Timer timer = new Timer();
	private Context ctx;
	private List<Mensagem> messageAdded=new ArrayList<Mensagem>();
	private FacebookAccount acc;
	private static int intentID=0; 
	
	@Override
	public IBinder onBind(Intent i) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		ctx = this;
		acc = Account.getFacebookAccount(ctx);
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		boolean isToDownload=settings.getBoolean(Constants.CONFIG_NOTIFICATIONS, true);
		if(acc==null&&!isToDownload){
			stopSelf();
			return;
		}
		startService();
	}

	private void startService() {
		timer.scheduleAtFixedRate(new MainTask(), 0, 1000 * 60 * 2);
		String ns = Context.NOTIFICATION_SERVICE;
		nManager = (NotificationManager) getSystemService(ns);
	}

	private class MainTask extends TimerTask {
		public void run() {

			SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
			boolean isToDownload=settings.getBoolean(Constants.CONFIG_NOTIFICATIONS, true);
			
			if(!isToDownload){
				stopSelf();
				return;
			}
			if (!DataBase.isOppened()) {
				Facade.destroy();
				DataBase.start(ctx);
			}

			
			Facade facade = Facade.getInstance(ctx);
			List<Mensagem> cachedMessages = facade
					.getMensagemOf(Mensagem.TIPO_FACEBOOK_NOTIFICATION);
			if (acc != null) {
				try {
					Facebook facebook = FacebookUtils.getFacebook(ctx, acc);

					if(facebook==null)
						return;
					
					Bundle params = new Bundle();
					params.putString("fields",
							"from.picture,from.name,updated_time,title,link,application.name");
					Mensagem lastNotification = null;
					if (cachedMessages != null) {
						for (Mensagem m : cachedMessages) {
							if (m.getIdMensagem().startsWith("notif_")) {
								lastNotification = m;
								break;
							}
						}
					}

					
					if(lastNotification!=null){
						params.putString("since", Long.toString(lastNotification.getData().getTime()/1000L));
					}
					
					String response = facebook.request("me/notifications",
							params);
					if (response != null) {
						JSONObject responseJSON = new JSONObject(response);

						JSONArray array = responseJSON.getJSONArray("data");
//						JSONArray batch_array = new JSONArray();
						for (int i = array.length()-1; i >=0; i--) {
							JSONObject metionJSON = array.getJSONObject(i);
							Mensagem m = Mensagem
									.createFromFacebookNotification(metionJSON,MeGetterServices.this);
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
								
								m.setIdMensagem(m.getIdMensagem().replace("__", "_"+acc.getId()+"_"));
								
//								JSONObject comentsToGet = new JSONObject();
//								try {
//									comentsToGet.put("method", "POST");
//									comentsToGet
//											.put("relative_url",
//													m.getIdMensagem()+"?unread=0");
//
//									batch_array.put(comentsToGet);
//								} catch (JSONException e) {
//
//								}
								
								messageAdded.add(m);
							}

						}
						
//						List<NameValuePair> args = new ArrayList<NameValuePair>();
//						args.add(new BasicNameValuePair("access_token", acc
//								.getToken()));
//						args.add(new BasicNameValuePair("batch",
//								batch_array.toString()));
//
//						WebService web = new WebService(
//								"https://graph.facebook.com/");
//						String responseBactch = web.doPost("", args);
//						JSONArray responseUnreadJSON = new JSONArray(
//								responseBactch);
//
//						if (responseUnreadJSON == null)
//							return ;
						
						for(Mensagem m:messageAdded){
							if(facade.exsistsStatus(m.getIdMensagem(), Mensagem.TIPO_FACEBOOK_NOTIFICATION))
								continue;
							
							facade.insert(m);
							showNotification(m);
						}
						messageAdded.clear();
						
					}

				} catch (LostUserAccessException e) {

				} catch (MalformedURLException e) {

				} catch (IOException e) {

				} catch (JSONException e) {

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	private void showNotification(Mensagem m){
 		int icon = R.drawable.icon_notification;
		CharSequence tickerText = m.getMensagem();
		long when = m.getData().getTime();
		
		CharSequence contentTitle = m.getNome_usuario();
		CharSequence contentText = m.getMensagem();
//		Intent notificationIntent = new Intent(this, FacebookMessageActivity.class);
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new Notification(icon, tickerText, when);
				
		Intent notificationIntent=null;
		try {
			notificationIntent = m.getAction().createIntent(m, this);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		notification.flags|=Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = null;
		if(notificationIntent!=null){
				contentIntent=PendingIntent.getActivity(this, intentID, notificationIntent, 0);
				intentID++;
		}else
			return;
		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		
		nManager.notify(m.getIdMensagem(), 1, notification);		
	}

}
