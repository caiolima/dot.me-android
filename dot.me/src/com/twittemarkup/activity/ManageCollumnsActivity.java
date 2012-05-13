package com.twittemarkup.activity;


import java.util.ArrayList;
import java.util.List;

import com.twittemarkup.adapter.DragNDropAdapter;
import com.twittemarkup.app.R;
import com.twittemarkup.model.CollumnConfig;
import com.twittemarkup.model.bd.Facade;
import com.twittemarkup.view.DragListener;
import com.twittemarkup.view.DragNDropListView;
import com.twittemarkup.view.DropListener;
import com.twittemarkup.view.RemoveListener;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ManageCollumnsActivity extends Activity {

	private Button bt_ok;
	private DragNDropListView lst_drag;
	private DragNDropAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.manage_collumn);
		
		bt_ok=(Button) findViewById(R.id.manage_collumn_bt_ok);
		lst_drag=(DragNDropListView) findViewById(R.id.manage_collumn_lst);
		
		ArrayList<CollumnConfig> list=Facade.getInstance(this).getAllConfig();
		
		adapter=new DragNDropAdapter(this, new int[]{R.layout.dragitem}, new int[]{R.id.TextView01}, list);
		
		lst_drag.setAdapter(adapter);
		
		lst_drag.setDragListener(mDragListener);
		lst_drag.setDropListener(mDropListener);
		lst_drag.setRemoveListener(mRemoveListener);
		
		bt_ok.setOnClickListener(mClickListener);
		
	}
	
	private View.OnClickListener mClickListener=
			new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Facade.getInstance(ManageCollumnsActivity.this).deleteAllCollumnConfig();
					
					for(int i=0;i<adapter.getCount();i++){
						CollumnConfig config=adapter.getItem(i);
						
						Facade.getInstance(ManageCollumnsActivity.this).insert(config);
					}
					
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse("timeline://reload"));
					
					startActivity(intent);
					
					finish();
					
				}
			};
		
	

	private DropListener mDropListener = 
			new DropListener() {
	        public void onDrop(int from, int to) {
	        	ListAdapter adapter = getListAdapter();
	        	if (adapter instanceof DragNDropAdapter) {
	        		((DragNDropAdapter)adapter).onDrop(from, to);
	        		getListView().invalidateViews();
	        	}
	        }
	    };
	    
	    private RemoveListener mRemoveListener =
	        new RemoveListener() {
	        public void onRemove(int which) {
	        	ListAdapter adapter = getListAdapter();
	        	if (adapter instanceof DragNDropAdapter) {
	        		((DragNDropAdapter)adapter).onRemove(which);
	        		getListView().invalidateViews();
	        	}
	        }
	    };
	    
	    private DragListener mDragListener =
	    	new DragListener() {

	    	int backgroundColor = Color.WHITE;
	    	int defaultBackgroundColor;
	    	
				public void onDrag(int x, int y, ListView listView) {
					// TODO Auto-generated method stub
				}

				public void onStartDrag(View itemView) {
					itemView.setVisibility(View.GONE);
					defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
					itemView.setBackgroundColor(backgroundColor);
					ImageView iv = (ImageView)itemView.findViewById(R.id.ImageView01);
					if (iv != null) iv.setVisibility(View.GONE);
				}

				public void onStopDrag(View itemView) {
					itemView.setVisibility(View.VISIBLE);
					itemView.setBackgroundColor(defaultBackgroundColor);
					ImageView iv = (ImageView)itemView.findViewById(R.id.ImageView01);
					if (iv != null) iv.setVisibility(View.VISIBLE);
				}
	    	
	    };

		protected ListAdapter getListAdapter() {
			
			return adapter;
		}

		protected AbsListView getListView() {
			return this.lst_drag;
		}
	
	
	
}
