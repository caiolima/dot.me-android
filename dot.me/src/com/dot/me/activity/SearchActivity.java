package com.dot.me.activity;

import java.util.ArrayList;

import com.dot.me.app.R;
import com.dot.me.assynctask.TrendTopicsGetterTask;
import com.dot.me.model.Account;
import com.dot.me.model.TrendLocation;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {

	private EditText txt_search;
	private Button bt_search;
	private ListView lst_tt;
	private LinearLayout lt_imgLoading;
	private boolean openTimeline;
	private TextView txt_change, txt_trends;
	private ImageView img_load;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search);

		if (Account.getTwitterAccount(this) == null) {
			Toast.makeText(this, getString(R.string.no_twitter_logged),
					Toast.LENGTH_SHORT).show();
			finish();
		}

		adapter = new ArrayAdapter<String>(this, R.layout.trend_item,
				R.id.txt_value);

		openTimeline = false;
		Intent intent = getIntent();
		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				openTimeline = b.getBoolean("open_timeline");
			}
		}

		txt_search = (EditText) findViewById(R.id.search_txt);
		bt_search = (Button) findViewById(R.id.bt_search);
		setLst_tt((ListView) findViewById(R.id.search_trend_list));
		img_load = (ImageView) findViewById(R.id.image_loading);
		setLt_imgLoading((LinearLayout) findViewById(R.id.lt_image_loading));
		txt_change = (TextView) findViewById(R.id.txt_change_trends);
		txt_trends = (TextView) findViewById(R.id.txt_trends);

		txt_change.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SearchActivity.this,
						TrendsLocationsActivity.class);
				startActivity(intent);
			}
		});

		lst_tt.setAdapter(adapter);
		lst_tt.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> ad, View v, int pos,
					long arg3) {

				Intent intent = new Intent(SearchActivity.this,
						SearchResultActivity.class);
				Bundle b = new Bundle();
				b.putString("search_value", adapter.getItem(pos));
				intent.putExtras(b);

				startActivityForResult(intent, Constants.COLUMN_SEARCH_RESULT);

			}
		});

		bt_search.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String search=txt_search.getText().toString()
						.toLowerCase().trim();
				if(search.equals(""))
					return;
				
				Intent intent = new Intent(SearchActivity.this,
						SearchResultActivity.class);
				Bundle b = new Bundle();
				b.putString("search_value", search);
				intent.putExtras(b);

				startActivityForResult(intent, Constants.COLUMN_SEARCH_RESULT);

			}

		});

		img_load.setBackgroundResource(R.drawable.load_icon);
		img_load.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable frameAnimation = (AnimationDrawable) img_load
						.getBackground();
				frameAnimation.start();
			}
		});
		
		TrendLocation t = Facade.getInstance(this).getSavedTrend();

		txt_trends.setText(getString(R.string.trends) + " > " + t.getName());

		new TrendTopicsGetterTask(this, t.getWoeid()).execute();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		Uri uri = intent.getData();
		if (uri.getScheme().equals("dotme")
				&& uri.getHost().equals("refresh_trends")) {
			TrendLocation t = Facade.getInstance(this).getSavedTrend();

			txt_trends
					.setText(getString(R.string.trends) + " > " + t.getName());

			adapter.clear();
			adapter.notifyDataSetChanged();

			img_load.post(new Runnable() {
				@Override
				public void run() {
					AnimationDrawable frameAnimation = (AnimationDrawable) img_load
							.getBackground();
					frameAnimation.start();
				}
			});
			lt_imgLoading.setVisibility(View.VISIBLE);

			new TrendTopicsGetterTask(this, t.getWoeid()).execute();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.COLUMN_SEARCH_RESULT) {
			if (resultCode == RESULT_OK) {

				Bundle b = data.getExtras();

				/*
				 * if(openTimeline){ Intent intent=new
				 * Intent(this,TimelineActivity.class); intent.putExtras(b);
				 * 
				 * startActivity(intent); }else{ Intent intent=new Intent();
				 * intent.putExtras(b); setResult(RESULT_OK, intent); }
				 */
				String search = b.getString("search_name");
				search = search.replace("#", "%23");
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("add_column://twitter_search?search_value="
								+ search));

				startActivity(intent);
				finish();

			}
		}

	}

	public ListView getLst_tt() {
		return lst_tt;
	}

	public void setLst_tt(ListView lst_tt) {
		this.lst_tt = lst_tt;
	}

	public ArrayAdapter<String> getAdapter() {
		return adapter;
	}

	public void setAdapter(ArrayAdapter<String> adapter) {
		this.adapter = adapter;
	}

	public LinearLayout getLt_imgLoading() {
		return lt_imgLoading;
	}

	public void setLt_imgLoading(LinearLayout lt_imgLoading) {
		this.lt_imgLoading = lt_imgLoading;
	}

}
