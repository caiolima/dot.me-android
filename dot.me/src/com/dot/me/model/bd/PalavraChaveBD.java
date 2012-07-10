package com.dot.me.model.bd;

import java.util.Vector;

import com.dot.me.model.PalavraChave;
import com.dot.me.utils.Menssage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PalavraChaveBD extends Dao{

	protected PalavraChaveBD(Context ctx) {
		super(ctx);
		
	}
	
	protected int insert(PalavraChave p){
		
		ContentValues values=new ContentValues();
		values.put(DataBase.PALAVRA_CHAVE_CONTEUDO, p.getConteudo());
		
		long r=db.getDB().insert(DataBase.TB_PALAVRA_CHAVE, null, values);
		Cursor c=db.getDB().query(DataBase.TB_PALAVRA_CHAVE, new String[] {DataBase.PALAVRA_CHAVE_ID}, null, null, null, null, DataBase.PALAVRA_CHAVE_ID+" DESC");
		c.moveToFirst();
		
		int id_palavra=c.getInt(0);
		c.close();
		if(r==-1)
			return Menssage.ERRO;
		else
			return id_palavra;
	}
	
	protected int conectaPalavra(int idPalavra,int idMarcador){
		
		ContentValues values=new ContentValues();
		values.put(DataBase.PALAVRA_CHAVE_ID, idPalavra);
		values.put(DataBase.MARCADOR_ID, idMarcador);
		
		long r=db.getDB().insert(DataBase.TB_PALAVRA_CHAVE_MARCADOR, null, values);
		if(r==-1)
			return Menssage.ERRO;
		else
			return Menssage.SUCCESS;
		
	}
	
	protected void desconectaPalavra(int idPalavra,int idMarcador){
		db.getDB().delete(DataBase.TB_PALAVRA_CHAVE_MARCADOR, 
				DataBase.PALAVRA_CHAVE_ID+"=? and "+DataBase.MARCADOR_ID+"=?",
				new String[]{Integer.toString(idPalavra),Integer.toString(idMarcador)});
	}
	
	protected void desconectaPalavraFromMarcador(int idMarcador){
		db.getDB().delete(DataBase.TB_PALAVRA_CHAVE_MARCADOR, 
				DataBase.MARCADOR_ID+"=?",
				new String[]{Integer.toString(idMarcador)});
	}
	
	protected int existsPalavra(String palavra){
		Cursor c=db.getDB().query(DataBase.TB_PALAVRA_CHAVE, new String[] {DataBase.PALAVRA_CHAVE_ID}, DataBase.PALAVRA_CHAVE_CONTEUDO+"=?", new String[]{palavra}, null, null,null);
		if(c.getCount()>0){
			c.moveToFirst();
			int out=c.getInt(0);
			c.close();
			return out;
		}
		c.close();
		return Menssage.ERRO;
	}
	
	protected void deletAll(){
		db.getDB().delete(DataBase.TB_PALAVRA_CHAVE_MARCADOR, null, null);
		db.getDB().delete(DataBase.TB_PALAVRA_CHAVE, null, null);
	}
	
	protected PalavraChave getOne(long id){
		Cursor c=db.getDB().query(DataBase.TB_PALAVRA_CHAVE, null, DataBase.PALAVRA_CHAVE_ID+"=?", new String[] {Long.toString(id)}, null, null, null);
	
		PalavraChave p=null;
		if(c.getCount()>0){
			c.moveToFirst();
			
			p=new PalavraChave();
			p.setIdPalavraChave(c.getInt(0));
			p.setConteudo(c.getString(1));
			
		}
		c.close();
		return p;
		
	}

	protected Vector<PalavraChave> getByMarcador(long idMarcador){
		Cursor c=db.getDB().query(DataBase.TB_PALAVRA_CHAVE_MARCADOR, null, DataBase.MARCADOR_ID+"=?", new String[] {Long.toString(idMarcador)}, null, null, null);
	
		Vector<PalavraChave> palavras=new Vector<PalavraChave>();
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			
			palavras.add(getOne(c.getLong(0)));
			
		}
		
		c.close();
		return palavras;
		
	}
	
	protected void deletePalavraByMarcador(long idMarcador){
		db.getDB().delete(DataBase.TB_PALAVRA_CHAVE_MARCADOR, DataBase.MARCADOR_ID+"=?", new String[]{Long.toString(idMarcador)});
	}
	
}
