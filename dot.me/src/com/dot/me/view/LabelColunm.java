package com.dot.me.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import twitter4j.auth.AccessToken;

import com.dot.me.activity.TimelineActivity;
import com.dot.me.app.R;
import com.dot.me.assynctask.UpdateTimelineTask;
import com.dot.me.model.Account;
import com.dot.me.model.Label;
import com.dot.me.model.Mensagem;
import com.dot.me.model.PalavraChave;
import com.dot.me.model.TwitterAccount;
import com.dot.me.model.bd.Facade;
import com.dot.me.utils.Constants;
import com.dot.me.utils.MessageObserver;
import com.dot.me.utils.Separator;
import com.dot.me.utils.SubjectMessage;
import com.dot.me.utils.UpdateParams;

import android.content.Context;

public class LabelColunm extends AbstractColumn implements MessageObserver {

	private Label marcador;
	private int currentPage;
	private boolean flagNextPage = false;

	public LabelColunm(Label m, Context ctx) {
		super(ctx, m.getNome(), false);

		marcador = m;
	}

	@Override
	public void updateTwittes(Vector<Mensagem> list, boolean top) {
		if (list == null)
			return;

		if (marcador == null) {
			addMensagens(list, top);
		} else {
			Vector<Mensagem> filteredList = new Vector<Mensagem>();
			for (Mensagem m : list) {
				for (PalavraChave palavra : marcador.getPalavrasChave()) {
					if (m.getMensagem().toLowerCase()
							.contains(palavra.getConteudo().toLowerCase())
							|| m.getNome_usuario()
									.toLowerCase()
									.contains(
											palavra.getConteudo().toLowerCase())) {
						filteredList.add(m);
					}
				}
			}
			addMensagens(filteredList, top);
		}
	}

	public Label getMarcador() {
		return marcador;
	}

	public void setMarcador(Label marcador) {
		this.marcador = marcador;
	}

	public static void cleanAllColumn() {
		for (AbstractColumn a : instances)
			if (a instanceof LabelColunm)
				a.adapter.removeAll();
	}

	public static void addSeparatorToAllMarcadores(Separator s) {
		/*
		 * for(AbstractColumn a:instances) if(a instanceof MarkupColunm)
		 * a.adapter.addSeparator(s);
		 */
	}

	public static void addSeparatorToAllMarcadores(int position, Separator s) {
		/*
		 * for(AbstractColumn a:instances) if(a instanceof MarkupColunm)
		 * a.adapter.addSeparator(s);
		 */
	}

	public static void removeAllSeparatorMarcadores(Separator s) {
		/*
		 * for(AbstractColumn a:instances) if(a instanceof MarkupColunm)
		 * a.adapter.addSeparator(s);
		 */
	}

	@Override
	protected void updateList() {

		/*
		 * TwitterAccount user = (TwitterAccount)
		 * Account.getTwitterAccount(ctx);// verificar depois
		 * 
		 * AccessToken token = new AccessToken(user.getToken(),
		 * user.getTokenSecret());
		 * 
		 * if(flagNextPage) currentPage++; else currentPage=1; UpdateParams
		 * params = new UpdateParams(currentPage, Constants.QTD_FEEDS, token);
		 * new UpdateTimelineTask(ctx, params, listView,this).execute();
		 */
		// adapter.sort();
		// listView.onRefreshComplete();
	}

	@Override
	public void deleteColumn() {
		marcador.setEnnabled(0);
		Facade.getInstance(ctx).manageMarkup(marcador);
		SubjectMessage sMessage = TimelineActivity.getmSubject();
		if (sMessage != null)
			sMessage.unregisterObserver(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LabelColunm) {
			LabelColunm other = (LabelColunm) o;
			if (other.getMarcador().getIdMarcador() == this.getMarcador()
					.getIdMarcador())
				return true;

		}

		return false;
	}

	@Override
	protected void onGetNextPage() {
		/*
		 * if(isLoaddingNextPage||currentPage==0) return;
		 * 
		 * super.onGetNextPage();
		 * 
		 * flagNextPage=true; updateList(); isLoaddingNextPage=true;
		 */
	}

	@Override
	public void init() {
		final Vector<Mensagem> list = facade
				.getMensagemOf(Mensagem.TIPO_STATUS);
		listView.post(new Runnable() {

			@Override
			public void run() {
				updateTwittes(list, true);
//				notifyInitFinished();
				
			}
		});

	}

	@Override
	public void notifyMessageAdded(Mensagem m) {
		if (validateMessage(m)) {
			adapter.addItem(m);
			adapter.sort();
		}
	}

	private boolean validateMessage(Mensagem m) {

		HashMap<String, String> hash = new HashMap<String, String>();

		String[] name_part = m.getNome_usuario().toLowerCase().split(" ");

		for (String part : name_part) {
			hash.put(part, part);
		}
		for (String word : m.getMensagem().toLowerCase().split(" ")) {

			hash.put(word, word);

		}

		for (PalavraChave keyWord : this.marcador.getPalavrasChave()) {
			String word = keyWord.getConteudo().toLowerCase();

			int count_equals = 0;

			String[] parts_word = word.split("\\+");
			for (String part_word : parts_word) {
				if (hash.get(part_word) != null) {
					count_equals++;
				}
			}

			if (count_equals == parts_word.length)
				return true;
		}

		return false;
	}

	@Override
	public void notifyMessageRemoved(String id,int type) {
		
		adapter.deleteMensagem(id,type);
		adapter.sort();
		
	}

}
