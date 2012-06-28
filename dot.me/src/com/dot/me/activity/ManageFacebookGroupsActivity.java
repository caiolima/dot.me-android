package com.dot.me.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dot.me.adapter.FacebookGroupAdapter;
import com.dot.me.app.R;
import com.dot.me.exceptions.LostUserAccessException;
import com.dot.me.model.Account;
import com.dot.me.model.FacebookAccount;
import com.dot.me.model.FacebookGroup;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.FacebookUtils;
import com.facebook.android.Facebook;

public class ManageFacebookGroupsActivity extends Activity {

	//private Button bt_ok;
	private FacebookGroupAdapter adapter;
	private View load_view;
	private ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.group_selector_layout);

		//bt_ok = (Button) findViewById(R.id.facebook_groups_bt_ok);
		list = (ListView) findViewById(R.id.facebook_groups_list);

		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		load_view = mInflater.inflate(R.layout.loading, null);
		// load_view.setLayoutParams(new
		// LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		list.addHeaderView(load_view);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				Facade facade = Facade
						.getInstance(ManageFacebookGroupsActivity.this);
				FacebookGroup group=adapter.getItem(position);
				if (!facade.existsGroup(group.getId())) {
					facade.insert(group);
					
				}else{
					Toast.makeText(ManageFacebookGroupsActivity.this, getString(R.string.facebook_group_added), Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("add_column://facebook_group?new_groups="
								+ group.getId()));
				startActivity(intent);
				finish();
				
			}
		});
		/*bt_ok.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Facade facade = Facade
						.getInstance(ManageFacebookGroupsActivity.this);
				facade.deleteAllGroups();
				String newGroups = "none";
				for (FacebookGroup group : adapter.getSelectds()) {
					if (newGroups.equals("none"))
						newGroups = "";
					if (!facade.existsGroup(group.getId())) {
						facade.insert(group);
						newGroups += group.getId() + "%20";
					}
				}
				// String
				// uri=Uri.encode("add_column://facebook_group?new_groups="+newGroups);

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("add_column://facebook_group?new_groups="
								+ newGroups));
				;
				startActivity(intent);
				finish();
			}
		});*/

		adapter = new FacebookGroupAdapter(this);

		list.setAdapter(adapter);

		FacebookAccount account = Account.getFacebookAccount(this);
		if (account != null) {
			try {
				new FacebookGroupGetterTask(FacebookUtils.getFacebook(this,
						account)).execute();
			} catch (LostUserAccessException e) {

			}
		} else {
			finish();
		}

	}

	private class FacebookGroupGetterTask extends AsyncTask<Void, Void, Void> {

		private Facebook facebook;
		private List<FacebookGroup> goups = new ArrayList<FacebookGroup>();

		public FacebookGroupGetterTask(Facebook facebook) {
			this.facebook = facebook;
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				Bundle bundle = new Bundle();
				bundle.putString("fields", "picture,name,description");
				// bundle.putString("limit", "5000");
				String response = facebook.request("me/groups", bundle);
				JSONObject responseJSON = new JSONObject(response);
				JSONArray array = responseJSON.getJSONArray("data");
				for (int i = 0; i < array.length(); i++) {
					FacebookGroup group = FacebookGroup.createFromJSON(array
							.getJSONObject(i));
					if (group != null)
						goups.add(group);
				}
			} catch (MalformedURLException e) {

			} catch (IOException e) {

			} catch (JSONException e) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			list.removeHeaderView(load_view);
			for (FacebookGroup group : goups) {
				adapter.addItem(group);
			}

		}

	}

}
