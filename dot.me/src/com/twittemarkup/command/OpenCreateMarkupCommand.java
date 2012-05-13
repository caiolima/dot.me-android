package com.twittemarkup.command;

import com.twittemarkup.activity.CreateMarkupActivity;
import com.twittemarkup.activity.TimelineActivity;

import android.app.Activity;
import android.content.Intent;

public class OpenCreateMarkupCommand implements AbstractCommand{

	@Override
	public void execute(Activity activity) {
		Intent intent = new Intent(
				activity,
				CreateMarkupActivity.class);
		activity.startActivity(intent);
		
	}

	
}
