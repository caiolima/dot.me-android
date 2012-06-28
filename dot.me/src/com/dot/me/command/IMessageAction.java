package com.dot.me.command;

import android.content.Context;

import com.dot.me.model.Mensagem;

public interface IMessageAction {

	public void execute(Mensagem m, Context ctx);
	
}
