package com.twittemarkup.view;

import java.net.URL;
import java.util.Vector;

import twitter4j.ResponseList;
import twitter4j.Status;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.twittemarkup.app.R;
import com.twittemarkup.model.Marcador;
import com.twittemarkup.utils.ImageUtils;

public class MarkupView {

	private Context ctx;
	private ScrollView scrollView;
	private LinearLayout list_twittes;
	private Vector<Status> tweets = new Vector<Status>();
	private Marcador marcador;
	
	public MarkupView(Marcador m, Context ctx) {
		marcador = m;

		this.ctx = ctx;
		scrollView = new ScrollView(ctx);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		scrollView.setScrollContainer(true);
		list_twittes = new LinearLayout(ctx);
		list_twittes.setOrientation(LinearLayout.VERTICAL);
		list_twittes.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		scrollView.addView(list_twittes);
	}
	
	public void updateTwittes(ResponseList<Status> list, boolean firstGet) {
		if (marcador == null) {
			for (twitter4j.Status status : list) {

				if (!tweets.contains(status) && firstGet)
					tweets.add(0, status);
				else if (!tweets.contains(status) && !firstGet)
					tweets.add(status);
			}
			list_twittes.removeAllViews();

			LayoutInflater inflater = (LayoutInflater) ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			for (Status status : tweets) {
				View row = inflater.inflate(R.layout.twitte_row, null);

				
				ImageView img = (ImageView) row.findViewById(R.id.profile_img);
				TextView screenName = (TextView) row
						.findViewById(R.id.screen_name);
				TextView time = (TextView) row.findViewById(R.id.time);
				TextView tweetText = (TextView) row.findViewById(R.id.twitte);
				
				
				URL imageURL=status.getUser().getProfileImageUrlHttps();
				Bitmap imgBit=ImageUtils.imageCache.get(imageURL);
				if(imgBit==null){
					imgBit=ImageUtils.loadProfileImages(imageURL,ctx);
					ImageUtils.imageCache.put(imageURL, imgBit);
					img.setImageBitmap(imgBit);
				}else{
					img.setImageBitmap(imgBit);
				}
				screenName.setText(status.getUser().getScreenName());
				time.setText("now");
				tweetText.setText(status.getText());

				list_twittes.addView(row, 0);

			}
			
		}
	}
	
	public ScrollView getScrollView() {
		return scrollView;
	}
	public void setScrollView(ScrollView scrollView) {
		this.scrollView = scrollView;
	}
	public LinearLayout getList_twittes() {
		return list_twittes;
	}
	public void setList_twittes(LinearLayout list_twittes) {
		this.list_twittes = list_twittes;
	}
	public Marcador getMarcador() {
		return marcador;
	}
	public void setMarcador(Marcador marcador) {
		this.marcador = marcador;
	}
	
}
