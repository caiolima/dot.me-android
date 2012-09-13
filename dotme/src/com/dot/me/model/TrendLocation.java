package com.dot.me.model;

import twitter4j.Location;

public class TrendLocation implements Comparable<TrendLocation>{

	private String name;
	private int woeid;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getWoeid() {
		return woeid;
	}
	public void setWoeid(int woeid) {
		this.woeid = woeid;
	}
	
	public static TrendLocation createFromLocation(Location l){
		
		TrendLocation mL=new TrendLocation();
		mL.setWoeid(l.getWoeid());
		mL.setName(l.getName()+" - "+l.getPlaceName());
		
		return mL;
		
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int compareTo(TrendLocation another) {
		return this.name.compareTo(another.getName());
	}
	
	
	
	
	
}
