package com.dot.me.model;

public class User {

	private long id;
	private int sysID;
	private String about;
	private String where;
	private String url;
	private String name;
	private String nick;
	private int num_followers;
	private int num_following;
	private int type;
	
	public final static int TWITTER=1;

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

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSysID() {
		return sysID;
	}

	public void setSysID(int sysID) {
		this.sysID = sysID;
	}
	
	
	public static User createFromTwitterUser(twitter4j.User user){
		User u=new User();
		
		u.setId(user.getId());
		u.setName(user.getName());
		u.setWhere(user.getLocation());
		u.setAbout(user.getDescription());
		u.setNum_followers(user.getFollowersCount());
		u.setNum_following(user.getFriendsCount());
		u.setType(TWITTER);
		u.setUrl(user.getProfileImageURL().toString());
		u.setNick(user.getScreenName());
		
		return u;
		
	}

}
