package com.twittemarkup.message.action;

import com.twittemarkup.model.Account;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public interface ISendAction {

	public void execute(Activity a,String message,Bundle b);
	public String getResultMessage(Context c);
	public Account getAccount(Context c);
	public String getDraftId();
	public boolean messageSent();
	
}
