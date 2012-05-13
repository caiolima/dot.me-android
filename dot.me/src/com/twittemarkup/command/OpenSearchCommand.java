package com.twittemarkup.command;

import com.twittemarkup.activity.SearchActivity;
import com.twittemarkup.utils.Constants;

import android.app.Activity;
import android.content.Intent;

public class OpenSearchCommand implements AbstractCommand {

	@Override
	public void execute(Activity activity) {
		Intent intent = new Intent(activity, SearchActivity.class);
		activity.startActivityForResult(intent, Constants.COLUMN_SEARCH_RESULT);

	}

}
