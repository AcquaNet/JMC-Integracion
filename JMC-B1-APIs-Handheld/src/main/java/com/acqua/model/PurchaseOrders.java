package com.acqua.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrders {

	@JsonProperty("DocEntry")
	private Integer docentry;
	@JsonProperty("DocNum")
	private Integer docnum;
	@JsonProperty("DocumentLines")
	private List<DocumentLines> documentlines;

	public PurchaseOrders() {
		super();
	}

	public PurchaseOrders(Integer docentry, Integer docnum, List<DocumentLines> documentlines) {
		super();
		this.docentry = docentry;
		this.docnum = docnum;
		this.documentlines = documentlines;
	}

	@JsonGetter("DocEntry")
	public Integer getDocEntry() {
		return docentry;
	}

	@JsonSetter("DocEntry")
	public void setDocEntry(Integer docentry) {
		this.docentry = docentry;
	}

	@JsonGetter("DocNum")
	public Integer getDocNum() {
		return docnum;
	}

	@JsonSetter("DocNum")
	public void setDocNum(Integer docnum) {
		this.docnum = docnum;
	}

	@JsonGetter("DocumentLines")
	public List<DocumentLines> getDocLines() {
		return documentlines;
	}

	@JsonSetter("DocumentLines")
	public void setDocLines(List<DocumentLines> documentlines) {
		this.documentlines = documentlines;
	}

}
