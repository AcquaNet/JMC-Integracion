package com.acqua.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
public class Documents {
	
	@JsonProperty("CardCode")
	private String cardcode;
	@JsonProperty("CardName")
	private String cardname;
	@JsonProperty("DocEntry")
	private Integer docentry;
	@JsonProperty("DocNum")
	private Integer docnum;
	@JsonProperty("DocumentLines")
	private List<DocumentLines> doclines;
	
	
	public Documents(){
		super();
	}
	
	public Documents(String cardcode, String cardname) {
		super();
		this.cardcode = cardcode;
		this.cardname = cardname;
	}
	
	public Documents(Integer docentry, Integer docnum, List<DocumentLines> doclines){
		this.docentry = docentry;
		this.docnum = docnum;
		this.doclines = doclines;
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
	
	@JsonGetter("DocEntry")
	public Integer getDocEntry(){
		return docentry;
	}
	
	@JsonSetter("DocEntry")
	public void setDocEntry(Integer docentry){
		this.docentry = docentry;
	}
	
	@JsonGetter("DocNum")
	public Integer getDocNum(){
		return docnum;
	}
	
	@JsonSetter("DocNum")
	public void setDocNum(Integer docnum){
		this.docnum = docnum;
	}
	
	@JsonGetter("DocumentLines")
	public List<DocumentLines> getDocLines(){
		return doclines;
	}
	
	@JsonSetter("DocumentLines")
	public void setDocLines(List<DocumentLines> doclines){
		this.doclines = doclines;
	}
	
}
