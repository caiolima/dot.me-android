package com.dot.me.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.dot.me.adapter.LabelAdapter;
import com.dot.me.adapter.LabelAdapter.ItemCheckedReciever;
import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.model.Label;
import com.dot.me.model.bd.Facade;

public class ManageLabelsActivity extends Activity implements ItemCheckedReciever{

	private LabelAdapter adapter;
	private Button bt_action;
	private ListView lst_marcadores;
	public static final int ADD_LABEL=1;
	//private boolean delete = false;
	
	@SuppressLint("ParserError")
	private OnClickListener clickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(adapter.getSelecteds().size()==0){
				
				Intent intent =new Intent(ManageLabelsActivity.this,CreateMarkupActivity.class);
				startActivityForResult(intent, ADD_LABEL);
				
			}else{
				
				String op[]={getString(R.string.change_disable_enable),getString(R.string.delete)};
				
				ArrayAdapter<String> adapter=new ArrayAdapter<String>(ManageLabelsActivity.this, android.R.layout.simple_list_item_1,op);
				AlertDialog dialog = new AlertDialog.Builder(ManageLabelsActivity.this).
						setTitle(getString(R.string.actions)).
						setAdapter(adapter, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if(which==0){
									for(Label m:ManageLabelsActivity.this.adapter.getSelecteds()){
										m.changeEnable();
										Facade.getInstance(ManageLabelsActivity.this).manageMarkup(m);
										
									}
									
									ManageLabelsActivity.this.adapter.notifyDataSetChanged();
									ManageLabelsActivity.this.adapter.clearSelecteds();
									onItemChecked(null);
								}else if(which==1){
									new DeletaMarcadoresTask().execute();
								}
								
								
							}
						}).create();
				
				dialog.show();
			}
			
			
		}
	};
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==ADD_LABEL){
			if(resultCode==RESULT_OK){
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("timeline://reload"));
				
				startActivity(intent);
				
				finish();
			}
		}
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.manage_labels);
		
		
		
		lst_marcadores = (ListView) findViewById(R.id.markup_list);
		bt_action=(Button) findViewById(R.id.bt_action);
		
		bt_action.setOnClickListener(clickListener);
		
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		if(adapter!=null)
			adapter.clear();
		
		adapter = new LabelAdapter(this,
				Facade.getInstance(this).getAllMarcadores());
		lst_marcadores.setAdapter(adapter);
		adapter.registerObserver(this);
	}



	private class DeletaMarcadoresTask extends AsyncTask<Void, Void, Void>{

		private ProgressDialog progressDialog;
		//private String response;
		private String out="";
		
		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(ManageLabelsActivity.this);

			progressDialog
					.setMessage(getString(R.string.delete_wait));

			progressDialog.show();

		}
		
		@Override
		protected Void doInBackground(Void... param) {
			
			/*WebService web=new WebService(Constants.SERVER_NAME
						+ "?action=DeleteMarkup");
			
			String idMarcadores="";
			for(Marcador m:selecteds){
				idMarcadores+=Long.toString(m.getIdMarcador())+",";
			}
			idMarcadores=idMarcadores.substring(0,idMarcadores.length()-1);
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("idtwitter", Long.toString(user.getId())));
			params.add(new BasicNameValuePair("idmarcadores", idMarcadores));
			
			response = web.doPost("", params);*/
			
			for(Label m:adapter.getSelecteds()){
				Facade.getInstance(ManageLabelsActivity.this).deleteMarcador(m.getIdMarcador());
			}
			
			
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result) {
			for(Label m:adapter.getSelecteds()){
				adapter.remove(m);
			}
			adapter.clearSelecteds();
			
			onItemChecked(null);
			out=getString(R.string.delete_markups_success);
			/*JSONObject json;
			try {
				json = new JSONObject(response);
				int action_status = json.getInt("action_status");
				if(Menssage.SUCCESS==action_status){
					out=getString(R.string.delete_markups_success);
					JSONArray marcadoresJSON=json.getJSONArray("marcadores");
					Facade facade=Facade.getInstance(MarcadoresListActivity.this);
					facade.deletAllMarcadores();
					UsuarioTwitter twitter=UsuarioTwitter.getCurrent(MarcadoresListActivity.this);
					twitter.getMarcadores().clear();
					for(int i=0;i<marcadoresJSON.length();i++){
						Marcador marcador=Marcador.FromJSONObject(marcadoresJSON.getJSONObject(i));
						facade.insert(marcador);
						twitter.getMarcadores().add(marcador);
					}
					
				}else{
					out = getString(R.string.delete_markups_erro);
					JSONArray erros = json.getJSONArray("erros");
					for (int i = 0; i < erros.length(); i++) {
						if (erros.getInt(i) == Menssage.MARKUP_EXISTS)
							out += "\n" + getString(R.string.markup_exists);
						
					}
				}				
			} catch (JSONException e) {
				
			}*/
			
			progressDialog.dismiss();
			
			Account.notifyDataChanged();
			
			Dialog d = new AlertDialog.Builder(ManageLabelsActivity.this)
			.setTitle(getString(R.string.text_message))
			.setMessage(out)
			.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							
						}
					}).create();
			d.show();
			
			
			
		}
		
	}

	@Override
	public void onItemChecked(Label l) {
		if(adapter.getSelecteds().size()>0){
			bt_action.setText(getString(R.string.remove));
		}else{
			bt_action.setText(getString(R.string.add));
		}
		
	}
	
	
}
