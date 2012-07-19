package com.dot.me.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.dot.me.app.R;
import com.dot.me.exceptions.LostUserAccessException;
import com.dot.me.model.Account;
import com.dot.me.model.FacebookAccount;
import com.dot.me.model.Mensagem;
import com.dot.me.model.bd.Facade;
import com.facebook.android.Facebook;

public class FacebookUtils {

	private static Facebook mFacebook;

	public static Facebook getFacebook(Context ctx, FacebookAccount account)
			throws LostUserAccessException {
		if (mFacebook == null) {
			mFacebook = new Facebook(Constants.FACEBOOK_APP_ID);

			mFacebook.setAccessToken(account.getToken());
			// mFacebook.setAccessExpires(account.getExpires());

			if (!mFacebook.isSessionValid()) {
				throw new LostUserAccessException(
						ctx.getString(R.string.facebook_connect_lost));
			}
		}
		return mFacebook;
	}

	public static Date getTime(String date) throws ParseException {
		long now = System.currentTimeMillis(); // Gets current local time in ms
		TimeZone local_tz = TimeZone.getDefault(); // Gets current local TZ of
													// phone
		long tz_offset_gmt = local_tz.getOffset(now);

		date = date.substring(0, date.length() - 5);
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		Date parsedDate = formatter.parse(date);
		// return parsedDate;
		return new Date(parsedDate.getTime() + tz_offset_gmt);
	}

	public static List<Mensagem> createListOfFeeds(Facade facade,
			Facebook facebook, JSONObject response, int tipo) {
		ArrayList<Mensagem> all = new ArrayList<Mensagem>();

		try {
			JSONArray array = response.getJSONArray("data");
			JSONArray batch_array = new JSONArray();

			List<Mensagem> photoMessages = new ArrayList<Mensagem>();

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);

				Mensagem m = Mensagem.createFromFacebookFeed(object);

				if (m == null)
					continue;

				m.setTipo(tipo);

				String obString = null;
				try {
					if (object.getString("type").equals("photo")) {

						obString = object.getString("object_id");

						JSONObject picturesToGet = new JSONObject();

						picturesToGet.put("method", "GET");
						picturesToGet
								.put("relative_url",
										obString
												+ "?fields=picture,source,id,width,height,from.name,name");

						batch_array.put(picturesToGet);
						photoMessages.add(m);
						// Bundle params = new Bundle();
						// params.putString("fields",
						// "picture,source,id,width,height,from.name,name");
						// String response2 = facebook.request(obString,
						// params);
						// try {
						// JSONObject pic_info = new JSONObject(response2);
						// m.getAddtions().put("pic_info", pic_info);
						// } catch (JSONException e) {
						// // TODO: handle exception
						// }
					}
				} catch (JSONException e) {
					// TODO: handle exception
				}

				all.add(m);

			}

			// carregando imagens
			List<NameValuePair> args = new ArrayList<NameValuePair>();
			args.add(new BasicNameValuePair("access_token", facebook
					.getAccessToken()));
			args.add(new BasicNameValuePair("batch", batch_array.toString()));

			WebService web = new WebService("https://graph.facebook.com/");
			String responseBactch = web.doPost("", args);
			JSONArray commentsArray = new JSONArray(responseBactch);

			if (commentsArray != null) {

				for (int j = 0; j < commentsArray.length(); j++) {
					JSONObject obj = commentsArray.getJSONObject(j);
					Mensagem pMessage = photoMessages.get(j);

					try {
						JSONObject body = new JSONObject(obj.getString("body"));

						pMessage.getAddtions().put("pic_info", body);
					} catch (JSONException e) {
						// TODO: handle exception
					}

				}

			}

			for (Mensagem m : all) {
				Mensagem cachedM = facade
						.getOneMessage(m.getIdMensagem(), tipo);

				if (cachedM != null) {
					/*
					 * try{ String
					 * pictureURL=m.getAddtions().getString("picture");
					 * if(pictureURL.equals("")){
					 * m.getAddtions().put("pic_info",
					 * cachedM.getAddtions().getString("pic_info")); } }catch
					 * (JSONException e) {
					 * 
					 * }
					 */
					m.getAddtions().remove("liked");
					m.getAddtions().put("liked", cachedM.isLiked());
					facade.update(m);
				} else {

					facade.insert(m);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return all;
	}

	public static Bundle getStandartFeedsBundle() {
		Bundle params = new Bundle();
		params.putString(
				"fields",
				"from.picture,from.name,from.id,message,likes,comments,picture,story,created_time,updated_time,type,to,object_id,link,name,caption,description");
		return params;
	}

	public static void logoutFacebook() {
		mFacebook = null;
	}

}
