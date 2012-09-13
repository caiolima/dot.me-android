package com.dot.me.model.bd;

import com.dot.me.model.TrendLocation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class LocationSelectedBD extends Dao{

	protected LocationSelectedBD(Context ctx) {
		super(ctx);
	}
	
	protected void insert(TrendLocation l){
		ContentValues values = new ContentValues();
		values.put(DataBase.LOCATION_SELECTED_WOEID, l.getWoeid());
		values.put(DataBase.LOCATION_SELECT_NAME, l.getName());
		

		db.getDB().insert(DataBase.TB_LOCATION_SELECTED, null, values);
	}
	
	protected void deleteCurrent(){
		db.getDB().delete(DataBase.TB_LOCATION_SELECTED, null, null);
	}
	
	protected TrendLocation getSaved(){
		Cursor c = db.getDB().query(DataBase.TB_LOCATION_SELECTED, null,
				null, null, null,
				null, null);
		if(c.getCount()>0){
			c.moveToFirst();
			
			TrendLocation l=new TrendLocation();
			l.setWoeid(c.getInt(0));
			l.setName(c.getString(1));
			
			return l;
		}
		c.close();
		return null;
	}
	

}
