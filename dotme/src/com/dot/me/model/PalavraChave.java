package com.dot.me.model;


public class PalavraChave {

	private int idPalavraChave;
	private String conteudo;
	
	public int getIdPalavraChave() {
		return idPalavraChave;
	}
	public void setIdPalavraChave(int idPalavraChave) {
		this.idPalavraChave = idPalavraChave;
	}
	public String getConteudo() {
		return conteudo;
	}
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	@Override
	public boolean equals(Object o) {
		if(o instanceof PalavraChave){
			PalavraChave outra=(PalavraChave) o;
			if(outra.getConteudo().equals(conteudo))
				return true;
			
			return false;
		}
		return false;
	}
	
	/*public static PalavraChave fromJSONObject(JSONObject json){
		PalavraChave p=new PalavraChave();
		
		try {
			p.setConteudo(json.getString("conteudo"));
			p.setIdPalavraChave(json.getLong("idpalavra_chave"));
		} catch (JSONException e) {
			return null;
		}
		
		
		return p;
	}*/
	
	
	
}
