package com.dot.me.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.app.R;
import com.dot.me.assynctask.TwitterImageDownloadTask;
import com.dot.me.exceptions.LostUserAccessException;
import com.dot.me.model.Account;
import com.dot.me.model.FacebookAccount;
import com.dot.me.model.Mensagem;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.BaseRequestListener;
import com.dot.me.utils.FacebookUtils;
import com.dot.me.utils.PictureInfo;
import com.dot.me.utils.TwitterUtils;
import com.dot.me.utils.WebService;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug.FlagToString;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookMessageActivity extends Activity {

	private Button bt_comment, bt_like, bt_refresh;
	private TextView txt_qtd_likes, txt_username, txt_message;
	private ImageView img_load, img_picture, img_avatar;
	private LinearLayout lt_load, list_comments, lt_more_comments,
			lt_facebook_profile;
	private Mensagem current_message;
	private Facade facade;
	private Vector<Mensagem> commentsAdded = new Vector<Mensagem>();
	private final int COMMENT_ACTIVITY = 1;
	private FacebookAccount acc;
	private boolean flagLike = true;
	private Facebook facebook;
	private int tipo;
	private String nextPage;
	private boolean commentsLoaded = false;

	private OnClickListener refreshClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			bt_refresh.setEnabled(false);
			bt_refresh.setText(R.string.refreshing);

			new LoadCommentsTask(facebook, null, true).execute();
		}
	};

	private OnClickListener openProfile = new OnClickListener() {

		@Override
		public void onClick(View v) {

			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.facebook.com/"
							+ current_message.getIdUser()));
			startActivity(intent);

		}
	};

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (!commentsLoaded)
			return null;

		CommentsCache c = new CommentsCache();
		c.messages = commentsAdded.toArray();
		c.nextPage = nextPage;
		return c;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		facade = Facade.getInstance(this);

		setContentView(R.layout.facebook_message);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		list_comments = (LinearLayout) findViewById(R.id.facebook_list_comments);
		lt_load = (LinearLayout) findViewById(R.id.lt_loading);
		lt_more_comments = (LinearLayout) findViewById(R.id.lt_facebook_more_comments);
		bt_comment = (Button) findViewById(R.id.bt_comment);
		bt_like = (Button) findViewById(R.id.bt_like);
		txt_qtd_likes = (TextView) findViewById(R.id.facebook_lbl_qtd_likes);
		// txt_qtd_comments = (TextView)
		// findViewById(R.id.facebook_lbl_qtd_comments);
		txt_message = (TextView) findViewById(R.id.facebook_txt_message);
		txt_username = (TextView) findViewById(R.id.facebook_txt_username);
		img_load = (ImageView) findViewById(R.id.facebook_img_loading);
		img_avatar = (ImageView) findViewById(R.id.facebook_img_user_avatar);
		img_picture = (ImageView) findViewById(R.id.img_image_source);
		bt_refresh = (Button) findViewById(R.id.bt_refresh);
		lt_facebook_profile = (LinearLayout) findViewById(R.id.facebook_profile_lt);

		bt_refresh.setOnClickListener(refreshClick);
		lt_facebook_profile.setOnClickListener(openProfile);

		Intent intent = getIntent();
		String id = null;
		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				id = b.getString("idMessage");
				tipo = b.getInt("type");
				current_message = facade.getOneMessage(id, tipo);

			}
		}

		try {
			acc = Account.getFacebookAccount(this);
			facebook = FacebookUtils.getFacebook(this, acc);

		} catch (LostUserAccessException e) {

		}

		if (current_message == null) {
			new LoadMessageTask(id).execute();
		} else {
			fillValues(current_message);
		}

	}

	private void fillValues(final Mensagem current_message) {
		this.current_message = current_message;
		if (current_message.isLiked()) {
			bt_like.setText(R.string.unlike);
			flagLike = false;
		}
		bt_like.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View view) {
				String method = "";

				if (flagLike) {
					method = "POST";
					bt_like.setText(R.string.unlike);
				} else {
					method = "DELETE";
					bt_like.setText(R.string.like);

				}
				flagLike = !flagLike;
				AsyncFacebookRunner runner = new AsyncFacebookRunner(facebook);
				runner.request(current_message.getIdMensagem() + "/likes",
						new Bundle(), method, new LikeListener(), null);

			}

		});
		bt_comment.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FacebookMessageActivity.this,
						FacebookCommentActivity.class);
				Bundle b = new Bundle();
				b.putString("id_comment", current_message.getIdMensagem());
				intent.putExtras(b);
				startActivityForResult(intent, COMMENT_ACTIVITY);
			}
		});
		bt_comment.setVisibility(View.GONE);

		txt_username.setText(current_message.getNome_usuario());

		txt_message.setText(TwitterUtils.createMessage(current_message
				.getMensagem()));
		txt_message.setMovementMethod(LinkMovementMethod.getInstance());
		TwitterUtils.stripUnderlines(txt_message);

		/*
		 * txt_qtd_comments.setText(Integer.toString(current_message
		 * .getCommentsCount()));
		 */
		txt_qtd_likes
				.setText(Integer.toString(current_message.getLikesCount()));
		TwitterImageDownloadTask.executeDownload(this, img_avatar,
				current_message.getImagePath());
		PictureInfo info = current_message.getPictureUrl();
		if (info != null) {
			img_picture.setVisibility(View.VISIBLE);
			/*
			 * int width=(int)(info.getWidth()*0.4); int height=(int)
			 * (info.getHeight()*0.4); img_picture.setLayoutParams(new
			 * LayoutParams(width,height));
			 */
			TwitterImageDownloadTask.executeDownload(this, img_picture,
					info.getSURL());
		}

		img_load.setBackgroundResource(R.drawable.load_icon);
		img_load.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable frameAnimation = (AnimationDrawable) img_load
						.getBackground();
				frameAnimation.start();
			}
		});
		final Object data = getLastNonConfigurationInstance();

		new LoadCommentsTask(facebook, (CommentsCache) data, false).execute();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == COMMENT_ACTIVITY) {
			if (resultCode == RESULT_OK) {
				LayoutInflater inflater = (LayoutInflater) FacebookMessageActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				Mensagem m = new Mensagem();
				Bundle b = data.getExtras();
				m.setIdMensagem(b.getString("id"));
				m.setMensagem(b.getString("comment"));
				m.setNome_usuario(acc.getName());
				m.setImagePath(acc.getProfileImage());
				m.setData(new Date());
				m.setTipo(Mensagem.TIPO_FACE_COMENTARIO);

				View row = inflater.inflate(R.layout.twitte_row, null);
				ImageView img = (ImageView) row.findViewById(R.id.profile_img);
				TextView screenName = (TextView) row
						.findViewById(R.id.screen_name);
				TextView time = (TextView) row.findViewById(R.id.time);
				TextView tweetText = (TextView) row.findViewById(R.id.twitte);

				int qtd_likes = m.getLikesCount();
				if (qtd_likes > 0) {
					LinearLayout lt_likes = (LinearLayout) findViewById(R.id.lt_likes);
					lt_likes.setVisibility(View.VISIBLE);

					TextView txt_qtd_likesTextView = (TextView) findViewById(R.id.lbl_qtd_likes);
					txt_qtd_likesTextView.setText(Integer.toString(qtd_likes));
				}
				screenName.setText(m.getNome_usuario());
				time.setText(TwitterUtils.friendlyFormat(m.getData()));
				tweetText.setText(TwitterUtils.createMessage(m.getMensagem()));
				tweetText.setMovementMethod(LinkMovementMethod.getInstance());
				TwitterUtils.stripUnderlines(tweetText);

				TwitterImageDownloadTask.executeDownload(
						FacebookMessageActivity.this, img, m.getImagePath());

				list_comments.addView(row);

				commentsAdded.add(m);
			}
		}

	}

	private class LoadCommentsTask extends AsyncTask<Void, Void, Void> {

		private Facebook facebook;
		private Vector<Mensagem> comments = new Vector<Mensagem>();
		private final static String QTD_COMMENTS = "15";
		private boolean nextPageFlag = false, isRefreshig = false;
		private CommentsCache data;

		public LoadCommentsTask(Facebook facebook, CommentsCache data,
				boolean isRefreshing) {
			this.facebook = facebook;
			this.data = data;
			this.isRefreshig = isRefreshing;
			nextPage = null;
		}

		public LoadCommentsTask(Facebook facebook, String nextPageToLoad) {
			this.facebook = facebook;
			nextPage = nextPageToLoad;
			this.nextPageFlag = true;
		}

		@Override
		protected Void doInBackground(Void... param) {

			if (data == null) {
				Vector<Mensagem> cachedMenssage = facade.getMensagemOfLikeId(
						Mensagem.TIPO_FACE_COMENTARIO,
						current_message.getIdMensagem());
				if (!cachedMenssage.isEmpty() && !(isRefreshig || nextPageFlag)) {
					String aToken = Account.getFacebookAccount(
							FacebookMessageActivity.this).getToken();
					/*
					 * String afterId = cachedMenssage.lastElement()
					 * .getIdMensagem(); int
					 * offset=cachedMenssage.size()-(cachedMenssage
					 * .size()%Integer.parseInt(QTD_COMMENTS));
					 */
					comments.addAll(cachedMenssage);
					/*
					 * nextPage="https://graph.facebook.com/"+
					 * current_message.getIdMensagem
					 * ()+"/comments?limit="+QTD_COMMENTS
					 * +"&access_token="+aToken+
					 * "&format=json&fields=message,likes,from.picture,from.name,from.id&"
					 * +"&offset="+offset+"&__after_id="+afterId;
					 */
					return null;
				}

				Bundle params = new Bundle();
				params.putString("fields", "likes");

				try {
					String response = null;
					response = facebook.request(
							current_message.getIdMensagem(), params);

					if (response != null) {
						JSONObject responseJSON = new JSONObject(response);

						JSONObject likesJSON = null;
						try {
							likesJSON = responseJSON.getJSONObject("likes");
						} catch (JSONException e) {
							// TODO: handle exception
						}

						if (likesJSON != null) {
							current_message.getAddtions().remove("likes");
							current_message.getAddtions().put("likes",
									likesJSON);
						}

						String responseComments = null;

						String query = "select id, time from comment where post_id=\""
								+ current_message.getIdMensagem() + "\"";

						Bundle params2 = new Bundle();
						params2.putString("method", "fql.query");
						params2.putString("query", query);

						responseComments = facebook.request(null, params2);

						if (responseComments != null) {
							JSONArray array = new JSONArray(responseComments);
							List<Mensagem> list = new Vector<Mensagem>();

							try {

								int lenght = array.length();
								int ini = 0;
								if (nextPage != null)
									ini = Integer.parseInt(nextPage);

								int toDownload = ini
										+ Integer.parseInt(QTD_COMMENTS);
								if (toDownload > lenght)
									toDownload = lenght;

								for (int i = ini; i < toDownload; i++) {
									JSONObject comment = array.getJSONObject(i);
									String commentId = comment.getString("id");

									if (facade.exsistsStatus(commentId,
											Mensagem.TIPO_FACE_COMENTARIO))
										continue;

									Bundle b = new Bundle();
									b.putString("fields",
											"message,likes,from.picture,from.name,from.id");
									String response2 = facebook.request(
											commentId, b);
									JSONObject commentMessage = new JSONObject(
											response2);

									Mensagem m = Mensagem
											.createFromFacebookFeed(commentMessage);
									if (m == null)
										continue;

									m.setTipo(Mensagem.TIPO_FACE_COMENTARIO);

									if (wasAddedOn(m, list))
										break;

									facade.insert(m);
									list.add(m);

								}
							} catch (JSONException e) {
							}

							for (Mensagem m : list) {
								if (wasAddedOn(m, comments))
									break;
								facade.insert(m);
							}

							comments.addAll(list);

						}
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				for (Object m : data.messages)
					comments.add((Mensagem) m);
				nextPage = data.nextPage;

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			lt_load.setVisibility(View.GONE);

			if (isRefreshig) {
				// list_comments.removeAllViews();
				// commentsAdded.clear();

				bt_refresh.setEnabled(true);
				bt_refresh.setText(R.string.refresh);
			}

			if (comments.size() == 0) {
				Toast.makeText(
						FacebookMessageActivity.this,
						FacebookMessageActivity.this
								.getString(R.string.no_new_comments),
						Toast.LENGTH_SHORT).show();
			}

			Collections.sort(comments);
			LayoutInflater inflater = (LayoutInflater) FacebookMessageActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			int cont = 0;
			// if (nextPageFlag) {
			int count = comments.size();
			for (int i = count - 1; i > -1; i--) {
				// for (int i = 0; i < comments.size(); i++) {
				Mensagem m = comments.get(i);

				if (wasAddedOn(m, commentsAdded))
					continue;
				else
					commentsAdded.add(m);

				cont++;
				View row = inflater.inflate(R.layout.twitte_row, null);
				ImageView img = (ImageView) row.findViewById(R.id.profile_img);
				TextView screenName = (TextView) row
						.findViewById(R.id.screen_name);
				TextView time = (TextView) row.findViewById(R.id.time);
				TextView tweetText = (TextView) row.findViewById(R.id.twitte);

				int qtd_likes = m.getLikesCount();
				if (qtd_likes > 0) {
					LinearLayout lt_likes = (LinearLayout) findViewById(R.id.lt_likes);
					lt_likes.setVisibility(View.VISIBLE);

					TextView txt_qtd_likesTextView = (TextView) findViewById(R.id.lbl_qtd_likes);
					txt_qtd_likesTextView.setText(Integer.toString(qtd_likes));
				}
				screenName.setText(m.getNome_usuario());
				time.setText(TwitterUtils.friendlyFormat(m.getData()));
				tweetText.setText(TwitterUtils.createMessage(m.getMensagem()));
				tweetText.setMovementMethod(LinkMovementMethod.getInstance());
				TwitterUtils.stripUnderlines(tweetText);

				TwitterImageDownloadTask.executeDownload(
						FacebookMessageActivity.this, img, m.getImagePath());

				list_comments.addView(row);
			}

			lt_more_comments.setVisibility(View.VISIBLE);
			lt_more_comments.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					lt_load.setVisibility(View.VISIBLE);
					lt_more_comments.setVisibility(View.GONE);

					new LoadCommentsTask(facebook, nextPage).execute();
				}
			});
			// }

			txt_qtd_likes.setText(Integer.toString(current_message
					.getLikesCount()));

			if (!bt_comment.isShown())
				bt_comment.setVisibility(View.VISIBLE);

			commentsLoaded = true;

			int qtd_messages = commentsAdded.size();
			/*
			 * int limit = qtd_messages - (qtd_messages %
			 * Integer.parseInt(QTD_COMMENTS)) + Integer.parseInt(QTD_COMMENTS);
			 */

			nextPage = "" + qtd_messages;

			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

		}

	}

	private boolean wasAddedOn(Mensagem m, List<Mensagem> list) {
		for (Mensagem message : list) {
			if (message.getMensagem().equals(m.getMensagem())
					&& message.getNome_usuario().equals(m.getNome_usuario())) {
				return true;
			}
		}

		return false;
	}

	private class LikeListener extends BaseRequestListener {

		@Override
		public void onComplete(String response, Object state) {
			if (!response.equals("true")) {
				flagLike = !flagLike;

				Toast.makeText(FacebookMessageActivity.this,
						getString(R.string.erro_liking), Toast.LENGTH_SHORT)
						.show();

				if (flagLike)
					bt_like.setText(R.string.like);
				else
					bt_like.setText(R.string.unlike);
			} else {

				try {
					current_message.getAddtions().remove("liked");
					current_message.getAddtions().put("liked", !flagLike);
					facade.update(current_message);

					final int currentLikes = Integer.parseInt(txt_qtd_likes
							.getText().toString());
					txt_qtd_likes.post(new Runnable() {

						@Override
						public void run() {
							if (current_message.isLiked())
								txt_qtd_likes.setText(Integer
										.toString(currentLikes + 1));
							else
								txt_qtd_likes.setText(Integer
										.toString(currentLikes - 1));
						}
					});

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	private class LoadMessageTask extends AsyncTask<Void, Void, Void> {

		private String id;
		private ProgressDialog progressDialog;
		private Mensagem m;

		public LoadMessageTask(String id) {
			this.id = id;

		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(FacebookMessageActivity.this);

			progressDialog.setMessage(getString(R.string.getting_information));

			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... v) {

			try {
				m = facade.getOneMessage(id, tipo);
				if (m == null) {
					String response = facebook.request(id,
							FacebookUtils.getStandartFeedsBundle());
					JSONObject object = new JSONObject(response);
					m = Mensagem.createFromFacebookFeed(object);
					m.setTipo(tipo);

					String obString = null;
					try {
						if (object.getString("type").equals("photo")) {

							obString = object.getString("object_id");

							Bundle params = new Bundle();
							params.putString("fields",
									"picture,source,id,width,height,from.name,name");
							String response2 = facebook.request(obString,
									params);
							try {
								JSONObject pic_info = new JSONObject(response2);
								m.getAddtions().put("pic_info", pic_info);
							} catch (JSONException e) {
								// TODO: handle exception
							}
						}
						
					} catch (JSONException e) {
						// TODO: handle exception
					}
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			progressDialog.dismiss();
			if (m != null) {
				facade.insert(m);
				fillValues(m);

			} else{
				Toast.makeText(FacebookMessageActivity.this, FacebookMessageActivity.this.getString(R.string.erro_connect), Toast.LENGTH_SHORT).show();
				finish();
			}
		}

	}

	private class CommentsCache {

		Object[] messages;
		String nextPage;

	}
}
