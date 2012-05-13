package com.twittemarkup.view;

import java.util.Vector;

import twitter4j.auth.AccessToken;

import com.markupartist.android.widget.PullToRefreshListView;
import com.twittemarkup.activity.TimelineActivity;
import com.twittemarkup.app.R;
import com.twittemarkup.assynctask.UpdateTimelineTask;
import com.twittemarkup.command.OpenTwitterWriter;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.Mensagem;
import com.twittemarkup.model.PalavraChave;
import com.twittemarkup.model.TwitterAccount;
import com.twittemarkup.utils.Constants;
import com.twittemarkup.utils.UpdateParams;

import android.content.Context;

public class TwitterFeedsCollumn extends AbstractColumn {

	private int currentPage;
	private boolean flagNextPage = false;

	public TwitterFeedsCollumn(Context ctx) {
		super(ctx, ctx.getString(R.string.main_column_name),true);
		command=new OpenTwitterWriter();
	}

	public TwitterFeedsCollumn(Context ctx, String title) {
		super(ctx, title,true);
		currentPage = 0;
		
	}

	@Override
	public void deleteColumn() {

	}

	@Override
	protected void updateList() {

		TwitterAccount user = (TwitterAccount) Account.getTwitterAccount(ctx);// verificar
																				// depois

		AccessToken token = new AccessToken(user.getToken(),
				user.getTokenSecret());

		if (flagNextPage)
			currentPage++;
		else
			currentPage = 1;
		UpdateParams params = new UpdateParams(currentPage,
				Constants.QTD_FEEDS, token);
		new UpdateTimelineTask(ctx, params, (PullToRefreshListView)listView, this).execute();
	}

	@Override
	public void init() {
		final Vector<Mensagem> list = facade
				.getMensagemOf(Mensagem.TIPO_STATUS);
		TimelineActivity.h.post(new Runnable() {

			@Override
			public void run() {
				updateTwittes(list, true);
				
			}
		});
	}
	
	

	@Override
	public boolean isDeletable() {
		return false;
	}

	@Override
	protected void onGetNextPage() {
		if (isLoaddingNextPage || currentPage == 0)
			return;

		super.onGetNextPage();

		flagNextPage = true;
		updateList();
		isLoaddingNextPage = true;
	}

	@Override
	public void updateTwittes(Vector<Mensagem> list, boolean top) {
		if (list == null)
			return;

		addMensagens(list, top);

	}

}
