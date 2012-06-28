package com.dot.me.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.dot.me.app.R;
import com.dot.me.assynctask.TwitterImageDownloadTask;
import com.dot.me.model.FacebookGroup;
import com.dot.me.model.bd.Facade;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FacebookGroupAdapter extends BaseAdapter{

	private List<FacebookGroup> data=new ArrayList<FacebookGroup>();
	private LayoutInflater mInflater;
	private Context ctx;
	private List<FacebookGroup> selected=new ArrayList<FacebookGroup>();
	private Facade facade;
	
	public FacebookGroupAdapter(Context ctx) {
		// operação necessária para usar o Inflater
		this.ctx = ctx;
		mInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		facade=Facade.getInstance(ctx);
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public FacebookGroup getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final FacebookGroup group=getItem(position);
		Log.w("dot.me", "Getting view at position " + position);
		if(group!=null){
			convertView = mInflater.inflate(R.layout.group_item, null);
			
			ImageView img_avatar=(ImageView) convertView.findViewById(R.id.group_img_picture);
			TextView txt_name=(TextView) convertView.findViewById(R.id.group_name);
			//TextView txt_description=(TextView) convertView.findViewById(R.id.group_txt_descripition);
			/*CheckBox chk_item=(CheckBox) convertView.findViewById(R.id.group_chk_selector);
			
			boolean exists=facade.existsGroup(group.getId());
			if(exists){
				chk_item.toggle();
				selected.add(group);
			}

			
			chk_item.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						selected.add(group);
						Log.d("dot.me", group.getName()+" Selected");
					}
					else{
						selected.remove(group);
						Log.d("dot.me", group.getName()+" removed");
					}
					
				}
			});*/
			txt_name.setText(group.getName());
			//txt_description.setText(group.getDescription());
			TwitterImageDownloadTask.executeDownload(ctx, img_avatar, group.getUrlImage());	
		}
		
		return convertView;
	}
	
	public void addItem(FacebookGroup group){
		data.add(group);
		notifyDataSetChanged();
	}
	
	public List<FacebookGroup> getSelectds() {
		return selected;
	}
	
}



