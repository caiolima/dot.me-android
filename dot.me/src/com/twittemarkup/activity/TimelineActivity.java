package com.twittemarkup.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.twittemarkup.adapter.TimelinePagerAdapter;
import com.twittemarkup.app.R;
import com.twittemarkup.assynctask.UpdateAction;
import com.twittemarkup.command.AbstractCommand;
import com.twittemarkup.command.OpenCreateMarkupCommand;
import com.twittemarkup.command.OpenFacebookGroupCommand;
import com.twittemarkup.command.OpenSearchCommand;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.CollumnConfig;
import com.twittemarkup.model.FacebookGroup;
import com.twittemarkup.model.Marcador;
import com.twittemarkup.model.Mensagem;
import com.twittemarkup.model.bd.Facade;
import com.twittemarkup.utils.Constants;
import com.twittemarkup.utils.Item;
import com.twittemarkup.utils.SubjectMessage;
import com.twittemarkup.view.AbstractColumn;
import com.twittemarkup.view.FacebookFeedsColumn;
import com.twittemarkup.view.FacebookGroupColumn;
import com.twittemarkup.command.OpenManageCollumnCommand;
import com.twittemarkup.view.MarkupColunm;
import com.twittemarkup.view.NotificationsCollumn;
import com.twittemarkup.view.SearchColumn;
import com.twittemarkup.view.TwitterFeedsCollumn;

public class TimelineActivity extends Activity {

	private static TimelineActivity current;
	private Vector<Account> users;
	private List<AbstractColumn> filters = new ArrayList<AbstractColumn>();
	private ViewPager view_flipper;
	public static Handler h = new Handler();
	private TextView currentTittle;
	private ImageButton bt_more, bt_send;
	private Vector<Mensagem> currentList;
	private AccessToken accessToken;
	private int curentFilterPosition;
	private Intent intent;
	private TimelinePagerAdapter adapter;
	private static SubjectMessage mSubject;
	private HashMap<AbstractColumn, Integer> hashSavedSelection = new HashMap<AbstractColumn, Integer>();
	private AbstractCommand currentCommand;
	
	
	public Vector<Mensagem> getCurrentList() {
		return currentList;
	}

	@Override
	protected void onResume() {
		super.onResume();
		current = this;

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.intent = intent;

		Uri uri = intent.getData();
		if (uri != null) {
			if (uri.getScheme().equals("add_column")) {
				if (uri.getHost().equals("facebook_group")) {
					String groupsAdded = uri.getQueryParameter("new_groups");
					if (!groupsAdded.equals("none")) {
						String[] groupsId = groupsAdded.split(" ");
						Facade facade = Facade.getInstance(this);
						for (String id : groupsId) {
							if (id.equals(""))
								continue;

							FacebookGroup group = facade
									.getOneFacebookGroup(id);
							if (group != null) {

								CollumnConfig confg = new CollumnConfig();
								confg.setType(CollumnConfig.FACEBOOK_GROUPS);
								JSONObject json = new JSONObject();
								try {
									json.put("id", group.getId());
									confg.setProprietes(json);
									json.put("name", group.getName());

									Facade.getInstance(TimelineActivity.this)
											.insert(confg);
								} catch (JSONException e) {

								}

								FacebookGroupColumn fgCollumn = new FacebookGroupColumn(
										this, group);
								fgCollumn.setConfig(confg);
								filters.add(fgCollumn);
								adapter.addView(fgCollumn.getScrollView());

							}

						}
						if (groupsId.length > 0)
							view_flipper.setCurrentItem(filters.size() - 1);
					}
				} else if (uri.getHost().equals("twitter_search")) {
					String searchContent = uri
							.getQueryParameter("search_value");

					CollumnConfig confg = new CollumnConfig();
					confg.setType(CollumnConfig.TWITTER_SEARCH);
					JSONObject json = new JSONObject();
					try {
						json.put("search", searchContent);
						json.put("name", searchContent);
						confg.setProprietes(json);

						Facade.getInstance(TimelineActivity.this).insert(confg);
					} catch (JSONException e) {

					}

					SearchColumn collumn = new SearchColumn(this, searchContent);
					filters.add(collumn);
					adapter.addView(collumn.getScrollView());
					view_flipper.setCurrentItem(filters.size() - 1);
					collumn.setConfig(confg);
					collumn.init();

				}
				return;
			} else if (uri.getScheme().equals("timeline")) {
				if (uri.getHost().equals("reload")) {
					finish();
					startActivity(new Intent(this,TimelineActivity.class));
					/*filters.clear();

					loadCollumns();
					currentTittle.setText(filters.get(curentFilterPosition)
							.getColumnTitle());
					try {
						adapter.notifyDataSetChanged();
						new InitViewTask().execute();
					} catch (Exception e) {
						finish();
						startActivity(new Intent(this,TimelineActivity.class));
					}*/

				}
			}
		}

		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				boolean columnAdded = b.getBoolean("collumnAdded");
				if (columnAdded) {
					Vector<String> searches = Facade.getInstance(current)
							.getAllSearches();
					SearchColumn s = new SearchColumn(this,
							searches.lastElement());

					adapter.addView(s.getScrollView());
					filters.add(s);
					view_flipper
							.setCurrentItem(view_flipper.getChildCount() - 1);

				}
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		current = null;

		for (AbstractColumn collumn : filters) {
			JSONObject obj = collumn.getConfig().getProprietes();
			obj.remove("top");
			obj.remove("scrollTo");
			try {
				int index = collumn.getScrollView().getFirstVisiblePosition();
				View v = collumn.getScrollView().getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();
				obj.put("top", top);
				obj.put("scrollTo", index);

				Facade.getInstance(this).update(collumn.getConfig());
			} catch (JSONException e) {

			}

		}

	}

