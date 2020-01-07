package com.acqua.common.sl.exceptions;

import java.io.Serializable;

public class SLErrorCode implements Serializable {
	 
	private static final long serialVersionUID = 123215435L;
	private String flowName; 
	private String errorCode;
	private String category;
	private Integer httpStatus;
	private String errorMessage;
	private SLExternalErrorCode externalErrorCode;
	 
	public SLErrorCode() {
		super();
	}
 
	public SLErrorCode(String result, String result_namespace) {
		super();		 
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public SLExternalErrorCode getExternalErrorCode() {
		return externalErrorCode;
	}

	public void setExternalErrorCode(SLExternalErrorCode externalErrorCode) {
		this.externalErrorCode = externalErrorCode;
	} 
	 
}