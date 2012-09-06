package com.dot.me.view;

import com.dot.me.interfaces.IOnLabelRefreshListener;
import com.markupartist.android.widget.PullToRefreshListView;

import android.content.Context;

/*
 * Esta classe foi criada para implementar os métodos de forcenextPage e 
 * forceRefresh para as colunas que serão LabelListener. Uma coluna labelListener
 * indica que será atualizada quando um Label forçar a atualização
 *
 */

public abstract class LabelListenerCollunm extends AbstractColumn implements
		IOnLabelRefreshListener {

	public LabelListenerCollunm(Context ctx, String title, boolean isToRefresh) {
		super(ctx, title, isToRefresh);

	}

	@Override
	public void forceRefresh() {
		((PullToRefreshListView) listView).prepareForRefresh();
		((PullToRefreshListView) listView).onRefresh();

	}

	@Override
	public void forceNextPage() {
		if (isLoading)
			return;

		onGetNextPage();

	}

	@Override
	public boolean isFinished() {
		if(isLoading||isLoaddingNextPage)
			return false;
		
		return true;
	}
	
	

}
