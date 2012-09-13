package com.dot.me.command;

import com.dot.me.activity.SendTweetActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class OpenFacebookWriter implements AbstractCommand {

	private String toID,typeMessage;
	
	public OpenFacebookWriter(String toID,String typeMessage){
		this.toID=toID;
		this.typeMessage=typeMessage;
	} 
	
	public OpenFacebookWriter(){}
	
	@Override
	public void execute(Activity activity) {
		
		Intent intent=new Intent(activity,SendTweetActivity.class);
		
		Bundle b=new Bundle();
		
		b.putString("action", "SendFacebookAction");
		
		if(toID!=null){
			Bundle sendParams=new Bundle();
			sendParams.putString("toID", toID);
			
			b.putBundle("send_params", sendParams);
		}
		
		String type="Facebook";
		if(typeMessage!=null)
			type=typeMessage;
		
		b.putString("type_message", type);
		
		
		intent.putExtras(b);
		
		activity.startActivity(intent);
		
		
	}

}
