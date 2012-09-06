package com.dot.me.subjects;

import java.util.Vector;

import com.dot.me.interfaces.IOnLabelRefreshListener;

public class LabelRefresListenerList {

	private Vector<IOnLabelRefreshListener> listeners=new Vector<IOnLabelRefreshListener>();
	
	//Singleton do LabelSubject. Ele ter‡ o registro de todos os LabelListeners
	private static LabelRefresListenerList singleton;
	
	public static LabelRefresListenerList getInstance(){
		if(singleton==null){
			singleton=new LabelRefresListenerList();
		}
		return singleton;
	}
	
	private LabelRefresListenerList(){}
	
	public void registerListerner(IOnLabelRefreshListener list){
		listeners.add(list);
	}
	
	public void unregisterListener(IOnLabelRefreshListener list){
		listeners.remove(list);
	}
	
	public void clearAll(){
		listeners.clear();
	}
	
	public void notifyAllToRefresh(){
		for(IOnLabelRefreshListener listener:listeners){
			listener.forceRefresh();
		}
	}
	
	public void notifyAllToGetNextPage(){
		for(IOnLabelRefreshListener listener:listeners){
			listener.forceRefresh();
		}
	}
	
	public boolean loadHasFinished(){
		
		for(IOnLabelRefreshListener listener:listeners){
			if(!listener.isFinished())
				return false;
		}
		
		return true;
	}
	
	
}
