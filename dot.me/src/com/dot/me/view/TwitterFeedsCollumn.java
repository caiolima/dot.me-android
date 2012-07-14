package com.dot.me.view;

import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.auth.AccessToken;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.app.R;
import com.dot.me.assynctask.UpdateTimelineTask;
import com.dot.me.command.OpenTwitterWriter;
import com.dot.me.model.Account;
import com.dot.me.model.CollumnConfig;
import com.dot.me.model.Mensagem;
import com.dot.me.model.PalavraChave;
import com.dot.me.model.TwitterAccount;
import com.dot.me.utils.Constants;
import com.dot.me.utils.UpdateParams;
import com.markupartist.android.widget.PullToRefreshListView;

import android.content.Context;

public class TwitterFeedsCollumn extends AbstractColumn {

	private int currentPage;
	private boolean flagNextPage = false;

	public TwitterFeedsCollumn(Context ctx) {
		super(ctx, ctx.getString(R.string.main_column_name),true);
		command=new OpenTwitterWriter();
	}

	public TwitterFeedsCollumn(Context ctx, String title) {
		super(ctx, title,true);
		currentPage = 0;
		
	}

	@Override
	public void deleteColumn() {

	}

	@Override
	protected void updateList() {

		TwitterAccount user = (TwitterAccount) Account.getTwitterAccount(ctx);// verificar
																				// depois

		AccessToken token = new AccessToken(user.getToken(),
				user.getTokenSecret());

		if (flagNextPage)
			currentPage++;
		else
			currentPage = 1;
		UpdateParams params = new UpdateParams(currentPage,
				Constants.QTD_FEEDS, token);
		new UpdateTimelineTask(ctx, params, (PullToRefreshListView)listView, this).execute();
		
		JSONObject prop=config.getProprietes();
		if(prop!=null){
			prop.remove("nextPage");
			try {
				prop.put("nextPage", currentPage);
			} catch (JSONException e) {
				
			}
		}
		
	}

	@Override
	public void init() {
		final Vector<Mensagem> list = facade
				.getMensagemOf(Mensagem.TIPO_STATUS);
		TimelineActivity.h.post(new Runnable() {

			@Override
			public void run() {
				updateTwittes(list, true);
				
			}
		});
		
		if(list.isEmpty())
			return;
		Mensagem m=null;
		try{
		m=list.firstElement();
		}catch (Exception e) {
			// TODO: handle exception
		}
		if(m!=null){
			if(System.currentTimeMillis()-m.getData().getTime()>Constants.QTD_MINUTES){
				currentPage=0;
				JSONObject prop=config.getProprietes();
				if (prop!=null) {
					prop.remove("nextPage");
				}
			}
		}
	}
	
	

	@Override
	public boolean isDeletable() {
		return false;
	}

	@Override
	protected void onGetNextPage() {
		if (isLoaddingNextPage || currentPage == 0)
			return;

		super.onGetNextPage();

		flagNextPage = true;
		updateList();
		isLoaddingNextPage = true;
	}

	@Override
	public void updateTwittes(Vector<Mensagem> list, boolean top) {
		if (list == null)
			return;

		addMensagens(list, top);

	}

	@Override
	public void setConfig(CollumnConfig config) {
		super.setConfig(config);
		try {
			currentPage=config.getProprietes().getInt("nextPage");
		} catch (JSONException e) {
			currentPage=0;
		}
	}
	
	
}
