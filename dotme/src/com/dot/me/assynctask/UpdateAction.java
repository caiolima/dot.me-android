package com.dot.me.assynctask;

import java.util.ArrayList;
import java.util.List;

import twitter4j.auth.AccessToken;

import android.content.Context;
import android.os.AsyncTask;

import com.dot.me.interfaces.IGetUpdateAction;
import com.dot.me.model.Mensagem;
import com.dot.me.model.bd.Facade;
import com.dot.me.view.AbstractColumn;
import com.dot.me.view.SearchColumn;
import com.markupartist.android.widget.PullToRefreshListView;

public class UpdateAction extends AsyncTask<IGetUpdateAction, Void, Void>{

	public static List<IGetUpdateAction> updateRequest=new ArrayList<IGetUpdateAction>();
	private Context ctx;
	private AccessToken token;
	private PullToRefreshListView listView;
	private AbstractColumn collumn;
	
	public UpdateAction(Context ctx,AccessToken token,PullToRefreshListView view,AbstractColumn collumn){
		this.ctx=ctx;
		this.token=token;
		this.listView=view;
		this.collumn=collumn;
	}
	
	public UpdateAction(Context ctx,AccessToken token){
		this.ctx=ctx;
		this.token=token;
		
	}
	
	@Override
	protected Void doInBackground(IGetUpdateAction... params) {
		Facade facade=Facade.getInstance(ctx);
		int num_status=facade.getCountMensagem(Mensagem.TIPO_TWEET_SEARCH);
		if(num_status>40){
			facade.deleteAllSearch();
			SearchColumn.cleanAll();
			//adapter.removeAll();
		}
		for(IGetUpdateAction updateAction:params){
			updateAction.onGetUpdate(token);
		}
		
		return null;
	}
	
	

	public static void registerUpdateRequest(IGetUpdateAction request){
		updateRequest.add(request);
	}
	
	public static void unRegisterUpdateRequest(IGetUpdateAction request){
		updateRequest.remove(request);
	}
	
}
