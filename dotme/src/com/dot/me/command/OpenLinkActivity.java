package com.dot.me.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.dot.me.model.Mensagem;

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
			
			ctx.startActivity(createIntent(m, ctx));
		} catch (JSONException e) {
			
		}
	}

	@Override
	public Intent createIntent(Mensagem m, Context ctx) throws JSONException {
		String link=m.getAddtions().getString("link");
		Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		
		return intent;
	}

}
