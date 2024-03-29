package com.dot.me.activity;

import java.util.Vector;

import com.dot.me.app.R;
import com.dot.me.model.bd.Facade;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.apps.analytics.easytracking.TrackedActivity;

import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class BlackListActivity extends TrackedActivity {

	private Button bt_add, bt_delete;
	private ListView lst_words;
	private EditText txt_word;
	private Vector<String> selectedWords = new Vector<String>();
	private ArrayAdapter<String> adapter;
	private GoogleAnalyticsTracker tracker=GoogleAnalyticsTracker.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.black_list);

		bt_add = (Button) findViewById(R.id.blacklist_bt_add);
		bt_delete = (Button) findViewById(R.id.blacklist_bt_del);
		lst_words = (ListView) findViewById(R.id.blacklist_lst_words);
		txt_word = (EditText) findViewById(R.id.blacklist_txt_word);

		final Facade facade = Facade.getInstance(this);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_checked,
				facade.getAllWordsInBlackList());

		lst_words.setAdapter(adapter);
		lst_words.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> ap, View view, int position,
					long id) {

				String word = adapter.getItem(position);
				CheckedTextView chk_view = (CheckedTextView) view;
				if (chk_view.isChecked()) {
					selectedWords.remove(word);

				} else {
					selectedWords.add(word);

				}
				chk_view.toggle();
			}
		});

		bt_delete.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedWords.size() < 0) {
					Toast.makeText(BlackListActivity.this,
							getString(R.string.no_items_selected),
							Toast.LENGTH_SHORT).show();
				}

				for (String word : selectedWords) {
					facade.deleteInBlackList(word);
					adapter.remove(word);
					tracker.trackEvent("Filter Events", "blacklist", "delete_word", selectedWords.size());
				}

				if (adapter.getCount() < 1) {

					lst_words.setVisibility(View.INVISIBLE);
				}

			}
		});
		bt_add.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String word = txt_word.getText().toString().toLowerCase().trim();
					int count = adapter.getCount();
					if (word.length() > 0) {

						facade.insertInBlackList(word);
						tracker.trackEvent("Filter Events", "blacklist", "added_word", 1);

						adapter.add(word);
						txt_word.setText("");
						if (count == 0) {
							lst_words.setVisibility(View.VISIBLE);
						}
					} else {
						Toast.makeText(BlackListActivity.this,
								getString(R.string.type_word_adivice),
								Toast.LENGTH_SHORT).show();
					}
				} catch (SQLException e) {
					Toast.makeText(BlackListActivity.this,
							getString(R.string.word_added),
							Toast.LENGTH_SHORT).show();
				}

			}
		});

		if (adapter.getCount() > 0) {
			lst_words.setVisibility(View.VISIBLE);
		}

	}

}
