package com.dot.me.assynctask;

import java.util.Vector;

import com.dot.me.activity.LoginActivity;
import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.model.TwitterAccount;
import com.dot.me.model.User;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.TwitterUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class RetweetTask extends AsyncTask<Void, Void, Void>{

	private long idmessage;
	private AccessToken token;
	private ProgressDialog progressDialog;
	private Context ctx;
	
	public RetweetTask(Context ctx,long id){
		idmessage=id;
		this.ctx=ctx;
		
	}
	
	@Override
	protected void onPreExecute() {

		progressDialog = new ProgressDialog(ctx);

		progressDialog.setMessage(ctx.getString(R.string.retweeting));

		progressDialog.show();

	}
	
	@Override
	protected Void doInBackground(Void... params) {
		Vector<Account> users=Facade.getInstance(ctx).lastSavedSession();
		TwitterAccount twitterAcc=(TwitterAccount) users.get(0);
		Twitter twitter=TwitterUtils.getTwitter(new AccessToken(twitterAcc.getToken(), twitterAcc.getTokenSecret()));
		
		try {
			twitter.retweetStatus(idmessage);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		progressDialog.dismiss();

	}
	
	
	
	
}
