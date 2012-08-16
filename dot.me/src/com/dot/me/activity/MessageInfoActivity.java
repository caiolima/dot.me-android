package com.dot.me.activity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.dot.me.app.R;
import com.dot.me.assynctask.RetweetTask;
import com.dot.me.assynctask.TwitterImageDownloadTask;
import com.dot.me.model.Account;
import com.dot.me.model.Mensagem;
import com.dot.me.model.User;
import com.dot.me.model.UsuarioTwitter;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.ImageUtils;
import com.dot.me.utils.TwitterUtils;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.ads.AdRequest.ErrorCode;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageInfoActivity extends TrackedActivity implements AdListener {

	private ImageView img_profile;
	private ImageButton bt_response, bt_retweet;
	private TextView txt_nick;
	private TextView txt_name;
	private TextView txt_content;
	private LinearLayout lt_top, lt_lst_conversation, lt_conversation;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.message_info);

		Facade facade = Facade.getInstance(this);
		String id_message = "";
		int message_type = -1;
		Intent intent = getIntent();
		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				id_message = b.getString("idMessage");
				message_type = b.getInt("type");
			}
		}
		final Mensagem m = facade.getOneMessage(id_message, message_type);

		img_profile = (ImageView) findViewById(R.id.message_img_profile);
		txt_content = (TextView) findViewById(R.id.message_lbl_text);
		txt_name = (TextView) findViewById(R.id.message_lbl_name);
		txt_nick = (TextView) findViewById(R.id.message_lbl_when);
		lt_top = (LinearLayout) findViewById(R.id.message_pane_top);
		bt_response = (ImageButton) findViewById(R.id.message_bt_response);
		bt_retweet = (ImageButton) findViewById(R.id.message_bt_retweet);
		lt_lst_conversation = (LinearLayout) findViewById(R.id.message_lst_conversation);
		lt_conversation = (LinearLayout) findViewById(R.id.message_lt_conversation);

		bt_retweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new RetweetTask(MessageInfoActivity.this, Long.parseLong(m
						.getIdMensagem())).execute();
			}
		});
		lt_top.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MessageInfoActivity.this,
						TwitterUserProfileActivity.class);
				Bundle b = new Bundle();
				b.putLong("idUser", m.getIdUser());
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		if (m != null) {
			txt_content.setText(TwitterUtils.createMessage(m.getHtmlText()));
			txt_content.setMovementMethod(LinkMovementMethod.getInstance());
			TwitterUtils.stripUnderlines(txt_content);
			txt_name.setText(m.getNome_usuario());
			txt_nick.setText(TwitterUtils.friendlyFormat(m.getData(),
					MessageInfoActivity.this));
			URL url = m.getImagePath();
			Bitmap b = ImageUtils.imageCache.get(url);
			if (b == null) {
				img_profile.setImageBitmap(null);
				TwitterImageDownloadTask
						.executeDownload(this, img_profile, url);
			} else {
				img_profile.setImageBitmap(b);
			}

			bt_response.setOnClickListener(new Button.OnClickListener() {

				@Override
				public void onClick(View v) {
					Context ctx = MessageInfoActivity.this;
					Intent intent = new Intent(ctx, SendTweetActivity.class);
					Bundle b = new Bundle();
					Bundle sendParams = new Bundle();

					sendParams.putLong("inReply",
							Long.parseLong(m.getIdMensagem()));

					b.putBundle("send_params", sendParams);
					User user = Facade.getInstance(ctx).getOneUser(
							m.getIdUser(), User.TWITTER);
					try {
						b.putString("pre_text", "@" + user.getNick());
					} catch (NullPointerException e) {
						b.putString("pre_text", txt_content.getText()
								.toString());
					}
					b.putString("type_message", "Twitter");
					b.putString("action", "SendTwitteAction");

					intent.putExtras(b);

					startActivity(intent);
				}

			});

			adView = (AdView) findViewById(R.id.adView);

			AdRequest adRequestBanner = new AdRequest();
			adRequestBanner.addTestDevice(AdRequest.TEST_EMULATOR);

			adView.setAdListener(this);

			adView.loadAd(adRequestBanner);

			new InReplyContent(Long.parseLong(id_message), message_type)
					.execute();
		}

	}

	private class InReplyContent extends AsyncTask<Void, Void, Void> {

		private long id;
		private int type;

		private List<Mensagem> conversation = new ArrayList<Mensagem>();

		public InReplyContent(long id, int type) {
			this.id = id;
			this.type = type;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			long nextStatus = id;
			Twitter t = TwitterUtils.getTwitter();
			try {
				nextStatus = Facade.getInstance(MessageInfoActivity.this)
						.getOneMessage(Long.toString(id), type).getInReplyId();
				while (nextStatus > 0) {

					Mensagem m = Facade.getInstance(MessageInfoActivity.this)
							.getOneMessage(Long.toString(nextStatus),
									Mensagem.TIPO_STATUS);
					if (m == null) {
						twitter4j.Status s = t.showStatus(nextStatus);
						m = Mensagem.creteFromTwitterStatus(s);
					}
					conversation.add(m);

					nextStatus = m.getInReplyId();

				}
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			for (Mensagem m : conversation) {
				LayoutInflater inflater = (LayoutInflater) MessageInfoActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View row = inflater.inflate(R.layout.twitte_row, null);
				ImageView img = (ImageView) row.findViewById(R.id.profile_img);
				TextView screenName = (TextView) row
						.findViewById(R.id.screen_name);
				TextView time = (TextView) row.findViewById(R.id.time);
				TextView tweetText = (TextView) row.findViewById(R.id.twitte);

				screenName.setText(m.getNome_usuario());
				time.setText(TwitterUtils.friendlyFormat(m.getData(),
						MessageInfoActivity.this));
				tweetText.setText(TwitterUtils.createMessage(m.getMensagem()));
				tweetText.setMovementMethod(LinkMovementMethod.getInstance());
				TwitterUtils.stripUnderlines(tweetText);

				TwitterImageDownloadTask.executeDownload(
						MessageInfoActivity.this, img, m.getImagePath());

				lt_lst_conversation.addView(row);

			}

			if (conversation.size() > 0)
				lt_conversation.setVisibility(View.VISIBLE);
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

	/** Overwrite the onDestroy() method to dispose of banners first. */
	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}
}
