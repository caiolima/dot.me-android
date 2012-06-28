package com.dot.me.model.bd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import com.dot.me.model.FacebookGroup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class FacebookGroupsBD extends Dao {

	protected FacebookGroupsBD(Context ctx) {
		super(ctx);
	}

	protected void insert(FacebookGroup fg) {
		ContentValues values = new ContentValues();
		values.put(DataBase.FACEBOOK_GROUPS_ID, fg.getId());
		values.put(DataBase.FACEBOOK_GROUPS_NAME, fg.getName());
		values.put(DataBase.FACEBOOK_GROUPS_DESCRIPTION, fg.getDescription());
		values.put(DataBase.FACEBOOK_GROUPS_ULR_IMAGE, fg.getUrlImage()
				.toString());

		db.getDB().insert(DataBase.TB_FACEBOOK_GROUPS, null, values);
	}

	protected void delete(String id) {
		db.getDB().delete(DataBase.TB_FACEBOOK_GROUPS,
				DataBase.FACEBOOK_GROUPS_ID + "=?", new String[] { id });
	}

	protected FacebookGroup getOne(String id) {

		Cursor c = db.getDB().query(DataBase.TB_FACEBOOK_GROUPS, null,
				DataBase.FACEBOOK_GROUPS_ID + "=?", new String[] { id }, null,
				null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();

			FacebookGroup fg = new FacebookGroup();

			fg.setId(c.getString(0));
			fg.setName(c.getString(1));
			fg.setDescription(c.getString(2));
			try {
				fg.setUrlImage(new URL(c.getString(3)));
			} catch (MalformedURLException e) {
				return null;
			}

			return fg;

		}

		return null;
	}

	protected Vector<FacebookGroup> getAll() {
		Vector<FacebookGroup> all = new Vector<FacebookGroup>();

		Cursor c = db.getDB().query(DataBase.TB_FACEBOOK_GROUPS, null, null,
				null, null, null, null);
		for (int i = 0; i < c.getCount(); i++) {
			
			c.moveToPosition(i);
			
			FacebookGroup fg = new FacebookGroup();

			fg.setId(c.getString(0));
			fg.setName(c.getString(1));
			fg.setDescription(c.getString(2));
			try {
				fg.setUrlImage(new URL(c.getString(3)));
			} catch (MalformedURLException e) {
				
			}
			
			all.add(fg);
		}
		return all;
	}

	protected void deleteAll() {
		db.getDB().delete(DataBase.TB_FACEBOOK_GROUPS, null, null);
	}

	protected boolean existsGroup(String id) {
		Cursor c = db.getDB().query(DataBase.TB_FACEBOOK_GROUPS, null,
				DataBase.FACEBOOK_GROUPS_ID + "=?", new String[] { id }, null,
				null, null);
		if (c.getCount() > 0)
			return true;
		return false;

	}

}
