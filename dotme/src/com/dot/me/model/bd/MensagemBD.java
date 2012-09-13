package com.dot.me.model.bd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.command.OpenFacebookGroupNotificationAction;
import com.dot.me.command.OpenFacebookStatusAction;
import com.dot.me.command.OpenFacebookTaggedAction;
import com.dot.me.command.OpenFriendPostAction;
import com.dot.me.command.OpenLinkActivity;
import com.dot.me.command.OpenTwitterStatusAction;
import com.dot.me.model.Mensagem;
import com.dot.me.utils.Menssage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class MensagemBD extends Dao {

	protected MensagemBD(Context ctx) {
		super(ctx);
	}

	protected int insert(Mensagem m) {

		ContentValues values = new ContentValues();
		values.put(DataBase.MENSAGEM_ID, m.getIdMensagem());
		values.put(DataBase.MENSAGEM_TEXTO, m.getMensagem());
		values.put(DataBase.MENSAGEM_USER, m.getNome_usuario());
		values.put(DataBase.MENSAGEM_IMAGE_URL, m.getImagePath().toString());
		values.put(DataBase.MENSAGEM_TIPO, m.getTipo());
		values.put(DataBase.MENSAGEM_DATE, m.getData().getTime());
		values.put(DataBase.MENSAGEM_ID_USER, m.getIdUser());
		values.put(DataBase.MENSAGEM_ADD_CONTENT, m.getAddtions().toString());
		long r = -1;
		try {
			r = db.getDB().insert(DataBase.TB_MENSAGEM, null, values);
		} catch (Exception e) {

		}
		if (r == -1)
			return Menssage.ERRO;
		else
			return Menssage.SUCCESS;
	}

	protected void update(Mensagem m) {
		ContentValues values = new ContentValues();

		values.put(DataBase.MENSAGEM_TEXTO, m.getMensagem());
		values.put(DataBase.MENSAGEM_USER, m.getNome_usuario());
		values.put(DataBase.MENSAGEM_IMAGE_URL, m.getImagePath().toString());
		values.put(DataBase.MENSAGEM_DATE, m.getData().getTime());
		values.put(DataBase.MENSAGEM_ID_USER, m.getIdUser());
		values.put(DataBase.MENSAGEM_ADD_CONTENT, m.getAddtions().toString());

		db.getDB()
				.update(DataBase.TB_MENSAGEM,
						values,
						DataBase.MENSAGEM_ID + "=? and "
								+ DataBase.MENSAGEM_TIPO + "=?",
						new String[] { m.getIdMensagem(),
								Integer.toString(m.getTipo()) });
	}

	protected Vector<Mensagem> getAllMensagens() throws MalformedURLException {
		try {
			Cursor c = db.getDB().query(DataBase.TB_MENSAGEM, null, null, null,
					null, null, null);

			Vector<Mensagem> mensagens = new Vector<Mensagem>();
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				Mensagem mensagem = createMessage(c);

				mensagens.add(mensagem);

			}
			c.close();
			Collections.sort(mensagens);
			return mensagens;
		} catch (Exception e) {

			return null;
		}

	}

	protected Mensagem getOne(String id, int type) {

		Cursor c = db.getDB().query(
				DataBase.TB_MENSAGEM,
				null,
				DataBase.MENSAGEM_ID + "=? and " + DataBase.MENSAGEM_TIPO
						+ "=?", new String[] { id, Integer.toString(type) },
				null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();

			try {
				Mensagem m = createMessage(c);
				c.close();
				return m;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		c.close();

		return null;
	}

	private Mensagem createMessage(Cursor c) throws MalformedURLException,
			JSONException {
		Mensagem mensagem = new Mensagem();

		mensagem.setIdMensagem(c.getString(0));
		mensagem.setMensagem(c.getString(1));
		mensagem.setNome_usuario(c.getString(2));
		mensagem.setImagePath(new URL(c.getString(3)));
		mensagem.setTipo(c.getInt(4));
		mensagem.setData(new Date(c.getLong(5)));
		mensagem.setIdUser(c.getLong(6));
		mensagem.setAddtions(new JSONObject(c.getString(7)));

		int tipo = mensagem.getTipo();
		if (tipo == Mensagem.TIPO_STATUS || tipo == Mensagem.TIPO_TWEET_SEARCH) {
			mensagem.setAction(OpenTwitterStatusAction.getInstance());
		} else if (tipo == Mensagem.TIPO_FACEBOOK_GROUP
				|| tipo == Mensagem.TIPO_NEWS_FEEDS) {
			mensagem.setAction(OpenFacebookStatusAction.getInstance());
		} else if (tipo == Mensagem.TIPO_FACEBOOK_NOTIFICATION) {
			try {
				String type = mensagem.getAddtions().getString("link");
				if (type.startsWith("http://www.facebook.com/groups/")) {
					type = type.replace("http://www.facebook.com/groups/", "");
					type.replace("/", "_");

					type = type.substring(0, type.length() - 1);
					if (type.contains("_"))
						mensagem.setAction(OpenFacebookGroupNotificationAction
								.getInstance());
					else
						mensagem.setAction(OpenLinkActivity.getInstance());
				} else if (type
						.startsWith("http://www.facebook.com/permalink.php?story_fbid=")) {
					mensagem.setAction(OpenFacebookTaggedAction.getInstance());
				} else if (type.startsWith("http://www.facebook.com/")
						&& type.contains("/posts/")) {
					mensagem.setAction(OpenFriendPostAction.getInstance());
				} else if (type
						.startsWith("http://www.facebook.com/photo.php?fbid=")) {
					mensagem.setAction(OpenLinkActivity.getInstance());
				}
			} catch (JSONException e) {
				mensagem.setAction(OpenTwitterStatusAction.getInstance());
			}
		}

		return mensagem;
	}

	protected Vector<Mensagem> getMensagemOf(int type) {
		try {

			Cursor c = db.getDB().query(DataBase.TB_MENSAGEM, null,
					DataBase.MENSAGEM_TIPO + "=?",
					new String[] { Integer.toString(type) }, null, null, null);

			Vector<Mensagem> mensagens = new Vector<Mensagem>();
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);

				Mensagem mensagem = createMessage(c);

				mensagens.add(mensagem);

			}
			c.close();
			Collections.sort(mensagens);
			return mensagens;
		} catch (Exception e) {
			return null;
		}

	}

	protected Vector<Mensagem> getMensagemOfLikeId(int type, String idLike) {
		try {

			Cursor c = db.getDB().query(DataBase.TB_MENSAGEM, null,
					DataBase.MENSAGEM_TIPO + "=?",
					new String[] { Integer.toString(type) }, null, null, null);

			Vector<Mensagem> mensagens = new Vector<Mensagem>();
			for (int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);

				Mensagem mensagem = createMessage(c);
				if (mensagem.getIdMensagem().startsWith(idLike))
					mensagens.add(mensagem);

			}

			c.close();
			return mensagens;
		} catch (Exception e) {
			return null;
		}

	}

	protected boolean existsStatus(String id, int tipo) {
		Cursor c = db.getDB().query(
				DataBase.TB_MENSAGEM,
				null,
				DataBase.MENSAGEM_ID + "=? and " + DataBase.MENSAGEM_TIPO
						+ "=?", new String[] { id, Integer.toString(tipo) },
				null, null, null);

		boolean result = c.getCount() > 0;
		c.close();
		return result;
	}

	protected void deleteAll() {
		db.getDB().delete(DataBase.TB_MENSAGEM, null, null);
	}

	protected void deleteAll(int type) {
		if (type == Mensagem.TIPO_FACEBOOK_GROUP
				|| type == Mensagem.TIPO_NEWS_FEEDS) {
			Vector<Mensagem> msgs = getMensagemOf(type);
			for (Mensagem m : msgs) {
				deleteAllComents(m.getIdMensagem());
			}

		}

		db.getDB().delete(DataBase.TB_MENSAGEM, DataBase.MENSAGEM_TIPO + "=?",
				new String[] { Integer.toString(type) });
	}

	protected void deleteAllComents(String id) {
		Vector<Mensagem> msgs = getMensagemOf(Mensagem.TIPO_FACE_COMENTARIO);
		for (Mensagem m : msgs) {
			if (m.getIdMensagem().startsWith(id))
				delete(m.getIdMensagem(), Mensagem.TIPO_FACE_COMENTARIO);
		}
	}

	protected void delete(String id, int type) {
		db.getDB().delete(
				DataBase.TB_MENSAGEM,
				DataBase.MENSAGEM_ID + "=? and " + DataBase.MENSAGEM_TIPO
						+ "=?", new String[] { id, Integer.toString(type) });
		if (type == Mensagem.TIPO_FACEBOOK_GROUP
				|| type == Mensagem.TIPO_NEWS_FEEDS)
			deleteAllComents(id);
	}

	protected void deleteAllSearch() {
		db.getDB().delete(DataBase.TB_MENSAGEM, DataBase.MENSAGEM_TIPO + "=?",
				new String[] { Integer.toString(Mensagem.TIPO_TWEET_SEARCH) });
	}

	protected void deleteAllTo(long date) {
		db.getDB().delete(DataBase.TB_MENSAGEM,
				DataBase.MENSAGEM_DATE + "<" + date, null);
	}

	protected int count(int type) {
		Cursor c = db.getDB().query(DataBase.TB_MENSAGEM, null,
				DataBase.MENSAGEM_TIPO + "=?",
				new String[] { Integer.toString(type) }, null, null, null);

		int out = c.getCount();
		c.close();
		return out;
	}

}
