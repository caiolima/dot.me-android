package com.twittemarkup.model.bd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import com.twittemarkup.model.FacebookAccount;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class FacebookBD extends Dao{

	protected FacebookBD(Context ctx) {
		super(ctx);
	}
	
	protected void insert(FacebookAccount acc){
		ContentValues values=new ContentValues();
		values.put(DataBase.FACEBOOK_ID, acc.getId());
		values.put(DataBase.FACEBOOK_EXPIRES, acc.getExpires());
		values.put(DataBase.FACEBOOK_TOKEN, acc.getToken());
		values.put(DataBase.FACEBOOK_PROFILE_IMG, acc.getProfileImage().toString());
		values.put(DataBase.FACEBOOK_NAME, acc.getName());
		
		db.getDB().insert(DataBase.TB_FACEBOOK, null, values);
	}
	
	protected void delete(){
		db.getDB().delete(DataBase.TB_FACEBOOK, null, null);
	}
	
	protected FacebookAccount getOne(long id){
		Cursor c=db.getDB().query(DataBase.TB_FACEBOOK, null,
				DataBase.FACEBOOK_ID+"=?", new String[]{Long.toString(id)},
				null, null, null);
		
		if(c.getCount()>0){
			c.moveToFirst();
			
			FacebookAccount f=new FacebookAccount();
			f.setId(c.getLong(0));
			try {
				f.setProfileImage(new URL(c.getString(1)));
			} catch (MalformedURLException e) {
			}
			f.setToken(c.getString(2));
			f.setExpires(c.getLong(3));
			f.setName(c.getString(4));
			
			return f;
			
		}
		
		return null;
	}
	
	protected Vector<FacebookAccount> getAll(){
		
		Vector<FacebookAccount> all=new Vector<FacebookAccount>();
		
		Cursor c=db.getDB().query(DataBase.TB_FACEBOOK, null,
				null,null,
				null, null, null);
		
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			
			FacebookAccount f=new FacebookAccount();
			f.setId(c.getLong(0));
			try {
				f.setProfileImage(new URL(c.getString(1)));
			} catch (MalformedURLException e) {
			}
			f.setToken(c.getString(2));
			f.setExpires(c.getLong(3));
			f.setName(c.getString(4));
			
			all.add(f);
		}
		
		return all;
		
	}

}
