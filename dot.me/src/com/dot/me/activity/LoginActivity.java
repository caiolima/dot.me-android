package com.dot.me.activity;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import oauth.signpost.OAuthProvider;

import com.dot.me.app.R;
import com.dot.me.model.Label;
import com.dot.me.model.UsuarioTwitter;
import com.dot.me.model.bd.DataBase;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;
import com.dot.me.utils.CriptoUtils;
import com.dot.me.utils.Menssage;
import com.dot.me.utils.TwitterUtils;
import com.dot.me.utils.WebService;

import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;


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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	public static boolean running=false;
	private Toast toast;
	private static CommonsHttpOAuthConsumer commonHttpOAuthConsumer;
	private static OAuthProvider authProvider;
	private static boolean flagOauth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		running=true;
		DataBase.start(this);
		
		this.setContentView(R.layout.login);

		

		Button bt_register = (Button) findViewById(R.id.bt_register);
		bt_register.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo nInfo = cm.getActiveNetworkInfo();
				if (nInfo != null)
					if (nInfo.isConnected()) {
						flagOauth = true;
						new ConnectTask().execute(LoginActivity.this);
					} else {
						showMessage(LoginActivity.this
								.getString(R.string.erro_connect),
								Toast.LENGTH_SHORT);

					}
				else
					showMessage(
							LoginActivity.this.getString(R.string.erro_connect),
							Toast.LENGTH_SHORT);
			}
		});
		
		final EditText txt_user = (EditText) findViewById(R.id.txt_user_login);
		final EditText txt_password = (EditText) findViewById(R.id.txt_password_login);

		Button bt_login = (Button) findViewById(R.id.bt_login);
		bt_login.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo nInfo = cm.getActiveNetworkInfo();
				if (nInfo != null){
					if (!nInfo.isConnected()){
						showMessage(LoginActivity.this
							.getString(R.string.erro_connect),
							Toast.LENGTH_SHORT);
						return;
					}
				}else{
					showMessage(
						LoginActivity.this.getString(R.string.erro_connect),
						Toast.LENGTH_SHORT);
					return;
				}
				
				if (txt_user.getText().length() <= 4) {
					showMessage(getString(R.string.user_required),
							Toast.LENGTH_SHORT);
					return;
				}
				if (txt_password.getText().length() < 6) {
					showMessage(getString(R.string.password_short),
							Toast.LENGTH_SHORT);
					return;
				}

				/*new LoginTask().execute(txt_user.getText().toString(),
						txt_password.getText().toString());*/

			}

		});
	}

	private void showMessage(String message, int duration) {
		if (toast != null) {
			toast.cancel();

			toast.setDuration(duration);
			toast.setText(message);
			toast.show();
		} else {
			toast = Toast.makeText(LoginActivity.this, message, duration);
			toast.show();
		}

	}

	private void showAlert(final String message,
			final DialogInterface.OnClickListener listaner) {
		Dialog d = new AlertDialog.Builder(LoginActivity.this)
				.setTitle("Message").setMessage(message)
				.setPositiveButton("Ok", listaner).create();
		d.show();

	}

	@Override
	protected void onResume() {
		super.onResume();

		Uri uri = getIntent().getData();

		if (uri != null && flagOauth) {

			new GetBasicInformationsTask().execute(uri);

		} else if (uri == null && flagOauth) {
			showAlert(getString(R.string.twitter_connect_fail),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							new ConnectTask().execute(LoginActivity.this);
						}
					});
		}
		

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

	@Override
	protected void onDestroy() {
		running=false;
		if(!DashboardActivity.running)
			DataBase.getInstance(this).close();
		super.onDestroy();
	}
	
	class ConnectTask extends AsyncTask<Activity, Void, Void> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(LoginActivity.this);

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

		}

	}

	class GetBasicInformationsTask extends AsyncTask<Uri, Void, Void> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(LoginActivity.this);

			progressDialog
					.setMessage(getString(R.string.get_basic_information));

			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Uri... v) {

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

				Intent intent = new Intent(LoginActivity.this,
						RegisterActivity.class);
				Bundle bundle = new Bundle();
				bundle.putLong("twitterId", id);
				bundle.putString("token", token);
				bundle.putString("tokenSecret", tokenSecret);

				intent.putExtras(bundle);

				startActivity(intent);

			} catch (Exception e) {
				showAlert(getString(R.string.twitter_connect_fail),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								new ConnectTask().execute(LoginActivity.this);
							}
						});
			}
			flagOauth = false;

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {

			progressDialog.dismiss();

		}

	}

	/*class LoginTask extends AsyncTask<String, Void, Void> {

		private ProgressDialog progressDialog;
		private String response;
		private String out="";
		
		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(LoginActivity.this);

			progressDialog
					.setMessage(getString(R.string.login_load));

			progressDialog.show();

		}

		@Override
		protected Void doInBackground(String... strings) {
			try {
				String user = strings[0];
				String password;

				password = CriptoUtils.encode(strings[1]);

				WebService web = new WebService(Constants.SERVER_NAME
						+ "?action=LoginCommand");
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user", user));
				params.add(new BasicNameValuePair("password", password));
				
				response = web.doPost("", params);
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			progressDialog.dismiss();
			try {
				JSONObject json = new JSONObject(response);
				int action_status = json.getInt("action_status");
				if (Menssage.SUCCESS == action_status) {
					UsuarioTwitter.fromJSON(json.getJSONObject("user"));
					Facade facade=Facade.getInstance(LoginActivity.this);
					UsuarioTwitter usuario=UsuarioTwitter.getCurrent(LoginActivity.this);
					facade.insert(usuario);
					for(Marcador marcador:usuario.getMarcadores()){
						facade.insert(marcador);
					}
					Intent intent=new Intent(LoginActivity.this,DashboardActivity.class);
					startActivity(intent);
					finish();
				} else {
					JSONArray erros = json.getJSONArray("erros");
					for (int i = 0; i < erros.length(); i++) {
						if (erros.getInt(i) == Menssage.LOGIN_ERROR)
							out += "\n" + getString(R.string.login_error);
							
					}
					Dialog d = new AlertDialog.Builder(LoginActivity.this)
					.setMessage(out)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									
								}
							}).create();
					d.show();
				}
			} catch (JSONException e) {

			}

		}

	}*/

}
