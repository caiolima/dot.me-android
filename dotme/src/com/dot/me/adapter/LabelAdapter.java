package com.dot.me.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dot.me.activity.CreateMarkupActivity;
import com.dot.me.activity.ManageLabelsActivity;
import com.dot.me.app.R;
import com.dot.me.model.Label;

public class LabelAdapter extends BaseAdapter{

	private List<Label> list=new ArrayList<Label>();
	private Vector<Label> selecteds=new Vector<Label>();
	private LayoutInflater mInflater;
	private Context ctx;
	private List<ItemCheckedReciever> observers=new ArrayList<LabelAdapter.ItemCheckedReciever>();
	
	public LabelAdapter(Context ctx){
		this.ctx=ctx;
		mInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public LabelAdapter(Context ctx,List<Label> ini){
		this(ctx);
		list=ini;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Label getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final Label l=getItem(position);
		if(l!=null){
			convertView = mInflater.inflate(R.layout.marcador_list, null);
			
			TextView text=(TextView) convertView.findViewById(R.id.txt_label_name);
			CheckBox chk=(CheckBox) convertView.findViewById(R.id.chk_labl_list);
			
			chk.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					
					if(!isChecked){
						selecteds.remove(l);
					}else{
						selecteds.add(l);
					}
					
					for(ItemCheckedReciever o:observers){
						o.onItemChecked(l);
					}
					
				}
				
			});
			String s="(A)";
			if(l.isEnnabled()==0)
				s="(D)";
			text.setText(l.getNome()+s);
			
			LinearLayout l_item=(LinearLayout) convertView.findViewById(R.id.l_item);
			l_item.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(ctx,CreateMarkupActivity.class);
					Bundle b=new Bundle();
					b.putInt("marcador_id", l.getIdMarcador());
					
					intent.putExtras(b);
					((Activity)ctx).startActivityForResult(intent, ManageLabelsActivity.ADD_LABEL);
				}
			});
		}
		
		return convertView;
	}

	public void add(Label l){
		list.add(l);
		notifyDataSetChanged();
	}
	
	public void remove(Label l){
		list.remove(l);
		notifyDataSetChanged();
	}
	
	public void clear(){
		list.clear();
		notifyDataSetChanged();
	}

	public Vector<Label> getSelecteds() {
		return selecteds;
	}

	public void registerObserver(ItemCheckedReciever r){
		observers.add(r);
	}
	
	public void unregisterObserver(ItemCheckedReciever r){
		observers.remove(r);
	}
	
	public interface ItemCheckedReciever{
		public void onItemChecked(Label l);
	}
	
	public void clearSelecteds(){
		selecteds.clear();
		
	}
	
}
