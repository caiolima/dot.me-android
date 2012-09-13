package com.dot.me.model;

import java.util.Vector;

public class Label{

	private int idMarcador;
	private String nome;
	private Vector<PalavraChave> palavrasChave=new Vector<PalavraChave>();
	//private Vector<Usuario> usuarios=new Vector<Usuario>();
	private long idUsuario;
	private int ennabled;
	
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public Vector<PalavraChave> getPalavrasChave() {
		return palavrasChave;
	}
	
	public void setPalavrasChave(Vector<PalavraChave> palavrasChave) {
		this.palavrasChave = palavrasChave;
	}
	
	/*public Vector<Usuario> getUsuarios() {
		return usuarios;
	}
	
	public void setUsuarios(Vector<Usuario> usuarios) {
		this.usuarios = usuarios;
	}*/
	
	public void addPalavra(PalavraChave palavra){
		this.palavrasChave.add(palavra);
	}
	
	/*public void addUsuario(Usuario usuario){
		this.usuarios.add(usuario);
	}*/
	
	public void setIdUsuario(long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public long getIdUsuario() {
		return idUsuario;
	}

	public void setIdMarcador(int idMarcador) {
		this.idMarcador = idMarcador;
	}

	public int getIdMarcador() {
		return idMarcador;
	}
	
	/*public static Marcador FromJSONObject(JSONObject json){
		Marcador m=new Marcador();
		try {
			m.setNome(json.getString("nome"));
			m.setIdMarcador(json.getLong("idMarcador"));
			m.setIdUsuario(json.getLong("idUsuario"));
			Vector<PalavraChave> palavras=new Vector<PalavraChave>();
			JSONArray jsonPalavras=json.getJSONArray("palavras");
			for(int i=0;i<jsonPalavras.length();i++){
				PalavraChave palavra=PalavraChave.fromJSONObject(jsonPalavras.getJSONObject(i));
				palavras.add(palavra);
			}
			m.setPalavrasChave(palavras);
		} catch (JSONException e) {
			return null;
		}
		
		
		return m;
		
	}*/

	public int isEnnabled() {
		return ennabled;
	}

	public void setEnnabled(int ennabled) {
		this.ennabled = ennabled;
	}

	@Override
	public String toString() {
		String isEnnabeld="";
		if(ennabled==1){
			isEnnabeld="V";
		}else{
			isEnnabeld="D";
		}
		
		return nome+" ("+isEnnabeld+")";
	}

	public void changeEnable() {
		if(ennabled==1)
			ennabled=0;
		else
			ennabled=1;
		
	}
	
	@Override
	public boolean equals(Object o) {
		try{
			Label l=(Label)o;
			return l.idMarcador==this.idMarcador;
		}catch (ClassCastException e) {
			return false;
		}
	}
	
}
