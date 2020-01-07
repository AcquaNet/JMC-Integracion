package com.acqua.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentLines {

	@JsonProperty("ItemCode")
	private String itemcode;
	@JsonProperty("ItemDescription")
	private String itemdesc;
	@JsonProperty("Quantity")
	private Integer quantity;
	@JsonProperty("TaxCode")
	private String taxcode;
	@JsonProperty("RemainingOpenQuantity")
	private Integer remaining;
	@JsonProperty("RemainingOpenInventoryQuantity")
	private Integer remainingO;
	@JsonProperty("UnitPrice")
	private String unitprice;
	@JsonProperty("LineStatus")
	private String lineStatus;
	@JsonProperty("LineNum")
	private Integer lineNum;
	@JsonProperty("BarCode")
	private String barCode;
	@JsonProperty("UnitsOfMeasurment")
	private Integer unitOM;
	
	public DocumentLines(){
		super();
	}
	
	public DocumentLines(String itemcode,
			String itemdesc,
			Integer quantity,
			String taxcode,
			Integer remaining,
			Integer remainingO,
			String unitprice,
			String lineStatus,
			Integer lineNum,
			String barCode,
			Integer unitOM){
		super();
		this.itemcode = itemcode;
		this.itemdesc = itemdesc;
		this.quantity = quantity;
		this.taxcode = taxcode;
		this.remaining = remaining;
		this.remainingO = remainingO;
		this.unitprice = unitprice;
		this.lineStatus = lineStatus;
		this.lineNum = lineNum;
		this.barCode = barCode;
		this.unitOM = unitOM;
	}
	
	@JsonGetter("ItemCode")
	public String getItemCode(){
		return itemcode;
	}
	@JsonSetter("ItemCode")
	public void setItemCode(String itemcode){
		this.itemcode = itemcode;
	}
	
	@JsonGetter("ItemDescription")
	public String getItemDesc(){
		return itemdesc;
	}
	
	@JsonSetter("ItemDescription")
	public void setItemDesc(String itemdesc){
		this.itemdesc = itemdesc;
	}

	@JsonGetter("Quantity")
	public Integer getQuantity(){
		return quantity;
	}
	
	
	@JsonSetter("Quantity")
	public void getQuantity(Integer quantity){
		this.quantity = quantity;
	}
	
	@JsonGetter("TaxCode")
	public String getTaxCode(){
		return taxcode;
	}
	
	@JsonSetter("TaxCode")
	public void setTaxCode(String taxcode){
		this.taxcode = taxcode;
	}
	
	@JsonGetter("RemainingOpenQuantity")
	public Integer getRemainingOQ(){
		return remaining;
	}
	
	@JsonSetter("RemainingOpenQuantity")
	public void setRemainingOQ(Integer remaining){
		this.remaining = remaining;
	}
	
	@JsonGetter("RemainingOpenInventoryQuantity")
	public Integer getRemainingOIQ(){
		return remainingO;
	}

	@JsonSetter("RemainingOpenInventoryQuantity")
	public void setRemainingOIQ(Integer remainingO){
		this.remainingO = remainingO;
	}
	
	@JsonGetter("UnitPrice")
	public String getUnitPrice(){
		return unitprice;
	}
	
	@JsonSetter("UnitPrice")
	public void setUnitPrice(String unitprice){
		this.unitprice = unitprice;
	}
	
	@JsonGetter("LineStatus")
	public String getLineStatus(){
		return lineStatus;
	}
	
	@JsonSetter("LineStatus")
	public void setLineStatus(String lineStatus){
		this.lineStatus = lineStatus;
	}
	
	@JsonGetter("LineNum")
	public Integer getLineNum() {
		return lineNum;
	}
	
	@JsonSetter("LineNum")
	public void setLineNum(Integer lineNum) {
		this.lineNum = lineNum;
	}
	
	@JsonGetter("BarCode")
	public String getBarCode() {
		return barCode;
	}
	
	@JsonSetter("BarCode")
	public void setBarCode(String barCode){
		this.barCode = barCode;
	}
	
	@JsonGetter("UnitsOfMeasurment")
	public Integer getUnitOfMea() {
		return unitOM;
	}
	
	@JsonSetter("UnitsOfMeasurment")
	public void setUnitOfMea(Integer unitOM) {
		this.unitOM = unitOM;
	}
}
