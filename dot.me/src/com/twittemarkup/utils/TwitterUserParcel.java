package com.twittemarkup.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class TwitterUserParcel implements Parcelable{

	private String about;
	private String where;
	private String url;
	private String name;
	private int num_followers;
	private int num_following;
	
	public TwitterUserParcel(){
		
	}
	
	public  TwitterUserParcel(Parcel in) {
		readParcelIn(in);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public TwitterUserParcel createFromParcel(Parcel in) {
            return new TwitterUserParcel(in);
        }
 
        public TwitterUserParcel[] newArray(int size) {
            return new TwitterUserParcel[size];
        }
    };
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(about);
		dest.writeString(where);
		dest.writeString(url);
		dest.writeString(name);
		dest.writeInt(num_followers);
		dest.writeInt(num_following);
	}
	
	private void readParcelIn(Parcel in){
		about=in.readString();
		where=in.readString();
		url=in.readString();
		name=in.readString();
		num_followers=in.readInt();
		num_following=in.readInt();
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(String where) {
		this.where = where;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNum_followers() {
		return num_followers;
	}

	public void setNum_followers(int num_followers) {
		this.num_followers = num_followers;
	}

	public int getNum_following() {
		return num_following;
	}

	public void setNum_following(int num_following) {
		this.num_following = num_following;
	}

}
