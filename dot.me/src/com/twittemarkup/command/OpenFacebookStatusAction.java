package com.twittemarkup.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.twittemarkup.activity.FacebookMessageActivity;
import com.twittemarkup.model.Mensagem;

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
		
		Intent intent = new Intent(ctx,
				FacebookMessageActivity.class);
		Bundle b = new Bundle();
		b.putString("idMessage", m.getIdMensagem());
		b.putInt("type", m.getTipo());
		intent.putExtras(b);

		ctx.startActivity(intent);

		
	}

}
