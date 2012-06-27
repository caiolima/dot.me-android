package com.twittemarkup.utils;

import com.twittemarkup.model.Mensagem;

public interface MessageObserver {

	public void notifyMessageAdded(Mensagem m);
	public void notifyMessageRemoved(String id,int type);
	
}
