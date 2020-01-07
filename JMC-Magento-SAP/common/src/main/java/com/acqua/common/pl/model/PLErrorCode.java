package com.acqua.common.pl.model;

import java.io.Serializable;
 
import com.acqua.common.sl.exceptions.SLResponseError;

public class PLErrorCode implements Serializable {
	 
	private static final long serialVersionUID = 123215435L;
	private String flowName; 
	private String errorCode;
	private String category;
	private Integer httpStatus;
	private String errorMessage;
	private SLResponseError slResponseError;
	 
	public PLErrorCode() {
		super();
	}
 
	public PLErrorCode(String result, String result_namespace) {
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

	public SLResponseError getSlResponseError() {
		return slResponseError;
	}

	public void setSlResponseError(SLResponseError slResponseError) {
		this.slResponseError = slResponseError;
	} 
	 
}