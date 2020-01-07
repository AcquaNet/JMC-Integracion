package com.acqua.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PickListLines {
	
	@JsonProperty("AbsoluteEntry")
	private Integer absoluteEntry;
	
	@JsonProperty("OrderEntry")
	private Integer orderEntry;
	
	@JsonProperty("OrderRowID")
	private Integer orderRowID;
	
	@JsonProperty("PickedQuantity")
	private Integer pickedQuantity;
	
	@JsonProperty("LineNumber")
	private Integer lineNumber;
	
	@JsonProperty("ReleasedQuantity")
	private Integer releasedQuantity;
	
	@JsonProperty("PreviouslyReleasedQuantity")
	private Integer prevRQ;
	
	@JsonProperty("PickStatus")
	private String pickStatus;
	
	@JsonProperty("DocumentLinesBinAllocations")
	private List<BinAllocation> docbinAlloc;
	
	
	
	public PickListLines() {
		super();
	}
	
	public PickListLines(Integer absoluteEntry, Integer orderEntry, Integer orderRowID, Integer pickedQuantity, Integer lineNumber, Integer releasedQuantity, Integer prevRQ, String pickStatus, List<BinAllocation> docbinAlloc) {
		super();
		this.absoluteEntry = absoluteEntry;
		this.orderEntry = orderEntry;
		this.orderRowID = orderRowID;
		this.pickedQuantity = pickedQuantity;
		this.lineNumber = lineNumber;
		this.releasedQuantity = releasedQuantity;
		this.pickStatus = pickStatus;
		this.docbinAlloc = docbinAlloc;
		this.prevRQ = prevRQ;
	}
	
	@JsonGetter("AbsoluteEntry")
	public Integer getAbsoluteEntry() {
		return absoluteEntry;
	}
	
	@JsonSetter("AbsoluteEntry")
	public void setAbsoluteEntry(Integer absoluteEntry) {
		this.absoluteEntry = absoluteEntry;
	}
	
	@JsonGetter("OrderEntry")
	public Integer getOrderEntry() {
		return orderEntry;
	}
	
	@JsonSetter("OrderEntry")
	public void setOrderEntry(Integer orderEntry) {
		this.orderEntry = orderEntry;
	}
	
	@JsonGetter("OrderRowID")
	public Integer getOrderRowID() {
		return orderRowID;
	}
	
	@JsonSetter("OrderRowID")
	public void setOrderRowID(Integer orderRowID){
		this.orderRowID = orderRowID;
	}
	
	@JsonGetter("PickedQuantity")
	public Integer getPickedQuantity() {
		return pickedQuantity;
	}
	
	@JsonSetter("PickedQuantity")
	public void setPickedQuantity(Integer pickedQuantity) {
		this.pickedQuantity = pickedQuantity;
	}
	
	@JsonGetter("LineNumber")
	public Integer getLineNumber() {
		return lineNumber;
		
	}
	
	@JsonSetter("LineNumber")
	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	@JsonGetter("ReleasedQuantity")
	public Integer getReleasedQuantity() {
		return releasedQuantity;
	}
	
	@JsonSetter("ReleasedQuantity")
	public void setReleasedQuantity(Integer releasedQuantity) {
		this.releasedQuantity = releasedQuantity;
	}
	
	@JsonGetter("PickStatus")
	public String getPickStatus() {
		return pickStatus;
	}
	
	@JsonSetter("PickStatus")
	public void setPickStatus(String pickStatus) {
		this.pickStatus = pickStatus;
	}
	
	@JsonGetter("DocumentLinesBinAllocations")
	private List<BinAllocation> getDocBinAlloc() {
		return docbinAlloc;
	}
	
	@JsonSetter("DocumentLinesBinAllocations")
	private void setDocBinAlloc(List<BinAllocation> docbinAlloc) {
		this.docbinAlloc = docbinAlloc;
	}
	
	@JsonGetter("PreviouslyReleasedQuantity")
	public Integer getPreviouslyReleasedQuantity() {
		return prevRQ;
	}
	
	@JsonSetter("PreviouslyReleasedQuantity")
	public void setPreviouslyReleasedQuantity(Integer prevRQ) {
		this.prevRQ = prevRQ;
	}

	@Override
	public String toString() {
		return "PickListLines [absoluteEntry=" + absoluteEntry + ", orderEntry=" + orderEntry + ", orderRowID="
				+ orderRowID + ", pickedQuantity=" + pickedQuantity + ", lineNumber=" + lineNumber
				+ ", releasedQuantity=" + releasedQuantity + ", prevRQ=" + prevRQ + ", pickStatus=" + pickStatus
				+ ", docbinAlloc=" + docbinAlloc + "]";
	}

	
	
	
}
