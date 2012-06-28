package com.dot.me.activity;

import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.utils.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchActivity extends Activity{

	private EditText txt_search;
	private Button bt_search;
	private ListView lst_tt;
	private boolean openTimeline;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.search);
		
		if(Account.getTwitterAccount(this)==null){
			Toast.makeText(this, getString(R.string.no_twitter_logged), Toast.LENGTH_SHORT).show();
			finish();
		}
		
		openTimeline=false;
		Intent intent=getIntent();
		if(intent!=null){
			Bundle b=intent.getExtras();
			if(b!=null){
				openTimeline=b.getBoolean("open_timeline");
			}
		}
		
		txt_search=(EditText)findViewById(R.id.search_txt);
		bt_search=(Button)findViewById(R.id.bt_search);
		lst_tt=(ListView)findViewById(R.id.search_trend_list);
		
		bt_search.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent=new Intent(SearchActivity.this,SearchResultActivity.class);
				Bundle b=new Bundle();
				b.putString("search_value", txt_search.getText().toString().toLowerCase().trim());
				intent.putExtras(b);
				
				startActivityForResult(intent,Constants.COLUMN_SEARCH_RESULT);
				
			}
			
		});
		
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==Constants.COLUMN_SEARCH_RESULT){
			if(resultCode==RESULT_OK){
				
				Bundle b=data.getExtras();
				
				
				/*if(openTimeline){
					Intent intent=new Intent(this,TimelineActivity.class);
					intent.putExtras(b);
					
					startActivity(intent);
				}else{
					Intent intent=new Intent();
					intent.putExtras(b);
					setResult(RESULT_OK, intent);
				}*/
				String search=b.getString("search_name");
				search=search.replace("#", "%23");
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("add_column://twitter_search?search_value="
								+ search));
				
				startActivity(intent);
				finish();
				
			}
		}
		
	}
	
	

	
	
}
