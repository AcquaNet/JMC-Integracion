package com.acqua.common.sl.exceptions;

import org.mule.api.MuleEvent;
import org.mule.extension.validation.api.ValidationException;
import org.mule.extension.validation.api.ValidationResult;
   
public class SLException extends ValidationException {

	private static final long serialVersionUID = 1997753363232807009L;
	 
	SLResponseError systemLayerResponseError;
	 
	public SLException(ValidationResult validationResult, MuleEvent event) {
		super(validationResult, event);
	}

	public SLResponseError getSystemLayerResponseError() {
		return systemLayerResponseError;
	}

	public void setSystemLayerResponseError(SLResponseError systemLayerResponseError) {
		this.systemLayerResponseError = systemLayerResponseError;
	}
 
	
}
