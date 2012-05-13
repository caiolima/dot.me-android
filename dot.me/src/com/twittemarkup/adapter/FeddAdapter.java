package com.twittemarkup.adapter;

import java.util.Collections;
import java.util.Vector;

import com.twittemarkup.activity.FacebookMessageActivity;
import com.twittemarkup.activity.MessageInfoActivity;
import com.twittemarkup.activity.TimelineActivity;
import com.twittemarkup.adapter.MessageAdapter.ViewHolder;
import com.twittemarkup.app.R;
import com.twittemarkup.assynctask.TwitterImageDownloadTask;
import com.twittemarkup.model.Mensagem;
import com.twittemarkup.model.bd.Facade;
import com.twittemarkup.utils.BlackListUtils;
import com.twittemarkup.utils.ImageUtils;
import com.twittemarkup.utils.MessageObserver;
import com.twittemarkup.utils.PictureInfo;
import com.twittemarkup.utils.SubjectMessage;
import com.twittemarkup.utils.TwitterUtils;
import com.twittemarkup.view.AbstractColumn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
	private Context ctx;
	private LayoutInflater mInflater;
	private AbstractColumn column;
	private int selection=0;

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
		selection=position;
		if (m != null) {
			convertView = mInflater.inflate(R.layout.twitte_row, null);

			ViewHolder holder = new ViewHolder();
			holder.txt_nome = (TextView) convertView
					.findViewById(R.id.screen_name);
			holder.img_avatar = (ImageView) convertView
					.findViewById(R.id.profile_img);
			holder.data = (TextView) convertView.findViewById(R.id.time);
			holder.txt_texto = (TextView) convertView.findViewById(R.id.twitte);
			View v = convertView.findViewById(R.id.line);
			v.setVisibility(View.GONE);
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
			Mensagem m = (Mensagem) o;
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
		notifyDataSetChanged();
	}

	public void addItem(Mensagem o) {

		if (o instanceof Mensagem) {
			Mensagem m = (Mensagem) o;
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
		public ImageView img_preview;
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

			data.setText(TwitterUtils.friendlyFormat(m.getData()));
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

	public void deleteMensagem(Mensagem m) {

		int pos = list.indexOf(m);
		try {
			list.remove(pos);

			notifyDataSetChanged();
		} catch (IndexOutOfBoundsException e) {
			// TODO: handle exception
		}
	}

	public int getSelection() {
		
		return selection;
	}

}
