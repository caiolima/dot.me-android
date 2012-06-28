package com.dot.me.activity;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.app.R;
import com.dot.me.utils.Constants;
import com.dot.me.utils.CriptoUtils;
import com.dot.me.utils.Menssage;
import com.dot.me.utils.WebService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private EditText txt_user, txt_password, txt_repassword;
	private Button bt_send;
	private Long twitterId;
	private String token, tokenSecret;
	private Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		Intent intent = getIntent();
		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				twitterId = b.getLong("twitterId");
				token = b.getString("token");
				tokenSecret = b.getString("tokenSecret");
			}

		}

		txt_user = (EditText) findViewById(R.id.register_form_user);
		txt_password = (EditText) findViewById(R.id.register_form_password);
		txt_repassword = (EditText) findViewById(R.id.register_form_repassword);
		bt_send = (Button) findViewById(R.id.register_form_bt_send);

		bt_send.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (txt_user.getText().length() <= 4) {
					showMessage(getString(R.string.user_required),
							Toast.LENGTH_SHORT);
					return;
				}
				if (txt_password.getText().length() < 6) {
					showMessage(getString(R.string.password_short),
							Toast.LENGTH_SHORT);
					return;
				}
				if (!(txt_repassword.getText().toString().equals(txt_password
						.getText().toString()))) {
					showMessage(getString(R.string.password_no_match),
							Toast.LENGTH_SHORT);
					return;
				}

				new CreateAccountTask().execute(RegisterActivity.this);
			}
		});

	}

	private void showMessage(String message, int duration) {
		if (toast != null) {
			toast.cancel();

			toast.setDuration(duration);
			toast.setText(message);
			toast.show();
		} else {
			toast = Toast.makeText(this, message, duration);
			toast.show();
		}

	}

	class CreateAccountTask extends AsyncTask<Activity, Void, Void> {

		private ProgressDialog progressDialog;
		private String response;
		private String out = "";

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(RegisterActivity.this);

			progressDialog.setMessage(getString(R.string.creating_account));

			progressDialog.show();

		}

		@Override
		protected Void doInBackground(Activity... param) {
			try {
				String url = Constants.SERVER_NAME+"?action=CreateAccount";
				WebService webService = new WebService(url);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user", txt_user.getText()
						.toString()));
				String password = CriptoUtils.encode(txt_password.getText()
						.toString());
				params.add(new BasicNameValuePair("password", password));
				params.add(new BasicNameValuePair("twitterId", Long
						.toString(twitterId)));
				params.add(new BasicNameValuePair("token", token));
				params.add(new BasicNameValuePair("tokenSecret", tokenSecret));
				
				response = webService.doPost("", params);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			try {
				JSONObject json = new JSONObject(response);
				int action_status = json.getInt("action_status");
				if (Menssage.SUCCESS == action_status) {
					out = getString(R.string.create_acc_succes);
				} else {
					out = getString(R.string.create_acc_erro);
					JSONArray erros = json.getJSONArray("erros");
					for (int i = 0; i < erros.length(); i++) {
						if (erros.getInt(i) == Menssage.TWITTER_USED)
							out += "\n" + getString(R.string.twitter_used);
						else if (erros.getInt(i) == Menssage.USER_USED)
							out += "\n" + getString(R.string.user_used);
					}
				}
			} catch (JSONException e) {

			}

			progressDialog.dismiss();

			Dialog d = new AlertDialog.Builder(RegisterActivity.this)
					.setTitle("Message")
					.setMessage(out)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									if (out.equals(getString(R.string.create_acc_succes)))
										RegisterActivity.this.finish();
								}
							}).create();
			d.show();

		}

	}

}
