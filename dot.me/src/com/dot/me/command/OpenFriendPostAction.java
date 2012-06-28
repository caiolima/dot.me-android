package com.dot.me.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dot.me.activity.FacebookMessageActivity;
import com.dot.me.model.Mensagem;

public class OpenFriendPostAction implements IMessageAction {

	private static OpenFriendPostAction singleton;
	
	public static OpenFriendPostAction getInstance(){
		if(singleton==null)
			singleton=new OpenFriendPostAction();
		
		return singleton;
	}
	
	private OpenFriendPostAction(){}
	
	@Override
	public void execute(Mensagem m, Context ctx) {
		
		try {
			String link=m.getAddtions().getString("link");
			String[] parts = link.split("/");
			
			String id=m.getAddtions().getString("id")+"_"+parts[parts.length-1];
			if(id.contains("?comment_id=")){
				id=id.substring(0, id.indexOf("?comment_id="));
			}
			
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
