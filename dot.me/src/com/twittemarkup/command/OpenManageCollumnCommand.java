package com.twittemarkup.command;

import com.twittemarkup.activity.ManageCollumnsActivity;

import android.app.Activity;
import android.content.Intent;

public class OpenManageCollumnCommand implements AbstractCommand{

	@Override
	public void execute(Activity activity) {
		
		Intent intent=new Intent(activity,ManageCollumnsActivity.class);
		
		activity.startActivity(intent);
		
	}

}
