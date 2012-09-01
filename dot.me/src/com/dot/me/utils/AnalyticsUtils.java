package com.dot.me.utils;

public class AnalyticsUtils {

	private static String[] return_interval={
		"< 10 min",
		"10-30 min",
		"30 min - 2 h",
		"2h - 6h",
		">6h"
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
	
}
