package com.dot.me.command;

import com.dot.me.activity.ManageLabelsActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.MatrixCursor;

public class OpenManageLabelsCommand implements AbstractCommand {

	@Override
	public void execute(Activity activity) {
		Intent intent=new Intent(activity,ManageLabelsActivity.class);
		
		activity.startActivity(intent);
	}

}
