package com.dot.me.utils;

public class Separator {

	private int page,qtd_tweets;

	public Separator(int page,int qtd_tweets){
		this.page=page;
		this.qtd_tweets=qtd_tweets;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getQtd_tweets() {
		return qtd_tweets;
	}

	public void setQtd_tweets(int qtd_tweets) {
		this.qtd_tweets = qtd_tweets;
	}
	
	
}
