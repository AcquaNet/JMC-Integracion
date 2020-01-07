package com.acqua.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PickList {
		@JsonProperty("Absoluteentry")
		private String absoluteEntry;
		@JsonProperty("Name")
		private String nameLb;
		@JsonProperty("PickDate")
		private String pickDate;
		@JsonProperty("ObjectType")
		private String objectType;
		@JsonProperty("Status")
		private String status;
		@JsonProperty("OwnerCode")
		private String ownerCode;
		@JsonProperty("PickListsLines")
		private List<PickListLines> pickListLines;
		
		
		public PickList() {
			super();
		}
		
		public PickList(String absoluteEntry, String nameLb, String pickDate, String objectType, List<PickListLines> pickListLines, String status, String ownerCode) {
			super();
			this.absoluteEntry = absoluteEntry;
			this.nameLb = nameLb;
			this.pickDate = pickDate;
			this.objectType = objectType;
			this.pickListLines =pickListLines;
			this.status = status;
			this.ownerCode = ownerCode;
		}
		
		@JsonGetter("Absoluteentry")
		public String getAbsoluteEntry() {
			return absoluteEntry;
		}
		
		@JsonSetter("Absoluteentry")
		public void setAbsoluteEntry(String absoluteEntry) {
			this.absoluteEntry = absoluteEntry;
		}
		
		@JsonGetter("Name")
		public String getNameLb() {
			return nameLb;
		}
		@JsonSetter("Name")
		public void setNameLb(String nameLb) {
			this.nameLb = nameLb;
		}
		
		@JsonGetter("PickDate")
		public String getPickDate() {
			return pickDate;
		}
		@JsonSetter("PickDate")
		public void setPickDate(String pickDate) {
			this.pickDate = pickDate;
		}
		
		@JsonGetter("ObjectType")
		public String getObjectType() {
			return objectType;
		}
		@JsonSetter("ObjectType")
		public void setObjectType(String objectType) {
			this.objectType = objectType;
		}
		
		@JsonGetter("PickListsLines")
		public List<PickListLines> getPickListLines(){
			return pickListLines;
		}
		@JsonSetter("PickListsLines")
		public void setPickListLines(List<PickListLines> pickListLines) {
			this.pickListLines = pickListLines;
		}
		
		@JsonGetter("Status")
		public String getStatus() {
			return status;
		}
		
		@JsonSetter("Status")
		public void setStatus(String status) {
			this.status = status;
		}
		
		@JsonGetter("OwnerCode")
		public String getOwnerCode() {
			return ownerCode;
		}
		
		@JsonSetter("OwnerCode")
		public void setOwnerCode(String ownerCode) {
			this.ownerCode = ownerCode;
		}

		@Override
		public String toString() {
			return "PickList [absoluteEntry=" + absoluteEntry + ", nameLb=" + nameLb + ", pickDate=" + pickDate
					+ ", objectType=" + objectType + ", status=" + status + ", ownerCode=" + ownerCode
					+ ", pickListLines=" + pickListLines + "]";
		}
		
		
}
