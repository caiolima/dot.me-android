package com.twittemarkup.command;

import android.content.Context;

import com.twittemarkup.model.Mensagem;

public interface IMessageAction {

	public void execute(Mensagem m, Context ctx);
	
}
