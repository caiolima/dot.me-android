package com.dot.me.command;

import com.dot.me.activity.BlackListActivity;

import android.app.Activity;
import android.content.Intent;
import android.sax.StartElementListener;

public class OpenManageBlackListCommand implements AbstractCommand {

	@Override
	public void execute(Activity activity) {
		Intent intent =new Intent(activity,BlackListActivity.class);
		activity.startActivity(intent);

	}

}
