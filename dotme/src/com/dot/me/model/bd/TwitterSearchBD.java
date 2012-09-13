package com.dot.me.model.bd;

import java.util.Date;
import java.util.Vector;

import com.dot.me.utils.Menssage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TwitterSearchBD extends Dao{

	protected TwitterSearchBD(Context ctx) {
		super(ctx);
	}

	protected int insert(String search){
		Date date=new Date();
		ContentValues values=new ContentValues();
		values.put(DataBase.SEARCH_CONTENT, search.toLowerCase());
		values.put(DataBase.SEARCH_CREATED_AT, date.getTime());
		SQLiteDatabase base=db.getDB();	
		long r=base.insert(DataBase.TB_SEARCH, null, values);
		if(r==-1)
			return Menssage.ERRO;
		else
			return Menssage.SUCCESS;
	}
	
	protected void delete(String search){
		db.getDB().delete(DataBase.TB_SEARCH, DataBase.SEARCH_CONTENT+"=?", new String[]{search.toLowerCase()});
	}
	
	protected void deleteAll(){
		db.getDB().delete(DataBase.TB_SEARCH,null, null);
	}
	
	protected boolean wasAdded(String search){
		Cursor c=db.getDB().query(DataBase.TB_SEARCH, null, DataBase.SEARCH_CONTENT+"=?",
				new String[]{search.toLowerCase()}, null, null, null);
		if(c.getCount()>0){
			c.close();
			return true;
		}
		c.close();
		return false;
	}
	
	protected Vector<String> getAll(){
		Cursor c=db.getDB().query(DataBase.TB_SEARCH, null, null, null, null, null, null);
		Vector<String> searches=new Vector<String>();
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			searches.add(c.getString(0));
		}
			
		c.close();
		return searches;
	}
	
}
