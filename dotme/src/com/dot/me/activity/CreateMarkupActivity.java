package com.dot.me.activity;


import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.model.CollumnConfig;
import com.dot.me.model.Label;
import com.dot.me.model.PalavraChave;
import com.dot.me.model.bd.Facade;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateMarkupActivity extends TrackedActivity{

	private EditText txt_nome;
	private EditText txt_palavras;
	private Button bt_criar;
	private TextView lb_title;
	private boolean atualiza=false;
	private Label currentMarcador;
	private boolean labelCreted=false;
	private GoogleAnalyticsTracker tracker=GoogleAnalyticsTracker.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.marcador);
		
		txt_nome=(EditText)findViewById(R.id.markup_form_txt_name);
		txt_palavras=(EditText)findViewById(R.id.markup_form_txt_words);
		bt_criar=(Button)findViewById(R.id.markup_form_bt_create);
		lb_title=(TextView)findViewById(R.id.markup_form_lb_title);
		
		Bundle extra=getIntent().getExtras();
		if(extra!=null){
			currentMarcador=Facade.getInstance(this).getOneMarcador(extra.getInt("marcador_id"));
			
			if(currentMarcador!=null){
				txt_nome.setText(currentMarcador.getNome());
				String palavrasText="";
				for(PalavraChave p:currentMarcador.getPalavrasChave()){
					palavrasText+=p.getConteudo()+"\n";
				}
				txt_palavras.setText(palavrasText);
				
				bt_criar.setText(getString(R.string.markup_bt_update));
				lb_title.setText(getString(R.string.markup_txt_update));
				
				atualiza=true;
			}
		}
		
		bt_criar.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				String text=txt_nome.getText().toString();
				text=text.trim();
				if(text.equals(""))
					return;
				
				String palavras_text=txt_palavras.getText().toString().trim();
				if(palavras_text.equals(""))
					return;
				
				String[] palavras=palavras_text.split("\n");
				if(palavras.length<0)
					return;
				
				
				
				new CreateMarkupTask().execute(CreateMarkupActivity.this);
			}
			
		});
	
		
	}

	class CreateMarkupTask extends AsyncTask<Activity, Void, Void>{

		private ProgressDialog progressDialog;
		private String response;
		private String out = "";

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(CreateMarkupActivity.this);

			progressDialog.setMessage(getString(R.string.creating_account));

			progressDialog.show();

		}
		
		@Override
		protected Void doInBackground(Activity... param) {
			
			/*String url = Constants.SERVER_NAME+"?action=CreateMarkup";
			if(atualiza)
				url = Constants.SERVER_NAME+"?action=UpdateMarkup";
			
			WebService webService = new WebService(url);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("nome", txt_nome.getText().toString()));
			params.add(new BasicNameValuePair("idusuario", Long.toString(UsuarioTwitter.getCurrent(CreateMarkupActivity.this).getId())));
			params.add(new BasicNameValuePair("palavras", txt_palavras.getText().toString()));
			if(atualiza){
				params.add(new BasicNameValuePair("idmarcador", Long.toString(currentMarcador.getIdMarcador())));
			}
			
			response=webService.doPost("", params);*/
			String nome=txt_nome.getText().toString();
			
			Label m=new Label();
			m.setNome(nome);
			
			
			String palavras_text=txt_palavras.getText().toString();
			String[] palavras=palavras_text.split("\n");
			for(int i=0;i<palavras.length;i++){
				palavras[i]=palavras[i].trim();
				PalavraChave p=new PalavraChave();
				p.setConteudo(palavras[i].trim());
				
				m.addPalavra(p);	
			}
			
			if(atualiza){
				currentMarcador.setNome(m.getNome());
				Facade.getInstance(CreateMarkupActivity.this).update(currentMarcador, palavras);
			}else{
				int id=Facade.getInstance(CreateMarkupActivity.this).insert(m);
				
				CollumnConfig config=new CollumnConfig();
				config.setType(CollumnConfig.MARKUP);
				JSONObject json=new JSONObject();
				try {
					json.put("id", id);
					json.put("name", m.getNome());
					config.setProprietes(json);
					
					Facade.getInstance(CreateMarkupActivity.this).insert(config);
				} catch (JSONException e) {
			
				}
				
				
				
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(!atualiza){
				out=getString(R.string.markup_create_success);
				tracker.trackEvent("Filter Events", "labels", "created", 0);
			}else{
				out=getString(R.string.markup_updated_success);
				tracker.trackEvent("Filter Events", "labels", "updated", 0);
			}
			/*JSONObject json;
			try {
				json = new JSONObject(response);
				int action_status = json.getInt("action_status");
				if(Menssage.SUCCESS==action_status){
					if(!atualiza)
						out=getString(R.string.markup_create_success);
					else{
						out=getString(R.string.markup_updated_success);
					}
					JSONArray marcadoresJSON=json.getJSONArray("marcadores");
					Facade facade=Facade.getInstance(CreateMarkupActivity.this);
					facade.deletAllMarcadores();
					UsuarioTwitter twitter=UsuarioTwitter.getCurrent(CreateMarkupActivity.this);
					twitter.getMarcadores().clear();
					for(int i=0;i<marcadoresJSON.length();i++){
						Marcador marcador=Marcador.FromJSONObject(marcadoresJSON.getJSONObject(i));
						facade.insert(marcador);
						twitter.getMarcadores().add(marcador);
					}
					
				}else{
					out = getString(R.string.markup_create_erro);
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
			Dialog d = new AlertDialog.Builder(CreateMarkupActivity.this)
			.setTitle(getString(R.string.text_message))
			.setMessage(out)
			.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							if (out.equals(getString(R.string.markup_create_success))||out.equals(getString(R.string.markup_updated_success))){
								labelCreted=true;
								CreateMarkupActivity.this.setResult(RESULT_OK);
								finish();
							}
						}
					}).create();
			d.show();
			
		}
		
		
		
	}

	@Override
	protected void onDestroy() {
		if(!labelCreted)
			setResult(RESULT_CANCELED);
		super.onDestroy();
	}
	
	
	
	
	
}
