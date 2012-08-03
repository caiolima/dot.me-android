package com.dot.me.message.action;

import com.dot.me.model.Account;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public interface ISendAction {

	public void execute(Activity a,String message,Bundle b);
	public String getResultMessage(Context c);
	public Account getAccount(Context c);
	public String getDraftId();
	public boolean messageSent();
	public void initAction(Bundle b);
	
}
