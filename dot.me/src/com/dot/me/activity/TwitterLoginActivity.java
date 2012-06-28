package com.dot.me.activity;

import com.dot.me.app.R;
import com.dot.me.utils.Item;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.ProgressBar;

;

public class TwitterLoginActivity extends Activity {

	private WebView wView;
	private ProgressBar load_bar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.twitter_login_web);

		wView = (WebView) findViewById(R.id.web_view);
		load_bar=(ProgressBar) findViewById(R.id.load_bar);
		Intent mIntent = getIntent();
		String url = (String) mIntent.getExtras().getString("url");

		

		wView.getSettings().setJavaScriptEnabled(true);

		
		wView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different
				// scales.
				// The progress meter will automatically disappear when we reach
				// 100%
				load_bar.setProgress(progress);
				if(progress>98)
					load_bar.setVisibility(View.GONE);
			}
		});

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
		wView.loadUrl(url);

	}

}
