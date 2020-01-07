package com.acqua.common.sl.exceptions;

import java.io.Serializable;

public class SLExternalErrorCode implements Serializable {
	 
	private static final long serialVersionUID = 123215433L;
	private String apiName;
	private String result;
	private String result_namespace;
	private String errorMessage;
	private Integer httpStatus;
	 
	public SLExternalErrorCode() {
		super();
	}
 
	public SLExternalErrorCode(String result, String result_namespace) {
		super();
		this.result = result;
		this.result_namespace = result_namespace;
	} 
 
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResult_namespace() {
		return result_namespace;
	}

	public void setResult_namespace(String result_namespace) {
		this.result_namespace = result_namespace;
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
	
}