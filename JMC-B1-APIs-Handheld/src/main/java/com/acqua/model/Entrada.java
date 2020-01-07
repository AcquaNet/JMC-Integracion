package com.acqua.model;

import java.util.List;

public class Entrada {

	private String cardCode;
	private String comentario;
	private String pte;
	private String foliod;
	private String folioa;
	private List<String> ordenes;
	private List<ItemProc> itemProc;

	public Entrada() {
		super();
	}

	public Entrada(String cardCode, String comentario, String pte, String foliod, String folioa, List<String> ordenes,
			List<ItemProc> itemProc) {
		this.cardCode = cardCode;
		this.comentario = comentario;
		this.pte = pte;
		this.foliod = foliod;
		this.folioa = folioa;
		this.ordenes = ordenes;
		this.itemProc = itemProc;
	}

	public String getCardCode() {
		return cardCode;
	}

	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getPte() {
		return pte;
	}

	public void setPte(String pte) {
		this.pte = pte;
	}

	public String getFolioD() {
		return foliod;
	}

	public void setFolioD(String foliod) {
		this.foliod = foliod;
	}

	public String getFolioA() {
		return folioa;
	}

	public void setFolioA(String folioa) {
		this.folioa = folioa;
	}

	public List<String> getOrdenes() {
		return ordenes;
	}

	public void setOrdenes(List<String> ordenes) {
		this.ordenes = ordenes;
	}

	public List<ItemProc> getItemProc() {
		return itemProc;
	}

	public void setItemProc(List<ItemProc> itemProc) {
		this.itemProc = itemProc;
	}

}
