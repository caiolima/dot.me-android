package com.twittemarkup.model.bd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.twittemarkup.model.Account;
import com.twittemarkup.model.TwitterAccount;
import com.twittemarkup.model.UsuarioTwitter;
import com.twittemarkup.utils.Menssage;

public class TwitterBD extends Dao{

	
	
	protected TwitterBD(Context ctx) {
		super(ctx);
	}

	protected int insert(TwitterAccount acc){
		
		ContentValues values=new ContentValues();
		values.put(DataBase.TWITTER_ID, acc.getId());
		values.put(DataBase.TWITTER_TOKEN, acc.getToken());
		values.put(DataBase.TWITTER_TOKEN_SECRET, acc.getTokenSecret());
		values.put(DataBase.TWITTER_PROF_IMF, acc.getProfileImage().toString());
		values.put(DataBase.TWITTER_NAME, acc.getName());
		SQLiteDatabase base=db.getDB();	
		
		long r=base.insert(DataBase.TB_TWITTER, null, values);
		if(r==-1)
			return Menssage.ERRO;
		else
			return Menssage.SUCCESS;
	}
	
	protected void delete(long twitterId){
		db.getDB().delete(DataBase.TB_TWITTER, DataBase.TWITTER_ID+"=?", new String[]{Long.toString(twitterId)});
	}
	
	protected void delete(){
		db.getDB().delete(DataBase.TB_TWITTER, null,null);
	}
	
	protected Vector<Account> lastSavedSession(){
		Cursor c=db.getDB().query(DataBase.TB_TWITTER, null, null, null, null, null, null);
		Vector<Account> all=new Vector<Account>();
		
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			
			TwitterAccount t=new TwitterAccount();
			
			t.setId(c.getLong(0));
			t.setToken(c.getString(1));
			t.setTokenSecret(c.getString(2));
			try {
				t.setProfileImage(new URL(c.getString(3)));
			} catch (MalformedURLException e) {
			
			}
			t.setName(c.getString(4));
			
			all.add(t);
		}
		c.close();
		return all;
		
	}
	
}
