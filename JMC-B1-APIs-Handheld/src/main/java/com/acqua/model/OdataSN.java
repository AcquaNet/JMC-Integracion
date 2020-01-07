package com.acqua.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OdataSN  {

	@JsonProperty("odata.metadata")
	private String odataMetadata;
	private List<SuppliersNames> value;
	
	
	public OdataSN() {
		super();
	}
	
	public OdataSN(List<SuppliersNames> value, String odataMetadata) {
		super();
		this.value = value;
		this.odataMetadata = odataMetadata;
		
	}

	public List<SuppliersNames> getValue() {
		return value;
	}

	public void setValue(List<SuppliersNames> value) {
		this.value = value;
	}

	@JsonGetter("odata.metadata")
	public String getOdataMeta() {
		return odataMetadata;
	}

	@JsonSetter("odata.metadata")
	public void setOdataMeta(String odataMetadata) {
		this.odataMetadata = odataMetadata;
	}
	
	
	
	public void sorted(){
		Collections.sort(value, new SortbynameSN());
	}
}
class SortbynameSN implements Comparator<SuppliersNames>{
	public int compare(SuppliersNames a, SuppliersNames b){
		return a.getCardname().compareTo(b.getCardname());
	}
}