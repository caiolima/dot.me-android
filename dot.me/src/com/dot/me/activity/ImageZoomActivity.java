package com.dot.me.activity;

import java.net.MalformedURLException;
import java.net.URL;

import com.dot.me.app.R;
import com.dot.me.assynctask.TwitterImageDownloadTask;
import com.dot.me.assynctask.TwitterImageDownloadTask.IOnImageLoaded;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ImageZoomActivity extends Activity{

	private ImageViewTouch img;
	private ProgressBar progress;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_view);
		
		img=(ImageViewTouch) findViewById(R.id.imageShow);
		progress=(ProgressBar) findViewById(R.id.progress_image);
		
		String urlImage="";
		Intent intent=getIntent();
		if(intent!=null){
			Bundle extras=intent.getExtras();
			if(extras!=null){
				urlImage=extras.getString("image_url");
			}
		}
		
		if(!urlImage.equals("")){
			try {
				new TwitterImageDownloadTask(this, img, new URL(urlImage),event).execute();
			} catch (MalformedURLException e) {
				
			}
		}
			
	}

	private IOnImageLoaded event=new IOnImageLoaded() {
		
		@Override
		public void onImageLoaded(ImageView img) {
			
			progress.setVisibility(View.GONE);
			img.setVisibility(View.VISIBLE);
		}
	};
	
	
}
