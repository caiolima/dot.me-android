package com.twittemarkup.assynctask;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

public class AssyncTaskManager {

	private static AssyncTaskManager singleton;
	private List<AsyncTask<?,?,?>> processList=new ArrayList<AsyncTask<?,?,?>>();
	
	public static AssyncTaskManager getInstance(){
		
		if(singleton==null)
			singleton=new AssyncTaskManager();
		
		return singleton;
	}
	
	private AssyncTaskManager(){}
	
	public void addProccess(AsyncTask<?, ?, ?> task){
		processList.add(task);
	}
	
	public void removeProcess(AsyncTask<?, ?, ?> task){
		processList.remove(task);
	}
	
	public void notifyToCancel(){
		for(AsyncTask<? , ? , ?> task:processList){
			task.cancel(true);
		}
	}
	
}
