package com.acqua.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OdataPO {

	@JsonProperty("odata.metadata")
	private String odataMetadata;
	private List<PurchaseOrders> value;
	
	
	public OdataPO() {
		super();
	}

	public OdataPO(List<PurchaseOrders> value, String odataMetadata) {
		super();
		this.value = value;
		this.odataMetadata = odataMetadata;
		
	}

	public List<PurchaseOrders> getValue() {
		return value;
	}

	public void setValue(List<PurchaseOrders> value) {
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
		Collections.sort(value, new SortbynamePO());
	}
}
class SortbynamePO implements Comparator<PurchaseOrders>{
	public int compare(PurchaseOrders a, PurchaseOrders b){
		return a.getDocNum().compareTo(b.getDocNum());
	}
}