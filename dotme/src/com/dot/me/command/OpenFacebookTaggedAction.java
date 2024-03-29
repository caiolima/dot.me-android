package com.dot.me.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dot.me.activity.FacebookMessageActivity;
import com.dot.me.model.Mensagem;

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
		
		
		try {
			
			
			ctx.startActivity(createIntent(m, ctx));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public Intent createIntent(Mensagem m, Context ctx) throws JSONException {
		String link = m.getAddtions().getString("link");
		link=link.replace("http://www.facebook.com/permalink.php?story_fbid=", "");
		String[] parts=link.split("&id=");
		String id=parts[1]+"_"+parts[0];
		
		Intent intent=new Intent(ctx,FacebookMessageActivity.class);
		Bundle b=new Bundle();
		b.putString("idMessage", id);
		b.putInt("type", Mensagem.TIPO_NEWS_FEEDS);
		b.putString("notify_id", m.getIdMensagem());
		intent.putExtras(b);
		
		return intent;
	}

}
