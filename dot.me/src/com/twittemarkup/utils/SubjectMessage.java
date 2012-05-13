package com.twittemarkup.utils;

import java.util.ArrayList;
import java.util.List;

import com.twittemarkup.model.Mensagem;

public class SubjectMessage {

	private List<MessageObserver> observers=new ArrayList<MessageObserver>();
	
	public void registerObserver(MessageObserver o){
		observers.add(o);
	}
	
	public void unregisterObserver(MessageObserver o){
		observers.remove(o);
	}
	
	public List<MessageObserver> getAllObservers(){
		return observers;
	}
	
	public void notifyMessageAddedObservers(Mensagem m){
		for(MessageObserver mObserver:observers){
			mObserver.notifyMessageAdded(m);
		}
	}
	
	public void notifyMessageRemovedObservers(Mensagem m){
		for(MessageObserver mObserver:observers){
			mObserver.notifyMessageRemoved(m);
		}
	}
	
	
	
}
