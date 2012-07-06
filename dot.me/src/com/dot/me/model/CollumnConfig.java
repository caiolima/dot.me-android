package com.dot.me.model;

import org.json.JSONException;
import org.json.JSONObject;

public class CollumnConfig {

	private int pos;
	private String type;
	private JSONObject proprietes;
	public final static String TWITTER_COLLUMN="twitter",FACEBOOK_COLLUMN="facebook",
				FACEBOOK_GROUPS="face_groups", TWITTER_SEARCH="twitter_search",
				ME="face_notif", MARKUP="markup";

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public JSONObject getProprietes() {
		return proprietes;
	}

	public void setProprietes(JSONObject proprietes) {
		this.proprietes = proprietes;
	}
	
	public void setName(String name) throws JSONException{
		
		if(proprietes!=null)
			proprietes.put("name", name);
		
	}
	
	public String getName(){
		String out="";
		try {
			out=proprietes.getString("name");
		} catch (JSONException e) {
			
		}
		return out;
	}
	
	public String getNextPage(){
		try{
			
			return proprietes.getString("nextPage");
		}catch (JSONException e ) {
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		try{
			CollumnConfig c=(CollumnConfig) o;
			return c.pos==this.pos;
				
		}catch (ClassCastException e) {
			return false;
		}
	}
	
	

}
