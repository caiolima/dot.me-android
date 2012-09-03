package com.dot.me.utils;

public class AnalyticsUtils {

	private static String[] return_interval={
		"< 10 min",
		"10-30 min",
		"30 min - 2 h",
		"2h - 6h",
		">6h"
	};
	
	private static String[] return_age={
		"<18",
		"18-25",
		"25-40",
		">40"
	};
	
	public static String classifyReturInterval(long interval){
		long min=interval/60000;
		if(min<10){
			return return_interval[0];
		}else if(min>=10&&min<30){
			return return_interval[1];
		}else if(min>=30&&min<120){
			return return_interval[2];
		}else if(min>=120&&min<360){
			return return_interval[3];
		}else {
			return return_interval[4];
		}
	}
	
	public static String classifyReturAge(String birthday){
		String year=birthday.split("/")[2];
		int year_v=Integer.parseInt(year);
		if(year_v<18){
			return return_age[0];
		}else if(year_v>=18&&year_v<25){
			return return_age[1];
		}else if(year_v>=25&&year_v<40){
			return return_age[2];
		}else {
			return return_age[3];
		}
		
	}
	
}
