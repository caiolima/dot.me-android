package com.dot.me.assynctask;

import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.dot.me.activity.SearchActivity;
import com.dot.me.model.Account;
import com.dot.me.model.TwitterAccount;
import com.dot.me.utils.TwitterUtils;

import android.os.AsyncTask;
import android.view.View;

public class TrendTopicsGetterTask extends AsyncTask<Void,Void,Void>{

	private SearchActivity activity;
	private int woid;
	private Trends trends;
	
	public TrendTopicsGetterTask(SearchActivity s,int woid) {
		this.activity=s;
		this.woid=woid;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		TwitterAccount tAccount=Account.getTwitterAccount(activity);
		if(tAccount!=null){
			
			Twitter twitter=TwitterUtils.getTwitter(new AccessToken(tAccount.getToken(), tAccount.getTokenSecret()));
			try {
				trends=twitter.getLocationTrends(woid);
				
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if(trends!=null){
			
			for(Trend t:trends.getTrends()){
				activity.getAdapter().add(t.getName());
			}
			activity.getLt_imgLoading().setVisibility(View.GONE);
			
		}
		
	}
	
	

	
	
}
