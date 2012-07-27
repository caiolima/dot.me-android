package com.dot.me.activity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Vector;

import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.dot.me.adapter.MessageAdapter;
import com.dot.me.app.R;
import com.dot.me.assynctask.TwitterImageDownloadTask;
import com.dot.me.model.Account;
import com.dot.me.model.Mensagem;
import com.dot.me.model.TwitterAccount;
import com.dot.me.model.User;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.TwitterUtils;

import twitter4j.Status;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TwitterUserProfileActivity extends Activity {

	private ImageView img_profile, img_loading;
	private TextView txt_user_profile, txt_about, txt_num_followers,
			txt_num_following;
	private LinearLayout layout, layout_loading;
	private Button bt_follow;
	private long idUser;
	private boolean isFollowin;
	private Vector<Account> users = new Vector<Account>();
	private Vector<Mensagem> currentMensagems = new Vector<Mensagem>();
	private AnimationDrawable rocketAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.user_info_twitter);

		users = Facade.getInstance(this).lastSavedSession();

		img_profile = (ImageView) findViewById(R.id.user_info_img_profile);
		txt_about = (TextView) findViewById(R.id.user_info_lbl_about);
		txt_user_profile = (TextView) findViewById(R.id.user_info_lbl_user_name);
		txt_num_followers = (TextView) findViewById(R.id.user_info_lbl_num_followes);
		txt_num_following = (TextView) findViewById(R.id.user_info_lbl_num_following);
		bt_follow = (Button) findViewById(R.id.user_info_bt_follow);
		layout = (LinearLayout) findViewById(R.id.user_info_list_recet);
		img_loading = (ImageView) findViewById(R.id.user_info_loading_list);
		layout_loading = (LinearLayout) findViewById(R.id.loading_list);

		img_loading.setBackgroundResource(R.drawable.load_icon);
		img_loading.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable frameAnimation = (AnimationDrawable) img_loading
						.getBackground();
				frameAnimation.start();
			}
		});

		bt_follow.setEnabled(false);
		// rocketAnimation = (AnimationDrawable) img_loading.getBackground();

		populateViews();

	}

	private void populateViews() {
		Intent intent = getIntent();
		Bundle b = intent.getExtras();

		Uri uri = intent.getData();
		if (uri != null) {
			String name = uri.getQueryParameter("username");
			new GetUserTask(name).execute();
			return;
		}

		if (b != null) {
			idUser = b.getLong("idUser");
			User u = Facade.getInstance(this).getOneUser(idUser, User.TWITTER);
			if (u != null) {
				fillValues(u);
			} else {
				new GetUserTask(idUser).execute();
			}

		}
	}

	private void fillValues(final User u) {
		txt_about.setText(TwitterUtils.createMessage(u.getAbout()));
		txt_about.setMovementMethod(LinkMovementMethod.getInstance());
		TwitterUtils.stripUnderlines(txt_about);

		txt_user_profile.setText(u.getName());
		txt_num_followers.setText(getString(R.string.followers) + ": "
				+ Integer.toString(u.getNum_followers()));
		txt_num_following.setText(getString(R.string.following) + ": "
				+ Integer.toString(u.getNum_following()));
		bt_follow.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Vector<Account> acc = Facade.getInstance(
						TwitterUserProfileActivity.this).lastSavedSession();
				TwitterAccount twitter_acc = (TwitterAccount) acc.get(0);
				Twitter twitter = TwitterUtils.getTwitter(new AccessToken(
						twitter_acc.getToken(), twitter_acc.getTokenSecret()));
				new RelationshipManager(twitter, u.getId(), isFollowin).execute();
			}
		});

		try {
			TwitterImageDownloadTask.executeDownload(this, img_profile, new URL(u.getUrl()));
		} catch (MalformedURLException e) {

		}

		new LoadRecentTweesTask(u.getId()).execute();
		new VerifyFriendship(u.getId()).execute();
	}

	private class LoadRecentTweesTask extends AsyncTask<Void, Void, Void> {

		private long id;
		private ResponseList<twitter4j.Status> list;

		public LoadRecentTweesTask(long idUser) {
			id = idUser;
		}

		@Override
		protected Void doInBackground(Void... params) {
			TwitterAccount acc = (TwitterAccount) users.get(0);
			try {
				Paging paging = new Paging(1, 15);
				list = TwitterUtils.getTwitter(
						new AccessToken(acc.getToken(), acc.getTokenSecret()))
						.getUserTimeline(id, paging);

			} catch (TwitterException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			layout_loading.setVisibility(View.GONE);

			if(list==null)
				return; 
			
			for (twitter4j.Status s : list) {
				final Mensagem m = Mensagem.creteFromTwitterStatus(s);
				if (Facade.getInstance(TwitterUserProfileActivity.this).insert(
						m))
					currentMensagems.add(m);

				LayoutInflater inflater = (LayoutInflater) TwitterUserProfileActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View row = inflater.inflate(R.layout.twitte_row, null);
				ImageView img = (ImageView) row.findViewById(R.id.profile_img);
				TextView screenName = (TextView) row
						.findViewById(R.id.screen_name);
				TextView time = (TextView) row.findViewById(R.id.time);
				TextView tweetText = (TextView) row.findViewById(R.id.twitte);

				screenName.setText(m.getNome_usuario());
				time.setText(TwitterUtils.friendlyFormat(m.getData(),TwitterUserProfileActivity.this));
				tweetText.setText(m.getMensagem());

				tweetText.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								TwitterUserProfileActivity.this,
								MessageInfoActivity.class);
						Bundle b = new Bundle();
						b.putString("idMessage", m.getIdMensagem());
						intent.putExtras(b);

						TwitterUserProfileActivity.this.startActivity(intent);

					}
				});

				TwitterImageDownloadTask.executeDownload(TwitterUserProfileActivity.this,
						img, m.getImagePath());

				layout.addView(row);

			}
		}

	}

	private class VerifyFriendship extends AsyncTask<Void, Void, Void> {

		private long id;

		public VerifyFriendship(long id) {
			this.id = id;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Vector<Account> acc = Facade.getInstance(
					TwitterUserProfileActivity.this).lastSavedSession();
			TwitterAccount twitter_acc = (TwitterAccount) acc.get(0);

			try {
				Relationship relationship = TwitterUtils.getTwitter(
						new AccessToken(twitter_acc.getToken(), twitter_acc
								.getTokenSecret())).showFriendship(
						twitter_acc.getId(), id);
				isFollowin = relationship.isSourceFollowingTarget();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (isFollowin) {
				bt_follow.setText(getString(R.string.unfollow));
				bt_follow.setBackgroundResource(R.drawable.my_button_pressed);
			} else {
				bt_follow.setText(getString(R.string.follow));
				bt_follow.setBackgroundResource(R.drawable.my_button);
			}
			bt_follow.setEnabled(true);

		}

	}

	private class GetUserTask extends AsyncTask<Void, Void, Void> {

		private long id;
		private String name = null;
		private twitter4j.User user;
		private ProgressDialog progressDialog;

		public GetUserTask(long id) {
			this.id = id;
		}

		public GetUserTask(String name) {
			this.name = name;
		}

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(TwitterUserProfileActivity.this);

			progressDialog.setCancelable(false);
			progressDialog
					.setMessage(getString(R.string.downloading_user_info));

			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (name == null) {
					user = TwitterUtils.getTwitter().showUser(id);
				} else {
					user = TwitterUtils.getTwitter().showUser(name);
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

			if (user != null) {
				User u = User.createFromTwitterUser(user);
				fillValues(u);
			}

			progressDialog.dismiss();

		}

	}

	private class RelationshipManager extends AsyncTask<Void, Void, Void> {

		private Twitter twitter;
		private long id;
		private boolean isFollowing;
		private ProgressDialog progressDialog;

		public RelationshipManager(Twitter t, long id, boolean isFollowing) {
			this.id = id;
			this.twitter = t;
			this.isFollowing = isFollowing;
		}
		
		

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(TwitterUserProfileActivity.this);

			progressDialog
					.setMessage(getString(R.string.changing));

			progressDialog.show();
		}



		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (isFollowing) {
					twitter.destroyFriendship(id);
				} else {
					twitter.createFriendship(id);
				}
			} catch (TwitterException e) {
				// TODO: handle exception
			}

			return null;
		}



		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			
			isFollowin=!isFollowin;
			if (isFollowin) {
				bt_follow.setText(getString(R.string.unfollow));
				bt_follow.setBackgroundResource(R.drawable.my_button_pressed);
			} else {
				bt_follow.setText(getString(R.string.follow));
				bt_follow.setBackgroundResource(R.drawable.my_button);
			}
			
			
		}
		
		

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		for (Mensagem m : currentMensagems) {
			Facade.getInstance(this).deleteMensagem(m.getIdMensagem(),
					m.getTipo());
		}
	}

}
