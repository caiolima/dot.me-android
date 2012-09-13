package com.dot.me.adapter;

import java.util.Collections;
import java.util.Vector;

import com.dot.me.activity.FacebookMessageActivity;
import com.dot.me.activity.MessageInfoActivity;
import com.dot.me.app.R;
import com.dot.me.assynctask.TwitterImageDownloadTask;
import com.dot.me.model.Mensagem;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.BlackListUtils;
import com.dot.me.utils.ImageUtils;
import com.dot.me.utils.PictureInfo;
import com.dot.me.utils.Separator;
import com.dot.me.utils.TwitterUtils;
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

public class MessageAdapter extends BaseAdapter {

	public static final int TYPE_ITEM = 0;
	public static final int TYPE_SEPARATOR = 1;
	private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

	private Vector<Mensagem> list = new Vector<Mensagem>();
	private Vector<Integer> initialPositionExecuted=new Vector<Integer>();
	// private TreeSet<Integer> separetorList = new TreeSet<Integer>();
	private LayoutInflater mInflater;
	private Context ctx;
	private Separator currentSeparator;

	public MessageAdapter(Context ctx) {
		// operação necessária para usar o Inflater
		this.ctx = ctx;
		mInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Mensagem getItem(int position) {
		// TODO Auto-generated method stub
		try {
			Mensagem mensagem = list.get(position);
			return mensagem;
		} catch (ClassCastException e) {
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		Log.w("dot.me", "Getting view at position " + position);
		
		final Mensagem m = getItem(position);
		int type = getItemViewType(position);

		if (type == TYPE_ITEM) {
			// if (convertView == null) {
			// criando o objeto do layout
			convertView = mInflater.inflate(R.layout.twitte_row, null);

			holder = new ViewHolder();
			holder.txt_nome = (TextView) convertView
					.findViewById(R.id.screen_name);
			holder.img_avatar = (ImageView) convertView
					.findViewById(R.id.profile_img);
			holder.data = (TextView) convertView.findViewById(R.id.time);
			holder.txt_texto = (TextView) convertView.findViewById(R.id.twitte);
			convertView.setTag(holder);// Tag que serve para identificar a
			// view
			LinearLayout linearLayout = (LinearLayout) convertView
					.findViewById(R.id.tweet_row_layout);

			if (m.getTipo() == Mensagem.TIPO_NEWS_FEEDS) {
				holder.createAndFillFacebookMessage(convertView, m);
				linearLayout.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx,
								FacebookMessageActivity.class);
						Bundle b = new Bundle();
						b.putString("idMessage", m.getIdMensagem());
						b.putInt("type", m.getTipo());
						intent.putExtras(b);

						ctx.startActivity(intent);
					}
				});
			} else {

				linearLayout.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx,
								MessageInfoActivity.class);
						Bundle b = new Bundle();
						b.putString("idMessage", m.getIdMensagem());
						b.putInt("type", m.getTipo());
						intent.putExtras(b);

						ctx.startActivity(intent);
					}
				});
			}

			holder.preencheLayout(m);
			/*
			 * } else { // reutilizando o objeto já criado. holder =
			 * (ViewHolder) convertView.getTag();
			 * 
			 * }
			 */

		} /*
		 * else if (type == TYPE_SEPARATOR) { convertView =
		 * mInflater.inflate(R.layout.separetor, null); LinearLayout
		 * separator_layout = (LinearLayout) convertView
		 * .findViewById(R.id.separator_layout); final Separator s = (Separator)
		 * list.get(position); separator_layout.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { Intent intent = new
		 * Intent("com.twittemarkup.status.update"); Bundle bundle = new
		 * Bundle(); bundle.putInt("page", s.getPage());
		 * bundle.putInt("qtd_feeds", s.getQtd_tweets());
		 * intent.putExtras(bundle); ctx.startService(intent); list.remove(s);
		 * notifyDataSetChanged(); } }); }
		 */

		return convertView;

	}

	public void addItem(int position, Mensagem o) {
		if (o instanceof Mensagem) {
			Mensagem m = o;
			if (list.contains(m)
					|| (!BlackListUtils.validateMessage(Facade.getInstance(ctx)
							.getAllWordsInBlackList(), m)))
				return;

		}
		list.add(position, o);
		notifyDataSetChanged();
	}

	public void addItem(Mensagem o) {
		if (o instanceof Mensagem) {
			Mensagem m = o;
			Vector<String> blacklist = Facade.getInstance(ctx)
					.getAllWordsInBlackList();
			if (list.contains(m)
					|| (!BlackListUtils.validateMessage(blacklist, m)))
				return;

		}
		list.add(o);
		notifyDataSetChanged();
	}

	public void addSeparator(Separator s) {
		setCurrentSeparator(s);
		// addItem(s);
		// separetorList.add(list.size()-1);
	}

	public void addSeparator(int position, Separator s) {
		setCurrentSeparator(s);
		// addItem(position, s);
		// separetorList.add(position);
	}

	@Override
	public int getItemViewType(int position) {

		return getItem(position) instanceof Mensagem ? TYPE_ITEM
				: TYPE_SEPARATOR;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	/* Esta classe serve para armazenar os campos das view. */
	public class ViewHolder {

		public TextView txt_nome;
		public ImageView img_avatar;
		public TextView data;
		public TextView txt_texto;
		public TextView txt_like_count;
		public TextView txt_comments_count;
		public ImageView img_preview;
		public LinearLayout lt_img_preview, lt_like, lt_comments;
		private Handler h = new Handler();

		// Método em que os campos da view são preenchidos com o conteudo da
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

	public void removeAll() {
		list.removeAllElements();
		notifyDataSetChanged();
	}

	public Separator getCurrentSeparator() {
		return currentSeparator;
	}

	public void setCurrentSeparator(Separator currentSeparator) {
		list.remove(this.currentSeparator);
		this.currentSeparator = currentSeparator;
	}

	public void removeSeparator(Separator s) {
		list.remove(s);
		currentSeparator = null;
		notifyDataSetChanged();
	}

	public void sort() {
		Collections.sort(list);
		notifyDataSetChanged();
	}

}
