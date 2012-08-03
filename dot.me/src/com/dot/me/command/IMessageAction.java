package com.dot.me.command;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;

import com.dot.me.model.Mensagem;

public interface IMessageAction {

	public void execute(Mensagem m, Context ctx);
	public Intent createIntent(Mensagem m, Context ctx) throws JSONException;
	
}
