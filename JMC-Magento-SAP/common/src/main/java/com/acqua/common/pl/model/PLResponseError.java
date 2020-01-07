package com.acqua.common.pl.model;

import java.io.Serializable;

public class PLResponseError implements Serializable {
	 
	private static final long serialVersionUID = 123215435L;
	
	private PLErrorCode errors; 
	
	public PLResponseError() {
		super();
	}

	public PLErrorCode getErrors() {
		return errors;
	}

	public void setErrors(PLErrorCode errors) {
		this.errors = errors;
	}
  
}