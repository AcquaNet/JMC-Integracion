package com.acqua.common.pl.exceptions;

import org.mule.api.MuleEvent;
import org.mule.extension.validation.api.ValidationException;
import org.mule.extension.validation.api.ValidationResult;

import com.acqua.common.pl.model.PLResponseError;
   
public class PLException extends ValidationException {

	private static final long serialVersionUID = 1997753363232807009L;
	 
	PLResponseError processLayerResponseError;
	 
	public PLException(ValidationResult validationResult, MuleEvent event) {
		super(validationResult, event);
	}

	public PLResponseError getProcessLayerResponseError() {
		return processLayerResponseError;
	}

	public void setProcessLayerResponseError(PLResponseError processLayerResponseError) {
		this.processLayerResponseError = processLayerResponseError;
	}
	 
}
