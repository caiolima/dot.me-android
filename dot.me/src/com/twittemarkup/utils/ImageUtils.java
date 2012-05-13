package com.twittemarkup.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.HashMap;

import com.twittemarkup.app.R;

import twitter4j.ResponseList;
import twitter4j.Status;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageUtils {

	public static HashMap<ImageView, URL> imageLoadMap = new HashMap<ImageView, URL>();
	public static HashMap<URL, Bitmap> imageCache = new HashMap<URL, Bitmap>();

	public static Bitmap loadProfileImages(URL imageUrl, Context ctx) {
		try {
			String cachePath = ctx.getCacheDir().getAbsolutePath();

			String cachedFile = cachePath + File.separator
					+ convertURLToFileName(imageUrl.toString());
			File file=new File(cachedFile);
			InputStream in=null;
			if(file.exists())
				in=new FileInputStream(file);
			
			Bitmap bMap=null;
			HttpURLConnection connection=null;
			if (in==null) {
				connection = (HttpURLConnection) imageUrl
						.openConnection();
				connection.setRequestProperty("Request-Method", "GET");
				connection.setDoInput(true);
				connection.setDoOutput(false);
				connection.connect();

				in = connection.getInputStream();
				byte[] byteImages = readBytes(in);
				bMap = BitmapFactory.decodeByteArray(byteImages, 0, byteImages.length);
				
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(byteImages);
				fos.close();
				
				
			}else{
				bMap = BitmapFactory.decodeStream(in);
			}	
				
			in.close();
			
			
			
			return bMap;
		} catch (IOException e) {
			return null;
		} catch (NullPointerException e) {
			Log.d("slide", "Erro no Download!");
			return null;
		}
	}

	private static byte[] readBytes(InputStream in) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) > 0) {
				bos.write(buffer, 0, len);
			}
			byte[] bytes = bos.toByteArray();
			bos.close();
			in.close();

			return bytes;
		} catch (IOException e) {
			return bos.toByteArray();
		}
	}

	public static void loadImages(ResponseList<Status> list) {
		for (Status status : list) {
			URL imageURL = status.getUser().getProfileImageUrlHttps();
			Bitmap imgBit = ImageUtils.imageCache.get(imageURL);
			if (imgBit == null) {
				// imgBit = ImageUtils.loadProfileImages(imageURL);
				ImageUtils.imageCache.put(imageURL, imgBit);
			}
		}
	}

	/*
	 * public static class LoadImageService extends Service {
	 * 
	 * @Override public void onStart(Intent intent, int startId) {
	 * 
	 * ConnectivityManager cm = (ConnectivityManager)
	 * getSystemService(Context.CONNECTIVITY_SERVICE);
	 * 
	 * NetworkInfo nInfo = cm.getActiveNetworkInfo(); if (nInfo != null) if
	 * (nInfo.isConnected()) { Bundle b = intent.getExtras(); if (b != null) {
	 * String url = b.getString("url"); if (url != null) try { new
	 * ImageDownloadTask().execute(new URL(url)); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 * 
	 * return; }
	 * 
	 * Toast t = Toast.makeText(this, getString(R.string.unable_update),
	 * Toast.LENGTH_LONG); t.show(); }
	 * 
	 * @Override public IBinder onBind(Intent intent) { // TODO Auto-generated
	 * method stub return null; }
	 * 
	 * 
	 * }
	 */

	private static String convertURLToFileName(String url) {
		url = url.replace("/", "");
		url = url.replace(".", "");
		url = url.replace(":", "");
		url= url.replace("_", "");
		return url;
	}
	
	public static String getPath(Uri uri, Activity activity) {
	    String[] projection = { MediaStore.Images.Media.DATA };
	    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
	    activity.startManagingCursor(cursor);
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}

}
