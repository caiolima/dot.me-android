package com.twittemarkup.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.twittemarkup.activity.FacebookMessageActivity;
import com.twittemarkup.model.Mensagem;

public class OpenFacebookTaggedAction implements IMessageAction{

	private static OpenFacebookTaggedAction singleton;
	
	public static OpenFacebookTaggedAction getInstance(){
		if(singleton==null)
			singleton=new OpenFacebookTaggedAction();
		
		return singleton;
	}
	
	private OpenFacebookTaggedAction(){}
	
	@Override
	public void execute(Mensagem m, Context ctx) {
		
		String link;
		try {
			link = m.getAddtions().getString("link");
			link=link.replace("http://www.facebook.com/permalink.php?story_fbid=", "");
			String[] parts=link.split("&id=");
			String id=parts[1]+"_"+parts[0];
			
			Intent intent=new Intent(ctx,FacebookMessageActivity.class);
			Bundle b=new Bundle();
			b.putString("idMessage", id);
			b.putInt("type", Mensagem.TIPO_NEWS_FEEDS);
			intent.putExtras(b);
			
			ctx.startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
