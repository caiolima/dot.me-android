package com.dot.me.activity;

import com.dot.me.app.R;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

;

public class TwitterLoginActivity extends Activity implements AdListener{

	private WebView wView;
	private ProgressBar load_bar;
	private AdView adView;
	private boolean isTwitterLogin = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.twitter_login_web);

		wView = (WebView) findViewById(R.id.web_view);
		load_bar = (ProgressBar) findViewById(R.id.load_bar);
		LinearLayout main_web=(LinearLayout) findViewById(R.id.lt_main_web);
		Intent mIntent = getIntent();
		String url = "";
		if (mIntent != null) {
			Uri uri = mIntent.getData();
			if (uri != null) {
				url = uri.getQueryParameter("url");
				isTwitterLogin=false;

			} else {

				Bundle extras = mIntent.getExtras();
				if (extras != null) {

					url = extras.getString("url");

				}
			}
		}

		wView.getSettings().setJavaScriptEnabled(true);

		
		wView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different
				// scales.
				// The progress meter will automatically disappear when we reach
				// 100%
				load_bar.setProgress(progress);
				if (progress > 98)
					load_bar.setVisibility(View.GONE);
			}
		});

		if (isTwitterLogin) {
			wView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.contains("twitter-client://back")) {
						Uri uri = Uri.parse(url);
						String oauthVerifier = uri
								.getQueryParameter("oauth_verifier");

						String myUri = "twitter-client://back?oauth_verifier="
								+ oauthVerifier;
						Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(myUri));
						startActivity(mIntent);

						finish();
						return true;
					}
					return false;
				}
			});
		}else{
			wView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					
					wView.loadUrl(url);
					load_bar.setVisibility(View.VISIBLE);
					return true;
				}
			});
			adView = (AdView) findViewById(R.id.adView);
			
			AdRequest adRequestBanner = new AdRequest();
		    adRequestBanner.addTestDevice(AdRequest.TEST_EMULATOR);
		    
		    adView.setAdListener(this);
		    
		    adView.loadAd(adRequestBanner);
		}
		
		
		wView.loadUrl(url);

	}


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
