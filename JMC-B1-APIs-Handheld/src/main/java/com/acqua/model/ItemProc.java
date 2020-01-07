package com.acqua.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemProc {

	private String itemId;
	private Integer desc;
	

	public ItemProc() {
		super();
	}

	public ItemProc(String itemId, Integer desc) {
		super();
		this.itemId = itemId;
		this.desc = desc;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	
	public Integer getDesc(){
		return desc;
	}
	
	public void setDesc(Integer desc){
		this.desc = desc;
	}
	
}
