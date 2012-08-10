package com.dot.me.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.net.Uri;

public class Utils {

	public static Uri createURIToLink(String link) throws UnsupportedEncodingException{
		return Uri.parse("dot_link://web?url="+URLEncoder.encode(link, "UTF-8"));
	}
	
}
