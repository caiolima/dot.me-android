package com.dot.me.activity;

import com.dot.me.app.R;
import com.dot.me.assynctask.GetTrendsLocationsTask;
import com.dot.me.model.TrendLocation;
import com.dot.me.model.bd.Facade;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class TrendsLocationsActivity extends Activity{

	private LinearLayout lt_loading;
	private ListView lst_locations;
	private ImageView img_loading;
	private ArrayAdapter<TrendLocation> adapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.trend_locations);
		
		lt_loading=((LinearLayout) findViewById(R.id.lt_loading));
		lst_locations=(ListView) findViewById(R.id.lst_locations);
		img_loading=(ImageView) findViewById(R.id.img_loading_anim);
		
		adapter=new ArrayAdapter<TrendLocation>(this, R.layout.location_item,R.id.txt_value);
		lst_locations.setAdapter(adapter);
		
		img_loading.setBackgroundResource(R.drawable.load_icon);
		img_loading.post(new Runnable() {
			@Override
			public void run() {
				AnimationDrawable frameAnimation = (AnimationDrawable) img_loading
						.getBackground();
				frameAnimation.start();
			}
		});
		
		lst_locations.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				TrendLocation t=adapter.getItem(position);
				Facade.getInstance(TrendsLocationsActivity.this).setLocation(t);
				
				Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("dotme://refresh_trends"));
				startActivity(intent);
				
				finish();
			}
			
		});
		
		new GetTrendsLocationsTask(this).execute();
		
	}


	public LinearLayout getLt_loading() {
		return lt_loading;
	}
	
	public ArrayAdapter<TrendLocation> getAdapter() {
		return adapter;
	}

	
	
}
