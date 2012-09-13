package com.dot.me.model;

import java.net.URL;
import java.util.Vector;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

import com.dot.me.utils.TwitterUtils;

public class UsuarioTwitter {

	private String usuario,password,token,tokenSecret;
	private long id;
	private String email="a";
	private long twitterId;
	private static UsuarioTwitter current;
	private Vector<Label> marcadores=new Vector<Label>();
	
	
	public Vector<Label> getMarcadores() {
		return marcadores;
	}

	public void setMarcadores(Vector<Label> marcadores) {
		this.marcadores = marcadores;
	}

	public UsuarioTwitter(){
		setCurrent(this);
	}
	
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getTokenSecret() {
		return tokenSecret;
	}
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTwitterId() {
		return twitterId;
	}
	public void setTwitterId(long twitterId) {
		this.twitterId = twitterId;
	}
	
	/*public static UsuarioTwitter fromJSON(JSONObject json){
		UsuarioTwitter user=null;
		try {
			user=new UsuarioTwitter();
			user.setId(json.getInt("idusuario"));
			user.setPassword(json.getString("password"));
			user.setToken(json.getString("token"));
			user.setTokenSecret(json.getString("tokenSecret"));
			user.setTwitterId(json.getLong("twitterId"));
			user.setUsuario(json.getString("user"));
			JSONArray jsonMarcadores=json.getJSONArray("marcadores");
			Vector<Marcador> marcadores=new Vector<Marcador>();
			for(int i=0;i<jsonMarcadores.length();i++){
				Marcador marcador=Marcador.FromJSONObject(jsonMarcadores.getJSONObject(i));
				marcadores.add(marcador);
			}
			user.setMarcadores(marcadores);
		} catch (JSONException e) {
			return null;
		}
		
		return user;
	}*/

	public static void setCurrent(UsuarioTwitter current) {
		UsuarioTwitter.current = current;
	}

	/*public static UsuarioTwitter getCurrent(Context ctx) {
		if(current==null)
			current=Facade.getInstance(ctx).lastSavedSession();
		return current;
	}*/

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
	
	public URL getURLProfile() throws TwitterException{
		Twitter twitter=TwitterUtils.getTwitter(new AccessToken(token, tokenSecret));
		return twitter.verifyCredentials().getProfileImageURL();
	}
	
	
}
