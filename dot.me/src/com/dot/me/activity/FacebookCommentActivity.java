package com.dot.me.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.app.R;
import com.dot.me.exceptions.LostUserAccessException;
import com.dot.me.model.Account;
import com.dot.me.model.Draft;
import com.dot.me.model.FacebookAccount;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.FacebookUtils;
import com.facebook.android.Facebook;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.ads.AdRequest.ErrorCode;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FacebookCommentActivity extends TrackedActivity implements
		AdListener {

	private Button bt_send;
	private EditText txt_comment;
	private String id;
	private boolean isToSaveDraft = true;
	private Facade facade;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.facebook_comment);

		bt_send = (Button) findViewById(R.id.bt_facebook_comment_send);
		txt_comment = (EditText) findViewById(R.id.txt_facebook_comment);

		facade = Facade.getInstance(this);

		Intent intent = getIntent();
		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				id = b.getString("id_comment");
			}
		}

		bt_send.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				String comment = txt_comment.getText().toString();
				if (!comment.equals("")) {

					FacebookAccount acc = Account
							.getFacebookAccount(FacebookCommentActivity.this);
					if (acc != null) {
						try {
							Facebook facebook = FacebookUtils.getFacebook(
									FacebookCommentActivity.this, acc);
							new SendCommentTask(id, comment, facebook)
									.execute();
						} catch (LostUserAccessException e) {

						}

					}

				} else {
					Toast.makeText(FacebookCommentActivity.this,
							getString(R.string.need_write_something),
							Toast.LENGTH_SHORT).show();
				}
			}

		});

		Draft d = facade.getOneDraft(id);
		if (d != null)
			txt_comment.setText(d.getText());

//		adView = (AdView) findViewById(R.id.adView);
//
//		AdRequest adRequestBanner = new AdRequest();
//
//		adView.setAdListener(this);
//
//		adView.loadAd(adRequestBanner);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isToSaveDraft) {
			String text = txt_comment.getText().toString();
			if (!text.equals("")) {
				Draft d = new Draft();
				d.setId(id);
				d.setText(text);
				if (facade.existsDraft(id))
					facade.deleteDraft(id);

				facade.insert(d);
				Toast.makeText(this, getString(R.string.draft_saved),
						Toast.LENGTH_SHORT).show();
			}
		} else if (facade.existsDraft(id))
			facade.deleteDraft(id);

	}

	private class SendCommentTask extends AsyncTask<Void, Void, Void> {

		private String comment, response, id;
		private ProgressDialog pDialog;
		private Facebook facebook;

		public SendCommentTask(String id, String comment, Facebook facebook) {
			this.id = id;
			this.facebook = facebook;
			this.comment = comment;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(FacebookCommentActivity.this);
			pDialog.setMessage(getString(R.string.posting_comment));
			pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {

			Bundle b = new Bundle();
			b.putString("message", comment);
			try {
				response = facebook.request(id + "/comments", b, "POST");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			pDialog.cancel();
			if (response == null) {
				Toast.makeText(FacebookCommentActivity.this,
						getString(R.string.erro_posting_comment),
						Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				JSONObject responseJSON = new JSONObject(response);
				String idComment = responseJSON.getString("id");

				Intent intent = new Intent();
				Bundle b = new Bundle();
				b.putString("id", idComment);
				b.putString("comment", comment);
				intent.putExtras(b);

				isToSaveDraft = false;
				setResult(RESULT_OK, intent);
				finish();

			} catch (JSONException e) {
				Toast.makeText(FacebookCommentActivity.this,
						getString(R.string.erro_posting_comment),
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	@Override
	public void onDismissScreen(Ad ad) {
		Log.v("teste ad", "onDismissScreen");
	}

	@Override
	public void onLeaveApplication(Ad ad) {
		Log.v("teste ad", "onLeaveApplication");
	}

	@Override
	public void onPresentScreen(Ad ad) {
		Log.v("teste ad", "onPresentScreen");
	}

	@Override
	public void onReceiveAd(Ad ad) {
		Log.v("teste ad", "Did Receive Ad");
		adView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onFailedToReceiveAd(Ad ad, ErrorCode errorCode) {
		Log.v("teste ad", "Failed to receive ad (" + errorCode + ")");
	}

}
