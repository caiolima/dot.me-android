package com.dot.me.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dot.me.activity.MessageInfoActivity;
import com.dot.me.model.Mensagem;

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
				MessageInfoActivity.class);
		Bundle b = new Bundle();
		b.putString("idMessage", m.getIdMensagem());
		b.putInt("type", m.getTipo());
		intent.putExtras(b);
		
		return intent;
	}
	
	

}
