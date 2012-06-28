package com.dot.me.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dot.me.activity.FacebookMessageActivity;
import com.dot.me.model.Mensagem;

public class OpenFacebookGroupNotificationAction implements IMessageAction{

	private static OpenFacebookGroupNotificationAction singleton;
	
	public static OpenFacebookGroupNotificationAction getInstance(){
		if(singleton==null)
			singleton = new OpenFacebookGroupNotificationAction();
		
		return singleton;
	}
	
	private OpenFacebookGroupNotificationAction(){}
	
	@Override
	public void execute(Mensagem m, Context ctx) {
		try {
			String link=m.getAddtions().getString("link");
			String id=link.replace("http://www.facebook.com/groups/", "").replace("permalink/", "").replace("/", "_");
			if(id.contains("?comment_id=")){
				id=id.substring(0, id.indexOf("?comment_id="));
			}
			id=id.substring(0, id.length()-1);
			
			Intent intent=new Intent(ctx,FacebookMessageActivity.class);
			Bundle b=new Bundle();
			b.putString("idMessage", id);
			b.putInt("type", Mensagem.TIPO_FACEBOOK_GROUP);
			intent.putExtras(b);
			
			ctx.startActivity(intent);
			
		} catch (JSONException e) {
			
		}
		
	}

}
