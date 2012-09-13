package com.dot.me.model.bd;

import java.util.Vector;

import com.dot.me.model.PalavraChave;
import com.dot.me.utils.Menssage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class BlackListDB extends Dao{

	protected BlackListDB(Context ctx) {
		super(ctx);
		// TODO Auto-generated constructor stub
	}
	
	protected void insert(String palavra){
		int idPalavra=Facade.getInstance(ctx).existsPalavra(palavra);
		if(idPalavra==Menssage.ERRO){
			PalavraChave p=new PalavraChave();
			p.setConteudo(palavra);
			idPalavra=Facade.getInstance(ctx).insert(p);
		}
		
		ContentValues values=new ContentValues();
		values.put(DataBase.BLACKLIST_ID_PALAVRA, idPalavra);
		
		db.getDB().insertOrThrow(DataBase.TB_BLACKLIST, null, values);
	}
	
	protected void delete(String word){
		int idPalavra=Facade.getInstance(ctx).existsPalavra(word);
		if(idPalavra!=Menssage.ERRO){
			db.getDB().delete(DataBase.TB_BLACKLIST, DataBase.BLACKLIST_ID_PALAVRA+"=?", new String[]{Integer.toString(idPalavra)});
		}
	}
	
	protected Vector<String> getAll(){
		
		Cursor c=db.getDB().query(DataBase.TB_BLACKLIST, null, null, null, null, null, null);
		Vector<String> all=new Vector<String>();
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			
			all.add(Facade.getInstance(ctx).getOnePalavra(c.getLong(0)).getConteudo());
		}
		c.close();
		return all;
	}

}
