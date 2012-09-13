package com.dot.me.command;

import com.dot.me.activity.ManageCollumnsActivity;

import android.app.Activity;
import android.content.Intent;

public class OpenManageCollumnCommand implements AbstractCommand{

	@Override
	public void execute(Activity activity) {
		
		Intent intent=new Intent(activity,ManageCollumnsActivity.class);
		
		activity.startActivity(intent);
	
		
	}

}
