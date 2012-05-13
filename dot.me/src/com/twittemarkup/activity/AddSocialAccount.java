package com.twittemarkup.activity;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import twitter4j.auth.AccessToken;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.twittemarkup.activity.LoginActivity.ConnectTask;
import com.twittemarkup.activity.LoginActivity.GetBasicInformationsTask;
import com.twittemarkup.app.R;
import com.twittemarkup.assynctask.TwitterImageDownloadTask;
import com.twittemarkup.exceptions.LostUserAccessException;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.CollumnConfig;
import com.twittemarkup.model.FacebookAccount;
import com.twittemarkup.model.TwitterAccount;
import com.twittemarkup.model.bd.DataBase;
import com.twittemarkup.model.bd.Facade;
import com.twittemarkup.utils.BaseRequestListener;
import com.twittemarkup.utils.Constants;
import com.twittemarkup.utils.FacebookUtils;
import com.twittemarkup.utils.ImageUtils;
import com.twittemarkup.utils.TwitterUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddSocialAccount extends Activity{

	public static boolean running=false;
	private Toast toast;
	private static CommonsHttpOAuthConsumer commonHttpOAuthConsumer;
	private Facebook facebook=new Facebook(Constants.FACEBOOK_APP_ID);
	private static OAuthProvider authProvider;
	private static boolean flagOauthTwitter,flagFacebook;
	private ProgressDialog loadingDialog;
	private String[] permissions={"manage_notifications",
			"user_groups",
			"user_status",
			"offline_access",
			"read_stream",
			"photo_upload",
			"video_upload",
			"share_item",
			"status_update",
			"read_friendlists"};
	
	private Button bt_logoutTwitter,bt_logoutFacebook, bt_twitter,bt_facebook,bt_ok;
	private LinearLayout facebookPane,twitterPane;
	private ImageView twitterImage,facebookImage;
	private TextView twitterName,facebookName;
	private View.OnClickListener twitterLoggoutClick=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Facade.destroy();
			DataBase.start(AddSocialAccount.this);
			
			Facade facade=Facade.getInstance(AddSocialAccount.this);
			facade.logoutTwitter();
			bt_twitter.setVisibility(View.VISIBLE);
			twitterPane.setVisibility(View.GONE);
			if(Account.getFacebookAccount(AddSocialAccount.this)==null&&
					Account.getTwitterAccount(AddSocialAccount.this)==null)
				bt_ok.setVisibility(View.GONE);
			
		}
	},facebookLogoutClick=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			try {
				Facade.destroy();
				DataBase.start(AddSocialAccount.this);
				
				facebook=FacebookUtils.getFacebook(AddSocialAccount.this, Account.getFacebookAccount(AddSocialAccount.this));
				new LogingOutFacebook(AddSocialAccount.this,facebook).execute();
			} catch (LostUserAccessException e) {
				
			}
			
		}
	};
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.account_manager);
		flagOauthTwitter=false;
		flagFacebook=false;
		running=true;
		DataBase.start(this);
		
		facebookPane=(LinearLayout) findViewById(R.id.facebook_pane);
		twitterPane=(LinearLayout) findViewById(R.id.twitter_pane);
		twitterImage=(ImageView) findViewById(R.id.twitter_img);
		facebookImage=(ImageView) findViewById(R.id.facebook_img);
		
		facebookName=(TextView) findViewById(R.id.facebook_name);
		twitterName=(TextView) findViewById(R.id.twitter_name);
		bt_logoutTwitter=(Button) findViewById(R.id.twitter_logout);
		bt_logoutFacebook=(Button) findViewById(R.id.facebook_logout);
		
		bt_logoutTwitter.setOnClickListener(twitterLoggoutClick);
		bt_logoutFacebook.setOnClickListener(facebookLogoutClick);
		
		bt_twitter=(Button)findViewById(R.id.singin_bt_twitter);
		bt_twitter.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo nInfo = cm.getActiveNetworkInfo();
				if (nInfo != null)
					if (nInfo.isConnected()) {
						flagOauthTwitter = true;
						new ConnectTask().execute(AddSocialAccount.this);
					} else {
						showMessage(AddSocialAccount.this
								.getString(R.string.erro_connect),
								Toast.LENGTH_SHORT);

					}
				else
					showMessage(
							AddSocialAccount.this.getString(R.string.erro_connect),
							Toast.LENGTH_SHORT);
			}
		});
		
		bt_ok=(Button) findViewById(R.id.bt_ok);
		bt_ok.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent=new Intent(AddSocialAccount.this,DashboardActivity.class);
				startActivity(intent);
				finish();
			}
			
		});
		
		 bt_facebook=(Button) findViewById(R.id.singin_bt_facebook);
		bt_facebook.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				facebook.authorize(AddSocialAccount.this,permissions, new DialogListener() {
		            @Override
		            public void onComplete(Bundle values) {
		            	facebook.setAccessToken(values.getString("access_token"));
		            	
		            	AsyncFacebookRunner runner=new AsyncFacebookRunner(facebook);
		    			Bundle params=new Bundle();
		    			params.putString("fields", "name,picture,id");
		    			runner.request("me", params,new GetFacebookInfoTask());
		    			//runner.request("me", new GetFacebookInfoTask());
		    			
		    			loadingDialog=new ProgressDialog(AddSocialAccount.this);
		    			loadingDialog.setMessage(getString(R.string.get_basic_information));
		    			loadingDialog.show();
		            }

		            @Override
		            public void onFacebookError(FacebookError error) {}

		            @Override
		            public void onError(DialogError e) {}

		            @Override
		            public void onCancel() {}
		           
		        });
				
			}
		});
		
		TwitterAccount tAcc=Account.getTwitterAccount(this);
		if(tAcc!=null){
			twitterPane.setVisibility(View.VISIBLE);
			bt_twitter.setVisibility(View.GONE);
			
			twitterName.setText(tAcc.getName());
			
			new TwitterImageDownloadTask(this,twitterImage,tAcc.getProfileImage()).execute();
			
		}
		
		FacebookAccount fAcc=Account.getFacebookAccount(this);
		if(fAcc!=null){
			facebookPane.setVisibility(View.VISIBLE);
			bt_facebook.setVisibility(View.GONE);
			
			facebookName.setText(fAcc.getName());
			
			new TwitterImageDownloadTask(this,facebookImage,fAcc.getProfileImage()).execute();
		}
		
		if(fAcc==null&&tAcc==null)
			bt_ok.setVisibility(View.GONE);
		
	}
	
	private static int countNewIntent=0;
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		countNewIntent++;
		Uri uri = intent.getData();

		if (uri != null && flagOauthTwitter && countNewIntent<2) {

			new GetBasicInformationsTask().execute(uri);

		}
		
	}

	


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
				
		if(flagFacebook){
			facebook.authorizeCallback(requestCode, resultCode, data);
			
			
			return;
		}
	}




	@Override
	protected void onResume() {
		super.onResume();

		/*Uri uri = getIntent().getData();

		if (uri != null && flagOauth) {

			new GetBasicInformationsTask().execute(uri);

		} else if (uri == null && flagOauth) {
			showAlert(getString(R.string.twitter_connect_fail),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							new ConnectTask().execute(AddSocialAccount.this);
						}
					});
		}*/
		

		/*
		 * String oauth_verifier = "liIw9HfHYzxIuObLfkYnnIVbFRUYAt3aqtGurJLsI";
		 * 
		 * try {
		 * 
		 * AccessToken aToken = new AccessToken(
		 * "248821347-enNMzcsFBostUuKFDZbw99Vmcjja3gUw7cjW9Vpx",
		 * "Fks3xALjFknvEWU9DrsCc7nZOtjAEgIeStE47A9g");
		 * TwitterUtils.twitter.setOAuthAccessToken(aToken);
		 * 
		 * Long id = TwitterUtils.twitter.getId(); Intent intent = new
		 * Intent(LoginActivity.this, RegisterActivity.class);
		 * 
		 * intent.putExtra("oauth_verifier", oauth_verifier);
		 * intent.putExtra("twitterId", id); startActivity(intent); } catch
		 * (Exception e) { showMessage(getString(R.string.twitter_connect_fail),
		 * Toast.LENGTH_SHORT); }
		 */

	}
	
	private void showAlert(final String message,
			final DialogInterface.OnClickListener listaner) {
		Dialog d = new AlertDialog.Builder(AddSocialAccount.this)
				.setTitle("Message").setMessage(message)
				.setPositiveButton("Ok", listaner).create();
		d.show();

	}
	
	class ConnectTask extends AsyncTask<Activity, Void, Void> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(AddSocialAccount.this);

			progressDialog.setMessage(getString(R.string.twitter_connecting));

			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Activity... v) {

			commonHttpOAuthConsumer = new CommonsHttpOAuthConsumer(
					Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
			authProvider = new DefaultOAuthProvider(
					"http://twitter.com/oauth/request_token",
					"http://twitter.com/oauth/access_token",
					"http://twitter.com/oauth/authorize");
			try {
				String oAuthURL = authProvider.retrieveRequestToken(
						commonHttpOAuthConsumer, "twitter-client://back");
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(oAuthURL));
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {

				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
			}

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {

			progressDialog.dismiss();
			//finish();
		}

	}
	
	private static boolean isAssyncRunning=false;
	
	class GetBasicInformationsTask extends AsyncTask<Uri, Void, Void> {

		private ProgressDialog progressDialog;
		private TwitterAccount t;
		

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(AddSocialAccount.this);

			progressDialog
					.setMessage(getString(R.string.get_basic_information));

			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Uri... v) {

			isAssyncRunning=true;
			Uri uri = v[0];
			String oauth_verifier = uri.getQueryParameter("oauth_verifier");

			try {

				authProvider.retrieveAccessToken(commonHttpOAuthConsumer,
						oauth_verifier);

				String token = commonHttpOAuthConsumer.getToken();
				String tokenSecret = commonHttpOAuthConsumer.getTokenSecret();

				AccessToken accessToken = new AccessToken(
						commonHttpOAuthConsumer.getToken(),
						commonHttpOAuthConsumer.getTokenSecret());

				TwitterUtils.getTwitter().setOAuthAccessToken(accessToken);
				long id = TwitterUtils.getTwitter().getId();

				t = new TwitterAccount();
				t.setId(id);
				t.setToken(token);
				t.setTokenSecret(tokenSecret);
				t.setProfileImage(t.processProfileImage());
				t.setName(TwitterUtils.getTwitter().getScreenName());
				//t.setNickname(TwitterUtils.getTwitter().verifyCredentials().getName());
				
				

			} catch (Exception e) {
				showAlert(getString(R.string.twitter_connect_fail),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								new ConnectTask().execute(AddSocialAccount.this);
							}
						});
			}
			flagOauthTwitter = false;

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			
			Facade.destroy();
			DataBase.start(AddSocialAccount.this);
			
			Facade.getInstance(AddSocialAccount.this).insert(t);
			
			CollumnConfig collumnConfig=new CollumnConfig();
			JSONObject prop=new JSONObject();
			try {
				prop.put("name", getString(R.string.main_column_name));
			} catch (JSONException e) {
				
			}
			collumnConfig.setProprietes(prop);
			collumnConfig.setType(CollumnConfig.TWITTER_COLLUMN);
			
			Facade.getInstance(AddSocialAccount.this).insert(collumnConfig);

			Intent intent = new Intent(AddSocialAccount.this,
					DashboardActivity.class);
			

			startActivity(intent);
			finish();
		}

	}
	
	
	private void showMessage(String message, int duration) {
		if (toast != null) {
			toast.cancel();

			toast.setDuration(duration);
			toast.setText(message);
			toast.show();
		} else {
			toast = Toast.makeText(this, message, duration);
			toast.show();
		}

	}
	
	private class LogingOutFacebook extends AsyncTask<Void, Void, Void>{

		private Facebook facebook;
		private Context ctx;
		private ProgressDialog progressDialog;
		private boolean logoutSuccessful=false;
		
		public LogingOutFacebook(Context ctx,Facebook facebook){
			this.facebook=facebook;
			this.ctx=ctx;
			
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if(logoutSuccessful){
				Facade.destroy();
				DataBase.start(AddSocialAccount.this);
				
				Facade facade=Facade.getInstance(AddSocialAccount.this);
				facade.logoutFacebook();
				bt_facebook.setVisibility(View.VISIBLE);
				facebookPane.setVisibility(View.GONE);
				if(Account.getFacebookAccount(AddSocialAccount.this)==null&&
						Account.getTwitterAccount(AddSocialAccount.this)==null)
					bt_ok.setVisibility(View.GONE);
			}
			progressDialog.dismiss();
		}



		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(AddSocialAccount.this);

			progressDialog
					.setMessage(getString(R.string.loggingout));

			progressDialog.show();
			
		}



		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				facebook.logout(ctx);
				logoutSuccessful=true;
			} catch (MalformedURLException e) {
			
			} catch (IOException e) {
				
			}
			
			return null;
		}
		
	}

	private class GetFacebookInfoTask extends BaseRequestListener{

		@Override
		public void onComplete(String response, Object state) {
			try {
				JSONObject object=new JSONObject(response);

				FacebookAccount acc=new FacebookAccount();
				acc.setId(object.getLong("id"));
				acc.setName(object.getString("name"));
				acc.setProfileImage(new URL(object.getString("picture")));
				acc.setToken(facebook.getAccessToken());
				acc.setExpires(facebook.getAccessExpires());
				
				Facade.destroy();
				DataBase.start(AddSocialAccount.this);
				
				Facade.getInstance(AddSocialAccount.this).insert(acc);
				
				CollumnConfig collumnConfig=new CollumnConfig();
				
				JSONObject prop=new JSONObject();
				prop.put("name", getString(R.string.news_fedd));
				collumnConfig.setProprietes(prop);
				collumnConfig.setType(CollumnConfig.FACEBOOK_COLLUMN);
				
				Facade.getInstance(AddSocialAccount.this).insert(collumnConfig);
				
				collumnConfig=new CollumnConfig();
				
				JSONObject prop2=new JSONObject();
				prop2.put("name", getString(R.string.notifications));
				collumnConfig.setProprietes(prop2);
				collumnConfig.setProprietes(prop2);
				collumnConfig.setType(CollumnConfig.NOTIFICATION_FACE);
				
				Facade.getInstance(AddSocialAccount.this).insert(collumnConfig);
				
				loadingDialog.dismiss();
				
				Intent intent=new Intent(AddSocialAccount.this,DashboardActivity.class);
				startActivity(intent);
				finish();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}

		
		
	}


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Facade.destroy();
		DataBase.start(AddSocialAccount.this);
		if(keyCode==KeyEvent.KEYCODE_BACK&&
		
				(Account.getFacebookAccount(this)!=null||Account.getTwitterAccount(this)!=null)){
			
			Intent intent=new Intent(this, DashboardActivity.class);
			startActivity(intent);
			finish();
			
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	
	
	
}
