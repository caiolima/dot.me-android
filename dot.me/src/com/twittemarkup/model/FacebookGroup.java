package com.twittemarkup.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class FacebookGroup {

	private String id, name, description;
	private URL urlImage;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public URL getUrlImage() {
		return urlImage;
	}

	public void setUrlImage(URL urlImage) {
		this.urlImage = urlImage;
	}

	public static FacebookGroup createFromJSON(JSONObject jsonObject) {
		FacebookGroup group=new FacebookGroup();
		
		try {
			group.setId(jsonObject.getString("id"));
			group.setName(jsonObject.getString("name"));
			group.setDescription("");
			group.setUrlImage(new URL(jsonObject.getString("picture")));
		} catch (JSONException e) {
			return null;
		} catch (MalformedURLException e) {
			return null; 
		}
		
		
		return group;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof FacebookGroup){
			return ((FacebookGroup)o).getId().equals(this.getId());
		}else{
			return false;
		}
		
	}
	
	

}
