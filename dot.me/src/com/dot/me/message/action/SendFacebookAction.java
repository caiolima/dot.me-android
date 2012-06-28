package com.dot.me.message.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.app.R;
import com.dot.me.exceptions.LostUserAccessException;
import com.dot.me.model.Account;
import com.dot.me.model.FacebookAccount;
import com.dot.me.utils.FacebookUtils;
import com.facebook.android.Facebook;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class SendFacebookAction implements ISendAction {

	private String id,toUser;
	
	
	@Override
	public void execute(Activity a, String message, Bundle sendParams) {
		try {
			FacebookAccount acc = Account.getFacebookAccount(a);
			Facebook facebook = FacebookUtils.getFacebook(a, acc);

			Bundle b = new Bundle();
			b.putString("message", message);
			toUser = "me";
			if (sendParams != null) {
				String to = sendParams.getString("toID");
				if (to != null)
					toUser = to;
			}

			String response = facebook.request(toUser + "/feed", b, "POST");

			if (response != null) {
				JSONObject jsonResponse = new JSONObject(response);
				id =null;
				try {
					 id = jsonResponse.getString("id");
				} catch (JSONException e) {
					
				}
			}

		} catch (LostUserAccessException e) {

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	}

	@Override
	public String getResultMessage(Context c) {
		String message=null;
		if(id!=null)
			message=c.getString(R.string.message_sent);
		else
			message=c.getString(R.string.erro_message_sent);
		
		return message;
	}

	@Override
	public Account getAccount(Context c) {
		// TODO Auto-generated method stub
		return  Account.getFacebookAccount(c);
	}

	@Override
	public String getDraftId() {
		
		return "facebook_"+toUser;
	}

	@Override
	public boolean messageSent() {
		
		if(id!=null)
			return true;
		else
			return false;
	}

}
