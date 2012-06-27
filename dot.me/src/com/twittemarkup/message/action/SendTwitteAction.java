package com.twittemarkup.message.action;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;

import com.twittemarkup.activity.SendTweetActivity;
import com.twittemarkup.app.R;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.TwitterAccount;
import com.twittemarkup.utils.Constants;
import com.twittemarkup.utils.ImageUtils;
import com.twittemarkup.utils.TwitterUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class SendTwitteAction implements ISendAction {
	private boolean messageSent = false;
	private String inReplyId="none";
	
	@Override
	public void execute(Activity a, String message, Bundle b) {

		TwitterAccount twitterAcc = (TwitterAccount) Account
				.getTwitterAccount(a);
		long idReplyStatus = -1;
		if (b != null)
			idReplyStatus = b.getLong("inReply");

		if (idReplyStatus != -1) {
			inReplyId=""+inReplyId;
			StatusUpdate update = new StatusUpdate(message);
			update.setInReplyToStatusId(idReplyStatus);

			try {
				TwitterUtils.getTwitter(
						new AccessToken(twitterAcc.getToken(), twitterAcc
								.getTokenSecret())).updateStatus(update);

			} catch (TwitterException e) {

			}
		} else
			twitterAcc.updateStatus(message);

		messageSent = true;

	}

	@Override
	public String getResultMessage(Context c) {
		String message = null;
		if (messageSent)
			message = c.getString(R.string.message_sent);
		else
			message = c.getString(R.string.erro_message_sent);
		return message;
	}

	@Override
	public Account getAccount(Context c) {
		
		return Account.getTwitterAccount(c);
	}

	@Override
	public String getDraftId() {
		// TODO Auto-generated method stub
		return "twitter_"+inReplyId;
	}

	@Override
	public boolean messageSent() {
		// TODO Auto-generated method stub
		return messageSent;
	}

}
