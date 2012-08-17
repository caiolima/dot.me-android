package com.dot.me.activity;

import java.net.MalformedURLException;
import java.util.Vector;

import org.json.JSONException;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.dot.me.adapter.MessageAdapter;
import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.model.Mensagem;
import com.dot.me.model.TwitterAccount;
import com.dot.me.model.User;
import com.dot.me.model.UsuarioTwitter;
import com.dot.me.model.bd.DataBase;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;
import com.dot.me.utils.TwitterUtils;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResultActivity extends TrackedActivity {

	private String search_content;
	private ListView lst_search;
	private TextView txt_tittle;
	private Toast toast;
	private Button bt_ok;
	private MessageAdapter adapter;
	private boolean flagToDelete = true, resultActivity = true;
	private Vector<Mensagem> sResult = new Vector<Mensagem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_result);

		txt_tittle = (TextView) findViewById(R.id.search_result_tittle);
		bt_ok = (Button) findViewById(R.id.bt_search_result_ok);
		lst_search = (ListView) findViewById(R.id.tweet_search_list);

		verifyIntent(getIntent());

		txt_tittle.setText(search_content);
		adapter = new MessageAdapter(this);
		lst_search.setAdapter(adapter);

		bt_ok.setEnabled(false);
		bt_ok.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				flagToDelete = false;
				Facade facade = Facade.getInstance(SearchResultActivity.this);

				if (facade.wasSearchAdded(search_content)) {
					showMessage(getString(R.string.add_search_exists),
							Toast.LENGTH_LONG);
					return;
				}
				facade.insert(search_content);
				for (Mensagem m : sResult) {

					if (!facade.exsistsStatus(m.getIdMensagem(), m.getTipo())) {

						facade.insert(m);

					}
				}
//				TimelineActivity timeline = TimelineActivity.getCurrent();
//				if (timeline != null) {
//					timeline.finish();
//				}
				if (resultActivity) {
					Intent intent = new Intent();
					Bundle b = new Bundle();
					b.putBoolean("collumnAdded", true);
					b.putString("search_name", search_content);
					intent.putExtras(b);
					setResult(RESULT_OK, intent);
				} else {
					Intent intent = new Intent(SearchResultActivity.this,
							TimelineActivity.class);
					Bundle b = new Bundle();
					b.putBoolean("collumnAdded", true);
					b.putString("search_name", search_content);
					intent.putExtras(b);

					startActivity(intent);
				}

				finish();
			}

		});

		new SearchTask().execute(this);
	}

	private void verifyIntent(Intent intent) {
		if (intent != null) {

			Uri uri = intent.getData();
			if (uri != null) {
				search_content = "#" + uri.getQueryParameter("search");
				resultActivity = false;
				return;
			}

			Bundle b = intent.getExtras();
			if (b != null) {
				search_content = b.getString("search_value");
			}
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

	private class SearchTask extends AsyncTask<Activity, Void, Void> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(SearchResultActivity.this);
			
			progressDialog.setCancelable(false);

			progressDialog.setMessage(getString(R.string.searching));

			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Activity... params) {
			DataBase.getInstance(SearchResultActivity.this).setExecuting(true);
			TwitterAccount user = (TwitterAccount) Account
					.getTwitterAccount(params[0]); // verificar
			// posteriormente
			Twitter twitter = TwitterUtils.getTwitter(new AccessToken(user
					.getToken(), user.getTokenSecret()));
			Query q = new Query(search_content);
			try {
				QueryResult result = twitter.search(q);

				if (result == null)
					return null;
				for (Tweet tweet : result.getTweets()) {
					try {
						Mensagem m = Mensagem.createFromTweet(tweet);
						Facade.getInstance(SearchResultActivity.this).insert(m);
						sResult.add(m);

						
					} catch (Exception e) {
						
					}

				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			for (Mensagem m : sResult)
				adapter.addItem(m);
			bt_ok.setEnabled(true);

			progressDialog.dismiss();
			DataBase.getInstance(SearchResultActivity.this).setExecuting(false);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (flagToDelete) {
			for (Mensagem m : sResult)
				Facade.getInstance(this).deleteMensagem(m.getIdMensagem(),
						m.getTipo());
		}
	}

}
