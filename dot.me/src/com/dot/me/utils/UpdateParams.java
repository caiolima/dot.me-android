package com.dot.me.utils;

import twitter4j.auth.AccessToken;

public class UpdateParams {

	private int page;
	private int qtdFeeds;
	private AccessToken token;
	
	public UpdateParams(){}
	
	public UpdateParams(int page,int qtdFeeds,AccessToken token) {
		this.page=page;
		this.qtdFeeds=qtdFeeds;
		this.token=token;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getQtdFeeds() {
		return qtdFeeds;
	}

	public void setQtdFeeds(int qtdFeeds) {
		this.qtdFeeds = qtdFeeds;
	}

	public AccessToken getToken() {
		return token;
	}

	public void setToken(AccessToken token) {
		this.token = token;
	}

}
