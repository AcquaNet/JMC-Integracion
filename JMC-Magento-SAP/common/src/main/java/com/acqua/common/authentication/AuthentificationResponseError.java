package com.acqua.common.authentication;

import java.io.Serializable;

public class AuthentificationResponseError implements Serializable {
	 
	private static final long serialVersionUID = 123215435L;
	
	private AuthentificationErrorCode errors; 
	
	public AuthentificationResponseError() {
		super();
	}

	public AuthentificationErrorCode getErrors() {
		return errors;
	}

	public void setErrors(AuthentificationErrorCode errors) {
		this.errors = errors;
	}
  
}