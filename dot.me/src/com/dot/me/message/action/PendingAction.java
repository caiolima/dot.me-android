package com.dot.me.message.action;

import android.content.Context;

import com.dot.me.command.IMessageAction;
import com.dot.me.model.Mensagem;

public class PendingAction {

	private IMessageAction action;
	private Mensagem m;
	
	public PendingAction(IMessageAction action, Mensagem m){
		this.m=m;
		this.action=action;
		
	}
	
	public void executeAction(Context ctx){
		action.execute(m, ctx);
	}
	
}

