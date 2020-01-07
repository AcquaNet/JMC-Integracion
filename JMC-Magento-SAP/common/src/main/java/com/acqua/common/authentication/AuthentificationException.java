package com.acqua.common.authentication;
 
public class AuthentificationException extends RuntimeException {

	private static final long serialVersionUID = 1997753363232807009L;
	 
	AuthentificationResponseError authResponseError;
	 
	public AuthentificationException(AuthentificationResponseError authResponseError) {
		super();
		this.authResponseError = authResponseError;
	}

	public AuthentificationResponseError getAuthResponseError() {
		return authResponseError;
	}

	public void setAuthResponseError(AuthentificationResponseError authResponseError) {
		this.authResponseError = authResponseError;
	}
	   
}
