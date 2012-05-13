package com.twittemarkup.interfaces;

import twitter4j.auth.AccessToken;

public interface IGetUpdateAction {

	public void onGetUpdate(AccessToken token);
	
}
