package com.dot.me.activity;

import java.util.Vector;

import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.model.Marcador;
import com.dot.me.model.bd.Facade;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ManageLabelsActivity extends Activity{

	private ArrayAdapter<Marcador> adapter;
	private ListView lst_marcadores;
	private boolean delete = false;
	private Vector<Marcador> selecteds=new Vector<Marcador>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.manage_labels);
		
		adapter = new ArrayAdapter<Marcador>(this,
				android.R.layout.simple_list_item_checked,
				Account.getMarcadores(this));
		
		lst_marcadores = (ListView) findViewById(R.id.markup_list);

		lst_marcadores.setAdapter(adapter);
		lst_marcadores
		.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> ap, View view,
					int position, long id) {

				Marcador marcador = adapter.getItem(position);
				CheckedTextView chk_view=(CheckedTextView)view;
				if(chk_view.isChecked()){
					selecteds.remove(marcador);
					
				}else{
					selecteds.add(marcador);
					
				}
				chk_view.toggle();

			}

		});
		
	}

	private class DeletaMarcadoresTask extends AsyncTask<String, Void, Void>{

		private ProgressDialog progressDialog;
		private String response;
		private String out="";
		
		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(ManageLabelsActivity.this);

			progressDialog
					.setMessage(getString(R.string.delete_wait));

			progressDialog.show();

		}
		
		@Override
		protected Void doInBackground(String... param) {
			
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
			
			for(Marcador m:selecteds){
				Facade.getInstance(ManageLabelsActivity.this).deleteMarcador(m.getIdMarcador());
			}
			
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result) {
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
							if (out.equals(getString(R.string.delete_markups_success))||out.equals(getString(R.string.markup_updated_success)))
								
								ManageLabelsActivity.this.finish();
						}
					}).create();
			d.show();
		}
		
	}
	
	
}
