package com.acqua.common.authentication;

import java.io.Serializable; 

public class AuthentificationErrorCode implements Serializable {
	 
	private static final long serialVersionUID = 123215435L;

	private Integer httpStatus;
	private String errorMessage; 
	
	public AuthentificationErrorCode() {
		super();
	}
 
	public AuthentificationErrorCode(String result, String result_namespace) {
		super();		 
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