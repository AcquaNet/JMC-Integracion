package com.acqua.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinLocations {
	
	@JsonProperty("BinCode")
	private String binCode;
	
	public BinLocations() {
		super();
	}
	
	public BinLocations(String binCode) {
		super();
		this.binCode = binCode;
	}
	
	@JsonGetter("BinCode")
	public String getBinCode() {
		return binCode;
	}
	
	@JsonSetter("BinCode")
	public void setBinCode(String binCode) {
		this.binCode = binCode;
	}

	@Override
	public String toString() {
		return "BinLocations [binCode=" + binCode + "]";
	}
	
	

}
