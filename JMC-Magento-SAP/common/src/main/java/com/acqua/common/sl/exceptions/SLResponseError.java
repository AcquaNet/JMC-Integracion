package com.acqua.common.sl.exceptions;

import java.io.Serializable;

public class SLResponseError implements Serializable {
	 
	private static final long serialVersionUID = 123215435L;
	
	private SLErrorCode errors; 
	
	public SLResponseError() {
		super();
	}

	public SLErrorCode getErrors() {
		return errors;
	}

	public void setErrors(SLErrorCode errors) {
		this.errors = errors;
	}
  
}