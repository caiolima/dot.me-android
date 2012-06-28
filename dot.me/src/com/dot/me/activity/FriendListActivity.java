package com.dot.me.activity;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;

import com.dot.me.app.R;
import com.dot.me.model.UsuarioTwitter;
import com.dot.me.utils.TwitterUtils;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class FriendListActivity extends Activity{

	private ArrayAdapter<Object> list_adapter;
	private UsuarioTwitter user;
	private Button bt_ok,bt_cancel;
	private ListView lst_friends;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_list);
		
		//user=UsuarioTwitter.getCurrent(this);
		
		bt_ok=(Button) findViewById(R.id.friendlst_bt_ok);
		bt_cancel=(Button) findViewById(R.id.friendlst_bt_cancel);
		lst_friends=(ListView) findViewById(R.id.friendlst_list);
		Twitter twitter=TwitterUtils.getTwitter(new AccessToken(user.getToken(), user.getTokenSecret()));
		try {
			long cursor=-2;
			IDs ids=twitter.getFriendsIDs(user.getTwitterId(), 10);
			 do {
	              
	                ids = twitter.getFriendsIDs(cursor);
	                for(long id:ids.getIDs()){
	                	User user=twitter.showUser(id);
	                	user.getName();
	                }
	            } while ((cursor = ids.getNextCursor()) != 0);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	

}
