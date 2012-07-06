package com.dot.me.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.model.Label;
import com.dot.me.model.UsuarioTwitter;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;
import com.dot.me.utils.Menssage;
import com.dot.me.utils.WebService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MarcadoresListActivity extends Activity {

	private ArrayAdapter<Label> adapter;
	private ListView lst_marcadores;
	private boolean delete = false;
	private Button bt_delete,bt_disable;
	private LinearLayout lt_bottom;
	private Vector<Label> selecteds=new Vector<Label>();
	private boolean wasItemSelected=false;
	private Vector<Account> users;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setTitle(getString(R.string.markups_list));
		setContentView(R.layout.marcadores_list);

		Bundle b = getIntent().getExtras();
		if (b != null) {
			delete = b.getBoolean("delete");
		}
		lt_bottom=(LinearLayout)findViewById(R.id.lst_markup_layout);
		
		users = Account.getLoggedUsers(this);
		if (delete){
			adapter = new ArrayAdapter<Label>(this,
					android.R.layout.simple_list_item_checked,
					Account.getMarcadores(this));
			
			bt_delete=(Button)findViewById(R.id.lst_markup_bt_delete);
			bt_disable=(Button)findViewById(R.id.lst_markup_bt_disable);
			
			bt_delete.setOnClickListener(new Button.OnClickListener(){

				@Override
				public void onClick(View view) {
					if(selecteds.size()>0){}
						new DeletaMarcadoresTask().execute("");
				}
				
			});
			
			bt_disable.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(selecteds.size()>0){
						for(Label m:selecteds){
							m.changeEnable();
							Facade.getInstance(MarcadoresListActivity.this).manageMarkup(m);
							
						}
						
						adapter.notifyDataSetChanged();
					}
					
				}
			});
			
			
		}else{
			adapter = new ArrayAdapter<Label>(this,
					android.R.layout.simple_list_item_1, Account.getMarcadores(this));
			lt_bottom.setVisibility(View.GONE);
		}
		lst_marcadores = (ListView) findViewById(R.id.markups_list);

		lst_marcadores.setAdapter(adapter);
		if (!delete) {
			lst_marcadores
					.setOnItemClickListener(new ListView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> ap, View view,
								int position, long id) {

							Label marcador = adapter.getItem(position);

							Intent itent = new Intent(
									MarcadoresListActivity.this,
									CreateMarkupActivity.class);
							Bundle extra = new Bundle();
							extra.putInt("marcador_id",
									marcador.getIdMarcador());
							itent.putExtras(extra);

							startActivity(itent);

						}

					});
		}else{
			lst_marcadores
			.setOnItemClickListener(new ListView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> ap, View view,
						int position, long id) {

					Label marcador = adapter.getItem(position);
					CheckedTextView chk_view=(CheckedTextView)view;
					if(chk_view.isChecked()){
						selecteds.remove(marcador);
						
					}else{
						selecteds.add(marcador);
						
					}
					chk_view.toggle();

					if(selecteds.size()>0&&!wasItemSelected){
						wasItemSelected=true;
						Animation fadeInAnimation = AnimationUtils.loadAnimation(MarcadoresListActivity.this, R.anim.fade_in);
						bt_delete.setAnimation(fadeInAnimation);
						bt_delete.setVisibility(View.VISIBLE);
						bt_disable.setAnimation(fadeInAnimation);
						bt_disable.setVisibility(View.VISIBLE);
					}
					
					if(selecteds.size()==0&&wasItemSelected){
						wasItemSelected=false;
						Animation fadeOutAnimation = AnimationUtils.loadAnimation(MarcadoresListActivity.this, R.anim.fade_out);
						bt_delete.setAnimation(fadeOutAnimation);
						bt_delete.setVisibility(View.INVISIBLE);
						bt_disable.setAnimation(fadeOutAnimation);
						bt_disable.setVisibility(View.INVISIBLE);
					}

				}

			});
		}
		
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		if(!delete){
			adapter=new ArrayAdapter<Label>(this,
					android.R.layout.simple_list_item_1, Account.getMarcadores(this));
			lst_marcadores.setAdapter(adapter);
		}
	}



	private class DeletaMarcadoresTask extends AsyncTask<String, Void, Void>{

		private ProgressDialog progressDialog;
		private String response;
		private String out="";
		
		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(MarcadoresListActivity.this);

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
			
			for(Label m:selecteds){
				Facade.getInstance(MarcadoresListActivity.this).deleteMarcador(m.getIdMarcador());
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
			
			Dialog d = new AlertDialog.Builder(MarcadoresListActivity.this)
			.setTitle(getString(R.string.text_message))
			.setMessage(out)
			.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							if (out.equals(getString(R.string.delete_markups_success))||out.equals(getString(R.string.markup_updated_success)))
								
								MarcadoresListActivity.this.finish();
						}
					}).create();
			d.show();
		}
		
	}

}