	public void setCurrentList(Vector<Mensagem> currentList) {
		this.currentList = currentList;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		current = this;
		super.onCreate(savedInstanceState);

		mSubject = new SubjectMessage();

		this.setContentView(R.layout.timeline);

		curentFilterPosition = 0;
		users = Account.getLoggedUsers(TimelineActivity.this);

		view_flipper = (ViewPager) findViewById(R.id.coluna);
		currentTittle = (TextView) findViewById(R.id.category_name);
		bt_send = (ImageButton) findViewById(R.id.timeline_bt_tweet);
		bt_more = (ImageButton) findViewById(R.id.timeline_bt_more);

		bt_send.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				currentCommand.execute(TimelineActivity.this);
			}
		});

		bt_more.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				final Vector<AbstractCommand> actions = new Vector<AbstractCommand>();
				final Vector<Item> items = new Vector<Item>();
				if (Account.getFacebookAccount(TimelineActivity.this) != null) {
					items.add(new Item(getString(R.string.group_columns),
							android.R.drawable.ic_menu_my_calendar));
					actions.add(new OpenFacebookGroupCommand());
				}

				if (Account.getTwitterAccount(TimelineActivity.this) != null) {
					items.add(new Item(getString(R.string.new_search),
							android.R.drawable.ic_menu_search));
					actions.add(new OpenSearchCommand());
				}
				items.add(new Item(getString(R.string.new_markup),
						android.R.drawable.ic_menu_add));
				actions.add(new OpenCreateMarkupCommand());

				items.add(new Item(getString(R.string.manage_collumns),
						android.R.drawable.ic_menu_agenda));

				actions.add(new OpenManageCollumnCommand());

				if (filters.get(curentFilterPosition).isDeletable()) {

					items.add(new Item(getString(R.string.remove_column),
							android.R.drawable.ic_menu_delete));
					actions.add(new AbstractCommand() {

						@Override
						public void execute(Activity activity) {

							Facade.getInstance(TimelineActivity.this)
									.deleteCollum(curentFilterPosition);
							AbstractColumn collumn = filters
									.get(curentFilterPosition);
							collumn.deleteColumn();
							filters.remove(curentFilterPosition);

							adapter.removeViewAtPosition(curentFilterPosition);

							curentFilterPosition--;
							if(curentFilterPosition<0)
								curentFilterPosition=0;
							view_flipper.setCurrentItem(curentFilterPosition);
							
							adapter.notifyDataSetChanged();
							currentTittle.setText(filters.get(curentFilterPosition).getColumnTitle());
							int i=0;
							for(CollumnConfig config:Facade.getInstance(TimelineActivity.this).getAllConfig()){
								filters.get(i).setConfig(config);
								i++;
							} 
							/*
							 * try {
							 * view_flipper.removeViewAt(curentFilterPosition);
							 * } catch (NullPointerException e) { int
							 * lastCurrentFilterPosition = curentFilterPosition;
							 * view_flipper .setCurrentItem(curentFilterPosition
							 * - 1); view_flipper.removeView(filters.get(
							 * lastCurrentFilterPosition) .getScrollView()); }
							 * filters.get(curentFilterPosition).deleteColumn();
							 * filters.remove(curentFilterPosition);
							 * curentFilterPosition--;
							 * view_flipper.setCurrentItem
							 * (curentFilterPosition);
							 */

						}
					});

				}

				ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(
						TimelineActivity.this,
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
								items.get(position).icon, 0, 0, 0);

						// Add margin between image and text (support various
						// screen densities)
						int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
						tv.setCompoundDrawablePadding(dp5);

						return v;
					}
				};

				AlertDialog dialog = new AlertDialog.Builder(
						TimelineActivity.this)
						.setTitle(getString(R.string.manage_columns))
						.setAdapter(adapter,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int position) {

										actions.get(position).execute(
												TimelineActivity.this);

										/*
										 * Intent intent = null;
										 * 
										 * 
										 * 
										 * if (position == 1) {
										 * 
										 * } else if (position == 0) {
										 * 
										 * } else if (position == 2) { try {
										 * view_flipper
										 * .removeViewAt(curentFilterPosition);
										 * } catch (NullPointerException e) {
										 * int lastCurrentFilterPosition =
										 * curentFilterPosition; view_flipper
										 * .setCurrentPage(curentFilterPosition
										 * - 1); view_flipper
										 * .removeView(filters
										 * .get(lastCurrentFilterPosition)
										 * .getScrollView()); }
										 * filters.get(curentFilterPosition)
										 * .deleteColumn();
										 * filters.remove(curentFilterPosition);
										 * curentFilterPosition--; view_flipper
										 * .
										 * setCurrentPage(curentFilterPosition);
										 * }
										 */
									}
								}).create();

				dialog.show();

			}

		});

		adapter = new TimelinePagerAdapter(view_flipper);
		loadCollumns();

		/*
		 * if (Account.getTwitterAccount(this) != null) { AbstractColumn
		 * timeline = new TwitterFeedsCollumn(this); filters.add(timeline);
		 * currentTittle.setText(timeline.getColumnTitle());
		 * 
		 * adapter.addView(timeline.getScrollView());
		 * 
		 * Vector<String> searches = Facade.getInstance(current)
		 * .getAllSearches(); for (String search : searches) { SearchColumn s =
		 * new SearchColumn(this, search);
		 * UpdateAction.registerUpdateRequest(s); filters.add(s);
		 * adapter.addView(s.getScrollView());
		 * 
		 * }
		 * 
		 * }
		 * 
		 * if(Account.getFacebookAccount(this)!=null) { AbstractColumn timeline
		 * = new FacebookFeedsColumn(this); filters.add(timeline);
		 * 
		 * adapter.addView(timeline.getScrollView());
		 * 
		 * AbstractColumn notifi = new NotificationsCollumn(this);
		 * filters.add(notifi);
		 * 
		 * adapter.addView(notifi.getScrollView());
		 * 
		 * for (FacebookGroup fg : Facade.getInstance(this)
		 * .getAllFacebookGroups()) { AbstractColumn fgCollumn = new
		 * FacebookGroupColumn(this, fg); filters.add(fgCollumn);
		 * adapter.addView(fgCollumn.getScrollView()); } }
		 * 
		 * for (Marcador m : Account.getMarcadores(this)) { if (m.isEnnabled()
		 * != 1) continue;
		 * 
		 * MarkupColunm column = new MarkupColunm(m, this);
		 * mSubject.registerObserver(column); filters.add(column);
		 * adapter.addView(column.getScrollView()); }
		 */

		view_flipper
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

					@Override
					public void onPageSelected(int position) {
						curentFilterPosition = position;
						AbstractColumn column = filters.get(position);
						currentTittle.setText(column.getColumnTitle());
						currentCommand=column.getCommand();
						if(currentCommand==null){
							bt_send.setVisibility(View.INVISIBLE);
						}else{
							bt_send.setVisibility(View.VISIBLE);
						}

					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {

					}

					@Override
					public void onPageScrollStateChanged(int arg0) {
						// TODO Auto-generated method stub

					}
				});

		/*
		 * view_flipper.addOnScrollListener(new
		 * HorizontalPager.OnScrollListener() {
		 * 
		 * @Override public void onViewScrollFinished(int currentPage) {
		 * curentFilterPosition = currentPage; AbstractColumn column =
		 * filters.get(currentPage);
		 * currentTittle.setText(column.getColumnTitle()); }
		 * 
		 * @Override public void onScroll(int scrollX) {
		 * 
		 * } });
		 */

		currentTittle.setText(filters.get(0).getColumnTitle());

		new InitViewTask().execute();
	}

	private void loadCollumns() {
		
		int cont=0;
		for (CollumnConfig config : Facade.getInstance(this).getAllConfig()) {
			
				
			AbstractColumn column = null;
			if (config.getType().equals(CollumnConfig.FACEBOOK_COLLUMN)) {
				column = new FacebookFeedsColumn(this);
				filters.add(column);

				/*
				 * Vector<Mensagem> list =
				 * Facade.getInstance(TimelineActivity.this)
				 * .getMensagemOf(Mensagem.TIPO_NEWS_FEEDS);
				 * 
				 * timeline.updateTwittes(list, true);
				 */

				adapter.addView(column.getScrollView());
			} else if (config.getType().equals(CollumnConfig.TWITTER_COLLUMN)) {
				column = new TwitterFeedsCollumn(this);
				filters.add(column);

				adapter.addView(column.getScrollView());
			} else if (config.getType().equals(CollumnConfig.NOTIFICATION_FACE)) {
				column = new NotificationsCollumn(this);
				filters.add(column);

				adapter.addView(column.getScrollView());
			} else if (config.getType().equals(CollumnConfig.FACEBOOK_GROUPS)) {
				try {
					String id = config.getProprietes().getString("id");
					FacebookGroup fGroup = Facade.getInstance(this)
							.getOneFacebookGroup(id);

					column = new FacebookGroupColumn(this, fGroup);
					filters.add(column);
					adapter.addView(column.getScrollView());

				} catch (JSONException e) {

				}
			} else if (config.getType().equals(CollumnConfig.MARKUP)) {
				try {
					int id = config.getProprietes().getInt("id");
					Marcador m = Facade.getInstance(this).getOneMarcador(id);

					if (m.isEnnabled() != 1)
						continue;

					column = new MarkupColunm(m, this);
					mSubject.registerObserver((MarkupColunm) column);
					filters.add(column);
					adapter.addView(column.getScrollView());
				} catch (JSONException e) {

				}
			} else if (config.getType().equals(CollumnConfig.TWITTER_SEARCH)) {
				try {
					String search = config.getProprietes().getString("search");

					column = new SearchColumn(this, search);
					UpdateAction.registerUpdateRequest((SearchColumn) column);
					filters.add(column);
					adapter.addView(column.getScrollView());

				} catch (JSONException e) {

				}
			}
			try {
				hashSavedSelection.put(column,
						config.getProprietes().getInt("lastSelection"));
			} catch (JSONException e) {
				hashSavedSelection.put(column, 0);
			}
			column.setConfig(config);

			cont++;
			if(cont==1){
				currentCommand=column.getCommand();
			}
		}
		view_flipper.setAdapter(adapter);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.COLUMN_SEARCH_RESULT) {
			if (resultCode == RESULT_OK) {
				Bundle b = data.getExtras();
				String new_search = b.getString("search_name");

				SearchColumn s = new SearchColumn(this, new_search);
				UpdateAction.registerUpdateRequest(s);
				filters.add(s);
				adapter.addView(s.getScrollView());

				view_flipper.setCurrentItem(adapter.getCount() - 1);
			}
		}

	}

	public void refreshFilters(final boolean top) {

		h.post(new Runnable() {

			@Override
			public void run() {
				for (AbstractColumn filter : filters) {
					if (filter instanceof MarkupColunm) {
						filter.updateTwittes(currentList, top);
						/*
						 * Separator mainSeparator = filters.get(0).getAdapter()
						 * .getCurrentSeparator(); if (mainSeparator != null &&
						 * filter instanceof MarkupColunm)
						 * filter.getAdapter().addSeparator(mainSeparator);
						 */
					}
				}

				/*
				 * Intent intent=new Intent("com.twittemarkup.download.images");
				 * startService(intent);
				 */
				// view_flipper.refreshDrawableState();

			}
		});

	}

	private void updateTweets(boolean top) {

		new UpdateTweets().execute(top);
		/*
		 * h.post(new Runnable() {
		 * 
		 * @Override public void run() { for (FilterView filter : filters) {
		 * filter.updateTwittes(list, firstGet); }
		 * view_flipper.refreshDrawableState();
		 * 
		 * } });
		 */

		/*
		 * for (twitter4j.Status status : list) {
		 * 
		 * if (!tweets.contains(status)&&firstGet) tweets.add(0,status); else if
		 * (!tweets.contains(status)&&!firstGet) tweets.add(status); }
		 * list_twittes.removeAllViews();
		 * 
		 * if (connected) { LayoutInflater inflater = (LayoutInflater)
		 * getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		 * 
		 * for (Status status : tweets) { View row =
		 * inflater.inflate(R.layout.twitte_row, null);
		 * 
		 * ImageView img = (ImageView) row.findViewById(R.id.profile_img);
		 * TextView screenName = (TextView) row .findViewById(R.id.screen_name);
		 * TextView time = (TextView) row.findViewById(R.id.time); TextView
		 * tweetText = (TextView) row.findViewById(R.id.twitte);
		 * 
		 * img.setImageResource(R.drawable.icon);
		 * screenName.setText(status.getUser().getScreenName());
		 * time.setText("now"); tweetText.setText(status.getText());
		 * 
		 * list_twittes.addView(row,0);
		 * 
		 * } } list_twittes.refreshDrawableState();
		 */

	}

	public static TimelineActivity getCurrent() {
		return current;
	}

	public static void setCurrent(TimelineActivity current) {
		TimelineActivity.current = current;
	}

	/*
	 * @Override protected void onResume() { super.onResume();
	 * 
	 * }
	 * 
	 * @Override protected void onPause() { super.onPause();
	 * unregisterReceiver(uiReciever); unregisterReceiver(mesageReceiver); }
	 */

	public static SubjectMessage getmSubject() {
		return mSubject;
	}

	public static void setmSubject(SubjectMessage mSubject) {
		TimelineActivity.mSubject = mSubject;
	}

	class InitViewTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(TimelineActivity.this);

			progressDialog
					.setMessage(getString(R.string.get_basic_information));

			try {
				progressDialog.show();
			} catch (Exception e) {
				// TODO: handle exception
			}

		}

		@Override
		protected Void doInBackground(Void... strings) {
			/*
			 * try {
			 * 
			 * refreshFilters(true);
			 * 
			 * } catch (IllegalStateException e) {
			 * 
			 * }
			 */

			for (AbstractColumn collumn : filters) {
				collumn.init();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				for (AbstractColumn collumn : filters) {
					int top = collumn.getConfig().getProprietes().getInt("top");
					int scrollTo = collumn.getConfig().getProprietes()
							.getInt("scrollTo");

					collumn.getScrollView().setSelectionFromTop(scrollTo, top);
				}

				progressDialog.dismiss();

			} catch (JSONException e) {
				// TODO: handle exception
			}
		}

	}

	class UpdateTweets extends AsyncTask<Boolean, Void, Void> {

		@Override
		protected Void doInBackground(Boolean... args) {
			refreshFilters(args[0]);
			/*
			 * try { AccessToken accessToken=new AccessToken(user.getToken(),
			 * user.getTokenSecret()); ResponseList<twitter4j.Status>
			 * list=TwitterUtils.getTwitter(accessToken).getHomeTimeline();
			 * ImageUtils.loadImages(list); Vector<Mensagem> mensagens=new
			 * Vector<Mensagem>(); for(twitter4j.Status status:list){ Mensagem
			 * m=Mensagem.creteFromTwitterStatus(status); Facade
			 * facade=Facade.getInstance(TimelineActivity.this);
			 * if(!facade.exsistsStatus(m.getIdMensagem())){ facade.insert(m);
			 * mensagens.add(m); }
			 * 
			 * } updateTweets(mensagens); firstGet = false;
			 * 
			 * } catch (TwitterException e) {
			 * 
			 * }
			 */

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
		}

	}

	public static class UpdateMensagesBroadcastReciever extends
			BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean top = false;
			if (intent != null) {
				Bundle b = intent.getExtras();
				if (b != null) {
					top = b.getBoolean("top");
				}
			}
			if (TimelineActivity.current != null)
				TimelineActivity.current.updateTweets(top);
		}

	}

}