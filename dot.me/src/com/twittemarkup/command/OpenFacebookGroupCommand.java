package com.twittemarkup.command;

import com.twittemarkup.activity.ManageFacebookGroupsActivity;

import android.app.Activity;
import android.content.Intent;

public class OpenFacebookGroupCommand implements AbstractCommand {

	@Override
	public void execute(Activity activity) {
		Intent intent = new Intent(
				activity,
				ManageFacebookGroupsActivity.class);
		activity.startActivity(intent);

	}

}
