package com.dot.me.command;

import com.dot.me.activity.SearchActivity;
import com.dot.me.utils.Constants;

import android.app.Activity;
import android.content.Intent;

public class OpenSearchCommand implements AbstractCommand {

	@Override
	public void execute(Activity activity) {
		Intent intent = new Intent(activity, SearchActivity.class);
		activity.startActivityForResult(intent, Constants.COLUMN_SEARCH_RESULT);

	}

}
