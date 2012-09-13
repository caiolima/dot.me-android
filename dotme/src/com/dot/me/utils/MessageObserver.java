package com.dot.me.utils;

import com.dot.me.model.Mensagem;

public interface MessageObserver {

	public void notifyMessageAdded(Mensagem m);
	public void notifyMessageRemoved(String id,int type);
	
}
