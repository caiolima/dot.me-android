package com.dot.me.assynctask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.dot.me.activity.TrendsLocationsActivity;
import com.dot.me.model.Account;
import com.dot.me.model.TrendLocation;
import com.dot.me.model.TwitterAccount;
import com.dot.me.utils.TwitterUtils;

import android.os.AsyncTask;
import android.view.View;

public class GetTrendsLocationsTask extends AsyncTask<Void, Void, Void>{

	private TrendsLocationsActivity activity;
	private List<TrendLocation> response=new ArrayList<TrendLocation>();
	
	public GetTrendsLocationsTask(TrendsLocationsActivity a){
		this.activity=a;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		TwitterAccount tAccount=Account.getTwitterAccount(activity);
		if(tAccount!=null){
			
			Twitter twitter=TwitterUtils.getTwitter(new AccessToken(tAccount.getToken(), tAccount.getTokenSecret()));
			try {
				ResponseList<Location> mResponse=twitter.getAvailableTrends();
				for(Location l:mResponse){
					TrendLocation t=TrendLocation.createFromLocation(l);
					response.add(t);
				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		
		if(response!=null){
			TrendLocation t=new TrendLocation();
			t.setName("Wolrd");
			t.setWoeid(1);
			activity.getAdapter().add(t);
			Collections.sort(response);
			for(TrendLocation tL:response){
				activity.getAdapter().add(tL);
			}
			activity.getLt_loading().setVisibility(View.GONE);
		}
		
	}
	
	

}
