package com.dot.me.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.DirectMessage;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.UserMentionEntity;

import com.dot.me.command.IMessageAction;
import com.dot.me.command.OpenFacebookGroupNotificationAction;
import com.dot.me.command.OpenFacebookStatusAction;
import com.dot.me.command.OpenFacebookTaggedAction;
import com.dot.me.command.OpenFriendPostAction;
import com.dot.me.command.OpenLinkActivity;
import com.dot.me.command.OpenTwitterStatusAction;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.FacebookUtils;
import com.dot.me.utils.PictureInfo;

public class Mensagem implements Comparable<Mensagem> {

	private String idMensagem;
	private String mensagem;
	private String nome_usuario;
	private Date data;
	private URL imagePath;
	private int tipo;
	private long idUser;
	private JSONObject addtions;
	private IMessageAction action;

	public static final int TIPO_STATUS = 0, TIPO_TWEET_SEARCH = 1,
			TIPO_NEWS_FEEDS = 2, TIPO_FACE_COMENTARIO = 3,
			TIPO_FACEBOOK_GROUP = 4, TIPO_FACEBOOK_NOTIFICATION = 5;

	public String getIdMensagem() {
		return idMensagem;
	}

	public void setIdMensagem(String idMensagem) {
		this.idMensagem = idMensagem;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getNome_usuario() {
		return nome_usuario;
	}

	public void setNome_usuario(String nome_usuario) {
		this.nome_usuario = nome_usuario;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public URL getImagePath() {
		return imagePath;
	}

	public void setImagePath(URL imagePath) {
		this.imagePath = imagePath;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public static Mensagem createFromDirectMensagem(DirectMessage dm){
		try {
			Mensagem mensagem = new Mensagem();

			mensagem.setAction(OpenTwitterStatusAction.getInstance());
			
			mensagem.idMensagem = Long.toString(dm.getId());
			mensagem.nome_usuario = dm.getSender().getName();
			mensagem.mensagem = dm.getText();
			mensagem.imagePath = dm.getSender().getProfileImageURL();
			mensagem.data = dm.getCreatedAt();
			mensagem.idUser = dm.getSender().getId();
			mensagem.tipo = TIPO_STATUS;

			return mensagem;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Mensagem creteFromTwitterStatus(Status s) {
		try {
			Mensagem mensagem = new Mensagem();

			mensagem.setAction(OpenTwitterStatusAction.getInstance());
			mensagem.addtions = createAddtions(s);
			mensagem.idMensagem = Long.toString(s.getId());
			mensagem.nome_usuario = s.getUser().getName();
			mensagem.mensagem = s.getText();
			mensagem.imagePath = s.getUser().getProfileImageURL();
			mensagem.data = s.getCreatedAt();
			mensagem.idUser = s.getUser().getId();
			mensagem.tipo = TIPO_STATUS;

			return mensagem;
		} catch (JSONException e) {
			return null;
		}
	}

	private static JSONObject createAddtions(Status s) throws JSONException {
		JSONObject json = new JSONObject();

		Vector<String> metions = new Vector<String>();
		UserMentionEntity[] in_metions = s.getUserMentionEntities();
		if (in_metions != null) {
			for (UserMentionEntity metion : in_metions) {
				metions.add(metion.getName());
			}
		}

		Vector<String> image_files = new Vector<String>();
		MediaEntity[] in_medias = s.getMediaEntities();
		if (in_medias != null) {
			for (MediaEntity media : in_medias) {
				image_files.add(media.getMediaURL().toString());
			}
		}

		json.put("metions", metions);
		json.put("image_files", metions);

		json.put("inReplyId", s.getInReplyToStatusId());
		return json;

	}

	public static Mensagem createFromTweet(Tweet t)
			throws MalformedURLException, JSONException {
		Mensagem m = new Mensagem();

		JSONObject json = new JSONObject();
		json.put("inReplyId", -1);
		m.setAddtions(json);
		m.data = t.getCreatedAt();
		m.imagePath = new URL(t.getProfileImageUrl());
		m.idMensagem = Long.toString(t.getId());
		m.mensagem = t.getText();
		m.nome_usuario = t.getFromUser();
		m.idUser = t.getFromUserId();
		m.tipo = TIPO_TWEET_SEARCH;

		m.setAction(OpenTwitterStatusAction.getInstance());

		return m;

	}

	public static Mensagem createFromFacebookNotification(JSONObject object) {
		Mensagem m = new Mensagem();

		JSONObject addtions = new JSONObject();
		try {
			// JSONObject application=object.getJSONObject("application");
			String type = object.getString("link");
			if (type.startsWith("http://www.facebook.com/groups/")) {
				m.setAction(OpenFacebookGroupNotificationAction.getInstance());
			} else if (type
					.startsWith("http://www.facebook.com/permalink.php?story_fbid=")) {
				m.setAction(OpenFacebookTaggedAction.getInstance());
			} else if (type.startsWith("http://www.facebook.com/")
					&& type.contains("/posts/")) {
				m.setAction(OpenFriendPostAction.getInstance());
			} else if (type
					.startsWith("http://www.facebook.com/photo.php?fbid=")) {
				m.setAction(OpenLinkActivity.getInstance());
			} else {
				return null;
			}

			String mensagem = object.getString("title");
			m.setMensagem(mensagem);

			m.setIdMensagem(object.getString("id"));
			JSONObject userObject = object.getJSONObject("from");
			String user = userObject.getString("name");
			m.setNome_usuario(user);
			m.setIdUser(userObject.getLong("id"));
			m.setImagePath(new URL(userObject.getString("picture")));
			m.setTipo(TIPO_FACEBOOK_NOTIFICATION);

			String date = object.getString("created_time");

			m.setData(FacebookUtils.getTime(date));

			try {
				addtions.put("updated_time", object.getString("updated_time"));
			} catch (JSONException e) {

			}
			try {
				addtions.put("link", object.getString("link"));
			} catch (JSONException e) {
				// TODO: handle exception
			}

			m.setAddtions(addtions);

			return m;
		} catch (JSONException e) {
			// TODO Auto-generated catch block

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block

		} catch (ParseException e) {
			// TODO Auto-generated catch block

		}

		return null;
	}

	public static Mensagem createFromFacebookFeed(JSONObject object) {
		Mensagem m = new Mensagem();

		m.setAction(OpenFacebookStatusAction.getInstance());
		String mensagem = "";
		try {
			mensagem = object.getString("message");
		} catch (JSONException e) {
			try {
				mensagem = object.getString("story");
			} catch (JSONException e1) {

			}
		}
		m.setMensagem(mensagem);

		try {
			m.setIdMensagem(object.getString("id"));
			JSONObject userObject = object.getJSONObject("from");
			String user = userObject.getString("name");
			try {

				JSONObject toData = object.getJSONObject("to");
				try {
					JSONObject toUser = toData.getJSONObject("data");
					user += " > " + toUser.getString("name");
				} catch (JSONException e) {
					try {
						JSONArray toArray = toData.getJSONArray("data");
						int arrayTam = toArray.length();

						for (int i = 0; i < arrayTam; i++) {
							if (i == 0)
								user += " > ";
							JSONObject arrayObject = toArray.getJSONObject(i);
							user += arrayObject.getString("name");
							if (i < arrayTam - 1)
								user += ", ";
						}
					} catch (JSONException e2) {
						// TODO: handle exception
					}
				}
				/*
				 * int arrayTam=toArray.length();
				 * 
				 * for(int i=0;i<arrayTam;i++){ if(i==0) user+=">"; JSONObject
				 * arrayObject=toArray.getJSONObject(i);
				 * user+=arrayObject.getString("name"); if(i<arrayTam-1)
				 * user+=","; }
				 */
			} catch (JSONException e) {
				// e.printStackTrace();
			}

			m.setNome_usuario(user);
			m.setIdUser(userObject.getLong("id"));
			m.setImagePath(new URL(userObject.getString("picture")));
			m.setTipo(TIPO_NEWS_FEEDS);
			String date = object.getString("created_time");

			m.setData(FacebookUtils.getTime(date));

			JSONObject addtions = new JSONObject();

			try {
				addtions.put("comments", object.getJSONObject("comments"));
			} catch (JSONException e) {
			}

			String picture = "";
			JSONObject likes = null;
			try {
				picture = object.getString("picture");
			} catch (JSONException e) {
				// TODO: handle exception
			}
			try {
				likes = object.getJSONObject("likes");
			} catch (JSONException e) {
				// TODO: handle exception
			}
			if (likes != null)
				addtions.put("likes", likes);
			if (picture != null)
				addtions.put("picture", picture);
			String type = "status";
			try {
				type = object.getString("type");
				if (type.equals("video") || type.equals("link")) {
					String message = m.getMensagem();

					String link = object.getString("link");
					message = message.replace(link, "");

					if (!message.equals(""))
						message += "\n\n";

					message += object.getString("name") + ":\n" + link;
					m.setMensagem(message);
				}

			} catch (JSONException e) {
			}
			addtions.put("type", type);
			try {
				addtions.put("updated_time", object.getString("updated_time"));
			} catch (JSONException e) {
				// TODO: handle exception
			}
			addtions.put("liked", false);
			m.setAddtions(addtions);

		} catch (JSONException e) {
			return null;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			return null;
		} catch (ParseException e) {
			return null;
		}

		return m;
	}

	@Override
	public int compareTo(Mensagem another) {
		return another.getUpdatedTime().compareTo(getUpdatedTime());
	}

	@Override
	public boolean equals(Object o) {
		try {
			Mensagem other = (Mensagem) o;
			return other.getIdMensagem().equals(idMensagem)&&tipo==other.tipo;
		} catch (ClassCastException e) {
			return false;
		}
	}

	public long getIdUser() {
		return idUser;
	}

	public void setIdUser(long idUser) {
		this.idUser = idUser;
	}

	public JSONObject getAddtions() {
		return addtions;
	}

	public void setAddtions(JSONObject addtions) {
		this.addtions = addtions;
	}

	public long getInReplyId() {
		try {
			long out = addtions.getLong("inReplyId");
			return out;
		} catch (JSONException e) {
			return -1;
		}

	}

	public int getLikesCount() {
		JSONObject additions = this.addtions;

		try {
			JSONObject likesJSON = addtions.getJSONObject("likes");
			return likesJSON.getInt("count");

		} catch (JSONException e) {
			return 0;
		} catch (NullPointerException e) {
			return 0;
		}

	}

	public String getTypeMessage() {
		try {
			return addtions.getString("type");
		} catch (JSONException e) {
			return null;
		}
	}

	public PictureInfo getPictureUrl() {
		String type = getTypeMessage();
		if (type != null) {
			if (type.equals("photo")) {
				String pictureAdress = null;
				try {
					pictureAdress = addtions.getString("picture");
					PictureInfo pInfo = null;
					if (pictureAdress.equals("")) {
						pInfo = new PictureInfo(
								addtions.getJSONObject("pic_info"));
					}

					if (pInfo == null) {
						pInfo = new PictureInfo();
						pInfo.setSURL(new URL(pictureAdress.replace("_s", "_a")));
						pInfo.setNormalURL(new URL(pictureAdress.replace("_s",
								"_n")));
					}
					return pInfo;
				} catch (JSONException e) {

				} catch (MalformedURLException e) {

				}
			}
		}
		return null;
	}

	public int getCommentsCount() {
		try {
			JSONObject commentsJSON = addtions.getJSONObject("comments");
			return commentsJSON.getInt("count");
		} catch (JSONException e) {
			return 0;
		} catch (NullPointerException e) {
			return 0;
		}

	}

	public String[] getAllComments() {
		try {
			JSONObject commentsJSON = addtions.getJSONObject("comments");
			JSONArray array = commentsJSON.getJSONArray("data");
			String[] comments = new String[array.length()];
			for (int i = 0; i < comments.length; i++) {
				comments[i] = array.getJSONObject(i).getString("id");
			}
			return comments;
		} catch (JSONException e) {
			return new String[0];
		}
	}

	public Date getUpdatedTime() {
		try {
			String data = addtions.getString("updated_time");
			return FacebookUtils.getTime(data);
		} catch (Exception e) {
			return data;
		}

	}

	public IMessageAction getAction() {
		return action;
	}

	public void setAction(IMessageAction action) {
		this.action = action;
	}

	public boolean isLiked() {
		try {
			return addtions.getBoolean("liked");
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isValidToFilter() {
		int tipo = getTipo();
		if (tipo == TIPO_FACEBOOK_GROUP || tipo == TIPO_NEWS_FEEDS
				|| tipo == TIPO_STATUS)
			return true;

		return false;
	}

}
