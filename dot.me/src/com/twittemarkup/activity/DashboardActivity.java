
package com.twittemarkup.activity;

import java.util.Vector;

import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.twittemarkup.app.R;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.bd.DataBase;
import com.twittemarkup.model.bd.Facade;
import com.twittemarkup.utils.Item;

public class DashboardActivity extends Activity {

	public static boolean running = false;
	private Button bt_timeline;
	private Button bt_marcador;
	private Button bt_logout;
	private Button bt_tt;
	private Button bt_option;
	private String op_new_markup, op_edit_markup, op_remove_markup,op_manage_blacklist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		op_edit_markup = getString(R.string.markup_edit);
		op_new_markup = getString(R.string.new_markup);
		op_remove_markup = getString(R.string.markup_remove);
		op_manage_blacklist=getString(R.string.manage_black_list);
		Facade.destroy();
		running = true;
		DataBase.start(this);

		Vector<Account> users = Facade.getInstance(this).lastSavedSession();

		if (users.isEmpty()) {
			Intent intent = new Intent(this, AddSocialAccount.class);
			finish();
			startActivity(intent);
			return;
		} else {
			for(Account acc:users){
				Account.addUser(acc);
			}
		}
		setContentView(R.layout.dashboard);

		bt_timeline = (Button) findViewById(R.id.dashboard_bt_timeline);
		bt_marcador = (Button) findViewById(R.id.dashboard_bt_markup);
		bt_logout = (Button) findViewById(R.id.dashboard_bt_logout);
		bt_tt = (Button) findViewById(R.id.dashboard_bt_tt);
		bt_option = (Button) findViewById(R.id.dashboard_bt_config);

		bt_timeline.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(DashboardActivity.this,
						TimelineActivity.class);

				startActivity(intent);
				
			}

		});
		bt_marcador.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				final Item[] items = {
						new Item(op_manage_blacklist, android.R.drawable.ic_menu_agenda),
						new Item(op_new_markup, android.R.drawable.ic_menu_add),
						new Item(op_edit_markup,
								android.R.drawable.ic_menu_edit),
						new Item(op_remove_markup,
								android.R.drawable.ic_menu_delete)

				};

				ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(
						DashboardActivity.this,
						android.R.layout.select_dialog_item,
						android.R.id.text1, items) {
					public View getView(int position, View convertView,
							ViewGroup parent) {
						// User super class to create the View
						View v = super.getView(position, convertView, parent);
						TextView tv = (TextView) v
								.findViewById(android.R.id.text1);

						// Put the image on the TextView
						tv.setCompoundDrawablesWithIntrinsicBounds(
								items[position].icon, 0, 0, 0);

						// Add margin between image and text (support various
						// screen densities)
						int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
						tv.setCompoundDrawablePadding(dp5);

						return v;
					}
				};
				;

				/*
				 * ArrayList<HashMap<String, Object>> lista=new
				 * ArrayList<HashMap<String,Object>>(); HashMap<String, Object>
				 * newMarkup=new HashMap<String, Object>();
				 * newMarkup.put("image", R.drawable.plus);
				 * newMarkup.put("op_text", op_new_markup);
				 * 
				 * lista.add(newMarkup);
				 * 
				 * HashMap<String, Object> editMarkup=new HashMap<String,
				 * Object>(); newMarkup.put("image", R.drawable.edit);
				 * newMarkup.put("op_text", op_new_markup);
				 * 
				 * lista.add(editMarkup);
				 * 
				 * SimpleAdapter adapter=new
				 * SimpleAdapter(DashboardActivity.this, lista,
				 * R.layout.markup_option, new String[]{"image","op_text"}, new
				 * int[]{R.id.op_marcador_icon,R.id.op_marcador_txt});
				 */

				AlertDialog dialog = new AlertDialog.Builder(
						DashboardActivity.this)
						.setTitle(getString(R.string.dashboard_markup))
						.setAdapter(adapter,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int position) {
										Intent intent = null;
										if(position == 0){
											intent = new Intent(
													DashboardActivity.this,
													BlackListActivity.class);
										}else if (position == 1) {
											intent = new Intent(
													DashboardActivity.this,
													CreateMarkupActivity.class);
										} else if (position == 2) {
											intent = new Intent(
													DashboardActivity.this,
													MarcadoresListActivity.class);
										} else if (position == 3) {
											intent = new Intent(
													DashboardActivity.this,
													MarcadoresListActivity.class);
											Bundle b = new Bundle();
											b.putBoolean("delete", true);
											intent.putExtras(b);
										}
										if (intent != null)
											startActivity(intent);
									}
								}).create();

				dialog.show();

			}

		});
		bt_logout.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				

				
			}

		});

		bt_tt.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(DashboardActivity.this,
						SearchActivity.class);
				Bundle b=new Bundle();
				b.putBoolean("open_timeline", true);
				intent.putExtras(b);
				startActivity(intent);

			}

		});
		
		bt_option.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(DashboardActivity.this, AddSocialAccount.class);
				
				startActivity(intent);
				finish();
				
			}
		});
	}

	@Override
	protected void onDestroy() {
		running = false;
		if (!LoginActivity.running)
			DataBase.getInstance(DashboardActivity.this).close();

		super.onDestroy();
	}

}
