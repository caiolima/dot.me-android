package com.twittemarkup.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.sax.StartElementListener;

import com.twittemarkup.model.Mensagem;

public class OpenLinkActivity implements IMessageAction {

	private static OpenLinkActivity singleton;
	
	public static OpenLinkActivity getInstance(){
		if(singleton==null)
			singleton= new OpenLinkActivity();
		
		return singleton;
	}
	
	@Override
	public void execute(Mensagem m, Context ctx) {
		try {
			String link=m.getAddtions().getString("link");
			Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			ctx.startActivity(intent);
		} catch (JSONException e) {
			
		}
	}

}
