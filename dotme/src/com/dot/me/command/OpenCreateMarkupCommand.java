package com.dot.me.command;

import com.dot.me.activity.CreateMarkupActivity;
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
