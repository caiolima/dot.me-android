package com.twittemarkup.model.bd;

import java.util.Vector;

import com.twittemarkup.model.Marcador;
import com.twittemarkup.model.PalavraChave;
import com.twittemarkup.utils.Menssage;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class MarcadorBD extends Dao{

	
	
	protected MarcadorBD(Context ctx) {
		super(ctx);
	}

	protected int insert(Marcador m){
		
		ContentValues values=new ContentValues();
		values.put(DataBase.MARCADOR_NOME, m.getNome());
		values.put(DataBase.MARCADOR_ENABLED, 1);
		long r=db.getDB().insert(DataBase.TB_MARCADOR, null, values);
		
		Cursor c=db.getDB().query(DataBase.TB_MARCADOR, new String[]{DataBase.MARCADOR_ID}, null, null, null, null, DataBase.MARCADOR_ID+" DESC");
		c.moveToFirst();
		
		int id_marcador=c.getInt(0);
		if(r==-1)
			return Menssage.ERRO;
		else{
			for(PalavraChave p:m.getPalavrasChave()){
				int resp=Facade.getInstance(ctx).insert(p);
				if(resp!=Menssage.ERRO)
					Facade.getInstance(ctx).conectaPalavra(resp, id_marcador);
				
				
			}
			return id_marcador;
		}
			
	}
	
	protected void delete(int id){
		db.getDB().delete(DataBase.TB_MARCADOR, DataBase.MARCADOR_ID+"=?", new String[]{Integer.toString(id)});
	}
	
	protected void deletAll(){
		db.getDB().delete(DataBase.TB_MARCADOR, null, null);
	}
	
	protected Vector<Marcador> getAll(){
		Cursor c=db.getDB().query(DataBase.TB_MARCADOR, null, null,null, null, null, null);
		Vector<Marcador> marcadores=new Vector<Marcador>();
		
		for(int i=0;i<c.getCount();i++){
			c.moveToPosition(i);
			
			Marcador m=new Marcador();
			m.setIdMarcador(c.getInt(0));
			m.setNome(c.getString(1));
			m.setPalavrasChave(Facade.getInstance(ctx).getByMarcador(m.getIdMarcador()));
			m.setEnnabled(c.getInt(2));
			
			marcadores.add(m);
		}
		
		return marcadores;
	}
	
	protected Marcador getOne(int id){
		Cursor c=db.getDB().query(DataBase.TB_MARCADOR, null, DataBase.MARCADOR_ID+"=?",new String[]{Long.toString(id)}, null, null, null);
	
		Marcador m=null;
		if(c.getCount()>0){
			c.moveToFirst();
			
			m=new Marcador();
			m.setIdMarcador(c.getInt(0));
			m.setNome(c.getString(1));
			m.setEnnabled(c.getInt(2));
			
			m.setPalavrasChave(Facade.getInstance(ctx).getByMarcador(m.getIdMarcador()));
			
		}
		
		return m;
		
	}
	
	protected int update(Marcador m, String[] palavras){
		
		ContentValues values=new ContentValues();
		values.put(DataBase.MARCADOR_NOME, m.getNome());
		
		db.getDB().update(DataBase.TB_MARCADOR, values, DataBase.MARCADOR_ID+"=?", new String[]{Integer.toString(m.getIdMarcador())});
		
		Vector<PalavraChave> palavrasAtuais=Facade.getInstance(ctx).getByMarcador(m.getIdMarcador());
		Vector<PalavraChave> novasPalavras=new Vector<PalavraChave>();
		
		for(String palavra:palavras){
			if(palavra.length()==0)
				continue;
			PalavraChave p=new PalavraChave();
			p.setConteudo(palavra);
			
			novasPalavras.add(p);
			
		}
		
		@SuppressWarnings("unchecked")
		Vector<PalavraChave> novasPalavrasAux=(Vector<PalavraChave>) novasPalavras.clone();
		for(PalavraChave palavraChave:novasPalavrasAux){
			if(palavrasAtuais.contains(palavraChave)){
				palavrasAtuais.remove(palavraChave);
				novasPalavras.remove(palavraChave);
			}
			
		}
		
		for(PalavraChave palavra:novasPalavras){
			int resp=Facade.getInstance(ctx).insert(palavra);
			if(resp!=Menssage.ERRO)
				Facade.getInstance(ctx).conectaPalavra(resp, m.getIdMarcador());
			
			
		}
		
		for(PalavraChave p:palavrasAtuais){
			Facade.getInstance(ctx).desconectaPalavra(p.getIdPalavraChave(), m.getIdMarcador());
		}
		
		return Menssage.SUCCESS;
	}
	
	protected void manageMarkup(Marcador m){
		
		ContentValues values=new ContentValues();
		values.put(DataBase.MARCADOR_ENABLED,m.isEnnabled());
		
		db.getDB().update(DataBase.TB_MARCADOR, values, DataBase.MARCADOR_ID+"=?", new String[]{Integer.toString(m.getIdMarcador())});
		
	}
	

	
}
