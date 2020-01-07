package com.acqua.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinAllocation {
		
	@JsonProperty("BinAbsEntry")
	private Integer binAbsEntry;
	
	@JsonProperty("BaseLineNumber")
	private Integer baseLineNumber;
	
	public BinAllocation() {
		super();
	}
	
	public BinAllocation (Integer binAbsEntry, Integer baseLineNumber) {
		super();
		this.binAbsEntry = binAbsEntry;
		this.baseLineNumber = baseLineNumber;
		
	}
	
	@JsonGetter("BinAbsEntry")
	public Integer getBinAbsEntry() {
		return binAbsEntry;
	}
	
	@JsonSetter("BinAbsEntry")
	public void setBinAbsEntry(Integer binAbsEntry) {
		this.binAbsEntry = binAbsEntry;
	}
	
	@JsonGetter("BaseLineNumber")
	public Integer getBaseLineNumber() {
		return baseLineNumber;
	}

	@JsonSetter("BaseLineNumber")
	public void setBaseLineNumber(Integer baseLineNumber)
	{
		this.baseLineNumber = baseLineNumber;
	}

	@Override
	public String toString() {
		return "BinAllocation [binAbsEntry=" + binAbsEntry + ", baseLineNumber=" + baseLineNumber + "]";
	}
	
	
	
}
