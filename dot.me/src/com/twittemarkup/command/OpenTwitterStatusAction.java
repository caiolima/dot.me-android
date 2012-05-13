package com.twittemarkup.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.twittemarkup.activity.MessageInfoActivity;
import com.twittemarkup.model.Mensagem;

public class OpenTwitterStatusAction implements IMessageAction {

	private static OpenTwitterStatusAction singleton;
	
	public static OpenTwitterStatusAction getInstance(){
		if(singleton==null)
			singleton=new OpenTwitterStatusAction();
		
		return singleton;
	}
	
	private OpenTwitterStatusAction(){}
	
	@Override
	public void execute(Mensagem m, Context ctx) {
		
		Intent intent = new Intent(ctx,
				MessageInfoActivity.class);
		Bundle b = new Bundle();
		b.putString("idMessage", m.getIdMensagem());
		b.putInt("type", m.getTipo());
		intent.putExtras(b);

		ctx.startActivity(intent);
		
	}

}
