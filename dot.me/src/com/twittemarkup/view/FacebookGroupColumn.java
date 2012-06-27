package com.twittemarkup.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.Facebook;
import com.markupartist.android.widget.PullToRefreshListView;
import com.twittemarkup.activity.TimelineActivity;
import com.twittemarkup.assynctask.AssyncTaskManager;
import com.twittemarkup.command.OpenFacebookWriter;
import com.twittemarkup.exceptions.LostUserAccessException;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.CollumnConfig;
import com.twittemarkup.model.FacebookAccount;
import com.twittemarkup.model.FacebookGroup;
import com.twittemarkup.model.Mensagem;
import com.twittemarkup.model.bd.Facade;
import com.twittemarkup.utils.Constants;
import com.twittemarkup.utils.FacebookUtils;
import com.twittemarkup.utils.WebService;
import com.twittemarkup.view.FacebookFeedsColumn.FacebookNewsFeedGetterTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class FacebookGroupColumn extends AbstractColumn {

	private FacebookGroup fGroup;
	private String nextPage;
	private Facebook facebook;

	public FacebookGroupColumn(Context ctx, FacebookGroup fg) {
		super(ctx, fg.getName(),true);

		this.fGroup = fg;
		command=new OpenFacebookWriter(fg.getId(), "Facebook Group: "+fg.getName());
	}

	private FacebookGroupColumn(Context ctx, String title) {
		super(ctx, title,true);

	}

	@Override
	public void deleteColumn() {
		facade.deleteFacebookGroup(fGroup.getId());
		for (Mensagem m : facade.getMensagemOf(Mensagem.TIPO_FACEBOOK_GROUP)) {
			if (m.getIdMensagem().startsWith(fGroup.getId() + "_"))
				facade.deleteMensagem(m.getIdMensagem(), m.getTipo());
		}
	}

	@Override
	protected void updateList() {

		FacebookAccount acc = Account.getFacebookAccount(ctx);
		if (acc != null) {
			try {
				facebook=FacebookUtils.getFacebook(ctx, acc);
				new FacebookGroupFeedsGetterTask(fGroup).execute();
			} catch (LostUserAccessException e) {

			}
		}

	}

	private class FacebookGroupFeedsGetterTask extends
			AsyncTask<Void, Void, Void> {

		private FacebookGroup fGroup;
		private List<Mensagem> createdMessages = new ArrayList<Mensagem>();
		private String nextPageRequest;
		private boolean flagNextPage=false;
		private List<Mensagem> lastMessages=new ArrayList<Mensagem>();

		public FacebookGroupFeedsGetterTask(FacebookGroup fGroud) {
			this.fGroup = fGroud;
		}

		public FacebookGroupFeedsGetterTask(FacebookGroup fGroud, String nextPageRequest) {
			this(fGroud);
			this.nextPageRequest = nextPageRequest;
			this.flagNextPage=true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			AssyncTaskManager.getInstance().addProccess(this);
			try {
				
				for (Mensagem m : facade.getMensagemOf(Mensagem.TIPO_FACEBOOK_GROUP)) {
					if (m.getIdMensagem().startsWith(fGroup.getId() + "_"))
						lastMessages.add(m);
				}
				
				String response="";
				if (nextPageRequest == null) {
					response = facebook.request(
							fGroup.getId() + "/feed",
							FacebookUtils.getStandartFeedsBundle());
				} else {
					WebService wservice = new WebService(nextPageRequest);
					response = wservice.webGet("", null);
				}
				
				JSONObject json = new JSONObject(response);
				createdMessages.addAll(FacebookUtils.createListOfFeeds(facade,
						facebook, json, Mensagem.TIPO_FACEBOOK_GROUP));

				try {
					JSONObject pagingObject = json.getJSONObject("paging");
					nextPage = pagingObject.getString("next");
				} catch (JSONException e) {
					nextPage = "none";
				}

			} catch (MalformedURLException e) {

			} catch (IOException e) {

			} catch (JSONException e) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if(!createdMessages.isEmpty()&&!flagNextPage){
				adapter.clear();
				for (Mensagem m : lastMessages) {
					if(!createdMessages.contains(m))	
						facade.deleteMensagem(m.getIdMensagem(), m.getTipo());
				}
			}
			
			for (Mensagem m : createdMessages) {
				adapter.addItem(m);
			}
			
			adapter.sort();
			
			if(!flagNextPage){
				((PullToRefreshListView)listView).onRefreshComplete();
			}else
				notifyNextPageFinish();
			

			JSONObject prop=config.getProprietes();
			if(prop!=null){
				prop.remove("nextPage");
				try {
					prop.put("nextPage", nextPage);
				} catch (JSONException e) {
					
				}
			}
			AssyncTaskManager.getInstance().removeProcess(this);
		}
		

	}

	@Override
	protected void onGetNextPage() {
		if(nextPage==null||isLoaddingNextPage)
			return;
		super.onGetNextPage();

		if (nextPage != null) {
			if (!nextPage.equals("none")) {
				Log.w("facebook-group-collumn", "next_started");
				isLoaddingNextPage = true;
				new FacebookGroupFeedsGetterTask(fGroup, nextPage).execute();
			} else {
				notifyNextPageFinish();

			}
		}

	}

	@Override
	public void init() {
		Vector<Mensagem> mensagens = facade
				.getMensagemOf(Mensagem.TIPO_FACEBOOK_GROUP);
		final Vector<Mensagem> toAdd=new Vector<Mensagem>();
		for (final Mensagem m : mensagens) {
			if (m.getNome_usuario().contains(fGroup.getName())) {
				toAdd.add(m);
			}
		}
		TimelineActivity.h.post(new Runnable() {
			
			@Override
			public void run() {
				for(Mensagem m:toAdd)
					adapter.addItem(m);
				
				
			}
		});
		firstPut = false;
		
		Mensagem m=toAdd.firstElement();
		if(m!=null){
			if(System.currentTimeMillis()-m.getData().getTime()>Constants.QTD_MINUTES){
				nextPage=null;
				JSONObject prop=config.getProprietes();
				if (prop!=null) {
					prop.remove("nextPage");
				}
			}
		}

	}
	
	@Override
	public void setConfig(CollumnConfig config) {
		super.setConfig(config);
		try {
			nextPage=config.getProprietes().getString("nextPage");
		} catch (JSONException e) {
			
		}
	}

}
