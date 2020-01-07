package com.acqua.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OdataPL {

	@JsonProperty("odata.metadata")
	private String odataMetadata;
	private List<PickList> value;
	
	
	public OdataPL() {
		super();
	}

	public OdataPL(List<PickList> value, String odataMetadata) {
		super();
		this.value = value;
		this.odataMetadata = odataMetadata;
		
	}

	public List<PickList> getValue() {
		return value;
	}

	public void setValue(List<PickList> value) {
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
		Collections.sort(value, new SortbynamePL());
	}
}
class SortbynamePL implements Comparator<PickList>{
	public int compare(PickList a, PickList b){
		return a.getAbsoluteEntry().compareTo(b.getAbsoluteEntry());
	}
}