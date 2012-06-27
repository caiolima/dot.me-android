package com.twittemarkup.model.bd;

import com.twittemarkup.model.Draft;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DraftBD extends Dao{

	protected DraftBD(Context ctx) {
		super(ctx);
	}
	
	protected void insert(Draft d){
		
		ContentValues values=new ContentValues();
		values.put(DataBase.DRAFTS_ID, d.getId());
		values.put(DataBase.DRAFTS_TEXT, d.getText());
		
		db.getDB().insert(DataBase.TB_DRAFTS, null, values);
		
	}
	
	protected void delete(String id){
		db.getDB().delete(DataBase.TB_DRAFTS, DataBase.DRAFTS_ID+"=?", new String[]{id});
	}

	protected Draft getOne(String id) {
		Cursor c=db.getDB().query(DataBase.TB_DRAFTS, null,
				DataBase.DRAFTS_ID+"=?", new String[]{id},
				null, null, null);
		
		Draft draft=null;
		if(c.getCount()>0){
			c.moveToFirst();
			
			draft=new Draft();
			draft.setId(c.getString(0));
			draft.setText(c.getString(1));
			
		}
		
		return draft;
		
	}
	
	protected boolean exists(String id){
		Cursor c=db.getDB().query(DataBase.TB_DRAFTS, null,
				DataBase.DRAFTS_ID+"=?", new String[]{id},
				null, null, null);
		
		
		if(c.getCount()>0)
			return true;
		
		return false;
	}
	
	
}
