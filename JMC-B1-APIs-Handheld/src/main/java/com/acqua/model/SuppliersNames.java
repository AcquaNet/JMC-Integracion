package com.acqua.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
public class SuppliersNames {
	
	@JsonProperty("CardCode")
	private String cardcode;
	@JsonProperty("CardName")
	private String cardname;
	
	
	
	public SuppliersNames(){
		super();
	}
	
	public SuppliersNames(String cardcode, String cardname) {
		super();
		this.cardcode = cardcode;
		this.cardname = cardname;
	}
	

	@JsonGetter("CardCode")
	public String getCardcode() {
		return cardcode;
	}
	
	@JsonSetter("CardCode")
	public void setCardcode(String cardcode) {
		this.cardcode = cardcode;
	}
	
	@JsonGetter("CardName")
	public String getCardname() {
		return cardname;
	}

	@JsonSetter("CardName")
	public void setCardname(String cardname) {
		this.cardname = cardname;
	}
	
}
