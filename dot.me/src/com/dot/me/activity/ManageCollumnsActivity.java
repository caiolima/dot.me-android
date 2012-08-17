package com.dot.me.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.adapter.DragNDropAdapter;
import com.dot.me.app.R;
import com.dot.me.model.Account;
import com.dot.me.model.CollumnConfig;
import com.dot.me.model.bd.Facade;
import com.dot.me.view.DragListener;
import com.dot.me.view.DragNDropListView;
import com.dot.me.view.DropListener;
import com.dot.me.view.RemoveListener;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
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

public class ManageCollumnsActivity extends TrackedActivity {

	private Button bt_ok, bt_reset_collumns;
	private DragNDropListView lst_drag;
	private DragNDropAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.manage_collumn);

		bt_ok = (Button) findViewById(R.id.manage_collumn_bt_ok);
		bt_reset_collumns = (Button) findViewById(R.id.bt_reset_collumns);
		lst_drag = (DragNDropListView) findViewById(R.id.manage_collumn_lst);

		ArrayList<CollumnConfig> list = Facade.getInstance(this).getAllConfig();

		adapter = new DragNDropAdapter(this, new int[] { R.layout.dragitem },
				new int[] { R.id.TextView01 }, list);

		lst_drag.setAdapter(adapter);

		lst_drag.setDragListener(mDragListener);
		lst_drag.setDropListener(mDropListener);
		lst_drag.setRemoveListener(mRemoveListener);

		bt_ok.setOnClickListener(mClickListener);
		bt_reset_collumns.setOnClickListener(onResetCollumnsClick);

	}

	private View.OnClickListener mClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Facade.getInstance(ManageCollumnsActivity.this)
					.deleteAllCollumnConfig();

			for (int i = 0; i < adapter.getCount(); i++) {
				CollumnConfig config = adapter.getItem(i);

				Facade.getInstance(ManageCollumnsActivity.this).insert(config);
			}

			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("timeline://reload"));

			startActivity(intent);

			finish();

		}
	};

	private DropListener mDropListener = new DropListener() {
		public void onDrop(int from, int to) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof DragNDropAdapter) {
				((DragNDropAdapter) adapter).onDrop(from, to);
				getListView().invalidateViews();
			}
		}
	};

	private RemoveListener mRemoveListener = new RemoveListener() {
		public void onRemove(int which) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof DragNDropAdapter) {
				((DragNDropAdapter) adapter).onRemove(which);
				getListView().invalidateViews();
			}
		}
	};

	private DragListener mDragListener = new DragListener() {

		int backgroundColor = Color.WHITE;
		int defaultBackgroundColor;

		public void onDrag(int x, int y, ListView listView) {
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) {
			itemView.setVisibility(View.GONE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.ImageView01);
			if (iv != null)
				iv.setVisibility(View.GONE);
		}

		public void onStopDrag(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.ImageView01);
			if (iv != null)
				iv.setVisibility(View.VISIBLE);
		}

	};

	protected ListAdapter getListAdapter() {

		return adapter;
	}

	protected AbsListView getListView() {
		return this.lst_drag;
	}

	private View.OnClickListener onResetCollumnsClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog dialog = new AlertDialog.Builder(
					ManageCollumnsActivity.this)
					.setTitle(getString(R.string.warning)).setMessage(getString(R.string.reset_collumns_message))
					.create();

			dialog.setButton(DialogInterface.BUTTON_POSITIVE,
					getString(R.string.yes),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							resetColumns();

						}
					});
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					
				}
			});
			dialog.show();

		}
	};

	private void resetColumns() {
		Context ctx = ManageCollumnsActivity.this;
		Facade facade = Facade.getInstance(ctx);

		facade.deleteAllCollumnConfig();
		facade.deleteAllGroups();
		facade.deleteAllSearch();
		if (Account.getFacebookAccount(ctx) != null) {

			CollumnConfig collumnConfig = new CollumnConfig();

			JSONObject prop = new JSONObject();
			try {
				prop.put("name", getString(R.string.news_fedd));
				collumnConfig.setProprietes(prop);
				collumnConfig.setType(CollumnConfig.FACEBOOK_COLLUMN);

				facade.insert(collumnConfig);
			} catch (JSONException e) {

			}

			collumnConfig = new CollumnConfig();

			JSONObject prop2 = new JSONObject();
			try {
				prop2.put("name", "@me");
				collumnConfig.setProprietes(prop2);

				collumnConfig.setType(CollumnConfig.ME);

				if (!facade.existsCollumnType(CollumnConfig.ME))
					facade.insert(collumnConfig);
			} catch (JSONException e) {

			}

		}

		if (Account.getTwitterAccount(ctx) != null) {
			CollumnConfig collumnConfig = new CollumnConfig();
			JSONObject prop = new JSONObject();
			try {
				prop.put("name", getString(R.string.main_column_name));
			} catch (JSONException e) {

			}
			collumnConfig.setProprietes(prop);
			collumnConfig.setType(CollumnConfig.TWITTER_COLLUMN);

			facade.insert(collumnConfig);

			collumnConfig = new CollumnConfig();
			try {
				JSONObject prop2 = new JSONObject();

				prop2.put("name", "@me");

				collumnConfig.setProprietes(prop2);

				collumnConfig.setType(CollumnConfig.ME);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (!facade.existsCollumnType(CollumnConfig.ME))
				facade.insert(collumnConfig);
		}

		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("timeline://reload"));

		startActivity(intent);

		finish();
	}
}
