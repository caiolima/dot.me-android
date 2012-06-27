package com.twittemarkup.model.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DataBase {

	// classe para o banco de Dados
	public static final String TB_TWITTER = "twitter",
			TB_MARCADOR = "marcador", TB_PALAVRA_CHAVE = "palavra_chave",
			TB_PALAVRA_CHAVE_MARCADOR = "palavra_chave_marcador",
			TB_MENSAGEM = "mensagem", TB_SEARCH = "search_tb",
			TB_USER = "user_table", TB_BLACKLIST = "blacklist",
			TB_FACEBOOK = "tb_facebook",
			TB_FACEBOOK_GROUPS = "facebook_groups",
			TB_COLUMNS_CONFIG = "collumn_config", TB_DRAFTS = "drafts",

			TWITTER_ID = "idTwitter", TWITTER_NAME = "name",
			TWITTER_TOKEN = "token", TWITTER_TOKEN_SECRET = "tokenSecret",
			TWITTER_PROF_IMF = "profile_img",

			FACEBOOK_ID = "idFacebook", FACEBOOK_TOKEN = "access_token",
			FACEBOOK_PROFILE_IMG = "image_url",
			FACEBOOK_EXPIRES = "expires_at", FACEBOOK_NAME = "name",

			MARCADOR_ID = "idMarcador", MARCADOR_NOME = "nome",
			MARCADOR_ENABLED = "visivel",

			MENSAGEM_ID = "idMensagem", MENSAGEM_TEXTO = "texto",
			MENSAGEM_USER = "userName", MENSAGEM_DATE = "createdAt",
			MENSAGEM_IMAGE_URL = "imgURL", MENSAGEM_TIPO = "tipoMensagem",
			MENSAGEM_ID_USER = "id_from", MENSAGEM_ADD_CONTENT = "add_content",

			SEARCH_ID = "idSearch", SEARCH_CONTENT = "content_word",
			SEARCH_CREATED_AT = "created_at",

			PALAVRA_CHAVE_ID = "idPalavraChave",
			PALAVRA_CHAVE_CONTEUDO = "conteudo",

			USER_SYS_ID = "id", USER_ID = "idUser", USER_NAME = "name",
			USER_URL = "url_img", USER_ABOUT = "about", USER_NICK = "nick",
			USER_NUM_FOLLOWERS = "followers", USER_NUM_FOLLOWING = "following",
			USER_TYPE = "tipo_usuario",

			FACEBOOK_GROUPS_ID = "idGroup",
			FACEBOOK_GROUPS_ULR_IMAGE = "url_image",
			FACEBOOK_GROUPS_NAME = "name",
			FACEBOOK_GROUPS_DESCRIPTION = "facebook_groups_description",

			BLACKLIST_ID_PALAVRA = "idPalavra",

			COLUMNS_CONFIG_POS = "pos", COLUMNS_CONFIG_TYPE = "c_type",
			COLUMNS_CONFIG_PROPRITIES = "c_propreties",

			DRAFTS_ID = "drafts_id", DRAFTS_TEXT = "drafts_text";

	private static DataBase singleton;
	private final String[] CREATE_SQL = new String[] {

			"CREATE TABLE IF NOT EXISTS " + TB_COLUMNS_CONFIG + "("
					+ COLUMNS_CONFIG_POS + " integer," + ""
					+ COLUMNS_CONFIG_TYPE + " text not null, " + ""
					+ COLUMNS_CONFIG_PROPRITIES + " text not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_SEARCH + "(" + SEARCH_CONTENT
					+ " text primary key, " + "" + SEARCH_CREATED_AT
					+ " long not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_TWITTER + "(" + TWITTER_ID
					+ " long primary key," + "" + TWITTER_TOKEN
					+ " text not null," + "" + TWITTER_TOKEN_SECRET
					+ " text not null," + "" + TWITTER_PROF_IMF
					+ " text not null," + "" + TWITTER_NAME
					+ " text not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_FACEBOOK_GROUPS + "("
					+ FACEBOOK_GROUPS_ID + " text primary key," + ""
					+ FACEBOOK_GROUPS_NAME + " text not null," + ""
					+ FACEBOOK_GROUPS_DESCRIPTION + " text not null," + ""
					+ FACEBOOK_GROUPS_ULR_IMAGE + " text not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_FACEBOOK + "(" + FACEBOOK_ID
					+ " long primary key," + "" + FACEBOOK_PROFILE_IMG
					+ " text not null," + "" + FACEBOOK_TOKEN
					+ " text not null," + "" + FACEBOOK_EXPIRES
					+ " long not null," + "" + FACEBOOK_NAME
					+ " text not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_MARCADOR + "(" + MARCADOR_ID
					+ " integer primary key autoincrement," + ""
					+ MARCADOR_NOME + " text not null," + "" + MARCADOR_ENABLED
					+ " integer not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_PALAVRA_CHAVE + "("
					+ PALAVRA_CHAVE_ID + " integer primary key autoincrement,"
					+ "" + PALAVRA_CHAVE_CONTEUDO + " text not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_DRAFTS + "(" + DRAFTS_ID
					+ " text not null," + "" + DRAFTS_TEXT + " text not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_PALAVRA_CHAVE_MARCADOR + "("
					+ PALAVRA_CHAVE_ID + " long not null," + "" + MARCADOR_ID
					+ " long not null);",

			"CREATE TABLE IF NOT EXISTS " + TB_MENSAGEM + "(" + MENSAGEM_ID
					+ " text," + "" + MENSAGEM_TEXTO + " text not null," + ""
					+ MENSAGEM_USER + " text not null," + ""
					+ MENSAGEM_IMAGE_URL + " text not null," + ""
					+ MENSAGEM_TIPO + " int not null," + "" + MENSAGEM_DATE
					+ " long not null," + "" + MENSAGEM_ID_USER
					+ " long not null," + "" + MENSAGEM_ADD_CONTENT + " text);",

			"CREATE TABLE IF NOT EXISTS " + TB_USER + "(" + USER_SYS_ID
					+ " integer primary key autoincrement," + "" + USER_ID
					+ " long," + "" + USER_ABOUT + " text not null," + ""
					+ USER_NAME + " text not null," + "" + USER_URL
					+ " text not null," + "" + USER_NUM_FOLLOWERS + " int,"
					+ "" + USER_NUM_FOLLOWING + " int," + "" + USER_TYPE
					+ " int not null," + "" + USER_NICK + " text);",

			"CREATE TABLE IF NOT EXISTS " + TB_BLACKLIST + "("
					+ BLACKLIST_ID_PALAVRA + " integer primary key)"

	};
	private SQLiteDatabase db;
	private SQLiteHelper dbHelper;

	public DataBase(Context c) {
		dbHelper = new SQLiteHelper(c, "TwitteMarkup", 17, CREATE_SQL);
		setDB(dbHelper.getWritableDatabase());
	}

	public void setDB(SQLiteDatabase db) {
		this.db = db;
	}

	public SQLiteDatabase getDB() {
		return db;
	}

	public void close() {
		singleton = null;
		try {
			db.close();
		} catch (SQLiteException e) {

		}
	}

	public static void start(Context ctx) {
		if (singleton == null)
			singleton = new DataBase(ctx);

	}

	public static DataBase getInstance(Context ctx) {
		if (singleton == null)
			singleton = new DataBase(ctx);
		return singleton;
	}

}
