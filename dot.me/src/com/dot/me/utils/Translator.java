package com.dot.me.utils;

import java.util.HashMap;

import android.content.Context;

import com.dot.me.app.R;

public class Translator {

	private static HashMap<String, Integer> stringMap=new HashMap<String, Integer>();
	
	//traduoes
	static{
		stringMap.put("updated his status", R.string.update_his_status);
		stringMap.put("updated her status", R.string.update_her_status);
		stringMap.put("shared", R.string.shared);
		stringMap.put("also commented on a post in", R.string.commented_on_post);
		stringMap.put("posted in", R.string.posted_in);
		stringMap.put("added a new photo", R.string.added_photo);
		stringMap.put("commented on a photo that you're tagged in", R.string.commented_photo_tag);
		stringMap.put("commented on a status that you're tagged in", R.string.commented_status_tag);
		stringMap.put("added a photo of you", R.string.added_photo_you);
		stringMap.put("likes your comment", R.string.likes_your_comment);
		stringMap.put("likes your comment", R.string.likes_your_comment);
		stringMap.put("commented on your status", R.string.commented_on_your_status);
		stringMap.put("like your comment", R.string.like_comment);
		stringMap.put("like your status", R.string.like_status);
	}

	public static String translate(String message,Context ctx){
		String tMessage=message;
		
		for(String string:stringMap.keySet()){
			if(tMessage.contains(string)){
				tMessage=tMessage.replace(string, ctx.getString(stringMap.get(string)));
			}
		}
		
		return tMessage;
		
	}
	
	
}
