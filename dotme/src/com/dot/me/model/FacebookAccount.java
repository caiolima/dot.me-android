package com.dot.me.model;

import java.net.URL;

public class FacebookAccount extends Account{

	private String token;
	private long expires,id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

	@Override
	public URL processProfileImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateStatus(String status) {
		return false;
		
	}

}
