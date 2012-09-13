package com.dot.me.adapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.app.R;
import com.dot.me.assynctask.TwitterImageDownloadTask;
import com.dot.me.model.Mensagem;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.BlackListUtils;
import com.dot.me.utils.ImageUtils;
import com.dot.me.utils.MessageObserver;
import com.dot.me.utils.PictureInfo;
import com.dot.me.utils.SubjectMessage;
import com.dot.me.utils.TwitterUtils;
import com.dot.me.view.AbstractColumn;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FeddAdapter extends BaseAdapter {

	private Vector<Mensagem> list = new Vector<Mensagem>();
	private HashMap<String, Mensagem> hash = new HashMap<String, Mensagem>();
	private Context ctx;
	private LayoutInflater mInflater;
	private AbstractColumn column;
	private int selection = 0;

	public FeddAdapter(Context ctx, AbstractColumn column) {
		this.ctx = ctx;
		mInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.column = column;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Log.w("dot.me", "Getting view at position " + position);
		Mensagem m = (Mensagem) getItem(position);
		selection = position;
		if (m != null) {
			convertView = mInflater.inflate(R.layout.feed_row, null);

			LinearLayout lt = (LinearLayout) convertView
					.findViewById(R.id.twitte_row_view);
			lt.setBackgroundResource(R.color.backgorung);

			LinearLayout container = (LinearLayout) convertView
					.findViewById(R.id.text_container);
			container.setBackgroundResource(R.drawable.timeline_shape);

			ViewHolder holder = new ViewHolder();
			holder.txt_nome = (TextView) convertView
					.findViewById(R.id.screen_name);
			holder.img_avatar = (ImageView) convertView
					.findViewById(R.id.profile_img);
			holder.data = (TextView) convertView.findViewById(R.id.time);
			holder.txt_texto = (TextView) convertView.findViewById(R.id.twitte);
			View v = convertView.findViewById(R.id.line);

			v.setBackgroundResource(R.color.light_blue);

			// v.setVisibility(View.GONE);
			convertView.setTag(holder);// Tag que serve para identificar a
			// view
			LinearLayout linearLayout = (LinearLayout) convertView
					.findViewById(R.id.tweet_row_layout);

			if (m.getTipo() == Mensagem.TIPO_NEWS_FEEDS
					|| m.getTipo() == Mensagem.TIPO_FACEBOOK_GROUP) {
				holder.createAndFillFacebookMessage(convertView, m);
				/*
				 * linearLayout.setOnClickListener(new View.OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { Intent intent = new
				 * Intent(ctx, FacebookMessageActivity.class); Bundle b = new
				 * Bundle(); b.putString("idMessage", m.getIdMensagem());
				 * b.putInt("type", m.getTipo()); intent.putExtras(b);
				 * 
				 * ctx.startActivity(intent); } });
				 */
			} else {

				/*
				 * linearLayout.setOnClickListener(new View.OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { Intent intent = new
				 * Intent(ctx, MessageInfoActivity.class); Bundle b = new
				 * Bundle(); b.putString("idMessage", m.getIdMensagem());
				 * b.putInt("type", m.getTipo()); intent.putExtras(b);
				 * 
				 * ctx.startActivity(intent); } });
				 */
			}

			holder.preencheLayout(m);
		}

		return convertView;
	}

	public void addItem(int position, Mensagem o) {
		if (o instanceof Mensagem) {
			Mensagem m = o;
			if (messageWasAdded(m)
					|| (!BlackListUtils.validateMessage(Facade.getInstance(ctx)
							.getAllWordsInBlackList(), m)))
				return;

		}

		if (!(column instanceof MessageObserver) && (o.isValidToFilter())) {
			SubjectMessage subject = TimelineActivity.getmSubject();
			if (subject != null)
				subject.notifyMessageAddedObservers(o);

		}

		list.add(position, o);
		hash.put(o.getIdMensagem() + "_" + o.getTipo(), o);
		notifyDataSetChanged();
	}

	public void addItem(Mensagem o) {

		if (o instanceof Mensagem) {
			Mensagem m = o;
			Vector<String> blacklist = Facade.getInstance(ctx)
					.getAllWordsInBlackList();
			if (messageWasAdded(m)
					|| (!BlackListUtils.validateMessage(blacklist, m)))
				return;

		}

		if ((!(column instanceof MessageObserver) && (o.isValidToFilter()))
				|| (column.getColumnTitle().equals(ctx
						.getString(R.string.main_column_name)))) {
			SubjectMessage subject = TimelineActivity.getmSubject();
			if (subject != null)
				subject.notifyMessageAddedObservers(o);

		}
		hash.put(o.getIdMensagem() + "_" + o.getTipo(), o);
		list.add(o);

		notifyDataSetChanged();
	}

	private boolean messageWasAdded(Mensagem m) {
		for (Mensagem message : list) {
			if (message.getIdMensagem().equals(m.getIdMensagem())
					&& message.getTipo() == m.getTipo()) {
				return true;
			}
		}
		return false;
	}

	public void sort() {
		Collections.sort(list);
		notifyDataSetChanged();
	}

	public void removeAll() {
		list.removeAllElements();
		notifyDataSetChanged();
	}

	public class ViewHolder {

		public TextView txt_nome;
		public ImageView img_avatar;
		public TextView data;
		public TextView txt_texto;
		public TextView txt_like_count;
		public TextView txt_comments_count;
		public TextView txt_caption;
		public ImageView img_preview, img_thumbnails;
		public TextView txt_link_details;

		public LinearLayout lt_img_preview, lt_like, lt_comments, lt_caption;
		private Handler h = new Handler();

		// MŽtodo em que os campos da view s‹o preenchidos com o conteudo da
		// mensagem
		public void preencheLayout(Mensagem m) {
			String mensagem = m.getMensagem();
			if (mensagem.length() > 150) {
				mensagem = mensagem.substring(0, 140) + "...";
			}
			txt_nome.setText(m.getNome_usuario());
			// addImageViewURL(m.getImagePath(), img_avatar);
			Bitmap bMap = ImageUtils.imageCache.get(m.getImagePath());
			if (bMap == null) {
				img_avatar.setImageBitmap(null);
				// ImageUtils.imageLoadMap.put(img_avatar, m.getImagePath());
				// new ImageDownloadTask().execute(m.getImagePath());
				TwitterImageDownloadTask.executeDownload(ctx, img_avatar,
						m.getImagePath());
			} else {
				img_avatar.setImageBitmap(bMap);
			}

			data.setText(TwitterUtils.friendlyFormat(m.getData(),ctx));
			txt_texto.setText(mensagem);
		}

		public void createAndFillFacebookMessage(View convertView, Mensagem m) {
			PictureInfo pInfo = m.getPictureUrl();
			if (pInfo != null) {
				img_preview = (ImageView) convertView
						.findViewById(R.id.img_preview_source);
				lt_img_preview = (LinearLayout) convertView
						.findViewById(R.id.lt_image_preview);

				TwitterImageDownloadTask.executeDownload(ctx, img_preview,
						pInfo.getSURL());
				// img_preview.setLayoutParams(new LayoutParams(100, 100));
				lt_img_preview.setVisibility(View.VISIBLE);
				String capition = pInfo.getCaption();
				if (capition != null) {
					lt_caption = (LinearLayout) convertView
							.findViewById(R.id.lt_twiite_caption);
					txt_caption = (TextView) convertView
							.findViewById(R.id.txt_tweet_caption);

					txt_caption.setText(capition);
					lt_caption.setVisibility(View.VISIBLE);
				}

			}

			if (m.getTypeMessage().equals("link")
					|| m.getTypeMessage().equals("video")) {

				LinearLayout lt_link_detais = (LinearLayout) convertView
						.findViewById(R.id.lt_link_details);
				txt_link_details = (TextView) convertView
						.findViewById(R.id.txt_link_datails);
				img_thumbnails = (ImageView) convertView
						.findViewById(R.id.img_thumbnail);
				TextView txt_link_details_extra=(TextView) convertView.findViewById(R.id.txt_link_detais_extra);
				LinearLayout lt_vertical_line=(LinearLayout) convertView.findViewById(R.id.lt_vertical_line);
				JSONObject linkDetais=null;
				try {
					 linkDetais = new JSONObject(m.getAddtions()
							.getString("link_details"));
					
				} catch (JSONException e) {
					// TODO: handle exception
				}
				
				String pictureURL = null;
				try {
					pictureURL = linkDetais.getString("picture");
				} catch (JSONException e) {

				}catch (Exception e) {
					// TODO: handle exception
				}

				if (pictureURL != null) {
					URL url;
					try {
						url = new URL(pictureURL);
						TwitterImageDownloadTask.executeDownload(ctx,
								img_thumbnails, url);
					} catch (MalformedURLException e) {
					}

				}else{
					lt_vertical_line.setVisibility(View.GONE);
					img_thumbnails.setVisibility(View.GONE);
				}

				
				try {
					txt_link_details.setText(linkDetais.getString("name"));
					String text_link="";
					try{
						text_link+=linkDetais.getString("caption")+"\n";
					}catch (Exception e) {
						
					}
					try{
						text_link+=linkDetais.getString("description");
					}catch (Exception e) {
						// TODO: handle exception
					}
					if(text_link.length()>100)
						text_link=text_link.substring(0, 100)+"...";
					txt_link_details_extra.setText(text_link);
					lt_link_detais.setVisibility(View.VISIBLE);
				} catch (JSONException e) {
				
				}catch (Exception e) {
					// TODO: handle exception
				}
				
				

			}

			int likesCount = m.getLikesCount();
			if (likesCount > 0) {
				txt_like_count = (TextView) convertView
						.findViewById(R.id.lbl_qtd_likes);
				txt_like_count.setText(Integer.toString(likesCount));

				lt_like = (LinearLayout) convertView
						.findViewById(R.id.lt_likes);
				lt_like.setVisibility(View.VISIBLE);
			}

			int commentsCount = m.getCommentsCount();
			if (commentsCount > 0) {

				lt_comments = (LinearLayout) convertView
						.findViewById(R.id.lt_comments);
				if (lt_like == null) {
					lt_comments.setPadding(0, 0, 0, 0);
				}
				lt_comments.setVisibility(View.VISIBLE);

				txt_comments_count = (TextView) convertView
						.findViewById(R.id.lbl_qtd_comments);
				txt_comments_count.setText(Integer.toString(commentsCount));

			}

		}

	}

	public void clear() {
		list.clear();
		notifyDataSetChanged();
	}

	public AbstractColumn getColumn() {
		return column;
	}

	public void setColumn(AbstractColumn column) {
		this.column = column;
	}

	public void deleteMensagem(String id, int type) {

		try {
			Mensagem m = hash.get(id + "_" + type);
			try {
				list.remove(m);

				notifyDataSetChanged();
			} catch (IndexOutOfBoundsException e) {
				// TODO: handle exception
			}
		} catch (NullPointerException e) {
			// TODO: handle exception
		}
	}

	public int getSelection() {

		return selection;
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (observer != null) {
			super.unregisterDataSetObserver(observer);
		}
	}

}
