package com.dot.me.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dot.me.activity.FacebookMessageActivity;
import com.dot.me.model.Mensagem;

public class OpenFacebookStatusAction implements IMessageAction {
	
	private static OpenFacebookStatusAction singleton;
	
	public static OpenFacebookStatusAction getInstance(){
		if(singleton==null)
			singleton=new OpenFacebookStatusAction();
		
		return singleton;
	}
	
	private OpenFacebookStatusAction(){}
	
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
		Intent intent = new Intent(ctx,
				FacebookMessageActivity.class);
		Bundle b = new Bundle();
		b.putString("idMessage", m.getIdMensagem());
		b.putInt("type", m.getTipo());
		b.putString("notify_id", m.getIdMensagem());
		intent.putExtras(b);
		
		return intent;
	}

}
