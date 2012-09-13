package com.dot.me.command;

import com.dot.me.activity.SendTweetActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class OpenTwitterWriter implements AbstractCommand {

	private String preText;
	
	public OpenTwitterWriter(){}
	
	public OpenTwitterWriter(String preText){
		this.preText=preText;
	}
	
	
	@Override
	public void execute(Activity activity) {
		Intent intent=new Intent(activity,SendTweetActivity.class);
		Bundle extras=new Bundle();
		extras.putString("action", "SendTwitteAction");
		
		if(preText!=null){
			extras.putString("pre_text", preText);
		}
		
		extras.putString("type_message", "Twitter");
		
		intent.putExtras(extras);
		
		activity.startActivity(intent);
		
	}

	
	
}
