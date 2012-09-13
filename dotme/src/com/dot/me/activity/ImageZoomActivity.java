package com.dot.me.activity;

import java.net.MalformedURLException;
import java.net.URL;

import com.dot.me.app.R;
import com.dot.me.assynctask.TwitterImageDownloadTask;
import com.dot.me.assynctask.TwitterImageDownloadTask.IOnImageLoaded;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.ads.AdRequest.ErrorCode;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ImageZoomActivity extends TrackedActivity implements AdListener {

	private ImageViewTouch img;
	private ProgressBar progress;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_view);

		img = (ImageViewTouch) findViewById(R.id.imageShow);
		progress = (ProgressBar) findViewById(R.id.progress_image);

		String urlImage = "";
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				urlImage = extras.getString("image_url");
			}
		}

		if (!urlImage.equals("")) {
			try {
				new TwitterImageDownloadTask(this, img, new URL(urlImage),
						event).execute();
			} catch (MalformedURLException e) {

			}
		}

		adView = (AdView) findViewById(R.id.adView);

		AdRequest adRequestBanner = new AdRequest();

		adView.setAdListener(this);

		adView.loadAd(adRequestBanner);
	}

	private IOnImageLoaded event = new IOnImageLoaded() {

		@Override
		public void onImageLoaded(ImageView img) {

			progress.setVisibility(View.GONE);
			img.setVisibility(View.VISIBLE);
		}
	};

	@Override
	public void onDismissScreen(Ad ad) {
		Log.v("teste ad", "onDismissScreen");
	}

	@Override
	public void onLeaveApplication(Ad ad) {
		Log.v("teste ad", "onLeaveApplication");
	}

	@Override
	public void onPresentScreen(Ad ad) {
		Log.v("teste ad", "onPresentScreen");
	}

	@Override
	public void onReceiveAd(Ad ad) {
		Log.v("teste ad", "Did Receive Ad");
		adView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onFailedToReceiveAd(Ad ad, ErrorCode errorCode) {
		Log.v("teste ad", "Failed to receive ad (" + errorCode + ")");
	}

	/** Overwrite the onDestroy() method to dispose of banners first. */
	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}
}
